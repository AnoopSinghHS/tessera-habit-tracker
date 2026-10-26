import React from 'react';
import {
  Mic,
  Activity,
  Music,
  PenTool,
  Pencil,
  Feather,
  BookOpen,
  BookMarked,
  Library,
  Dumbbell,
  Flame,
  Heart,
  Apple,
  Droplet,
  Utensils,
  Scale,
  Laptop,
  Code,
  Terminal,
  Clock,
  Timer,
  Hourglass,
  TrendingUp,
  Sprout,
  Target,
  Compass,
  Star,
  Sun,
  Moon,
  Zap,
  Check,
  Plus,
  RefreshCw,
  Sliders,
  ChevronRight,
  ChevronLeft,
  X,
  Play,
  RotateCcw,
  Sparkles
} from 'lucide-react';

interface IconProps {
  name: string;
  size?: number;
  className?: string;
  strokeWidth?: number;
}

export const HabitIcon: React.FC<IconProps> = ({ name, size = 20, className = '', strokeWidth = 1.75 }) => {
  const norm = (name || 'bolt').toLowerCase();

  switch (norm) {
    case 'mic':
    case 'microphone':
    case 'sing':
    case 'singing':
    case 'vocal':
      return <Mic size={size} strokeWidth={strokeWidth} className={className} />;
    case 'waveform':
    case 'audio':
      return <Activity size={size} strokeWidth={strokeWidth} className={className} />;
    case 'clef':
    case 'music':
      return <Music size={size} strokeWidth={strokeWidth} className={className} />;
    case 'pen':
    case 'fountain-pen':
      return <PenTool size={size} strokeWidth={strokeWidth} className={className} />;
    case 'pencil':
    case 'write':
    case 'writing':
      return <Pencil size={size} strokeWidth={strokeWidth} className={className} />;
    case 'quill':
    case 'feather':
      return <Feather size={size} strokeWidth={strokeWidth} className={className} />;
    case 'book':
    case 'read':
    case 'reading':
      return <BookOpen size={size} strokeWidth={strokeWidth} className={className} />;
    case 'books':
    case 'library':
      return <Library size={size} strokeWidth={strokeWidth} className={className} />;
    case 'bookmark':
      return <BookMarked size={size} strokeWidth={strokeWidth} className={className} />;
    case 'fitness':
    case 'kettlebell':
    case 'dumbbell':
    case 'weights':
      return <Dumbbell size={size} strokeWidth={strokeWidth} className={className} />;
    case 'flame':
    case 'streak':
      return <Flame size={size} strokeWidth={strokeWidth} className={className} />;
    case 'heart':
    case 'vitality':
      return <Heart size={size} strokeWidth={strokeWidth} className={className} />;
    case 'apple':
    case 'nutrition':
    case 'diet':
      return <Apple size={size} strokeWidth={strokeWidth} className={className} />;
    case 'water':
    case 'hydration':
    case 'drop':
      return <Droplet size={size} strokeWidth={strokeWidth} className={className} />;
    case 'bowl':
    case 'meal':
      return <Utensils size={size} strokeWidth={strokeWidth} className={className} />;
    case 'scale':
    case 'weight':
      return <Scale size={size} strokeWidth={strokeWidth} className={className} />;
    case 'laptop':
    case 'pc':
      return <Laptop size={size} strokeWidth={strokeWidth} className={className} />;
    case 'code':
    case 'dev':
      return <Code size={size} strokeWidth={strokeWidth} className={className} />;
    case 'terminal':
    case 'cli':
      return <Terminal size={size} strokeWidth={strokeWidth} className={className} />;
    case 'clock':
    case 'time':
      return <Clock size={size} strokeWidth={strokeWidth} className={className} />;
    case 'stopwatch':
    case 'timer':
      return <Timer size={size} strokeWidth={strokeWidth} className={className} />;
    case 'hourglass':
      return <Hourglass size={size} strokeWidth={strokeWidth} className={className} />;
    case 'trending-up':
    case 'trending':
    case 'growth':
      return <TrendingUp size={size} strokeWidth={strokeWidth} className={className} />;
    case 'sprout':
    case 'plant':
      return <Sprout size={size} strokeWidth={strokeWidth} className={className} />;
    case 'target':
    case 'goal':
      return <Target size={size} strokeWidth={strokeWidth} className={className} />;
    case 'compass':
      return <Compass size={size} strokeWidth={strokeWidth} className={className} />;
    case 'star':
    case 'north-star':
      return <Star size={size} strokeWidth={strokeWidth} className={className} />;
    case 'sun':
      return <Sun size={size} strokeWidth={strokeWidth} className={className} />;
    case 'moon':
      return <Moon size={size} strokeWidth={strokeWidth} className={className} />;
    case 'bolt':
    default:
      return <Zap size={size} strokeWidth={strokeWidth} className={className} />;
  }
};

export const UI_ICONS = {
  Check,
  Plus,
  RefreshCw,
  Sliders,
  ChevronRight,
  ChevronLeft,
  X,
  Play,
  RotateCcw,
  Sparkles,
  Sun,
  Moon
};
