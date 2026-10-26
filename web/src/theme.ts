import { ThemeMode } from './types.js';

export interface ColorTokens {
  name: string;
  isDark: boolean;
  bg: string;
  surface: string;
  surfaceElevated: string;
  border: string;
  textPrimary: string;
  textSecondary: string;
  textMuted: string;
  accent: string;
}

export const THEMES: Record<ThemeMode, ColorTokens> = {
  light: {
    name: 'Warm Limestone',
    isDark: false,
    bg: '#F5F3EF',
    surface: '#FCFBF9',
    surfaceElevated: '#EAE6DF',
    border: '#E8E4DC',
    textPrimary: '#1C1B19',
    textSecondary: '#5A564F',
    textMuted: '#8E8A82',
    accent: '#2D5A43'
  },
  dark: {
    name: 'Matte Carbon',
    isDark: true,
    bg: '#121210',
    surface: '#1A1917',
    surfaceElevated: '#242320',
    border: '#2A2825',
    textPrimary: '#F2EFE9',
    textSecondary: '#ABA79F',
    textMuted: '#706D67',
    accent: '#488B69'
  },
  dune: {
    name: 'Dune (Arrakis)',
    isDark: true,
    bg: '#181412',
    surface: '#221C18',
    surfaceElevated: '#2E2620',
    border: '#3B3028',
    textPrimary: '#F7EFE6',
    textSecondary: '#C5B5A3',
    textMuted: '#867666',
    accent: '#D4A373'
  },
  space: {
    name: 'Star Wars (Deep Space)',
    isDark: true,
    bg: '#07090E',
    surface: '#111520',
    surfaceElevated: '#171D2B',
    border: '#1F273B',
    textPrimary: '#F0F4FA',
    textSecondary: '#A4B3C9',
    textMuted: '#6B7A94',
    accent: '#48CAE4'
  },
  tatooine: {
    name: 'Star Wars (Tatooine Dusk)',
    isDark: true,
    bg: '#1F120E',
    surface: '#2C1B15',
    surfaceElevated: '#38231C',
    border: '#472C24',
    textPrimary: '#FDF5ED',
    textSecondary: '#D4A999',
    textMuted: '#966D60',
    accent: '#E07A5F'
  }
};

export interface PaletteStop {
  key: string;
  name: string;
  accent: string;
  stops: [string, string, string, string, string]; // level 0 (empty), 1, 2, 3, 4
}

export const PALETTES: Record<string, PaletteStop> = {
  emerald: {
    key: 'emerald',
    name: 'Botanical Forest',
    accent: '#2D5A43',
    stops: ['rgba(0,0,0,0.06)', '#8FB996', '#5E936C', '#3E734D', '#2D5A43']
  },
  amber: {
    key: 'amber',
    name: 'Warm Ochre',
    accent: '#C27D38',
    stops: ['rgba(0,0,0,0.06)', '#E9B97F', '#DFA055', '#D18738', '#B06822']
  },
  rose: {
    key: 'rose',
    name: 'Braun Terracotta',
    accent: '#B85435',
    stops: ['rgba(0,0,0,0.06)', '#E89880', '#D46A48', '#C35432', '#A33E20']
  },
  cyan: {
    key: 'cyan',
    name: 'Prussian Slate',
    accent: '#3B526B',
    stops: ['rgba(0,0,0,0.06)', '#96B2CE', '#6A8CAE', '#4B6F93', '#334D69']
  },
  violet: {
    key: 'violet',
    name: 'Muted Fig',
    accent: '#5E4B6B',
    stops: ['rgba(0,0,0,0.06)', '#AE98BB', '#886FA0', '#6C5082', '#513766']
  },
  glacier: {
    key: 'glacier',
    name: 'Nordic Fjord',
    accent: '#3E6B6E',
    stops: ['rgba(0,0,0,0.06)', '#8FB7B9', '#62989B', '#45797C', '#2E5A5D']
  },
  quartz: {
    key: 'quartz',
    name: 'Dusty Clay',
    accent: '#9E5A63',
    stops: ['rgba(0,0,0,0.06)', '#D9A1A8', '#BE7681', '#A65561', '#843C48']
  },
  monochrome: {
    key: 'monochrome',
    name: 'Espresso Ink',
    accent: '#2B2927',
    stops: ['rgba(0,0,0,0.06)', '#AFA89E', '#80796F', '#565047', '#2B2927']
  }
};

export function getPalette(key: string, isDark: boolean): PaletteStop {
  const normKey = (key || 'emerald').toLowerCase();
  const found = PALETTES[normKey] || PALETTES.emerald;
  if (!isDark) {
    return found;
  }
  // Dark mode stops
  const darkEmpty = 'rgba(255,255,255,0.07)';
  return {
    ...found,
    stops: [darkEmpty, found.stops[1], found.stops[2], found.stops[3], found.stops[4]]
  };
}
