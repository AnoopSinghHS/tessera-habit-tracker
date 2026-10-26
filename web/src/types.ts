export type ThemeMode = 'light' | 'dark' | 'dune' | 'space' | 'tatooine';

export type RitualTime = 'DAWN' | 'FOCUS' | 'TWILIGHT' | 'ANYTIME';

export interface Habit {
  id: string;
  slug: string;
  title: string;
  description: string;
  cueAnchor: string;
  microStarter: string;
  identityStatement: string;
  category: string;
  iconKey: string;
  colorKey: string;
  targetPerDay: number;
  weeklyGoalDays: number;
  ritualTimeOfDay: RitualTime;
  sortOrder: number;
  isArchived: boolean;
  createdAt: number;
  updatedAt: number;
}

export interface HabitLog {
  id: string;
  habitSlug: string;
  dateEpochDay: number; // LocalDate.toEpochDay()
  count: number;
  note: string;
  updatedAt: number;
}

export interface HabitWithLogs {
  habit: Habit;
  logs: HabitLog[];
  currentStreak: number;
  bestStreak: number;
  totalCompletions: number;
  totalActiveDays: number;
  completionRate30Days: number;
  isCompletedToday: boolean;
  todayCount: number;
}

export interface UserProfile {
  id: string;
  email: string;
  displayName: string;
  createdAt: number;
}

export interface SyncState {
  isOnline: boolean;
  isSyncing: boolean;
  lastSyncTimestamp: number | null;
  pendingChangesCount: number;
  user: UserProfile | null;
  token: string | null;
}
