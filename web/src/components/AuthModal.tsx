import React, { useState } from 'react';
import { ColorTokens } from '../theme.js';
import { UserProfile } from '../types.js';
import { loginUser, registerUser, triggerSync, setStoredToken, setStoredUser } from '../syncClient.js';
import { UI_ICONS } from '../icons.js';

interface AuthModalProps {
  tokens: ColorTokens;
  currentUser: UserProfile | null;
  onUserChange: (user: UserProfile | null) => void;
  onClose: () => void;
  onSyncComplete: () => void;
}

export const AuthModal: React.FC<AuthModalProps> = ({
  tokens,
  currentUser,
  onUserChange,
  onClose,
  onSyncComplete
}) => {
  const [isRegisterMode, setIsRegisterMode] = useState(false);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [displayName, setDisplayName] = useState('');
  const [errorMsg, setErrorMsg] = useState('');
  const [loading, setLoading] = useState(false);
  const [syncStatus, setSyncStatus] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg('');
    setLoading(true);

    try {
      if (isRegisterMode) {
        const res = await registerUser(email, password, displayName);
        onUserChange(res.user);
        await handleSync();
      } else {
        const res = await loginUser(email, password);
        onUserChange(res.user);
        await handleSync();
      }
    } catch (err: any) {
      setErrorMsg(err.message || 'Operation failed');
    } finally {
      setLoading(false);
    }
  };

  const handleSync = async () => {
    setLoading(true);
    setSyncStatus('Syncing with Tessera Engine...');
    try {
      const result = await triggerSync();
      setSyncStatus(`Sync successful! ${result.syncedHabits} habits & ${result.syncedLogs} logs reconciled.`);
      onSyncComplete();
    } catch (err: any) {
      setSyncStatus(`Sync failed: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    setStoredToken(null);
    setStoredUser(null);
    onUserChange(null);
    setSyncStatus('Logged out.');
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
      <div
        style={{
          backgroundColor: tokens.surface,
          borderColor: tokens.border,
          color: tokens.textPrimary
        }}
        className="rounded-3xl border max-w-md w-full p-6 sm:p-8 shadow-2xl"
      >
        <div className="flex items-center justify-between border-b pb-4 mb-6" style={{ borderColor: tokens.border }}>
          <div>
            <span style={{ color: tokens.accent }} className="text-[11px] font-mono uppercase tracking-widest block font-semibold">
              Cloud Synchronization
            </span>
            <h2 className="font-serif text-2xl font-normal tracking-tight">
              {currentUser ? 'Sync Account Vault' : isRegisterMode ? 'Create Cloud Account' : 'Connect to Cloud'}
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

        {errorMsg && (
          <div className="p-3 mb-4 rounded-xl text-xs font-sans bg-rose-500/10 border border-rose-500/30 text-rose-400">
            {errorMsg}
          </div>
        )}

        {syncStatus && (
          <div className="p-3 mb-4 rounded-xl text-xs font-mono bg-emerald-500/10 border border-emerald-500/30 text-emerald-400">
            {syncStatus}
          </div>
        )}

        {currentUser ? (
          <div className="space-y-6">
            <div style={{ backgroundColor: tokens.surfaceElevated, borderColor: tokens.border }} className="p-4 rounded-2xl border">
              <span className="text-[11px] font-mono uppercase block" style={{ color: tokens.textSecondary }}>
                Authenticated As
              </span>
              <p className="font-sans text-base font-medium mt-1">{currentUser.displayName || currentUser.email}</p>
              <p className="text-xs font-mono text-opacity-80" style={{ color: tokens.textSecondary }}>{currentUser.email}</p>
            </div>

            <div className="flex flex-col gap-3">
              <button
                type="button"
                onClick={handleSync}
                disabled={loading}
                style={{ backgroundColor: tokens.accent, color: '#FFFFFF' }}
                className="w-full py-3 rounded-xl font-sans text-sm font-medium flex items-center justify-center gap-2 hover:brightness-110 shadow-sm"
              >
                <UI_ICONS.RefreshCw size={16} className={loading ? 'animate-spin' : ''} />
                {loading ? 'Reconciling State...' : 'Sync Now with Cloud'}
              </button>

              <button
                type="button"
                onClick={handleLogout}
                style={{ backgroundColor: tokens.surfaceElevated, borderColor: tokens.border, color: tokens.textSecondary }}
                className="w-full py-3 rounded-xl border font-sans text-sm font-medium hover:brightness-110"
              >
                Sign Out
              </button>
            </div>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4">
            {isRegisterMode && (
              <div>
                <label className="block text-xs font-mono uppercase mb-1.5" style={{ color: tokens.textSecondary }}>
                  Display Name
                </label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Anoop Singh"
                  value={displayName}
                  onChange={e => setDisplayName(e.target.value)}
                  style={{
                    backgroundColor: tokens.surfaceElevated,
                    borderColor: tokens.border,
                    color: tokens.textPrimary
                  }}
                  className="w-full px-3.5 py-2.5 rounded-xl border text-sm outline-none"
                />
              </div>
            )}

            <div>
              <label className="block text-xs font-mono uppercase mb-1.5" style={{ color: tokens.textSecondary }}>
                Email Address
              </label>
              <input
                type="email"
                required
                placeholder="your.email@domain.com"
                value={email}
                onChange={e => setEmail(e.target.value)}
                style={{
                  backgroundColor: tokens.surfaceElevated,
                  borderColor: tokens.border,
                  color: tokens.textPrimary
                }}
                className="w-full px-3.5 py-2.5 rounded-xl border text-sm outline-none"
              />
            </div>

            <div>
              <label className="block text-xs font-mono uppercase mb-1.5" style={{ color: tokens.textSecondary }}>
                Password
              </label>
              <input
                type="password"
                required
                placeholder="••••••••••••"
                value={password}
                onChange={e => setPassword(e.target.value)}
                style={{
                  backgroundColor: tokens.surfaceElevated,
                  borderColor: tokens.border,
                  color: tokens.textPrimary
                }}
                className="w-full px-3.5 py-2.5 rounded-xl border text-sm outline-none"
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              style={{ backgroundColor: tokens.accent, color: '#FFFFFF' }}
              className="w-full py-3 rounded-xl font-sans text-sm font-medium mt-2 hover:brightness-110 shadow-md transition-all flex items-center justify-center gap-2"
            >
              {loading && <UI_ICONS.RefreshCw size={14} className="animate-spin" />}
              {isRegisterMode ? 'Create Account & Sync' : 'Sign In & Sync'}
            </button>

            <div className="text-center pt-2">
              <button
                type="button"
                onClick={() => {
                  setIsRegisterMode(!isRegisterMode);
                  setErrorMsg('');
                }}
                style={{ color: tokens.textSecondary }}
                className="text-xs font-sans hover:underline"
              >
                {isRegisterMode ? 'Already have an account? Sign In' : "Don't have an account? Create One"}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
};
