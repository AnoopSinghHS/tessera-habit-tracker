import React, { useState, useEffect } from 'react';
import { Habit, HabitWithLogs, ThemeMode, RitualTime, UserProfile } from './types.js';
import { THEMES } from './theme.js';
import { getHabitsWithLogs, toggleCheckIn, saveHabits, loadHabits } from './storage.js';
import { getStoredUser, triggerSync } from './syncClient.js';
import { HabitCard } from './components/HabitCard.js';
import { RitualChamber } from './components/RitualChamber.js';
import { HabitStackWeaverModal } from './components/HabitStackWeaverModal.js';
import { TelemetryView } from './components/TelemetryView.js';
import { AuthModal } from './components/AuthModal.js';
import { UI_ICONS } from './icons.js';

export function App() {
  const [themeMode, setThemeMode] = useState<ThemeMode>('light');
  const [currentTab, setCurrentTab] = useState<'today' | 'chamber' | 'insights' | 'sync'>('today');
  const [habitsWithLogs, setHabitsWithLogs] = useState<HabitWithLogs[]>([]);
  const [selectedFilter, setSelectedFilter] = useState<RitualTime | 'ALL'>('ALL');
  const [isWeaverOpen, setIsWeaverOpen] = useState(false);
  const [isAuthOpen, setIsAuthOpen] = useState(false);
  const [currentUser, setCurrentUser] = useState<UserProfile | null>(null);
  const [syncStatus, setSyncStatus] = useState<'idle' | 'syncing' | 'synced' | 'error'>('idle');

  const tokens = THEMES[themeMode] || THEMES.light;

  const refreshData = () => {
    setHabitsWithLogs(getHabitsWithLogs());
  };

  useEffect(() => {
    refreshData();
    setCurrentUser(getStoredUser());
  }, []);

  const handleToggleCheckIn = (slug: string) => {
    toggleCheckIn(slug);
    refreshData();
  };

  const handleSaveHabit = (newHabit: Habit) => {
    const existing = loadHabits();
    const idx = existing.findIndex(h => h.slug === newHabit.slug);
    if (idx >= 0) {
      existing[idx] = newHabit;
    } else {
      existing.push(newHabit);
    }
    saveHabits(existing);
    refreshData();
    setIsWeaverOpen(false);
  };

  const cycleTheme = () => {
    const modes: ThemeMode[] = ['light', 'dark', 'dune', 'space', 'tatooine'];
    const nextIdx = (modes.indexOf(themeMode) + 1) % modes.length;
    setThemeMode(modes[nextIdx]);
  };

  const handleManualSync = async () => {
    setSyncStatus('syncing');
    try {
      await triggerSync();
      refreshData();
      setSyncStatus('synced');
      setTimeout(() => setSyncStatus('idle'), 3000);
    } catch {
      setSyncStatus('error');
      setTimeout(() => setSyncStatus('idle'), 3000);
    }
  };

  const filteredHabits = habitsWithLogs.filter(h => {
    if (selectedFilter === 'ALL') return true;
    return h.habit.ritualTimeOfDay === selectedFilter;
  });

  const completedCount = habitsWithLogs.filter(h => h.isCompletedToday).length;
  const totalCount = habitsWithLogs.length;

  const formattedDate = new Date().toLocaleDateString('en-US', {
    weekday: 'long',
    month: 'short',
    day: 'numeric'
  });

  return (
    <div
      style={{
        backgroundColor: tokens.bg,
        color: tokens.textPrimary,
        minHeight: '100vh',
        fontFamily: 'Inter, system-ui, sans-serif'
      }}
      className="transition-colors duration-300 flex flex-col"
    >
      {/* Top Architectural Navigation Bar */}
      <header
        style={{
          borderColor: tokens.border,
          backgroundColor: tokens.bg
        }}
        className="sticky top-0 z-40 border-b backdrop-blur-md px-4 sm:px-8 py-3.5 flex items-center justify-between max-w-6xl mx-auto w-full"
      >
        <div className="flex items-center gap-3">
          <div
            style={{ backgroundColor: tokens.accent, color: '#FFFFFF' }}
            className="w-8 h-8 rounded-xl flex items-center justify-center shadow-sm"
          >
            <UI_ICONS.Sparkles size={16} />
          </div>
          <div>
            <span style={{ color: tokens.textSecondary }} className="text-[10px] font-mono tracking-widest uppercase block">
              Tessera Ritual Engine
            </span>
            <span className="font-serif text-lg font-normal tracking-tight block leading-tight">
              {tokens.name}
            </span>
          </div>
        </div>

        {/* Action Controls */}
        <div className="flex items-center gap-2">
          {/* Cloud Sync Status Button */}
          <button
            type="button"
            onClick={() => {
              if (currentUser) {
                handleManualSync();
              } else {
                setIsAuthOpen(true);
              }
            }}
            style={{
              backgroundColor: tokens.surfaceElevated,
              borderColor: tokens.border,
              color: syncStatus === 'error' ? '#ef4444' : syncStatus === 'synced' ? '#10b981' : tokens.textSecondary
            }}
            className="text-xs font-mono px-3 py-1.5 rounded-xl border flex items-center gap-1.5 hover:brightness-105 transition-all"
            title={currentUser ? `Signed in as ${currentUser.email}. Click to sync.` : 'Connect Cloud Account'}
          >
            <UI_ICONS.RefreshCw size={12} className={syncStatus === 'syncing' ? 'animate-spin' : ''} />
            <span className="hidden sm:inline">
              {currentUser ? (syncStatus === 'syncing' ? 'Syncing...' : syncStatus === 'synced' ? 'Synced' : 'Cloud Sync') : 'Connect Sync'}
            </span>
          </button>

          {/* Theme Mode Toggle Button */}
          <button
            type="button"
            onClick={cycleTheme}
            style={{
              backgroundColor: tokens.surfaceElevated,
              borderColor: tokens.border,
              color: tokens.textPrimary
            }}
            className="p-2 rounded-xl border hover:brightness-105 transition-all flex items-center gap-1 text-xs font-mono"
            title="Cycle Themes (Limestone, Carbon, Dune, Deep Space, Tatooine)"
          >
            {tokens.isDark ? <UI_ICONS.Sun size={15} /> : <UI_ICONS.Moon size={15} />}
          </button>

          {/* Guided Ritual Chamber Launch Button */}
          <button
            type="button"
            onClick={() => setCurrentTab('chamber')}
            style={{
              backgroundColor: tokens.accent,
              color: '#FFFFFF'
            }}
            className="px-3.5 py-1.5 rounded-xl text-xs font-sans font-medium flex items-center gap-1.5 hover:brightness-110 shadow-sm transition-all"
          >
            <UI_ICONS.Play size={12} strokeWidth={2.5} />
            <span className="hidden sm:inline">Begin Ritual Flow</span>
          </button>

          {/* New Habit Button */}
          <button
            type="button"
            onClick={() => setIsWeaverOpen(true)}
            style={{
              backgroundColor: tokens.surfaceElevated,
              borderColor: tokens.border,
              color: tokens.textPrimary
            }}
            className="p-2 rounded-xl border hover:brightness-105"
            title="Weave New Habit"
          >
            <UI_ICONS.Plus size={16} />
          </button>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="flex-1 max-w-5xl mx-auto w-full px-4 sm:px-8 py-8">
        {currentTab === 'today' && (
          <div className="space-y-8">
            {/* Editorial Hero Header */}
            <div className="text-center py-4 space-y-1">
              <h1
                style={{
                  fontFamily: 'Newsreader, Georgia, serif',
                  letterSpacing: '-0.02em'
                }}
                className="text-4xl sm:text-5xl font-normal"
              >
                {formattedDate}
              </h1>
              <p
                style={{ color: tokens.textSecondary }}
                className="text-sm font-sans tracking-wide"
              >
                {completedCount} of {totalCount} rituals completed today
              </p>
            </div>

            {/* Ritual Track Filter Pills (Dawn, Focus, Twilight) */}
            <div className="flex items-center justify-center gap-2 overflow-x-auto py-1">
              {[
                { id: 'ALL', label: 'All Rituals' },
                { id: 'DAWN', label: 'Dawn Sequence' },
                { id: 'FOCUS', label: 'Deep Focus' },
                { id: 'TWILIGHT', label: 'Twilight' }
              ].map(pill => (
                <button
                  key={pill.id}
                  type="button"
                  onClick={() => setSelectedFilter(pill.id as any)}
                  style={{
                    backgroundColor: selectedFilter === pill.id ? tokens.accent : tokens.surfaceElevated,
                    color: selectedFilter === pill.id ? '#FFFFFF' : tokens.textSecondary,
                    borderColor: tokens.border
                  }}
                  className="px-4 py-1.5 rounded-full border text-xs font-sans font-medium transition-all hover:brightness-105"
                >
                  {pill.label}
                </button>
              ))}
            </div>

            {/* Grid of Habit Cards */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-2">
              {filteredHabits.map(hwl => (
                <HabitCard
                  key={hwl.habit.id}
                  habitWithLogs={hwl}
                  tokens={tokens}
                  onToggleCheckIn={handleToggleCheckIn}
                />
              ))}
            </div>

            {filteredHabits.length === 0 && (
              <div className="text-center py-16" style={{ color: tokens.textSecondary }}>
                <p className="text-sm font-sans">No rituals found in this sequence track.</p>
                <button
                  type="button"
                  onClick={() => setIsWeaverOpen(true)}
                  style={{ color: tokens.accent }}
                  className="text-xs font-mono underline mt-2 block mx-auto"
                >
                  + Weave First Habit
                </button>
              </div>
            )}
          </div>
        )}

        {currentTab === 'insights' && (
          <TelemetryView habitsWithLogs={habitsWithLogs} tokens={tokens} />
        )}
      </main>

      {/* Floating Bottom Nav Dock */}
      <nav
        style={{
          borderColor: tokens.border,
          backgroundColor: tokens.surface
        }}
        className="fixed bottom-6 left-1/2 transform -translate-x-1/2 z-40 border rounded-full px-4 py-2 shadow-xl flex items-center gap-2 backdrop-blur-lg"
      >
        <button
          type="button"
          onClick={() => setCurrentTab('today')}
          style={{
            backgroundColor: currentTab === 'today' ? tokens.accent : 'transparent',
            color: currentTab === 'today' ? '#FFFFFF' : tokens.textSecondary
          }}
          className="px-4 py-1.5 rounded-full text-xs font-sans font-medium transition-colors"
        >
          Rituals
        </button>

        <button
          type="button"
          onClick={() => setCurrentTab('chamber')}
          style={{
            backgroundColor: currentTab === 'chamber' ? tokens.accent : 'transparent',
            color: currentTab === 'chamber' ? '#FFFFFF' : tokens.textSecondary
          }}
          className="px-4 py-1.5 rounded-full text-xs font-sans font-medium transition-colors flex items-center gap-1.5"
        >
          <UI_ICONS.Play size={10} strokeWidth={2.5} />
          Chamber
        </button>

        <button
          type="button"
          onClick={() => setCurrentTab('insights')}
          style={{
            backgroundColor: currentTab === 'insights' ? tokens.accent : 'transparent',
            color: currentTab === 'insights' ? '#FFFFFF' : tokens.textSecondary
          }}
          className="px-4 py-1.5 rounded-full text-xs font-sans font-medium transition-colors"
        >
          Insights
        </button>

        <button
          type="button"
          onClick={() => setIsAuthOpen(true)}
          style={{
            backgroundColor: currentTab === 'sync' ? tokens.accent : 'transparent',
            color: tokens.textSecondary
          }}
          className="px-3 py-1.5 rounded-full text-xs font-sans font-medium transition-colors"
        >
          Sync
        </button>
      </nav>

      {/* Guided Chamber Modal */}
      {currentTab === 'chamber' && (
        <RitualChamber
          habitsWithLogs={habitsWithLogs}
          tokens={tokens}
          onToggleCheckIn={handleToggleCheckIn}
          onClose={() => setCurrentTab('today')}
        />
      )}

      {/* Habit Stack Weaver Modal */}
      {isWeaverOpen && (
        <HabitStackWeaverModal
          tokens={tokens}
          onSave={handleSaveHabit}
          onClose={() => setIsWeaverOpen(false)}
        />
      )}

      {/* Auth / Cloud Sync Modal */}
      {isAuthOpen && (
        <AuthModal
          tokens={tokens}
          currentUser={currentUser}
          onUserChange={setCurrentUser}
          onClose={() => setIsAuthOpen(false)}
          onSyncComplete={refreshData}
        />
      )}
    </div>
  );
}
