import React, { useState } from 'react';
import { Habit, RitualTime } from '../types.js';
import { ColorTokens, PALETTES } from '../theme.js';
import { HabitIcon, UI_ICONS } from '../icons.js';

interface HabitStackWeaverModalProps {
  tokens: ColorTokens;
  onSave: (habit: Habit) => void;
  onClose: () => void;
  initialHabit?: Habit | null;
}

const AVAILABLE_ICONS = [
  'mic', 'waveform', 'clef', 'bolt',
  'pen', 'pencil', 'quill', 'book', 'books', 'bookmark',
  'fitness', 'flame', 'heart', 'apple', 'water', 'bowl', 'scale',
  'laptop', 'code', 'terminal', 'clock', 'stopwatch', 'hourglass',
  'trending-up', 'sprout', 'target', 'compass', 'star', 'sun', 'moon'
];

export const HabitStackWeaverModal: React.FC<HabitStackWeaverModalProps> = ({
  tokens,
  onSave,
  onClose,
  initialHabit
}) => {
  const [title, setTitle] = useState(initialHabit?.title || '');
  const [cueAnchor, setCueAnchor] = useState(initialHabit?.cueAnchor || '');
  const [microStarter, setMicroStarter] = useState(initialHabit?.microStarter || '');
  const [identityStatement, setIdentityStatement] = useState(initialHabit?.identityStatement || '');
  const [category, setCategory] = useState(initialHabit?.category || 'CRAFT');
  const [ritualTime, setRitualTime] = useState<RitualTime>(initialHabit?.ritualTimeOfDay || 'DAWN');
  const [iconKey, setIconKey] = useState(initialHabit?.iconKey || 'bolt');
  const [colorKey, setColorKey] = useState(initialHabit?.colorKey || 'emerald');
  const [targetPerDay, setTargetPerDay] = useState(initialHabit?.targetPerDay || 1);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim()) return;

    const slug = initialHabit?.slug || title.trim().toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)/g, '');
    const now = Date.now();

    const saved: Habit = {
      id: initialHabit?.id || `habit-${slug}-${now}`,
      slug,
      title: title.trim(),
      description: cueAnchor ? `After ${cueAnchor.trim()}, I will ${title.trim()}` : '',
      cueAnchor: cueAnchor.trim(),
      microStarter: microStarter.trim(),
      identityStatement: identityStatement.trim(),
      category: category.trim().toUpperCase(),
      iconKey,
      colorKey,
      targetPerDay,
      weeklyGoalDays: initialHabit?.weeklyGoalDays || 7,
      ritualTimeOfDay: ritualTime,
      sortOrder: initialHabit?.sortOrder || 99,
      isArchived: false,
      createdAt: initialHabit?.createdAt || now,
      updatedAt: now
    };

    onSave(saved);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm overflow-y-auto">
      <div
        style={{
          backgroundColor: tokens.surface,
          borderColor: tokens.border,
          color: tokens.textPrimary
        }}
        className="rounded-3xl border max-w-xl w-full p-6 sm:p-8 shadow-2xl my-8"
      >
        <div className="flex items-center justify-between border-b pb-4 mb-6" style={{ borderColor: tokens.border }}>
          <div>
            <span style={{ color: tokens.accent }} className="text-[11px] font-mono uppercase tracking-widest block font-semibold">
              The Habit Stack Weaver
            </span>
            <h2 className="font-serif text-2xl font-normal tracking-tight">
              {initialHabit ? 'Refine Habit Architecture' : 'Architect New Ritual'}
            </h2>
          </div>
          <button
            type="button"
            onClick={onClose}
            style={{ backgroundColor: tokens.surfaceElevated, borderColor: tokens.border, color: tokens.textSecondary }}
            className="p-2 rounded-full border hover:brightness-110"
          >
            <UI_ICONS.X size={18} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* 1. Habit Title */}
          <div>
            <label className="block text-xs font-mono uppercase tracking-wider mb-2" style={{ color: tokens.textSecondary }}>
              Habit Name
            </label>
            <input
              type="text"
              required
              placeholder="e.g. Vocal Riyaaz & Singing"
              value={title}
              onChange={e => setTitle(e.target.value)}
              style={{
                backgroundColor: tokens.surfaceElevated,
                borderColor: tokens.border,
                color: tokens.textPrimary
              }}
              className="w-full px-4 py-3 rounded-xl border font-sans text-sm outline-none focus:ring-2 focus:ring-offset-1"
            />
          </div>

          {/* 2. Atoms Habit Stacking Equation */}
          <div style={{ backgroundColor: tokens.surfaceElevated, borderColor: tokens.border }} className="p-4 rounded-2xl border space-y-4">
            <span className="text-[11px] font-mono uppercase tracking-wider font-semibold block" style={{ color: tokens.accent }}>
              Behavioral Stacking Bond (Atoms)
            </span>

            <div>
              <label className="block text-xs font-sans mb-1" style={{ color: tokens.textSecondary }}>
                Anchor Cue: "After I..."
              </label>
              <input
                type="text"
                placeholder="e.g. Pour morning pour-over coffee"
                value={cueAnchor}
                onChange={e => setCueAnchor(e.target.value)}
                style={{
                  backgroundColor: tokens.surface,
                  borderColor: tokens.border,
                  color: tokens.textPrimary
                }}
                className="w-full px-3.5 py-2.5 rounded-lg border font-sans text-sm outline-none"
              />
            </div>

            <div>
              <label className="block text-xs font-sans mb-1" style={{ color: tokens.textSecondary }}>
                The 2-Minute Micro-Starter:
              </label>
              <input
                type="text"
                placeholder="e.g. Hum 3 vocal scales for 120 seconds"
                value={microStarter}
                onChange={e => setMicroStarter(e.target.value)}
                style={{
                  backgroundColor: tokens.surface,
                  borderColor: tokens.border,
                  color: tokens.textPrimary
                }}
                className="w-full px-3.5 py-2.5 rounded-lg border font-sans text-sm outline-none"
              />
            </div>

            <div>
              <label className="block text-xs font-sans mb-1" style={{ color: tokens.textSecondary }}>
                Identity Statement: "I am the type of person who..."
              </label>
              <input
                type="text"
                placeholder="e.g. I am a dedicated vocalist building vocal range"
                value={identityStatement}
                onChange={e => setIdentityStatement(e.target.value)}
                style={{
                  backgroundColor: tokens.surface,
                  borderColor: tokens.border,
                  color: tokens.textPrimary
                }}
                className="w-full px-3.5 py-2.5 rounded-lg border font-sans text-sm outline-none"
              />
            </div>
          </div>

          {/* 3. Ritual Sequence Track */}
          <div>
            <label className="block text-xs font-mono uppercase tracking-wider mb-2" style={{ color: tokens.textSecondary }}>
              Ritual Sequence Flow (Fabulous)
            </label>
            <div className="grid grid-cols-3 gap-2">
              {(['DAWN', 'FOCUS', 'TWILIGHT'] as RitualTime[]).map(r => (
                <button
                  key={r}
                  type="button"
                  onClick={() => setRitualTime(r)}
                  style={{
                    backgroundColor: ritualTime === r ? tokens.accent : tokens.surfaceElevated,
                    color: ritualTime === r ? '#FFFFFF' : tokens.textPrimary,
                    borderColor: tokens.border
                  }}
                  className="py-2.5 px-3 rounded-xl border text-xs font-sans font-medium text-center transition-all"
                >
                  {r === 'DAWN' ? 'Dawn (Morning)' : r === 'FOCUS' ? 'Deep Focus' : 'Twilight (Night)'}
                </button>
              ))}
            </div>
          </div>

          {/* 4. Color Palette & Icon Selection */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-mono uppercase tracking-wider mb-2" style={{ color: tokens.textSecondary }}>
                Pigment Palette
              </label>
              <div className="flex flex-wrap gap-2">
                {Object.values(PALETTES).map(p => (
                  <button
                    key={p.key}
                    type="button"
                    onClick={() => setColorKey(p.key)}
                    style={{
                      backgroundColor: p.accent,
                      borderColor: colorKey === p.key ? tokens.textPrimary : 'transparent'
                    }}
                    className="w-6 h-6 rounded-full border-2 transition-transform hover:scale-110"
                    title={p.name}
                  />
                ))}
              </div>
            </div>

            <div>
              <label className="block text-xs font-mono uppercase tracking-wider mb-2" style={{ color: tokens.textSecondary }}>
                Daily Target Count
              </label>
              <div className="flex items-center gap-2">
                <button
                  type="button"
                  onClick={() => setTargetPerDay(Math.max(1, targetPerDay - 1))}
                  style={{ backgroundColor: tokens.surfaceElevated, borderColor: tokens.border }}
                  className="w-8 h-8 rounded-lg border flex items-center justify-center font-mono"
                >
                  -
                </button>
                <span className="font-mono text-sm w-8 text-center">{targetPerDay}</span>
                <button
                  type="button"
                  onClick={() => setTargetPerDay(targetPerDay + 1)}
                  style={{ backgroundColor: tokens.surfaceElevated, borderColor: tokens.border }}
                  className="w-8 h-8 rounded-lg border flex items-center justify-center font-mono"
                >
                  +
                </button>
              </div>
            </div>
          </div>

          {/* 5. Fine-Line Icon Picker */}
          <div>
            <label className="block text-xs font-mono uppercase tracking-wider mb-2" style={{ color: tokens.textSecondary }}>
              Fine-Line Icon
            </label>
            <div
              style={{ backgroundColor: tokens.surfaceElevated, borderColor: tokens.border }}
              className="p-3 rounded-2xl border flex flex-wrap gap-2 max-h-32 overflow-y-auto"
            >
              {AVAILABLE_ICONS.map(ic => (
                <button
                  key={ic}
                  type="button"
                  onClick={() => setIconKey(ic)}
                  style={{
                    backgroundColor: iconKey === ic ? tokens.surface : 'transparent',
                    borderColor: iconKey === ic ? tokens.accent : 'transparent',
                    color: iconKey === ic ? tokens.accent : tokens.textSecondary
                  }}
                  className="p-2 rounded-xl border transition-all hover:scale-110 flex items-center justify-center"
                  title={ic}
                >
                  <HabitIcon name={ic} size={18} />
                </button>
              ))}
            </div>
          </div>

          {/* Submit Action */}
          <div className="flex items-center justify-end gap-3 pt-3 border-t" style={{ borderColor: tokens.border }}>
            <button
              type="button"
              onClick={onClose}
              style={{ color: tokens.textSecondary }}
              className="px-4 py-2.5 rounded-xl font-sans text-sm font-medium hover:underline"
            >
              Cancel
            </button>
            <button
              type="submit"
              style={{ backgroundColor: tokens.accent, color: '#FFFFFF' }}
              className="px-6 py-2.5 rounded-xl font-sans text-sm font-medium hover:brightness-110 transition-all shadow-md"
            >
              Seal Architecture
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
