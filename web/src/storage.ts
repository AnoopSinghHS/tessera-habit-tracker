import { Habit, HabitLog, HabitWithLogs } from './types.js';

const HABITS_KEY = 'tessera_web_habits_v1';
const LOGS_KEY = 'tessera_web_logs_v1';
const SYNC_QUEUE_KEY = 'tessera_web_sync_queue_v1';

export function getTodayEpochDay(): number {
  const now = new Date();
  const utc = Date.UTC(now.getFullYear(), now.getMonth(), now.getDate());
  return Math.floor(utc / (86400 * 1000));
}

export function epochDayToDate(epochDay: number): Date {
  return new Date(epochDay * 86400 * 1000);
}

export function dateToEpochDay(d: Date): number {
  const utc = Date.UTC(d.getFullYear(), d.getMonth(), d.getDate());
  return Math.floor(utc / (86400 * 1000));
}

// Initial realistic showcase habits matching Android DemoSeedGenerator
const SEED_HABITS: Habit[] = [
  {
    id: 'habit-deep-work',
    slug: 'deep-work',
    title: 'Deep Work Session',
    description: 'After morning espresso, I will close all tabs and focus for 90m',
    cueAnchor: 'Morning espresso poured',
    microStarter: 'Open text editor and write 1 line',
    identityStatement: 'I am a disciplined craftsman',
    category: 'CRAFT',
    iconKey: 'bolt',
    colorKey: 'emerald',
    targetPerDay: 2,
    weeklyGoalDays: 5,
    ritualTimeOfDay: 'FOCUS',
    sortOrder: 1,
    isArchived: false,
    createdAt: Date.now() - 120 * 86400 * 1000,
    updatedAt: Date.now() - 120 * 86400 * 1000
  },
  {
    id: 'habit-strength-training',
    slug: 'strength-training',
    title: 'Strength Training',
    description: 'After changing into athletic shorts, I will step into gym',
    cueAnchor: 'Putting on workout shoes',
    microStarter: 'Complete 5 air squats and stretch wrists',
    identityStatement: 'I am an athlete building longevity',
    category: 'BODY',
    iconKey: 'fitness',
    colorKey: 'rose',
    targetPerDay: 1,
    weeklyGoalDays: 4,
    ritualTimeOfDay: 'DAWN',
    sortOrder: 2,
    isArchived: false,
    createdAt: Date.now() - 120 * 86400 * 1000,
    updatedAt: Date.now() - 120 * 86400 * 1000
  },
  {
    id: 'habit-vocal-riyaaz',
    slug: 'vocal-riyaaz',
    title: 'Vocal Warmup & Riyaaz',
    description: 'After morning water, I will practice octave scales for 15m',
    cueAnchor: 'First glass of warm water',
    microStarter: 'Hum 1 middle-C scale for 60 seconds',
    identityStatement: 'I am a dedicated vocalist',
    category: 'ART',
    iconKey: 'mic',
    colorKey: 'amber',
    targetPerDay: 1,
    weeklyGoalDays: 6,
    ritualTimeOfDay: 'DAWN',
    sortOrder: 3,
    isArchived: false,
    createdAt: Date.now() - 120 * 86400 * 1000,
    updatedAt: Date.now() - 120 * 86400 * 1000
  },
  {
    id: 'habit-read-pages',
    slug: 'read-30-pages',
    title: 'Read 30 Pages',
    description: 'After evening dinner is cleaned, I will open physical book',
    cueAnchor: 'Kitchen counter wiped down',
    microStarter: 'Read the first paragraph of next chapter',
    identityStatement: 'I am a perpetual student',
    category: 'MIND',
    iconKey: 'book',
    colorKey: 'cyan',
    targetPerDay: 1,
    weeklyGoalDays: 7,
    ritualTimeOfDay: 'TWILIGHT',
    sortOrder: 4,
    isArchived: false,
    createdAt: Date.now() - 120 * 86400 * 1000,
    updatedAt: Date.now() - 120 * 86400 * 1000
  }
];

function generateSeedLogs(todayEpoch: number): HabitLog[] {
  const logs: HabitLog[] = [];
  const habits = [
    { slug: 'deep-work', prob: 0.85, maxCount: 2 },
    { slug: 'strength-training', prob: 0.65, maxCount: 1 },
    { slug: 'vocal-riyaaz', prob: 0.88, maxCount: 1 },
    { slug: 'read-30-pages', prob: 0.90, maxCount: 1 }
  ];

  // 140 days of history
  for (let daysAgo = 140; daysAgo >= 0; daysAgo--) {
    const day = todayEpoch - daysAgo;
    const dateObj = epochDayToDate(day);
    const dayOfWeek = dateObj.getUTCDay(); // 0 is Sunday, 6 is Sat

    for (const h of habits) {
      // Deterministic pseudo-random seed pattern
      const pseudo = Math.sin(day * 13 + h.slug.length * 7) * 0.5 + 0.5;
      const isWeekend = dayOfWeek === 0 || dayOfWeek === 6;
      const threshold = isWeekend && h.slug === 'deep-work' ? 0.3 : h.prob;

      if (pseudo < threshold) {
        const count = h.maxCount > 1 ? (pseudo < 0.4 ? 1 : 2) : 1;
        logs.push({
          id: `log-${h.slug}-${day}`,
          habitSlug: h.slug,
          dateEpochDay: day,
          count,
          note: daysAgo === 0 ? 'Logged today' : '',
          updatedAt: Date.now() - daysAgo * 86400 * 1000
        });
      }
    }
  }
  return logs;
}

