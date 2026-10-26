import { Habit, HabitLog, UserProfile, SyncState } from './types.js';
import { loadHabits, saveHabits, loadLogs, saveLogs } from './storage.js';

const TOKEN_KEY = 'tessera_jwt_token';
const USER_KEY = 'tessera_user_profile';
const LAST_SYNC_KEY = 'tessera_last_sync_timestamp';

export function getStoredToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function setStoredToken(token: string | null): void {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token);
  } else {
    localStorage.removeItem(TOKEN_KEY);
  }
}

export function getStoredUser(): UserProfile | null {
  try {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function setStoredUser(user: UserProfile | null): void {
  if (user) {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  } else {
    localStorage.removeItem(USER_KEY);
  }
}

export function getLastSyncTimestamp(): number {
  const raw = localStorage.getItem(LAST_SYNC_KEY);
  return raw ? parseInt(raw, 10) : 0;
}

export function setLastSyncTimestamp(ts: number): void {
  localStorage.setItem(LAST_SYNC_KEY, ts.toString());
}

export async function loginUser(email: string, password: string): Promise<{ token: string; user: UserProfile }> {
  const res = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });

  if (!res.ok) {
    const data = await res.json().catch(() => ({}));
    throw new Error(data.error || 'Failed to login');
  }

  const data = await res.json();
  setStoredToken(data.token);
  setStoredUser(data.user);
  return data;
}

export async function registerUser(email: string, password: string, displayName?: string): Promise<{ token: string; user: UserProfile }> {
  const res = await fetch('/api/auth/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password, displayName })
  });

  if (!res.ok) {
    const data = await res.json().catch(() => ({}));
    throw new Error(data.error || 'Failed to register');
  }

  const data = await res.json();
  setStoredToken(data.token);
  setStoredUser(data.user);
  return data;
}

export async function triggerSync(): Promise<{ syncedHabits: number; syncedLogs: number }> {
  const token = getStoredToken();
  if (!token) {
    return { syncedHabits: 0, syncedLogs: 0 };
  }

  const lastSync = getLastSyncTimestamp();
  const localHabits = loadHabits();
  const localLogs = loadLogs();

  // Changed locally since lastSync
  const habitsToSend = localHabits.filter(h => h.updatedAt > lastSync);
  const logsToSend = localLogs.filter(l => l.updatedAt > lastSync);

  const res = await fetch('/api/sync', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      lastSyncTimestamp: lastSync,
      habits: habitsToSend,
      logs: logsToSend
    })
  });

  if (!res.ok) {
    throw new Error(`Sync request failed with status: ${res.status}`);
  }

  const data = await res.json();
  const serverHabits: Habit[] = data.habits || [];
  const serverLogs: HabitLog[] = data.logs || [];

  // Merge server habits into local state
  const habitMap = new Map<string, Habit>();
  for (const h of localHabits) {
    habitMap.set(h.slug, h);
  }
  for (const sh of serverHabits) {
    const existing = habitMap.get(sh.slug);
    if (!existing || sh.updatedAt >= existing.updatedAt) {
      habitMap.set(sh.slug, sh);
    }
  }
  const mergedHabits = Array.from(habitMap.values());
  saveHabits(mergedHabits);

  // Merge server logs into local state
  const logKey = (l: { habitSlug: string; dateEpochDay: number }) => `${l.habitSlug}_${l.dateEpochDay}`;
  const logMap = new Map<string, HabitLog>();
  for (const l of localLogs) {
    logMap.set(logKey(l), l);
  }
  for (const sl of serverLogs) {
    const key = logKey(sl);
    const existing = logMap.get(key);
    if (!existing || sl.updatedAt >= existing.updatedAt) {
      logMap.set(key, sl);
    }
  }
  const mergedLogs = Array.from(logMap.values());
  saveLogs(mergedLogs);

  setLastSyncTimestamp(data.serverTimestamp || Date.now());
  return {
    syncedHabits: serverHabits.length,
    syncedLogs: serverLogs.length
  };
}
