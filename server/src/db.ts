import Database, { Database as DatabaseType } from 'better-sqlite3';
import path from 'path';
import fs from 'fs';

let dbInstance: DatabaseType | null = null;

export function getDb(customPath?: string): DatabaseType {
  if (dbInstance) {
    return dbInstance;
  }

  const dbPath = customPath || process.env.DATABASE_PATH || path.join(__dirname, '..', 'data', 'tessera.db');
  const dir = path.dirname(dbPath);
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }

  dbInstance = new Database(dbPath);
  dbInstance.pragma('journal_mode = WAL');
  dbInstance.pragma('foreign_keys = ON');

  initSchema(dbInstance);
  return dbInstance;
}

export function closeDb(): void {
  if (dbInstance) {
    dbInstance.close();
    dbInstance = null;
  }
}

function initSchema(db: DatabaseType) {
  db.exec(`
    CREATE TABLE IF NOT EXISTS users (
      id TEXT PRIMARY KEY,
      email TEXT UNIQUE NOT NULL,
      password_hash TEXT NOT NULL,
      display_name TEXT NOT NULL,
      created_at INTEGER NOT NULL
    );

    CREATE TABLE IF NOT EXISTS habits (
      id TEXT PRIMARY KEY,
      user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
      slug TEXT NOT NULL,
      title TEXT NOT NULL,
      description TEXT DEFAULT '',
      cue_anchor TEXT DEFAULT '',
      micro_starter TEXT DEFAULT '',
      identity_statement TEXT DEFAULT '',
      category TEXT DEFAULT 'General',
      icon_key TEXT DEFAULT 'bolt',
      color_key TEXT DEFAULT 'emerald',
      target_per_day INTEGER DEFAULT 1,
      weekly_goal_days INTEGER DEFAULT 7,
      ritual_time_of_day TEXT DEFAULT 'DAWN',
      sort_order INTEGER DEFAULT 0,
      is_archived INTEGER DEFAULT 0,
      is_deleted INTEGER DEFAULT 0,
      created_at INTEGER NOT NULL,
      updated_at INTEGER NOT NULL,
      UNIQUE(user_id, slug)
    );

    CREATE TABLE IF NOT EXISTS habit_logs (
      id TEXT PRIMARY KEY,
      user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
      habit_id TEXT NOT NULL REFERENCES habits(id) ON DELETE CASCADE,
      habit_slug TEXT NOT NULL,
      date_epoch_day INTEGER NOT NULL,
      count INTEGER DEFAULT 1,
      note TEXT DEFAULT '',
      is_deleted INTEGER DEFAULT 0,
      updated_at INTEGER NOT NULL,
      UNIQUE(user_id, habit_slug, date_epoch_day)
    );

    CREATE INDEX IF NOT EXISTS idx_habits_user_updated ON habits(user_id, updated_at);
    CREATE INDEX IF NOT EXISTS idx_logs_user_updated ON habit_logs(user_id, updated_at);
  `);
}
