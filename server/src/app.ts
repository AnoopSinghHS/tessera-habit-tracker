import express, { Request, Response } from 'express';
import cors from 'cors';
import crypto from 'crypto';
import path from 'path';
import fs from 'fs';
import { getDb } from './db.js';
import { hashPassword, comparePassword, signToken, authenticateToken, AuthenticatedRequest } from './auth.js';
import { performSync, SyncRequest } from './sync.js';

export function createApp(customDbPath?: string) {
  const app = express();
  const db = getDb(customDbPath);

  app.use(cors());
  app.use(express.json({ limit: '10mb' }));

  // Health check
  app.get('/api/health', (_req: Request, res: Response) => {
    res.json({
      status: 'ok',
      service: 'tessera-sync-engine',
      version: '1.0.0',
      timestamp: Date.now()
    });
  });

  // Auth: Register
  app.post('/api/auth/register', async (req: Request, res: Response) => {
    try {
      const { email, password, displayName } = req.body;
      if (!email || !password) {
        res.status(400).json({ error: 'Email and password are required.' });
        return;
      }

      const normalizedEmail = String(email).trim().toLowerCase();
      const existing = db.prepare('SELECT id FROM users WHERE email = ?').get(normalizedEmail);
      if (existing) {
        res.status(409).json({ error: 'An account with this email already exists.' });
        return;
      }

      const passwordHash = await hashPassword(password);
      const userId = crypto.randomUUID();
      const name = displayName?.trim() || normalizedEmail.split('@')[0];
      const now = Date.now();

      db.prepare(`
        INSERT INTO users (id, email, password_hash, display_name, created_at)
        VALUES (?, ?, ?, ?, ?)
      `).run(userId, normalizedEmail, passwordHash, name, now);

      const token = signToken({ userId, email: normalizedEmail });
      res.status(201).json({
        token,
        user: { id: userId, email: normalizedEmail, displayName: name, createdAt: now }
      });
    } catch (err: any) {
      res.status(500).json({ error: 'Failed to create user account.', details: err.message });
    }
  });

  // Auth: Login
  app.post('/api/auth/login', async (req: Request, res: Response) => {
    try {
      const { email, password } = req.body;
      if (!email || !password) {
        res.status(400).json({ error: 'Email and password are required.' });
        return;
      }

      const normalizedEmail = String(email).trim().toLowerCase();
      const user = db.prepare('SELECT * FROM users WHERE email = ?').get(normalizedEmail) as any;
      if (!user) {
        res.status(401).json({ error: 'Invalid email or password.' });
        return;
      }

      const passwordMatches = await comparePassword(password, user.password_hash);
      if (!passwordMatches) {
        res.status(401).json({ error: 'Invalid email or password.' });
        return;
      }

      const token = signToken({ userId: user.id, email: user.email });
      res.json({
        token,
        user: {
          id: user.id,
          email: user.email,
          displayName: user.display_name,
          createdAt: user.created_at
        }
      });
    } catch (err: any) {
      res.status(500).json({ error: 'Failed to authenticate.', details: err.message });
    }
  });

  // Auth: Current User Profile
  app.get('/api/auth/me', authenticateToken, (req: AuthenticatedRequest, res: Response) => {
    try {
      const user = db.prepare('SELECT id, email, display_name, created_at FROM users WHERE id = ?').get(req.user!.userId) as any;
      if (!user) {
        res.status(404).json({ error: 'User not found.' });
        return;
      }
      res.json({
        user: {
          id: user.id,
          email: user.email,
          displayName: user.display_name,
          createdAt: user.created_at
        }
      });
    } catch (err: any) {
      res.status(500).json({ error: 'Failed to fetch user.', details: err.message });
    }
  });

  // Sync: Bi-directional Atomic Sync
  app.post('/api/sync', authenticateToken, (req: AuthenticatedRequest, res: Response) => {
    try {
      const payload: SyncRequest = req.body || {};
      const result = performSync(db, req.user!.userId, payload);
      res.json(result);
    } catch (err: any) {
      res.status(500).json({ error: 'Sync failed.', details: err.message });
    }
  });

  // Serve static web client if built
  const webDistPath = path.resolve(process.cwd(), '..', 'web', 'dist');
  if (fs.existsSync(webDistPath)) {
    app.use(express.static(webDistPath));
    app.use((req: Request, res: Response, next) => {
      if (req.path.startsWith('/api')) return next();
      res.sendFile(path.join(webDistPath, 'index.html'));
    });
  }

  return app;
}
