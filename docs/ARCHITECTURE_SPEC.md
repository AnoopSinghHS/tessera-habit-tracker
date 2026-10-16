# Tessera (`app.tessera.habit`) — Technical Architecture Specification

## 1. Gradle & SDK Configuration

Tessera is a native Android application built with 100% Kotlin, Jetpack Compose (Material 3), Room Database (with KSP), RemoteViews + High-DPI Canvas Bitmap rendering for Samsung One UI Home Screen Widgets, and an exported programmatic API (`BroadcastReceiver` + Deep Links + JSON Backup/Restore).

### 1.1 Toolchain & SDK Versions
- **Application ID / Package**: `app.tessera.habit`
- **Compile SDK**: `35` (Android 15)
- **Target SDK**: `35` (Android 15, fully compatible with Android 16 / Samsung Galaxy S26 Ultra One UI 7/8)
- **Min SDK**: `26` (Android 8.0, native `java.time.LocalDate` support without desugaring overhead)
- **Java / JVM Target**: `JavaVersion.VERSION_17` (`jvmTarget = "17"`)
- **Gradle Wrapper**: Gradle `8.11.1` (`gradle-wrapper.properties` + self-contained `gradlew` / `gradlew.bat` / `gradle-wrapper.jar` for reproducible GitHub Actions builds)

### 1.2 Version Catalog (`gradle/libs.versions.toml`)
- **Android Gradle Plugin (AGP)**: `8.7.3`
- **Kotlin**: `2.0.21`
- **Compose Compiler Plugin**: `org.jetbrains.kotlin.plugin.compose` (`2.0.21`)
- **KSP (Kotlin Symbol Processing)**: `2.0.21-1.0.28`
- **Kotlinx Serialization Plugin**: `org.jetbrains.kotlin.plugin.serialization` (`2.0.21`)
- **Compose BOM**: `2024.12.01` (`ui`, `ui-graphics`, `ui-tooling-preview`, `material3`, `material-icons-extended`)
- **Activity Compose**: `1.9.3`
- **Lifecycle (`runtime-ktx`, `viewmodel-compose`, `runtime-compose`)**: `2.8.7`
- **Navigation Compose**: `2.8.5`
- **Room (`room-runtime`, `room-ktx`, `room-compiler` via KSP)**: `2.6.1`
- **Kotlinx Coroutines Android**: `1.9.0`
- **Kotlinx Serialization JSON**: `1.7.3`
- **Core KTX**: `1.15.0`

---

## 2. Room Database Schema (`TesseraDatabase`)

Database name: `"tessera_habits.db"`, `@Database(entities = [HabitEntity::class, HabitLogEntity::class], version = 1, exportSchema = false)`.

### 2.1 `HabitEntity` (`tableName = "habits"`)
Index: `@Index(value = ["slug"], unique = true)`

| Column | Kotlin Type | SQLite Affinity | Default | Description |
|---|---|---|---|---|
| `id` | `Long` | `INTEGER PRIMARY KEY AUTOINCREMENT` | `0` | Unique internal ID |
| `slug` | `String` | `TEXT NOT NULL` | — | Unique URL/API-safe identifier (e.g. `"deep-work"`, `"morning-run"`), auto-generated from title or customizable |
| `title` | `String` | `TEXT NOT NULL` | — | Display title (e.g. `"Deep Work Session"`) |
| `description` | `String` | `TEXT NOT NULL` | `""` | Subtitle / motivation / cue |
| `category` | `String` | `TEXT NOT NULL` | `"General"` | Grouping tag (`"Focus"`, `"Fitness"`, `"Mind"`, `"Health"`, etc.) |
| `iconKey` | `String` | `TEXT NOT NULL` | `"bolt"` | Key mapped in `HabitIcons` (`"bolt"`, `"fitness"`, `"book"`, `"water"`, `"code"`, `"meditation"`, `"run"`, `"sleep"`, `"music"`, `"leaf"`, `"flame"`, `"heart"`) |
| `colorKey` | `String` | `TEXT NOT NULL` | `"emerald"` | Key mapped in `HabitPalette` (`"emerald"`, `"cyan"`, `"violet"`, `"amber"`, `"rose"`, `"indigo"`, `"lime"`, `"orange"`) |
| `targetPerDay` | `Int` | `INTEGER NOT NULL` | `1` | Daily target completions (`1` = binary habit; `>1` = multi-step intensity 0..4 quartiles on GitHub heatmap) |
| `weeklyGoalDays` | `Int` | `INTEGER NOT NULL` | `7` | Target days per week (`1..7`) |
| `isArchived` | `Boolean` | `INTEGER NOT NULL` | `false` | Archived state |
| `sortOrder` | `Int` | `INTEGER NOT NULL` | `0` | Display ordering |
| `createdAt` | `Long` | `INTEGER NOT NULL` | `System.currentTimeMillis()` | Epoch millis creation timestamp |

