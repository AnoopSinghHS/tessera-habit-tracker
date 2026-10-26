import React, { useMemo } from 'react';
import { HabitWithLogs } from '../types.js';
import { ColorTokens, getPalette } from '../theme.js';
import { epochDayToDate } from '../storage.js';

interface TelemetryViewProps {
  habitsWithLogs: HabitWithLogs[];
  tokens: ColorTokens;
}

export const TelemetryView: React.FC<TelemetryViewProps> = ({ habitsWithLogs, tokens }) => {
  // Aggregate 30-day rate
  const aggregateRate = useMemo(() => {
    if (habitsWithLogs.length === 0) return 0;
    const total = habitsWithLogs.reduce((acc, h) => acc + h.completionRate30Days, 0);
    return Math.round(total / habitsWithLogs.length);
  }, [habitsWithLogs]);

  // Aggregate weekday rhythm
  const weekdayCounts = useMemo(() => {
    const counts = [0, 0, 0, 0, 0, 0, 0]; // Mon..Sun
    for (const h of habitsWithLogs) {
      for (const l of h.logs) {
        if (l.count >= h.habit.targetPerDay) {
          const date = epochDayToDate(l.dateEpochDay);
          const day = (date.getUTCDay() + 6) % 7; // 0 = Mon, 6 = Sun
          counts[day]++;
        }
      }
    }
    return counts;
  }, [habitsWithLogs]);

  const maxWeekday = Math.max(...weekdayCounts, 1);
  const weekdayNames = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

  return (
    <div className="space-y-8 max-w-4xl mx-auto w-full py-4">
      {/* Top Header */}
      <div>
        <span style={{ color: tokens.textSecondary }} className="text-[11px] font-mono tracking-widest uppercase block mb-1">
          Architectural Insights & Telemetry
        </span>
        <h2 className="font-serif text-3xl font-normal tracking-tight">
          Consistency Index & Cadence
        </h2>
        <p style={{ color: tokens.textSecondary }} className="text-sm font-sans mt-1">
          Objective telemetry tracking rolling 30-day adherence and weekly rhythms across all rituals.
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* 1. The 30-Day Consistency Radial Dial */}
        <div
          style={{ backgroundColor: tokens.surface, borderColor: tokens.border }}
          className="rounded-3xl border p-6 flex flex-col items-center justify-center text-center shadow-sm"
        >
          <span style={{ color: tokens.textSecondary }} className="text-xs font-mono uppercase tracking-wider mb-4">
            30-Day Rolling Adherence
          </span>

          <div className="relative w-44 h-44 flex items-center justify-center my-2">
            <svg className="w-full h-full transform -rotate-90" viewBox="0 0 100 100">
              <circle
                cx="50"
                cy="50"
                r="42"
                stroke={tokens.surfaceElevated}
                strokeWidth="7"
                fill="none"
              />
              <circle
                cx="50"
                cy="50"
                r="42"
                stroke={tokens.accent}
                strokeWidth="7"
                strokeDasharray="264"
                strokeDashoffset={264 - (264 * aggregateRate) / 100}
                strokeLinecap="round"
                fill="none"
                className="transition-all duration-1000 ease-out"
              />
            </svg>
            <div className="absolute flex flex-col items-center">
              <span
                style={{ fontFamily: 'Newsreader, Georgia, serif', color: tokens.accent }}
                className="text-4xl font-normal"
              >
                {aggregateRate}%
              </span>
              <span style={{ color: tokens.textSecondary }} className="text-[11px] font-sans uppercase tracking-wider">
                consistency
              </span>
            </div>
          </div>

          <p style={{ color: tokens.textSecondary }} className="text-xs font-sans mt-4 max-w-xs leading-relaxed">
            Measures the percentage of targeted days where all intended ritual goals were successfully met over the past month.
          </p>
        </div>

        {/* 2. Weekday Rhythm Column Chart */}
        <div
          style={{ backgroundColor: tokens.surface, borderColor: tokens.border }}
          className="rounded-3xl border p-6 flex flex-col justify-between shadow-sm"
        >
          <div>
            <span style={{ color: tokens.textSecondary }} className="text-xs font-mono uppercase tracking-wider block mb-1">
              Weekday Rhythm & Distribution
            </span>
            <p style={{ color: tokens.textSecondary }} className="text-xs font-sans mb-6">
              Total lifetime completed rituals organized by day of the week.
            </p>
          </div>

          <div className="flex items-end justify-between gap-3 h-40 pt-4 px-2">
            {weekdayCounts.map((count, idx) => {
              const heightPct = Math.max(12, Math.round((count / maxWeekday) * 100));
              return (
                <div key={idx} className="flex-1 flex flex-col items-center gap-2 h-full justify-end">
                  <span style={{ color: tokens.textSecondary }} className="text-[10px] font-mono">
                    {count}
                  </span>
                  <div
                    style={{
                      height: `${heightPct}%`,
                      backgroundColor: count === maxWeekday ? tokens.accent : tokens.surfaceElevated,
                      borderColor: tokens.border
                    }}
                    className="w-full rounded-t-lg transition-all duration-500 border border-b-0 hover:brightness-110"
                  />
                  <span style={{ color: tokens.textPrimary }} className="text-[11px] font-sans font-medium">
                    {weekdayNames[idx]}
                  </span>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      {/* 3. Habit Leaderboard Table */}
      <div
        style={{ backgroundColor: tokens.surface, borderColor: tokens.border }}
        className="rounded-3xl border p-6 shadow-sm overflow-hidden"
      >
        <span style={{ color: tokens.textSecondary }} className="text-xs font-mono uppercase tracking-wider block mb-4">
          Ritual Telemetry Breakdown
        </span>

        <div className="overflow-x-auto">
          <table className="w-full text-left font-sans text-sm">
            <thead>
              <tr className="border-b text-[11px] font-mono uppercase" style={{ borderColor: tokens.border, color: tokens.textSecondary }}>
                <th className="pb-3 font-normal">Ritual</th>
                <th className="pb-3 font-normal">Current Streak</th>
                <th className="pb-3 font-normal">Best Streak</th>
                <th className="pb-3 font-normal">30-Day Rate</th>
                <th className="pb-3 font-normal">Total Days</th>
              </tr>
            </thead>
            <tbody className="divide-y" style={{ borderColor: tokens.border }}>
              {habitsWithLogs.map(h => {
                const palette = getPalette(h.habit.colorKey, tokens.isDark);
                return (
                  <tr key={h.habit.id} className="hover:bg-black/5 dark:hover:bg-white/5 transition-colors">
                    <td className="py-3.5 font-medium flex items-center gap-2">
                      <div className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: palette.accent }} />
                      {h.habit.title}
                    </td>
                    <td className="py-3.5 font-mono" style={{ color: palette.accent }}>
                      {h.currentStreak}d
                    </td>
                    <td className="py-3.5 font-mono" style={{ color: tokens.textSecondary }}>
                      {h.bestStreak}d
                    </td>
                    <td className="py-3.5 font-mono">
                      {h.completionRate30Days}%
                    </td>
                    <td className="py-3.5 font-mono" style={{ color: tokens.textSecondary }}>
                      {h.totalActiveDays}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
