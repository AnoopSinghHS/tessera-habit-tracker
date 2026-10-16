# Tessera — Architectural Habit Matrix & Telemetry Engine

**Tessera** (`app.tessera.habit`) is a precision-engineered Android habit tracker and GitHub-style contribution heatmap engine tailored for the **Samsung Galaxy S26 Ultra** (One UI 8 / Android 15+, QHD+ Dynamic AMOLED 2X 120Hz display).

Built with **Kotlin 2.0**, **Jetpack Compose (Material 3)**, **Room SQLite**, and a **High-DPI Canvas RemoteViews Widget Engine**, Tessera combines tactile daily ritual tracking with an open **Inter-App Broadcast & Deep-Link Automation API** so external apps, Tasker, Samsung Modes & Routines, MacroDroid, or ADB scripts can log habits programmatically in the background and immediately refresh your Home Screen Widgets.

---

## Key Capabilities

1. **GitHub-Style Contribution Heatmaps (120Hz Compose Canvas)**
   - **Full-Width Architectural Cards**: 28-week horizontal contribution matrix with 5-stop quartile intensity shading (`Level 0..4`), monospace streak badge, and kinetic 1-tap check-in button.
   - **2-Column Bento Matrix Mode**: Toggle between full cards and a high-density 2-column Bento layout showing 11-week mini matrices side-by-side.
   - **52-Week Portfolio Telemetry**: Aggregated year-in-pixels contribution matrix, 30-day rolling consistency index, weekday rhythm cadence bar chart, and streak leaderboard.
   - **Interactive Back-Fill & Field Notes**: Tap any historical square in the 52-week explorer or monthly calendar to adjust daily counts (`0..N`) or attach a timestamped field note.

2. **Samsung Galaxy S26 Ultra Home Screen Widgets (One UI 8)**
   - **4×2 Wide Matrix Widget (`HabitHeatmapWidgetProvider`)**: Renders an anti-aliased 18-week (126-day) GitHub contribution matrix via hardware-backed `Bitmap` + `Canvas`, complete with month headers, today highlight ring, streak badge, and a direct **1-tap `+` / `✓` check-in button** that updates Room and redraws the widget on the spot without opening the app.
   - **2×2 Compact Tile Widget (`HabitCompactWidgetProvider`)**: Renders an 8-week (56-day) compact matrix with streak badge and 1-tap check-in.
   - **Live Reconfiguration (`HabitWidgetConfigureActivity`)**: Long-press any placed widget on One UI to switch bound ritual, toggle **Obsidian Dark (`#0A0C10`)** vs **Alabaster Light (`#FFFFFF`)** surface theme, or choose between **Smart Toggle** and **Always +1** tap behavior.

3. **Programmatic Inter-App Broadcast & Deep Link API**
   - Exported `HabitApiReceiver` (`app.tessera.habit.ACTION_LOG_HABIT`) and `tessera://` URI Deep Link handler allow zero-UI background habit logging from any Android app, automation tool, or shell script.
   - Includes an in-app **Live Interactive API Sandbox** (*Bridge & Vault* tab) to test broadcast intents in real time and copy ready-to-use ADB, Kotlin, and URI snippets.

4. **8 Curated 5-Stop Chromatic Palettes & Dual Surface Themes**
   - **Obsidian Dark (`#0A0C10`)** true-black AMOLED default and **Alabaster Light (`#F6F5F0`)** gallery paper mode.
   - 8 architectural palettes: *Emerald Matrix*, *Cyber Amber*, *Cobalt Pulse*, *Crimson Ember*, *Violet Nebula*, *Cyan Glacier*, *Rose Quartz*, and *Monochrome Ink*.

5. **Zero-Cloud Local Vault & Lossless JSON Backup**
   - 100% offline-first Room SQLite persistence with instant JSON file export, clipboard snapshot copy, and JSON payload import.

---

## Samsung Galaxy S26 Ultra Widget Setup Guide

1. **From the One UI Home Screen**:
   - Long-press an empty area on your Galaxy S26 Ultra Home Screen → tap **Widgets** → select **Tessera**.
   - Choose either **Tessera · 18-Week Matrix (4×2)** or **Tessera · Compact Tile (2×2)** and tap **Add**.
2. **Or Pin Directly from Inside Tessera**:
   - Open Tessera → navigate to the **Bridge & Vault** tab → tap **PIN 4×2 MATRIX** or **PIN 2×2 TILE**.
3. **Configure Your Widget**:
   - Select the ritual you want bound to the widget, preview the live heatmap in **Obsidian Dark** or **Alabaster Light**, choose **Smart Toggle** (`0 → Target → 0`) or **Always +1**, and tap **Deploy Widget to Home Screen**.

---

## Programmatic Automation API Reference

### 1. Android Broadcast Intent (`HabitApiReceiver`)