### 2.2 `HabitLogEntity` (`tableName = "habit_logs"`)
Foreign Key: `ForeignKey(entity = HabitEntity::class, parentColumns = ["id"], childColumns = ["habitId"], onDelete = ForeignKey.CASCADE)`
Indices:
- `@Index(value = ["habitId", "dateEpochDay"], unique = true)`
- `@Index(value = ["dateEpochDay"])`

| Column | Kotlin Type | SQLite Affinity | Default | Description |
|---|---|---|---|---|
| `id` | `Long` | `INTEGER PRIMARY KEY AUTOINCREMENT` | `0` | Unique log entry ID |
| `habitId` | `Long` | `INTEGER NOT NULL` | — | Foreign key to `habits.id` |
| `dateEpochDay` | `Long` | `INTEGER NOT NULL` | — | `LocalDate.toEpochDay()` (UTC-agnostic local calendar date) |
| `count` | `Int` | `INTEGER NOT NULL` | `1` | Number of completions on that date (`>= 0`) |
| `note` | `String` | `TEXT NOT NULL` | `""` | Optional reflection note or external API payload note |
| `updatedAt` | `Long` | `INTEGER NOT NULL` | `System.currentTimeMillis()` | Last update timestamp |

### 2.3 GitHub Heatmap Intensity Calculation
For any `(count, targetPerDay)`:
- `count <= 0` → **Level 0** (Empty cell surface)
- If `targetPerDay <= 1`:
  - `count >= 1` → **Level 4** (100% full vibrant accent)
- If `targetPerDay > 1`:
  - Ratio `r = count.toFloat() / targetPerDay.toFloat()`
  - `r in 0.01f..0.33f` → **Level 1** (28% alpha / dark shade)
  - `r in 0.34f..0.66f` → **Level 2** (52% alpha / medium shade)
  - `r in 0.67f..0.99f` → **Level 3** (76% alpha / bright shade)
  - `r >= 1.0f` → **Level 4** (100% full vibrant shade)

### 2.4 First-Launch Showcase Seed Data (`DemoSeedGenerator`)
On first launch (tracked via `SharedPreferences` flag `has_seeded_initial_data`), `TesseraRepository.ensureSeeded()` populates 4 realistic habits with deterministic, rich 16-week historical logs so the app and home screen widgets look alive immediately:
1. **Deep Work Session** (`slug = "deep-work"`, `iconKey = "bolt"`, `colorKey = "emerald"`, `targetPerDay = 2`, `weeklyGoalDays = 5`)
2. **Strength Training** (`slug = "strength-training"`, `iconKey = "fitness"`, `colorKey = "cyan"`, `targetPerDay = 1`, `weeklyGoalDays = 4`)
3. **Read 30 Pages** (`slug = "read-30-pages"`, `iconKey = "book"`, `colorKey = "violet"`, `targetPerDay = 1`, `weeklyGoalDays = 7`)
4. **Hydration 2.5L** (`slug = "hydration"`, `iconKey = "water"`, `colorKey = "amber"`, `targetPerDay = 4`, `weeklyGoalDays = 7`)

Settings includes both **"Reset / Restore Demo Data"** and **"Clear All Data"** with 1-tap confirmation.

