package app.tessera.habit.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import app.tessera.habit.MainActivity
import app.tessera.habit.R
import app.tessera.habit.data.local.TesseraDatabase
import app.tessera.habit.data.model.HabitWithLogs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * Redraws the home-screen widgets ([HabitHeatmapWidgetProvider] 4x2 and
 * [HabitCompactWidgetProvider] 2x2) from the database.
 */
object HabitWidgetUpdater {

    private val updaterScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Refreshes every placed widget in the background. */
    fun updateAllWidgets(context: Context) {
        val appContext = context.applicationContext
        updaterScope.launch {
            updateAllWidgetsSync(appContext)
        }
    }

    /** Refreshes every placed widget and returns when done. */
    suspend fun updateAllWidgetsSync(context: Context) = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext
        val appWidgetManager = AppWidgetManager.getInstance(appContext)
        val wideComponent = ComponentName(appContext, HabitHeatmapWidgetProvider::class.java)
        val compactComponent = ComponentName(appContext, HabitCompactWidgetProvider::class.java)

        val wideIds = runCatching { appWidgetManager.getAppWidgetIds(wideComponent) }
            .getOrDefault(IntArray(0))
        val compactIds = runCatching { appWidgetManager.getAppWidgetIds(compactComponent) }
            .getOrDefault(IntArray(0))

        if (wideIds.isEmpty() && compactIds.isEmpty()) return@withContext

        val dao = TesseraDatabase.getInstance(appContext).habitDao()
        val allHabits = runCatching { dao.getAllHabitsWithLogsOnce() }.getOrDefault(emptyList())

        for (widgetId in wideIds) {
            renderAndPushWidget(
                context = appContext,
                appWidgetManager = appWidgetManager,
                appWidgetId = widgetId,
                isCompact = false,
                allHabits = allHabits
            )
        }

