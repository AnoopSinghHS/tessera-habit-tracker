import React, { useState, useEffect } from 'react';
import { HabitWithLogs, RitualTime } from '../types.js';
import { ColorTokens, getPalette } from '../theme.js';
import { HabitIcon, UI_ICONS } from '../icons.js';

interface RitualChamberProps {
  habitsWithLogs: HabitWithLogs[];
  tokens: ColorTokens;
  onToggleCheckIn: (slug: string) => void;
  onClose: () => void;
}

export const RitualChamber: React.FC<RitualChamberProps> = ({
  habitsWithLogs,
  tokens,
  onToggleCheckIn,
  onClose
}) => {
  const [selectedRitual, setSelectedRitual] = useState<RitualTime>('DAWN');
  const [activeStepIndex, setActiveStepIndex] = useState(0);
  const [timerSeconds, setTimerSeconds] = useState(120);
  const [isTimerRunning, setIsTimerRunning] = useState(false);
  const [isCompletedState, setIsCompletedState] = useState(false);

  // Filter habits for the selected ritual track
  const ritualHabits = habitsWithLogs.filter(
    h => h.habit.ritualTimeOfDay === selectedRitual || selectedRitual === 'ANYTIME'
  );

  const currentHwl = ritualHabits[activeStepIndex] || ritualHabits[0];
  const palette = currentHwl ? getPalette(currentHwl.habit.colorKey, tokens.isDark) : getPalette('emerald', tokens.isDark);

  // Timer tick effect
  useEffect(() => {
    let interval: any = null;
    if (isTimerRunning && timerSeconds > 0) {
      interval = setInterval(() => {
        setTimerSeconds(s => s - 1);
      }, 1000);
    } else if (timerSeconds === 0) {
      setIsTimerRunning(false);
    }
    return () => {
      if (interval) clearInterval(interval);
    };
  }, [isTimerRunning, timerSeconds]);

  // Reset timer on step change
  useEffect(() => {
    setTimerSeconds(120);
    setIsTimerRunning(false);
  }, [activeStepIndex]);

  const handleNextStep = () => {
    if (!currentHwl) return;
    if (!currentHwl.isCompletedToday) {
      onToggleCheckIn(currentHwl.habit.slug);
    }
    if (activeStepIndex < ritualHabits.length - 1) {
      setActiveStepIndex(prev => prev + 1);
    } else {
      setIsCompletedState(true);
    }
  };

  const handlePrevStep = () => {
    if (activeStepIndex > 0) {
      setActiveStepIndex(prev => prev - 1);
    }
  };

  const formatTimer = (totalSec: number) => {
    const mins = Math.floor(totalSec / 60);
    const secs = totalSec % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  return (
    <div
      style={{
        backgroundColor: tokens.bg,
        color: tokens.textPrimary
      }}
      className="fixed inset-0 z-50 flex flex-col justify-between p-6 sm:p-10 overflow-y-auto"
    >
      {/* Top Chamber Header */}
      <div className="flex items-center justify-between border-b pb-4 max-w-4xl mx-auto w-full" style={{ borderColor: tokens.border }}>
        <div className="flex items-center gap-3">
          <div
            style={{ backgroundColor: tokens.surfaceElevated, borderColor: tokens.border }}
            className="w-9 h-9 rounded-full border flex items-center justify-center"
          >
            <UI_ICONS.Sparkles size={16} style={{ color: palette.accent }} />
          </div>
          <div>
            <span style={{ color: tokens.textSecondary }} className="text-[11px] font-mono tracking-widest uppercase block">
              Guided Ritual Chamber
            </span>
            <h2 className="font-serif text-xl sm:text-2xl font-normal tracking-tight">
              {selectedRitual === 'DAWN' ? 'The Dawn Sequence' : selectedRitual === 'FOCUS' ? 'The Deep Focus Track' : 'The Twilight Wind-Down'}
            </h2>
          </div>
        </div>

        {/* Ritual Track Selector & Close Button */}
        <div className="flex items-center gap-2">
          <div style={{ backgroundColor: tokens.surfaceElevated, borderColor: tokens.border }} className="hidden sm:flex p-1 rounded-xl border gap-1">
            {(['DAWN', 'FOCUS', 'TWILIGHT'] as RitualTime[]).map(r => (
              <button
                key={r}
                type="button"
                onClick={() => {
                  setSelectedRitual(r);
                  setActiveStepIndex(0);
                  setIsCompletedState(false);
                }}
                style={{
                  backgroundColor: selectedRitual === r ? tokens.surface : 'transparent',
                  color: selectedRitual === r ? tokens.textPrimary : tokens.textSecondary
                }}
                className="text-[12px] font-sans px-3 py-1 rounded-lg transition-colors font-medium"
              >
                {r === 'DAWN' ? 'Dawn' : r === 'FOCUS' ? 'Focus' : 'Twilight'}
              </button>
            ))}
          </div>

          <button
            type="button"
            onClick={onClose}
            style={{ backgroundColor: tokens.surfaceElevated, borderColor: tokens.border, color: tokens.textSecondary }}
            className="p-2 rounded-full border hover:brightness-110"
            title="Exit Ritual"
          >
            <UI_ICONS.X size={18} />
          </button>
        </div>
      </div>

      {/* Main Chamber Body */}
      <div className="flex-1 flex flex-col justify-center items-center py-8 max-w-2xl mx-auto w-full">
        {isCompletedState ? (
          /* Ritual Completed Celebration Card */
          <div
            style={{ backgroundColor: tokens.surface, borderColor: tokens.border }}
            className="rounded-3xl border p-8 sm:p-12 text-center max-w-lg w-full shadow-2xl transition-all"
          >
            <div
              style={{ backgroundColor: palette.accent, color: '#FFFFFF' }}
              className="w-16 h-16 rounded-full mx-auto flex items-center justify-center mb-6 shadow-md"
            >
              <UI_ICONS.Check size={32} strokeWidth={2.5} />
            </div>

            <span style={{ color: palette.accent }} className="text-[12px] font-mono tracking-widest uppercase block mb-2 font-semibold">
              Ritual Executed
            </span>
            <h3 className="font-serif text-3xl font-normal tracking-tight mb-3">
              Unbroken Cadence
            </h3>
            <p style={{ color: tokens.textSecondary }} className="text-sm font-sans mb-8 leading-relaxed">
              Every habit in this sequence has been woven into memory and illuminated across your contribution grid.
            </p>

            <button
              type="button"
              onClick={onClose}
              style={{ backgroundColor: palette.accent, color: '#FFFFFF' }}
              className="w-full py-3.5 rounded-xl font-sans text-sm font-medium transition-all hover:brightness-110 shadow-sm"
            >
              Return to Rituals
            </button>
          </div>
        ) : currentHwl ? (
          /* Step-by-Step Kinetic Ritual Stepper */
          <div className="w-full space-y-6">
            {/* Step Progress Pill */}
            <div className="flex items-center justify-between text-xs font-mono" style={{ color: tokens.textSecondary }}>
              <span>STEP {activeStepIndex + 1} OF {ritualHabits.length}</span>
              <div className="flex gap-1.5">
                {ritualHabits.map((_, idx) => (
                  <div
                    key={idx}
                    style={{
                      backgroundColor: idx === activeStepIndex ? palette.accent : tokens.surfaceElevated,
                      width: idx === activeStepIndex ? '20px' : '6px',
                      height: '4px'
                    }}
                    className="rounded-full transition-all duration-300"
                  />
                ))}
              </div>
            </div>

            {/* The Habit Stacking Weaver Visual Card */}
            <div
              style={{ backgroundColor: tokens.surface, borderColor: tokens.border }}
              className="rounded-3xl border p-6 sm:p-10 shadow-xl space-y-7"
            >
              {/* 1. The Cue / Anchor Link */}
              <div className="relative pl-8">
                <div
                  style={{ backgroundColor: palette.accent }}
                  className="absolute left-0 top-1 w-3.5 h-3.5 rounded-full ring-4 ring-opacity-20"
                />
                <span style={{ color: tokens.textSecondary }} className="text-[11px] font-mono uppercase tracking-wider block">
                  Anchor Cue (When)
                </span>
                <p className="font-sans text-[15px] font-medium mt-0.5">
                  {currentHwl.habit.cueAnchor ? `After ${currentHwl.habit.cueAnchor}` : 'First morning catalyst'}
                </p>

                {/* Vertical Connector Line */}
                <div style={{ backgroundColor: tokens.border }} className="absolute left-[6.5px] top-6 w-[1.5px] h-8" />
              </div>

              {/* 2. The 2-Minute Micro-Starter (Atoms Engine) */}
              <div className="relative pl-8">
                <div
                  style={{ backgroundColor: tokens.surfaceElevated, borderColor: palette.accent }}
                  className="absolute left-0 top-1 w-3.5 h-3.5 rounded-full border-2"
                />
                <span style={{ color: tokens.textSecondary }} className="text-[11px] font-mono uppercase tracking-wider block">
                  The 2-Minute Micro-Starter (Overcome Friction)
                </span>
                <p className="font-sans text-[15px] font-medium mt-0.5">
                  {currentHwl.habit.microStarter || 'Engage for 120 seconds to establish initial flow'}
                </p>

                {/* Micro-Timer Widget */}
                <div
                  style={{ backgroundColor: tokens.surfaceElevated, borderColor: tokens.border }}
                  className="mt-3 p-3.5 rounded-2xl border flex items-center justify-between max-w-sm"
                >
                  <div className="flex items-center gap-3">
                    <span className="font-mono text-2xl tracking-widest font-semibold" style={{ color: palette.accent }}>
                      {formatTimer(timerSeconds)}
                    </span>
                    <span style={{ color: tokens.textSecondary }} className="text-[11px] font-sans">
                      {isTimerRunning ? 'focusing...' : timerSeconds === 0 ? 'completed' : 'micro timer'}
                    </span>
                  </div>

                  <div className="flex items-center gap-1.5">
                    <button
                      type="button"
                      onClick={() => setIsTimerRunning(!isTimerRunning)}
                      style={{ backgroundColor: palette.accent, color: '#FFFFFF' }}
                      className="p-2 rounded-xl text-xs font-medium hover:brightness-110 transition-all flex items-center justify-center"
                    >
                      {isTimerRunning ? 'Pause' : 'Start'}
                    </button>
                    <button
                      type="button"
                      onClick={() => {
                        setTimerSeconds(120);
                        setIsTimerRunning(false);
                      }}
                      style={{ backgroundColor: tokens.surface, borderColor: tokens.border, color: tokens.textSecondary }}
                      className="p-2 rounded-xl border hover:brightness-110"
                      title="Reset Timer"
                    >
                      <UI_ICONS.RotateCcw size={14} />
                    </button>
                  </div>
                </div>

                {/* Vertical Connector Line */}
                <div style={{ backgroundColor: tokens.border }} className="absolute left-[6.5px] top-6 w-[1.5px] h-8" />
              </div>

              {/* 3. The Habit Action */}
              <div className="relative pl-8">
                <div
                  style={{ backgroundColor: palette.accent }}
                  className="absolute left-0 top-1 w-3.5 h-3.5 rounded-full ring-4 ring-opacity-20"
                />
                <span style={{ color: tokens.textSecondary }} className="text-[11px] font-mono uppercase tracking-wider block">
                  Habit Action
                </span>
                <div className="flex items-center gap-3 mt-1">
                  <div style={{ color: palette.accent }} className="p-1 rounded-lg">
                    <HabitIcon name={currentHwl.habit.iconKey} size={28} strokeWidth={1.8} />
                  </div>
                  <div>
                    <h3 className="font-sans text-xl font-medium tracking-tight">
                      {currentHwl.habit.title}
                    </h3>
                    <span style={{ color: tokens.textSecondary }} className="text-xs font-mono">
                      Current Streak: {currentHwl.currentStreak} days
                    </span>
                  </div>
                </div>

                {/* Vertical Connector Line */}
                <div style={{ backgroundColor: tokens.border }} className="absolute left-[6.5px] top-6 w-[1.5px] h-8" />
              </div>

              {/* 4. The Identity Statement (Belief Anchor) */}
              <div className="relative pl-8">
                <div
                  style={{ backgroundColor: tokens.surfaceElevated, borderColor: palette.accent }}
                  className="absolute left-0 top-1 w-3.5 h-3.5 rounded-full border-2"
                />
                <span style={{ color: tokens.textSecondary }} className="text-[11px] font-mono uppercase tracking-wider block">
                  Identity Anchor (Belief)
                </span>
                <p
                  style={{ fontFamily: 'Newsreader, Georgia, serif', color: tokens.textPrimary }}
                  className="text-lg italic mt-1 leading-snug"
                >
                  "{currentHwl.habit.identityStatement || `I am the type of person who stays consistent with ${currentHwl.habit.title.toLowerCase()}`}"
                </p>
              </div>
            </div>

            {/* Stepper Navigation Buttons */}
            <div className="flex items-center justify-between gap-4 pt-2">
              <button
                type="button"
                onClick={handlePrevStep}
                disabled={activeStepIndex === 0}
                style={{
                  backgroundColor: tokens.surfaceElevated,
                  borderColor: tokens.border,
                  color: activeStepIndex === 0 ? tokens.textMuted : tokens.textPrimary,
                  opacity: activeStepIndex === 0 ? 0.5 : 1
                }}
                className="px-5 py-3 rounded-2xl border font-sans text-sm font-medium flex items-center gap-2 hover:brightness-105 disabled:cursor-not-allowed"
              >
                <UI_ICONS.ChevronLeft size={16} />
                Previous
              </button>

              <button
                type="button"
                onClick={handleNextStep}
                style={{
                  backgroundColor: palette.accent,
                  color: '#FFFFFF'
                }}
                className="flex-1 py-3.5 rounded-2xl font-sans text-sm font-medium flex items-center justify-center gap-2 hover:brightness-110 shadow-lg active:scale-95 transition-all"
              >
                <UI_ICONS.Check size={18} strokeWidth={2.5} />
                {activeStepIndex < ritualHabits.length - 1 ? 'Complete Step & Advance' : 'Seal Ritual'}
                <UI_ICONS.ChevronRight size={16} />
              </button>
            </div>
          </div>
        ) : (
          <div className="text-center py-12" style={{ color: tokens.textSecondary }}>
            <p>No habits configured for this ritual sequence yet.</p>
          </div>
        )}
      </div>

      {/* Bottom Footer Tip */}
      <div className="text-center text-xs font-mono" style={{ color: tokens.textMuted }}>
        James Clear 4 Laws + Dan Ariely Ritual Stepper • Tessera Engine
      </div>
    </div>
  );
};