External Android apps, Tasker, MacroDroid, or `adb shell` can log or update any ritual in the background by sending an explicit broadcast to `app.tessera.habit/.api.HabitApiReceiver`.

- **Action**: `app.tessera.habit.ACTION_LOG_HABIT` (or shorthand `app.tessera.action.LOG_HABIT`)
- **Component**: `app.tessera.habit/app.tessera.habit.api.HabitApiReceiver`

| Extra Key | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `slug` | `String` | *(Required*)* | Unique habit slug (e.g., `"deep-work"`, `"strength-training"`, `"hydration"`). |
| `habit_id` | `Long` / `String` | *(Optional)* | Numeric habit ID if `slug` is omitted. |
| `mode` | `String` | `"increment"` | `"increment"` (adds `delta`), `"toggle"` (cycles `0 → target → 0`), or `"set"` (sets exact `count`). |
| `delta` | `Int` | `1` | Amount to add when `mode = "increment"`. |
| `count` | `Int` | `targetPerDay` | Explicit completion count when `mode = "set"`. |
| `date` | `String` | `LocalDate.now()` | ISO-8601 date (`YYYY-MM-DD`) for back-filling historical squares. |
| `note` | `String` | `null` | Optional field note attached to that date's log entry. |

#### Example: ADB / Termux Shell Command
```bash
adb shell am broadcast \
  -a app.tessera.habit.ACTION_LOG_HABIT \
  -n app.tessera.habit/.api.HabitApiReceiver \
  --es slug "deep-work" \
  --es mode "increment" \
  --ei delta 1 \
  --es note "Automated via ADB"
```

#### Example: Kotlin Inter-App Broadcast
```kotlin
val intent = Intent("app.tessera.habit.ACTION_LOG_HABIT").apply {
    setClassName("app.tessera.habit", "app.tessera.habit.api.HabitApiReceiver")
    putExtra("slug", "hydration")
    putExtra("mode", "increment")
    putExtra("delta", 1)
    putExtra("note", "Logged from companion app")
}
context.sendBroadcast(intent)
```

### 2. Deep Link URI Scheme (`tessera://`)

Use `tessera://` URIs in Samsung Modes & Routines, NFC tags, or web/shortcut launchers:

- **Log a habit completion**:
  ```text
  tessera://log?slug=deep-work&mode=increment&delta=1&note=Morning+Sprint
  ```
- **Open a habit's 52-week detail sheet**:
  ```text
  tessera://habit/deep-work
  ```

---

## Architecture & Project Structure

```text
app/src/main/java/app/tessera/habit/
├── TesseraApp.kt                        # Application singleton & initial 20-week showcase seeder
├── MainActivity.kt                      # Edge-to-edge Compose host & DeepLink intent router
├── api/
│   ├── HabitApiContract.kt              # Intent constants, ADB & URI builders
│   ├── HabitApiReceiver.kt              # Exported BroadcastReceiver (goAsync + Room + Widget sync)
│   └── DeepLinkHandler.kt               # tessera://log and tessera://habit/<slug> parser
├── data/
│   ├── local/                           # Room Database, HabitDao, HabitEntity, HabitLogEntity
│   ├── model/                           # HabitWithLogs, HabitStats (streak/quartile math), BackupModels
│   ├── repository/                      # TesseraRepository & DemoSeedGenerator
│   └── backup/                          # BackupManager (Kotlinx Serialization JSON import/export)
├── ui/
│   ├── theme/                           # Color tokens, JetBrains-style typography, 8x 5-stop HabitPalettes
│   ├── components/                      # GithubHeatmapGrid (Canvas), HabitCard, YearOverviewCard, HabitIcons
│   ├── screens/                         # DashboardScreen, AnalyticsScreen, ApiAndSettingsScreen,
│   │                                    # HabitDetailSheet, HabitEditorDialog
│   └── viewmodel/                       # TesseraViewModel (reactive StateFlow + UI preferences)
└── widget/
    ├── WidgetHeatmapBitmapRenderer.kt   # High-DPI Android Canvas Bitmap renderer for One UI RemoteViews
    ├── HabitWidgetUpdater.kt            # RemoteViews coordinator for 4x2 Wide & 2x2 Compact widgets
    ├── HabitHeatmapWidgetProvider.kt    # 4x2 Wide AppWidgetProvider
    ├── HabitCompactWidgetProvider.kt    # 2x2 Compact AppWidgetProvider
    ├── HabitWidgetActionReceiver.kt     # 1-tap widget check-in BroadcastReceiver
    ├── HabitWidgetConfigureActivity.kt  # Interactive Compose widget configuration screen
    └── WidgetPrefs.kt                   # Per-widget SharedPreferences store
```

---

## Building & Running Tests

```bash
# Run JUnit4 unit tests (streak math, quartile intensity, palettes, demo seed, JSON serialization)
./gradlew testDebugUnitTest

# Assemble Debug and Signed Release APKs
./gradlew assembleDebug assembleRelease
```