---

## 3. Per-Habit Android Home Screen Widgets (`AppWidgetProvider`)

Designed specifically for crisp rendering on high-DPI displays like the **Samsung Galaxy S26 Ultra** (One UI launcher grid) and Pixel launchers.

### 3.1 Why High-DPI `Bitmap` + `Canvas` Inside `RemoteViews`?
Android `RemoteViews` does not support custom Compose canvases or hundreds of nested `View` children without hitting Binder transaction size limits or layout inflation lag. By rendering the GitHub contribution matrix directly onto an anti-aliased `Bitmap` via `android.graphics.Canvas` (with rounded-rect cells, subtle inner borders, day/week alignment, and dynamic intensity levels) and setting it on an `ImageView` (`@id/widget_heatmap_image`), the widget is:
- **100% crash-proof & flicker-free** on Samsung One UI
- **Ultra-sharp** at 500+ PPI (`RoundRect` cells rendered at native density)
- **Interactive** with a dedicated 1-tap `+` / `✓` check-in button powered by `PendingIntent.getBroadcast(...)`

### 3.2 Two Widget Providers
1. **`HabitHeatmapWidgetProvider` (4x2 Wide Heatmap Widget — Resizable)**
   - Metadata: `@xml/habit_heatmap_widget_info` (`minWidth="250dp"`, `minHeight="110dp"`, `targetCellWidth="4"`, `targetCellHeight="2"`, `resizeMode="horizontal|vertical"`, `configure="app.tessera.habit.widget.HabitWidgetConfigureActivity"`)
   - Displays:
     - Habit icon tint badge + Habit title + current streak badge (`🔥 14d`) + today's progress (`1/2` or `Done`)
     - **18-week (126-day) GitHub Contribution Matrix** rendered via `WidgetHeatmapBitmapRenderer.renderWideHeatmap(...)`
     - **1-Tap Check-In Button** (`@id/widget_check_in_btn`): increments today's count for that habit (or toggles if binary and already completed, configurable; defaults to increment/toggle smart behavior: if `targetPerDay == 1`, toggles `0 <-> 1`; if `targetPerDay > 1`, increments `0 -> 1 -> ... -> targetPerDay -> 0` or `+1`)
     - Tapping the card header opens `MainActivity` deep-linked to that habit (`tessera://habit/<slug>`).

2. **`HabitCompactWidgetProvider` (2x2 Compact Heatmap Widget — Resizable)**
   - Metadata: `@xml/habit_compact_widget_info` (`minWidth="110dp"`, `minHeight="110dp"`, `targetCellWidth="2"`, `targetCellHeight="2"`, `resizeMode="horizontal|vertical"`, `configure="app.tessera.habit.widget.HabitWidgetConfigureActivity"`)
   - Displays:
     - Habit title + streak counter + 1-tap check-in button in top row
     - **7-week (49-day) Compact GitHub Contribution Matrix** rendered via `WidgetHeatmapBitmapRenderer.renderCompactHeatmap(...)`

### 3.3 `HabitWidgetConfigureActivity` & Widget Preferences
- Declared in `AndroidManifest.xml` with `<action android:name="android.appwidget.action.APPWIDGET_CONFIGURE" />`.
- Reads `AppWidgetManager.EXTRA_APPWIDGET_ID`. Defaults result to `RESULT_CANCELED` if backed out.
- Presents a sleek Compose UI where the user selects:
  1. **Which Habit** to display in this widget instance
  2. **Widget Surface Theme**: `Obsidian Dark` (default `#0D1117` GitHub dark) or `Alabaster Light` (`#FFFFFF`)
  3. **Tap Action Mode**: `Smart Toggle / Increment` (default) or `Always Increment (+1)`
- Saves binding in `WidgetPrefs` (`SharedPreferences` file `"tessera_widget_prefs"`):
  - `widget_habit_id_<appWidgetId>` -> `Long`
  - `widget_theme_<appWidgetId>` -> `"dark"` | `"light"`
  - `widget_tap_mode_<appWidgetId>` -> `"smart"` | `"increment"`
