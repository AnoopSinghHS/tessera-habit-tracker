import { Database } from 'better-sqlite3';
import crypto from 'crypto';

export interface HabitSyncDto {
  id?: string;
  slug: string;
  title: string;
  description?: string;
  cueAnchor?: string;
  microStarter?: string;
  identityStatement?: string;
  category?: string;
  iconKey?: string;
  colorKey?: string;
  targetPerDay?: number;
  weeklyGoalDays?: number;
  ritualTimeOfDay?: string;
  sortOrder?: number;
  isArchived?: boolean;
  isDeleted?: boolean;
  updatedAt: number;
}

export interface HabitLogSyncDto {
  id?: string;
  habitSlug: string;
  dateEpochDay: number;
  count: number;
  note?: string;
  isDeleted?: boolean;
  updatedAt: number;
}

export interface SyncRequest {
  lastSyncTimestamp?: number;
  habits?: HabitSyncDto[];
  logs?: HabitLogSyncDto[];
}

export interface SyncResponse {
  serverTimestamp: number;
  habits: HabitSyncDto[];
  logs: HabitLogSyncDto[];
}

export function performSync(db: Database, userId: string, payload: SyncRequest): SyncResponse {
  const lastSync = payload.lastSyncTimestamp || 0;
  const currentTimestamp = Date.now();
  const incomingHabits = payload.habits || [];
  const incomingLogs = payload.logs || [];

  const runSyncTransaction = db.transaction(() => {
    // 1. Process incoming habits (Last-Write-Wins based on updatedAt)
    const selectHabitStmt = db.prepare<[string, string]>(`
      SELECT id, slug, updated_at FROM habits WHERE user_id = ? AND slug = ?
    `);

    const updateHabitStmt = db.prepare(`
      UPDATE habits SET
        title = @title,
        description = @description,
        cue_anchor = @cueAnchor,
        micro_starter = @microStarter,
        identity_statement = @identityStatement,
        category = @category,
        icon_key = @iconKey,
        color_key = @colorKey,
        target_per_day = @targetPerDay,
        weekly_goal_days = @weeklyGoalDays,
        ritual_time_of_day = @ritualTimeOfDay,
        sort_order = @sortOrder,
        is_archived = @isArchived,
        is_deleted = @isDeleted,
        updated_at = @updatedAt
      WHERE user_id = @userId AND slug = @slug
    `);

    const insertHabitStmt = db.prepare(`
      INSERT INTO habits (
        id, user_id, slug, title, description, cue_anchor, micro_starter,
        identity_statement, category, icon_key, color_key, target_per_day,
        weekly_goal_days, ritual_time_of_day, sort_order, is_archived,
        is_deleted, created_at, updated_at
      ) VALUES (
        @id, @userId, @slug, @title, @description, @cueAnchor, @microStarter,
        @identityStatement, @category, @iconKey, @colorKey, @targetPerDay,
        @weeklyGoalDays, @ritualTimeOfDay, @sortOrder, @isArchived,
        @isDeleted, @createdAt, @updatedAt
      )
    `);

    for (const h of incomingHabits) {
      const existing = selectHabitStmt.get(userId, h.slug) as { id: string; updated_at: number } | undefined;
      const safeUpdatedAt = h.updatedAt || currentTimestamp;

      if (existing) {
        if (safeUpdatedAt > existing.updated_at) {
          updateHabitStmt.run({
            userId,
            slug: h.slug,
            title: h.title,
            description: h.description || '',
            cueAnchor: h.cueAnchor || '',
            microStarter: h.microStarter || '',
            identityStatement: h.identityStatement || '',
            category: h.category || 'General',
            iconKey: h.iconKey || 'bolt',
            colorKey: h.colorKey || 'emerald',
            targetPerDay: h.targetPerDay ?? 1,
            weeklyGoalDays: h.weeklyGoalDays ?? 7,
            ritualTimeOfDay: h.ritualTimeOfDay || 'DAWN',
            sortOrder: h.sortOrder ?? 0,
            isArchived: h.isArchived ? 1 : 0,
            isDeleted: h.isDeleted ? 1 : 0,
            updatedAt: safeUpdatedAt
          });
        }
      } else {
        insertHabitStmt.run({
          id: h.id || crypto.randomUUID(),
          userId,
          slug: h.slug,
          title: h.title,
          description: h.description || '',
          cueAnchor: h.cueAnchor || '',
          microStarter: h.microStarter || '',
          identityStatement: h.identityStatement || '',
          category: h.category || 'General',
          iconKey: h.iconKey || 'bolt',
          colorKey: h.colorKey || 'emerald',
          targetPerDay: h.targetPerDay ?? 1,
          weeklyGoalDays: h.weeklyGoalDays ?? 7,
          ritualTimeOfDay: h.ritualTimeOfDay || 'DAWN',
          sortOrder: h.sortOrder ?? 0,
          isArchived: h.isArchived ? 1 : 0,
          isDeleted: h.isDeleted ? 1 : 0,
          createdAt: safeUpdatedAt,
          updatedAt: safeUpdatedAt
        });
      }
    }

    // 2. Process incoming logs (Last-Write-Wins based on updatedAt)
    const selectLogStmt = db.prepare<[string, string, number]>(`
      SELECT id, updated_at FROM habit_logs WHERE user_id = ? AND habit_slug = ? AND date_epoch_day = ?
    `);

    const selectHabitIdStmt = db.prepare<[string, string]>(`
      SELECT id FROM habits WHERE user_id = ? AND slug = ?
    `);

    const updateLogStmt = db.prepare(`
      UPDATE habit_logs SET
        count = @count,
        note = @note,
        is_deleted = @isDeleted,
        updated_at = @updatedAt
      WHERE user_id = @userId AND habit_slug = @habitSlug AND date_epoch_day = @dateEpochDay
    `);

    const insertLogStmt = db.prepare(`
      INSERT INTO habit_logs (
        id, user_id, habit_id, habit_slug, date_epoch_day, count, note, is_deleted, updated_at
      ) VALUES (
        @id, @userId, @habitId, @habitSlug, @dateEpochDay, @count, @note, @isDeleted, @updatedAt
      )
    `);

    for (const l of incomingLogs) {
      const habitRow = selectHabitIdStmt.get(userId, l.habitSlug) as { id: string } | undefined;
      if (!habitRow) {
        // If habit hasn't been created yet, skip or create placeholder
        continue;
      }

      const existingLog = selectLogStmt.get(userId, l.habitSlug, l.dateEpochDay) as { id: string; updated_at: number } | undefined;
      const safeUpdatedAt = l.updatedAt || currentTimestamp;

      if (existingLog) {
        if (safeUpdatedAt > existingLog.updated_at) {
          updateLogStmt.run({
            userId,
            habitSlug: l.habitSlug,
            dateEpochDay: l.dateEpochDay,
            count: l.count,
            note: l.note || '',
            isDeleted: l.isDeleted ? 1 : 0,
            updatedAt: safeUpdatedAt
          });
        }
      } else {
        insertLogStmt.run({
          id: l.id || crypto.randomUUID(),
          userId,
          habitId: habitRow.id,
          habitSlug: l.habitSlug,
          dateEpochDay: l.dateEpochDay,
          count: l.count,
          note: l.note || '',
          isDeleted: l.isDeleted ? 1 : 0,
          updatedAt: safeUpdatedAt
        });
      }
    }

    // 3. Query all habits and logs modified on server strictly after lastSync
    // (excluding incoming records that client just sent us unless server had a newer record)
    const changedHabits = db.prepare<[string, number]>(`
      SELECT * FROM habits WHERE user_id = ? AND updated_at > ?
    `).all(userId, lastSync) as any[];

    const changedLogs = db.prepare<[string, number]>(`
      SELECT * FROM habit_logs WHERE user_id = ? AND updated_at > ?
    `).all(userId, lastSync) as any[];

    const outgoingHabits: HabitSyncDto[] = changedHabits.map(h => ({
      id: h.id,
      slug: h.slug,
      title: h.title,
      description: h.description,
      cueAnchor: h.cue_anchor,
      microStarter: h.micro_starter,
      identityStatement: h.identity_statement,
      category: h.category,
      iconKey: h.icon_key,
      colorKey: h.color_key,
      targetPerDay: h.target_per_day,
      weeklyGoalDays: h.weekly_goal_days,
      ritualTimeOfDay: h.ritual_time_of_day,
      sortOrder: h.sort_order,
      isArchived: Boolean(h.is_archived),
      isDeleted: Boolean(h.is_deleted),
      updatedAt: h.updated_at
    }));

    const outgoingLogs: HabitLogSyncDto[] = changedLogs.map(l => ({
      id: l.id,
      habitSlug: l.habit_slug,
      dateEpochDay: l.date_epoch_day,
      count: l.count,
      note: l.note,
      isDeleted: Boolean(l.is_deleted),
      updatedAt: l.updated_at
    }));

    return {
      serverTimestamp: currentTimestamp,
      habits: outgoingHabits,
      logs: outgoingLogs
    };
  });

  return runSyncTransaction();
}
