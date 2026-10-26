import React, { useMemo } from 'react';
import { HabitLog } from '../types.js';
import { getPalette } from '../theme.js';
import { getTodayEpochDay, epochDayToDate } from '../storage.js';

interface GithubHeatmapProps {
  logs: HabitLog[];
  colorKey: string;
  isDark: boolean;
  targetPerDay: number;
  weeks?: number;
  onSelectDate?: (epochDay: number) => void;
}

export const GithubHeatmap: React.FC<GithubHeatmapProps> = ({
  logs,
  colorKey,
  isDark,
  targetPerDay,
  weeks = 18,
  onSelectDate
}) => {
  const palette = useMemo(() => getPalette(colorKey, isDark), [colorKey, isDark]);
  const todayEpoch = useMemo(() => getTodayEpochDay(), []);

  const gridData = useMemo(() => {
    const logMap = new Map<number, number>();
    for (const l of logs) {
      logMap.set(l.dateEpochDay, l.count);
    }

    // Align to Monday of earliest week
    const todayDate = epochDayToDate(todayEpoch);
    const dayOfWeek = (todayDate.getUTCDay() + 6) % 7; // 0 = Mon, 6 = Sun
    const totalDays = weeks * 7;
    const startDay = todayEpoch - totalDays + (6 - dayOfWeek);

    const cols: { weekIdx: number; days: { epochDay: number; count: number; level: number; isToday: boolean }[] }[] = [];

    for (let w = 0; w < weeks; w++) {
      const daysInWeek: { epochDay: number; count: number; level: number; isToday: boolean }[] = [];
      for (let d = 0; d < 7; d++) {
        const ep = startDay + (w * 7) + d;
        const count = logMap.get(ep) || 0;
        let level = 0;
        if (count > 0) {
          if (targetPerDay <= 1) {
            level = 4;
          } else {
            const ratio = count / targetPerDay;
            if (ratio >= 1.0) level = 4;
            else if (ratio >= 0.75) level = 3;
            else if (ratio >= 0.5) level = 2;
            else level = 1;
          }
        }
        daysInWeek.push({
          epochDay: ep,
          count,
          level,
          isToday: ep === todayEpoch
        });
      }
      cols.push({ weekIdx: w, days: daysInWeek });
    }
    return cols;
  }, [logs, todayEpoch, targetPerDay, weeks]);

  return (
    <div className="flex items-center gap-[3px] select-none overflow-x-auto py-1">
      {gridData.map(col => (
        <div key={col.weekIdx} className="flex flex-col gap-[3px]">
          {col.days.map(day => {
            const fillColor = palette.stops[day.level];
            const isFuture = day.epochDay > todayEpoch;
            const title = isFuture
              ? 'Future'
              : `${epochDayToDate(day.epochDay).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}: ${day.count} ${day.count === 1 ? 'completion' : 'completions'}`;

            return (
              <div
                key={day.epochDay}
                title={title}
                onClick={() => !isFuture && onSelectDate && onSelectDate(day.epochDay)}
                style={{
                  width: '10px',
                  height: '10px',
                  borderRadius: '2.5px',
                  backgroundColor: isFuture ? (isDark ? '#1a1918' : '#eeebe4') : fillColor,
                  opacity: isFuture ? 0.35 : 1,
                  boxShadow: day.isToday
                    ? `0 0 0 1.5px ${palette.accent}`
                    : undefined,
                  cursor: isFuture ? 'default' : 'pointer'
                }}
                className="transition-transform hover:scale-125"
              />
            );
          })}
        </div>
      ))}
    </div>
  );
};
