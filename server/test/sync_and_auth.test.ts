import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import request from 'supertest';
import fs from 'fs';
import path from 'path';
import { createApp } from '../src/app.js';
import { closeDb } from '../src/db.js';

const TEST_DB_PATH = path.join(__dirname, 'test-tessera.db');

describe('Tessera Auth & Sync API', () => {
  let app: ReturnType<typeof createApp>;

  beforeEach(() => {
    if (fs.existsSync(TEST_DB_PATH)) {
      fs.unlinkSync(TEST_DB_PATH);
    }
    app = createApp(TEST_DB_PATH);
  });

  afterEach(() => {
    closeDb();
    if (fs.existsSync(TEST_DB_PATH)) {
      fs.unlinkSync(TEST_DB_PATH);
    }
  });

  it('health check returns ok', async () => {
    const res = await request(app).get('/api/health');
    expect(res.status).toBe(200);
    expect(res.body.status).toBe('ok');
    expect(res.body.service).toBe('tessera-sync-engine');
  });

  it('registers user, issues JWT token, and gets current user', async () => {
    const regRes = await request(app)
      .post('/api/auth/register')
      .send({
        email: 'anoop@example.com',
        password: 'secure-password-123',
        displayName: 'Anoop Singh'
      });

    expect(regRes.status).toBe(201);
    expect(regRes.body.token).toBeDefined();
    expect(regRes.body.user.email).toBe('anoop@example.com');
    expect(regRes.body.user.displayName).toBe('Anoop Singh');

    const token = regRes.body.token;

    // Get Me
    const meRes = await request(app)
      .get('/api/auth/me')
      .set('Authorization', `Bearer ${token}`);

    expect(meRes.status).toBe(200);
    expect(meRes.body.user.email).toBe('anoop@example.com');
  });

  it('rejects duplicate email registration and invalid login', async () => {
    await request(app)
      .post('/api/auth/register')
      .send({ email: 'test@example.com', password: 'password1' });

    const dupRes = await request(app)
      .post('/api/auth/register')
      .send({ email: 'test@example.com', password: 'password2' });
    expect(dupRes.status).toBe(409);

    const badLogin = await request(app)
      .post('/api/auth/login')
      .send({ email: 'test@example.com', password: 'wrong-password' });
    expect(badLogin.status).toBe(401);

    const goodLogin = await request(app)
      .post('/api/auth/login')
      .send({ email: 'test@example.com', password: 'password1' });
    expect(goodLogin.status).toBe(200);
    expect(goodLogin.body.token).toBeDefined();
  });

  it('handles offline habit sync and last-write-wins conflict resolution', async () => {
    // 1. Register User A
    const reg = await request(app)
      .post('/api/auth/register')
      .send({ email: 'sync-user@example.com', password: 'password123' });
    const token = reg.body.token;

    // 2. Client Device 1 sends offline created habit and log
    const t1 = 1760600000000;
    const sync1 = await request(app)
      .post('/api/sync')
      .set('Authorization', `Bearer ${token}`)
      .send({
        lastSyncTimestamp: 0,
        habits: [
          {
            slug: 'deep-work',
            title: 'Deep Work Session',
            cueAnchor: 'After morning coffee',
            microStarter: 'Open IDE and sit for 2 minutes',
            identityStatement: 'I am a focused builder',
            category: 'CRAFT',
            iconKey: 'bolt',
            colorKey: 'emerald',
            targetPerDay: 2,
            weeklyGoalDays: 5,
            ritualTimeOfDay: 'FOCUS',
            updatedAt: t1
          }
        ],
        logs: [
          {
            habitSlug: 'deep-work',
            dateEpochDay: 20743,
            count: 2,
            note: 'Architecture session',
            updatedAt: t1
          }
        ]
      });

    expect(sync1.status).toBe(200);
    expect(sync1.body.habits.length).toBe(1);
    expect(sync1.body.habits[0].slug).toBe('deep-work');
    expect(sync1.body.logs.length).toBe(1);
    expect(sync1.body.logs[0].count).toBe(2);

    // 3. Client Device 2 syncs for the first time
    const syncDevice2 = await request(app)
      .post('/api/sync')
      .set('Authorization', `Bearer ${token}`)
      .send({
        lastSyncTimestamp: 0,
        habits: [],
        logs: []
      });

    expect(syncDevice2.status).toBe(200);
    expect(syncDevice2.body.habits.length).toBe(1);
    expect(syncDevice2.body.habits[0].slug).toBe('deep-work');
    expect(syncDevice2.body.logs[0].count).toBe(2);

    // 4. Client Device 1 updates habit with newer timestamp (t2 > t1)
    const t2 = t1 + 5000;
    const syncUpdateNewer = await request(app)
      .post('/api/sync')
      .set('Authorization', `Bearer ${token}`)
      .send({
        lastSyncTimestamp: t1,
        habits: [
          {
            slug: 'deep-work',
            title: 'Deep Work (Refined)',
            updatedAt: t2
          }
        ]
      });

    expect(syncUpdateNewer.status).toBe(200);
    expect(syncUpdateNewer.body.habits[0].title).toBe('Deep Work (Refined)');

    // 5. Client Device 2 tries to overwrite with older timestamp (t0 < t2) -> Server rejects/retains newer
    const t0 = t1 - 1000;
    const syncUpdateOlder = await request(app)
      .post('/api/sync')
      .set('Authorization', `Bearer ${token}`)
      .send({
        lastSyncTimestamp: t1,
        habits: [
          {
            slug: 'deep-work',
            title: 'Stale Title From Offline Device',
            updatedAt: t0
          }
        ]
      });

    expect(syncUpdateOlder.status).toBe(200);
    // Server must have preserved 'Deep Work (Refined)'
    const pullLatest = await request(app)
      .post('/api/sync')
      .set('Authorization', `Bearer ${token}`)
      .send({ lastSyncTimestamp: 0 });

    expect(pullLatest.body.habits[0].title).toBe('Deep Work (Refined)');
  });

  it('rejects unauthenticated sync attempts', async () => {
    const res = await request(app).post('/api/sync').send({});
    expect(res.status).toBe(401);
  });

  it('handles soft-deletion propagation and empty sync gracefully', async () => {
    const reg = await request(app)
      .post('/api/auth/register')
      .send({ email: 'delete-user@example.com', password: 'password123' });
    const token = reg.body.token;

    // 1. Create habit
    const t1 = 1760600000000;
    await request(app)
      .post('/api/sync')
      .set('Authorization', `Bearer ${token}`)
      .send({
        lastSyncTimestamp: 0,
        habits: [{ slug: 'delete-me', title: 'To Delete', updatedAt: t1 }]
      });

    // 2. Soft delete
    const t2 = t1 + 1000;
    const delRes = await request(app)
      .post('/api/sync')
      .set('Authorization', `Bearer ${token}`)
      .send({
        lastSyncTimestamp: t1,
        habits: [{ slug: 'delete-me', title: 'To Delete', isDeleted: true, updatedAt: t2 }]
      });

    expect(delRes.status).toBe(200);
    expect(delRes.body.habits[0].isDeleted).toBe(true);

    // 3. Empty payload returns server status without errors
    const emptyRes = await request(app)
      .post('/api/sync')
      .set('Authorization', `Bearer ${token}`)
      .send({});
    expect(emptyRes.status).toBe(200);
    expect(emptyRes.body.serverTimestamp).toBeGreaterThan(0);
  });
});