        for (widgetId in compactIds) {
            renderAndPushWidget(
                context = appContext,
                appWidgetManager = appWidgetManager,
                appWidgetId = widgetId,
                isCompact = true,
                allHabits = allHabits
            )
        }
    }

    /** Refreshes one widget in the background. */
    fun updateSingleWidget(context: Context, appWidgetId: Int, isCompact: Boolean = false) {
        val appContext = context.applicationContext
        updaterScope.launch {
            updateSingleWidgetSync(appContext, appWidgetId, isCompact)
        }
    }

    /** Refreshes one widget and returns when done. */
    suspend fun updateSingleWidgetSync(
        context: Context,
        appWidgetId: Int,
        isCompact: Boolean = false
    ) = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext
        val appWidgetManager = AppWidgetManager.getInstance(appContext)
        val providerInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)
        val resolvedCompact = if (providerInfo != null) {
            providerInfo.provider.className == HabitCompactWidgetProvider::class.java.name
        } else {
            isCompact
        }

        val dao = TesseraDatabase.getInstance(appContext).habitDao()
        val allHabits = runCatching { dao.getAllHabitsWithLogsOnce() }.getOrDefault(emptyList())

        renderAndPushWidget(
            context = appContext,
            appWidgetManager = appWidgetManager,
            appWidgetId = appWidgetId,
            isCompact = resolvedCompact,
            allHabits = allHabits
        )
    }

    private fun renderAndPushWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        isCompact: Boolean,
        allHabits: List<HabitWithLogs>
    ) {
        val configuredHabitId = WidgetPrefs.getHabitId(context, appWidgetId)
        val isDark = WidgetPrefs.isDarkTheme(context, appWidgetId)
        val colors = WidgetColors.of(isDark)

        val selectedHabit = allHabits.firstOrNull { it.habit.id == configuredHabitId }
            ?: allHabits.firstOrNull { !it.habit.isArchived }
            ?: allHabits.firstOrNull()

        // A widget placed without going through setup shows the first habit; remember it.
        if (selectedHabit != null && configuredHabitId <= 0L) {
            WidgetPrefs.setHabitId(context, appWidgetId, selectedHabit.habit.id)
        }

        val layoutRes = if (isCompact) {
            R.layout.widget_habit_compact
        } else {
            R.layout.widget_habit_heatmap
        }
        val views = RemoteViews(context.packageName, layoutRes)

        val bgRes = if (isDark) R.drawable.widget_bg_dark else R.drawable.widget_bg_light
        views.setInt(R.id.widget_root, "setBackgroundResource", bgRes)
        views.setTextColor(R.id.widget_habit_title, colors.textPrimary)
        views.setTextColor(R.id.widget_streak_badge, colors.accent)
        views.setTextColor(R.id.widget_habit_subtitle, colors.textSecondary)

        if (selectedHabit == null) {
            views.setTextViewText(R.id.widget_habit_title, "No habits yet")
            views.setViewVisibility(R.id.widget_streak_badge, View.GONE)
            views.setTextViewText(
                R.id.widget_habit_subtitle,
                if (isCompact) "Tap to add one" else "Tap to add one in Tessera"
            )
            setCheckInButton(views, isDark, colors, label = "+", filled = false)
            views.setContentDescription(R.id.widget_check_in_btn, "Open Tessera")

            val emptyBitmap = if (isCompact) {
                WidgetHeatmapBitmapRenderer.renderCompactHeatmap(context, null, isDark)
            } else {
                WidgetHeatmapBitmapRenderer.renderWideHeatmap(context, null, isDark)
            }
            views.setImageViewBitmap(R.id.widget_heatmap_image, emptyBitmap)

            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val launchPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_header_container, launchPendingIntent)
            views.setOnClickPendingIntent(R.id.widget_heatmap_image, launchPendingIntent)
            views.setOnClickPendingIntent(R.id.widget_check_in_btn, launchPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
            return
        }

        val habit = selectedHabit.habit
        val stats = selectedHabit.computeStats()
        val todayCount = selectedHabit.countForDate(LocalDate.now())
        val target = habit.targetPerDay.coerceAtLeast(1)
        val isCompletedToday = todayCount >= target

        views.setTextViewText(R.id.widget_habit_title, habit.title)

        // Second line: "23-day streak · Done today" (4x2) or "23 days · Done" (2x2).
        val streak = stats.currentStreak
        val hasStreak = streak > 0
        if (hasStreak) {
            views.setTextViewText(
                R.id.widget_streak_badge,
                if (isCompact) streakShort(streak) else "$streak-day streak"
            )
            views.setViewVisibility(R.id.widget_streak_badge, View.VISIBLE)
        } else {
            views.setViewVisibility(R.id.widget_streak_badge, View.GONE)
        }

        val status = when {
            target > 1 && isCompact -> "$todayCount of $target"
            target > 1 -> "$todayCount of $target today"
            isCompletedToday && isCompact -> "Done"
            isCompletedToday -> "Done today"
            isCompact && hasStreak -> ""
            else -> "Not done yet"
        }
        val subtitleText = when {
            status.isEmpty() -> ""
            hasStreak -> " \u00B7 $status"
            else -> status
        }
        views.setTextViewText(R.id.widget_habit_subtitle, subtitleText)

        val checkButtonLabel = when {
            isCompletedToday -> "\u2713"
            todayCount <= 0 -> "+"
            // Part-way through a several-times-a-day habit. "2/3" fits; longer ones show the count.
            todayCount < 10 && target < 10 -> "$todayCount/$target"
            else -> todayCount.toString()
        }
        setCheckInButton(views, isDark, colors, label = checkButtonLabel, filled = isCompletedToday)
        views.setContentDescription(
            R.id.widget_check_in_btn,
            if (isCompletedToday) {
                "${habit.title}: done today"
            } else {
                "Check in: ${habit.title}"
            }
        )

        val heatmapBitmap = if (isCompact) {
            WidgetHeatmapBitmapRenderer.renderCompactHeatmap(
                context = context,
                habitWithLogs = selectedHabit,
                isDarkTheme = isDark
            )
        } else {
            WidgetHeatmapBitmapRenderer.renderWideHeatmap(
                context = context,
                habitWithLogs = selectedHabit,
                isDarkTheme = isDark
            )
        }
        views.setImageViewBitmap(R.id.widget_heatmap_image, heatmapBitmap)

        // Button: check in without opening the app.
        val checkInIntent = Intent(context, HabitWidgetActionReceiver::class.java).apply {
            action = HabitWidgetActionReceiver.ACTION_WIDGET_CHECK_IN
            data = Uri.parse("tessera://widget/checkin/$appWidgetId/${habit.id}")
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            putExtra(HabitWidgetActionReceiver.EXTRA_HABIT_ID, habit.id)
        }
        val checkInPendingIntent = PendingIntent.getBroadcast(
            context,
            appWidgetId * 100 + 1,
            checkInIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_check_in_btn, checkInPendingIntent)

        // Anywhere else: open this habit in the app.
        val detailIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("tessera://habit/${habit.slug}"),
            context,
            MainActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val detailPendingIntent = PendingIntent.getActivity(
            context,
            appWidgetId * 100 + 2,
            detailIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_header_container, detailPendingIntent)
        views.setOnClickPendingIntent(R.id.widget_heatmap_image, detailPendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun streakShort(streak: Int): String {
        return if (streak == 1) "1 day" else "$streak days"
    }

    /**
     * Styles the round check-in button: filled accent when today is done, accent outline
     * otherwise. The circle drawables come in light and dark versions.
     */
    private fun setCheckInButton(
        views: RemoteViews,
        isDark: Boolean,
        colors: WidgetColors,
        label: String,
        filled: Boolean
    ) {
        val backgroundRes = when {
            filled && isDark -> R.drawable.widget_btn_checkin_done_dark
            filled -> R.drawable.widget_btn_checkin_done_light
            isDark -> R.drawable.widget_btn_checkin_todo_dark
            else -> R.drawable.widget_btn_checkin_todo_light
        }
        views.setInt(R.id.widget_check_in_label, "setBackgroundResource", backgroundRes)
        views.setTextViewText(R.id.widget_check_in_label, label)
        views.setTextColor(
            R.id.widget_check_in_label,
            if (filled) colors.onAccent else colors.accent
        )
    }
}
