# Tessera

A personal, offline habit tracker for Android. Each habit is a card with a GitHub-style
heatmap, a one-tap daily check-in and a streak. Home-screen widgets do the same without
opening the app.

Kotlin, Jetpack Compose (Material 3), Room. Android 8.0+ (minSdk 26).

## What it does

- **Today**: your habits, each with its recent weeks as a heatmap and a check-in button.
  A habit can be once a day or several times a day. Full-width cards or a compact grid.
  Filter by category; archived habits are tucked away.
- **Habit detail**: streak and next milestone, a year of history, tap any past day to fix
  its count or add a note, month calendar, edit, archive, delete.
- **Progress**: completion across all habits over the last 30 days, the year at a glance,
  and which weekdays are strongest.
- **Widgets**: a 4×2 heatmap widget and a 2×2 compact one, each bound to one habit, with a
  check-in button. Light or dark background per widget.
- **Automation**: other apps (Tasker, Samsung Routines, NFC tags, ADB) can log a habit by
  broadcast or `tessera://` link. See below.
- **Backup**: save everything to a JSON file or the clipboard, and load it back.
- **Updates**: Settings can check this repository's GitHub releases for a newer version.

Data stays on the phone in a local database. The only network request the app makes is the
update check.

## Look

One theme, Homestead: sun-bleached sand and clear-sky blue. The theme owns the colour, so
every heatmap uses the same blue ramp and there is one accent on screen. Type is Fraunces
for the few large display sizes and Libre Franklin for everything else, both bundled under
the SIL Open Font License (see `licenses/`). The design tokens live in
`app/src/main/java/app/tessera/habit/ui/theme/` and the shared components in
`ui/components/TesseraUi.kt`.

## Automation API

### 1. Android Broadcast Intent (`HabitApiReceiver`)

External Android apps, Tasker, MacroDroid, or `adb shell` can log or update any habit in the background by sending an explicit broadcast to `app.tessera.habit/.api.HabitApiReceiver`.

- **Action**: `app.tessera.habit.ACTION_LOG_HABIT` (or shorthand `app.tessera.action.LOG_HABIT`)
- **Component**: `app.tessera.habit/app.tessera.habit.api.HabitApiReceiver`

| Extra Key | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `slug` | `String` | *(Required*)* | Unique habit slug (e.g., `"deep-work"`, `"strength-training"`, `"hydration"`). |
| `habit_id` | `Long` / `String` | *(Optional)* | Numeric habit ID if `slug` is omitted. |
| `mode` | `String` | `"increment"` | `"increment"` (adds `delta`), `"toggle"` (cycles `0 → target → 0`), or `"set"` (sets exact `count`). |
| `delta` | `Int` | `1` | Amount to add when `mode = "increment"`. |
| `count` | `Int` | `targetPerDay` | Explicit completion count when `mode = "set"`. |
| `date` | `String` | `LocalDate.now()` | ISO-8601 date (`YYYY-MM-DD`) for filling in past days. |
| `note` | `String` | `null` | Optional note attached to that date's log entry. |

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
- **Open a habit's detail screen**:
  ```text
  tessera://habit/deep-work
  ```

Each habit's automation ID (its "slug") is shown on its detail screen and can be changed
in the editor. Settings has copyable examples for the habit you pick.

Note: the broadcast receiver is exported without a permission, so any app installed on the
phone can log habits through it.

## Building

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug assembleRelease
```

GitHub Actions builds every push. Pushes to `main` also publish a GitHub release tagged
`v<versionName>` with the APKs attached; bump `versionName` in `app/build.gradle.kts` to
cut a new one. Release builds are signed with the debug key, so an APK built on a
different machine may refuse to install over an existing copy: save a backup, uninstall,
install, load the backup.

## Other things in this repository

- `web/`: a separate browser habit tracker (React + Vite) that keeps its own data in the
  browser.
- `server/`: a small sync server (Express + SQLite) for the web client.

The Android app does not sync with either; they share no data with the phone.

- `docs/`: earlier design and architecture notes. They predate the current design and
  describe some features that were never built on Android.