- If a widget is placed without configuration (or if the bound habit was deleted), falls back gracefully to the first non-archived habit so the widget is never blank.
- Immediately calls `HabitWidgetUpdater.updateSingleWidget(context, appWidgetId)` and finishes with `RESULT_OK`.

### 3.4 Instant Widget Sync (`HabitWidgetUpdater`)
- Whenever any habit or log changes via:
  - In-app check-in / heatmap cell edit / habit edit
  - Widget 1-tap check-in button (`ACTION_WIDGET_CHECK_IN` handled by `HabitWidgetActionReceiver`)
  - External Broadcast Intent (`HabitApiReceiver`)
  - Deep link (`tessera://log?...`)
- `HabitWidgetUpdater.updateAllWidgets(context)` queries the latest `HabitEntity` + `HabitLogEntity` map on `Dispatchers.IO`, re-renders the `Bitmap` for every active `HabitHeatmapWidgetProvider` and `HabitCompactWidgetProvider` instance, and pushes `appWidgetManager.updateAppWidget(appWidgetId, views)`.

---

## 4. Programmatic External App Integration (Inter-App API)

Tessera is architected as a **habit hub** that other Android apps, Tasker, MacroDroid, ADB scripts, or custom fitness/pomodoro apps can update automatically.

### 4.1 Exported BroadcastReceiver (`HabitApiReceiver`)
Registered in `AndroidManifest.xml` with `android:exported="true"`:
- **Action**: `app.tessera.habit.ACTION_LOG_HABIT`
- **Supported Intent Extras**:
  - `slug` (`String`): Target habit slug, e.g., `"deep-work"` (case-insensitive).
  - `habit_id` (`Long` or `Int` or `String`): Alternative to `slug` if caller knows numeric ID.
  - `mode` (`String`, optional): `"increment"` (default), `"set"`, or `"toggle"`.
  - `delta` (`Int`, optional, default `1`): Amount to add when `mode == "increment"`.
  - `count` (`Int`, optional): Exact count to set when `mode == "set"` (or if `count` extra is explicitly provided without `delta`).
  - `date` (`String`, optional): ISO date `"YYYY-MM-DD"`, defaults to `LocalDate.now()`.
  - `note` (`String`, optional): Appends/sets log note (e.g., `"Logged from Pomodoro App"`).
- **Execution**: Uses `goAsync()` + `CoroutineScope(Dispatchers.IO)` to update Room DB, record the update timestamp, and invoke `HabitWidgetUpdater.updateAllWidgets(context)` so home screen widgets update in real time even when Tessera is closed.

#### Example External App / ADB Usage:
```kotlin
// From any other Android app:
val intent = Intent("app.tessera.habit.ACTION_LOG_HABIT").apply {
    setPackage("app.tessera.habit")
    putExtra("slug", "deep-work")
    putExtra("delta", 1)
    putExtra("note", "Completed 50m session")
}
context.sendBroadcast(intent)
```
```bash
# Via ADB or Termux:
adb shell am broadcast -a app.tessera.habit.ACTION_LOG_HABIT -n app.tessera.habit/.api.HabitApiReceiver --es slug "deep-work" --ei delta 1
```

### 4.2 Deep Link URI API (`tessera://`)
Registered on `MainActivity` via `<intent-filter>`:
1. **Log Habit Deep Link**:
   `tessera://log?slug=deep-work&delta=1&date=2025-05-12&note=Session`
   - Parses query parameters (`slug`, `delta`, `count`, `mode`, `date`, `note`), writes to Room DB, refreshes all widgets, shows an in-app snackbar confirmation, and highlights the updated habit.
2. **Open Habit Detail Deep Link**:
   `tessera://habit/deep-work`
   - Opens `MainActivity` and navigates directly to the Habit Detail & Analytics sheet/screen for `deep-work`.

