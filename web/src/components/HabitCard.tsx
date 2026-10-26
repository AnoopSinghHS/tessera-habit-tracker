import React, { useState } from 'react';
import { HabitWithLogs } from '../types.js';
import { ColorTokens, getPalette } from '../theme.js';
import { HabitIcon, UI_ICONS } from '../icons.js';
import { GithubHeatmap } from './GithubHeatmap.js';

interface HabitCardProps {
  habitWithLogs: HabitWithLogs;
  tokens: ColorTokens;
  onToggleCheckIn: (slug: string) => void;
  onOpenDetail?: (habitWithLogs: HabitWithLogs) => void;
}

export const HabitCard: React.FC<HabitCardProps> = ({
  habitWithLogs,
  tokens,
  onToggleCheckIn,
  onOpenDetail
}) => {
  const { habit, logs, currentStreak, isCompletedToday, todayCount } = habitWithLogs;
  const palette = getPalette(habit.colorKey, tokens.isDark);
  const [isPressing, setIsPressing] = useState(false);

  const handleCheckIn = (e: React.MouseEvent) => {
    e.stopPropagation();
    setIsPressing(true);
    setTimeout(() => setIsPressing(false), 200);
    onToggleCheckIn(habit.slug);
  };

  return (
    <div
      onClick={() => onOpenDetail && onOpenDetail(habitWithLogs)}
      style={{
        backgroundColor: tokens.surface,
        borderColor: tokens.border,
        color: tokens.textPrimary
      }}
      className="group relative rounded-2xl border p-5 transition-all duration-200 hover:shadow-lg hover:border-opacity-80 cursor-pointer"
    >
      {/* Top Header Row */}
      <div className="flex items-start justify-between gap-3 mb-4">
        <div className="flex items-start gap-3 flex-1 min-w-0">
          <div
            style={{ color: tokens.textPrimary }}
            className="p-1 rounded-lg mt-0.5"
          >
            <HabitIcon name={habit.iconKey} size={22} strokeWidth={1.8} />
          </div>

          <div className="flex flex-col min-w-0 flex-1">
            <div className="flex items-center gap-2">
              <h3 className="font-sans text-[17px] font-medium tracking-tight truncate">
                {habit.title}
              </h3>
              {habit.targetPerDay > 1 && (
                <span
                  style={{
                    backgroundColor: tokens.surfaceElevated,
                    color: tokens.textSecondary,
                    borderColor: tokens.border
                  }}
                  className="text-[11px] font-mono px-1.5 py-0.5 rounded border"
                >
                  {todayCount}/{habit.targetPerDay}
                </span>
              )}
            </div>

            {/* Habit Stacking Cue Subtitle (Atoms Equation) */}
            {habit.cueAnchor ? (
              <p
                style={{ color: tokens.textSecondary }}
                className="text-[13px] font-sans truncate mt-0.5 opacity-90"
              >
                After {habit.cueAnchor}, I will {habit.title.toLowerCase()}
              </p>
            ) : habit.description ? (
              <p
                style={{ color: tokens.textSecondary }}
                className="text-[13px] font-sans truncate mt-0.5 opacity-90"
              >
                {habit.description}
              </p>
            ) : null}
          </div>
        </div>

        {/* Circular Check-In Button */}
        <button
          type="button"
          aria-label={`Log ${habit.title}`}
          onClick={handleCheckIn}
          style={{
            width: '36px',
            height: '36px',
            borderRadius: '50%',
            backgroundColor: isCompletedToday ? palette.accent : tokens.surfaceElevated,
            borderColor: isCompletedToday ? palette.accent : tokens.border,
            color: isCompletedToday ? '#FFFFFF' : tokens.textSecondary,
            transform: isPressing ? 'scale(0.88)' : 'scale(1)'
          }}
          className="flex items-center justify-center border transition-all duration-150 hover:brightness-110 active:scale-90 flex-shrink-0"
        >
          {isCompletedToday ? (
            <UI_ICONS.Check size={18} strokeWidth={2.5} />
          ) : habit.targetPerDay > 1 && todayCount > 0 ? (
            <span className="font-mono text-[12px] font-semibold">+{todayCount}</span>
          ) : (
            <UI_ICONS.Plus size={16} strokeWidth={2} />
          )}
        </button>
      </div>

      {/* Asymmetric Split Bottom Row */}
      <div className="flex items-end justify-between gap-4 pt-1">
        {/* Left Column: Sculptural Serif Streak */}
        <div className="flex flex-col flex-shrink-0 w-[72px]">
          <span
            style={{
              fontFamily: 'Newsreader, Georgia, serif',
              color: currentStreak > 0 ? palette.accent : tokens.textMuted
            }}
            className="text-[34px] leading-none font-normal tracking-tight"
          >
            {currentStreak}
          </span>
          <span
            style={{ color: tokens.textSecondary }}
            className="text-[12px] font-sans font-medium mt-1 uppercase tracking-wider text-[10px]"
          >
            day streak
          </span>
        </div>

        {/* Right Column: GitHub Squares Heatmap */}
        <div className="flex-1 flex justify-end overflow-hidden">
          <GithubHeatmap
            logs={logs}
            colorKey={habit.colorKey}
            isDark={tokens.isDark}
            targetPerDay={habit.targetPerDay}
            weeks={18}
          />
        </div>
      </div>
    </div>
  );
};