export function loadHabits(): Habit[] {
  try {
    const raw = localStorage.getItem(HABITS_KEY);
    if (!raw) {
      saveHabits(SEED_HABITS);
      return SEED_HABITS;
    }
    return JSON.parse(raw);
  } catch {
    return SEED_HABITS;
  }
}

export function saveHabits(habits: Habit[]): void {
  localStorage.setItem(HABITS_KEY, JSON.stringify(habits));
}

export function loadLogs(): HabitLog[] {
  try {
    const raw = localStorage.getItem(LOGS_KEY);
    if (!raw) {
      const seedLogs = generateSeedLogs(getTodayEpochDay());
      saveLogs(seedLogs);
      return seedLogs;
    }
    return JSON.parse(raw);
  } catch {
    return [];
  }
}

export function saveLogs(logs: HabitLog[]): void {
  localStorage.setItem(LOGS_KEY, JSON.stringify(logs));
}

export function toggleCheckIn(habitSlug: string, dateEpochDay?: number): { newCount: number; habitWithLogs: HabitWithLogs } {
  const day = dateEpochDay ?? getTodayEpochDay();
  const habits = loadHabits();
  const logs = loadLogs();
  const habit = habits.find(h => h.slug === habitSlug);

  if (!habit) {
    throw new Error(`Habit not found: ${habitSlug}`);
  }

  const existingIdx = logs.findIndex(l => l.habitSlug === habitSlug && l.dateEpochDay === day);
  let newCount = 1;
  const now = Date.now();

  if (existingIdx >= 0) {
    const current = logs[existingIdx].count;
    if (habit.targetPerDay <= 1) {
      // Toggle 1 -> 0
      newCount = current > 0 ? 0 : 1;
    } else {
      // Increment until targetPerDay, then wrap to 0
      newCount = current >= habit.targetPerDay ? 0 : current + 1;
    }
    logs[existingIdx] = {
      ...logs[existingIdx],
      count: newCount,
      updatedAt: now
    };
  } else {
    logs.push({
      id: `log-${habitSlug}-${day}-${now}`,
      habitSlug,
      dateEpochDay: day,
      count: 1,
      note: '',
      updatedAt: now
    });
    newCount = 1;
  }

  saveLogs(logs);
  const allHwl = getHabitsWithLogs();
  const hwl = allHwl.find(h => h.habit.slug === habitSlug)!;
  return { newCount, habitWithLogs: hwl };
}

export function computeHabitStats(habit: Habit, logs: HabitLog[], todayEpoch: number): HabitWithLogs {
  const habitLogs = logs.filter(l => l.habitSlug === habit.slug);
  const logMap = new Map<number, number>();
  for (const l of habitLogs) {
    logMap.set(l.dateEpochDay, l.count);
  }

  const todayCount = logMap.get(todayEpoch) || 0;
  const isCompletedToday = todayCount >= habit.targetPerDay;

  // Streak calculation
  let currentStreak = 0;
  let checkDay = todayEpoch;

  if (isCompletedToday) {
    currentStreak = 1;
    checkDay = todayEpoch - 1;
  } else {
    // If today is not completed, check if yesterday was completed
    const yestCount = logMap.get(todayEpoch - 1) || 0;
    if (yestCount >= habit.targetPerDay) {
      currentStreak = 1;
      checkDay = todayEpoch - 2;
    } else {
      currentStreak = 0;
    }
  }

  if (currentStreak > 0) {
    while (true) {
      const c = logMap.get(checkDay) || 0;
      if (c >= habit.targetPerDay) {
        currentStreak++;
        checkDay--;
      } else {
        break;
      }
    }
  }

  // Best streak
  let bestStreak = 0;
  let tempStreak = 0;
  const days = Array.from(logMap.keys()).sort((a, b) => a - b);
  if (days.length > 0) {
    let prev = days[0] - 1;
    for (const d of days) {
      if ((logMap.get(d) || 0) >= habit.targetPerDay) {
        if (d === prev + 1) {
          tempStreak++;
        } else {
          tempStreak = 1;
        }
        if (tempStreak > bestStreak) bestStreak = tempStreak;
        prev = d;
      } else {
        tempStreak = 0;
      }
    }
  }
  if (currentStreak > bestStreak) bestStreak = currentStreak;

  // 30 day consistency rate
  let completedIn30Days = 0;
  for (let i = 0; i < 30; i++) {
    const c = logMap.get(todayEpoch - i) || 0;
    if (c >= habit.targetPerDay) {
      completedIn30Days++;
    }
  }
  const completionRate30Days = Math.round((completedIn30Days / 30) * 100);

  const totalCompletions = habitLogs.reduce((acc, l) => acc + (l.count >= habit.targetPerDay ? 1 : 0), 0);
  const totalActiveDays = habitLogs.filter(l => l.count > 0).length;

  return {
    habit,
    logs: habitLogs,
    currentStreak,
    bestStreak,
    totalCompletions,
    totalActiveDays,
    completionRate30Days,
    isCompletedToday,
    todayCount
  };
}

export function getHabitsWithLogs(): HabitWithLogs[] {
  const habits = loadHabits();
  const logs = loadLogs();
  const today = getTodayEpochDay();
  return habits
    .filter(h => !h.isArchived)
    .sort((a, b) => a.sortOrder - b.sortOrder)
    .map(h => computeHabitStats(h, logs, today));
}