### 4.3 Full JSON Export & Import (`BackupManager`)
- Export complete database (`habits` + `habit_logs` + schema version + timestamp) to a human-readable JSON file via SAF (`ActivityResultContracts.CreateDocument("application/json")`) or copy to clipboard.
- Import JSON backup via SAF (`ActivityResultContracts.OpenDocument`) with merge/upsert by `slug` and `dateEpochDay`, followed by automatic widget refresh.

---

## 5. Exact Package & File Structure

All files under `/Users/anoop/Documents/antigravity/dazzling-darwin`:

```text
/Users/anoop/Documents/antigravity/dazzling-darwin/
├── .github/
│   └── workflows/
│       └── android-build.yml
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           ├── java/app/tessera/habit/
│           │   ├── TesseraApp.kt
│           │   ├── MainActivity.kt
│           │   ├── data/
│           │   │   ├── local/
│           │   │   │   ├── HabitEntity.kt
│           │   │   │   ├── HabitLogEntity.kt
│           │   │   │   ├── HabitDao.kt
│           │   │   │   └── TesseraDatabase.kt
│           │   │   ├── model/
│           │   │   │   ├── HabitWithLogs.kt
│           │   │   │   ├── HabitStats.kt
│           │   │   │   └── BackupModels.kt
│           │   │   ├── repository/
│           │   │   │   ├── TesseraRepository.kt
│           │   │   │   └── DemoSeedGenerator.kt
│           │   │   └── backup/
│           │   │       └── BackupManager.kt
│           │   ├── api/
│           │   │   ├── HabitApiContract.kt
│           │   │   ├── HabitApiReceiver.kt
│           │   │   └── DeepLinkHandler.kt
│           │   ├── widget/
│           │   │   ├── WidgetPrefs.kt
│           │   │   ├── WidgetHeatmapBitmapRenderer.kt
│           │   │   ├── HabitWidgetUpdater.kt
│           │   │   ├── HabitWidgetActionReceiver.kt
│           │   │   ├── HabitHeatmapWidgetProvider.kt
│           │   │   ├── HabitCompactWidgetProvider.kt
│           │   │   └── HabitWidgetConfigureActivity.kt
│           │   └── ui/
│           │       ├── theme/
│           │       │   ├── Color.kt
│           │       │   ├── Theme.kt
│           │       │   ├── Type.kt
│           │       │   └── HabitPalette.kt
│           │       ├── components/
│           │       │   ├── GithubHeatmapGrid.kt
│           │       │   ├── HabitCard.kt
│           │       │   ├── YearOverviewCard.kt
│           │       │   └── HabitIcons.kt
│           │       ├── screens/
│           │       │   ├── DashboardScreen.kt
│           │       │   ├── HabitDetailSheet.kt
│           │       │   ├── HabitEditorDialog.kt
│           │       │   ├── AnalyticsScreen.kt
│           │       │   └── ApiAndSettingsScreen.kt
│           │       └── viewmodel/
│           │           └── TesseraViewModel.kt
│           └── res/
│               ├── drawable/
│               │   ├── ic_launcher_Background.xml
│               │   ├── ic_launcher_foreground.xml
│               │   ├── widget_bg_dark.xml
│               │   ├── widget_bg_light.xml
│               │   ├── widget_btn_checkin_active.xml
│               │   ├── widget_btn_checkin_done.xml
│               │   ├── widget_streak_badge_bg.xml
│               │   └── widget_preview_heatmap.xml
│               ├── layout/
│               │   ├── widget_habit_heatmap.xml
│               │   └── widget_habit_compact.xml
│               ├── mipmap-anydpi-v26/
│               │   ├── ic_launcher.xml
│               │   └── ic_launcher_round.xml
│               ├── values/
│               │   ├── colors.xml
│               │   ├── strings.xml
│               │   └── themes.xml
│               └── xml/
│                   ├── habit_heatmap_widget_info.xml
│                   ├── habit_compact_widget_info.xml
│                   ├── backup_rules.xml
│                   └── data_extraction_rules.xml
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
└── gradle/
    ├── libs.versions.toml
    └── wrapper/
        ├── gradle-wrapper.jar
        └── gradle-wrapper.properties
```
