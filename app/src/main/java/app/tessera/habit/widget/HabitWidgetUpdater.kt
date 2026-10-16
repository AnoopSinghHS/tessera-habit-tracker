package app.tessera.habit.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import androidx.compose.ui.graphics.toArgb
import app.tessera.habit.MainActivity
import app.tessera.habit.R
import app.tessera.habit.TesseraApp
import app.tessera.habit.data.local.TesseraDatabase
import app.tessera.habit.data.model.HabitWithLogs
import app.tessera.habit.ui.theme.HabitPalette
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.Locale

/**
 * Coordinates real-time updates for all active Tessera Home Screen Widgets
 * ([HabitHeatmapWidgetProvider] 4x2 Wide and [HabitCompactWidgetProvider] 2x2 Compact).
 */
object HabitWidgetUpdater {

    private val updaterScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Asynchronously refreshes all placed widgets on [Dispatchers.IO].
     */
    fun updateAllWidgets(context: Context) {
        val appContext = context.applicationContext
        updaterScope.launch {
            updateAllWidgetsSync(appContext)
        }
    }

    /**
     * Synchronously refreshes all placed widgets (must be called from a coroutine).
     */
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
        var allHabits = runCatching { dao.getAllHabitsWithLogsOnce() }.getOrDefault(emptyList())
        if (allHabits.isEmpty()) {
            runCatching { (appContext as? TesseraApp)?.repository?.ensureSeeded() }
            allHabits = runCatching { dao.getAllHabitsWithLogsOnce() }.getOrDefault(emptyList())
        }

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

    /**
     * Asynchronously updates a single widget instance.
     */
    fun updateSingleWidget(context: Context, appWidgetId: Int, isCompact: Boolean = false) {
        val appContext = context.applicationContext
        updaterScope.launch {
            updateSingleWidgetSync(appContext, appWidgetId, isCompact)
        }
    }

    /**
     * Synchronously updates a single widget instance.
     */
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
        var allHabits = runCatching { dao.getAllHabitsWithLogsOnce() }.getOrDefault(emptyList())
        if (allHabits.isEmpty()) {
            runCatching { (appContext as? TesseraApp)?.repository?.ensureSeeded() }
            allHabits = runCatching { dao.getAllHabitsWithLogsOnce() }.getOrDefault(emptyList())
        }

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

        val selectedHabit = allHabits.firstOrNull { it.habit.id == configuredHabitId }
            ?: allHabits.firstOrNull { !it.habit.isArchived }
            ?: allHabits.firstOrNull()

        // Persist fallback habit binding if widget was placed directly without config
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

        val primaryTextColor = if (isDark) 0xFFF3F4F6.toInt() else 0xFF111317.toInt()
        val secondaryTextColor = if (isDark) 0xFF8A919E.toInt() else 0xFF686D76.toInt()

        if (selectedHabit == null) {
            views.setTextViewText(R.id.widget_habit_title, "Tessera Rituals")
            views.setTextColor(R.id.widget_habit_title, primaryTextColor)
            views.setTextViewText(R.id.widget_streak_badge, "0D")
            views.setTextViewText(R.id.widget_habit_subtitle, "Tap to create a habit")
            views.setTextColor(R.id.widget_habit_subtitle, secondaryTextColor)
            views.setTextViewText(R.id.widget_check_in_btn, "+")

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
        val palette = HabitPalette.resolve(habit.colorKey)
        val accentArgb = palette.levelColorArgb(4, isDark)
        val stats = selectedHabit.computeStats()
        val todayCount = selectedHabit.countForDate(LocalDate.now())
        val target = habit.targetPerDay.coerceAtLeast(1)
        val isCompletedToday = todayCount >= target

        // Header Title & Subtitle
        views.setTextViewText(R.id.widget_habit_title, habit.title)
        views.setTextColor(R.id.widget_habit_title, primaryTextColor)

        val streakText = if (isCompact) {
            "${stats.currentStreak}D"
        } else {
            "${stats.currentStreak}D STREAK"
        }
        views.setTextViewText(R.id.widget_streak_badge, streakText)
        views.setTextColor(R.id.widget_streak_badge, accentArgb)
        views.setViewVisibility(R.id.widget_streak_badge, View.VISIBLE)

        val subtitleText = if (isCompact) {
            if (target > 1) "$todayCount/$target" else if (isCompletedToday) "DONE" else "0/1"
        } else {
            val statusPart = if (target > 1) {
                "$todayCount/$target ${habit.unitLabel.uppercase(Locale.US)}"
            } else if (isCompletedToday) {
                "COMPLETED TODAY"
            } else {
                "PENDING TODAY"
            }
            "#${habit.slug} · $statusPart"
        }
        views.setTextViewText(R.id.widget_habit_subtitle, subtitleText)
        views.setTextColor(R.id.widget_habit_subtitle, secondaryTextColor)

        if (!isCompact) {
            views.setTextViewText(R.id.widget_color_dot, resolveGlyphForIconKey(habit.iconKey))
            views.setTextColor(R.id.widget_color_dot, accentArgb)
        }

        // 1-Tap Check-In Button State
        val checkButtonLabel = when {
            isCompletedToday -> "✓"
            target > 1 && todayCount > 0 -> "$todayCount/$target"
            else -> "+"
        }
        views.setTextViewText(R.id.widget_check_in_btn, checkButtonLabel)

        if (isCompletedToday) {
            views.setInt(
                R.id.widget_check_in_btn,
                "setBackgroundResource",
                R.drawable.widget_btn_checkin_done
            )
            views.setTextColor(R.id.widget_check_in_btn, palette.onAccent.toArgb())
        } else {
            views.setInt(
                R.id.widget_check_in_btn,
                "setBackgroundResource",
                R.drawable.widget_btn_checkin_active
            )
            views.setTextColor(
                R.id.widget_check_in_btn,
                if (todayCount > 0) accentArgb else primaryTextColor
            )
        }

        // High-DPI Contribution Matrix Bitmap
        val heatmapBitmap = if (isCompact) {
            WidgetHeatmapBitmapRenderer.renderCompactHeatmap(
                context = context,
                habitWithLogs = selectedHabit,
                isDarkTheme = isDark,
                weeks = 8
            )
        } else {
            WidgetHeatmapBitmapRenderer.renderWideHeatmap(
                context = context,
                habitWithLogs = selectedHabit,
                isDarkTheme = isDark,
                weeks = 18
            )
        }
        views.setImageViewBitmap(R.id.widget_heatmap_image, heatmapBitmap)

        // PendingIntent 1: 1-Tap Check-In Broadcast
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

        // PendingIntent 2: Deep-Link to Habit Detail Sheet in MainActivity
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

    private fun resolveGlyphForIconKey(iconKey: String): String {
        return when (iconKey.trim().lowercase(Locale.US)) {
            "bolt", "zap", "focus" -> "⚡"
            "terminal", "cli", "code" -> "⌘"
            "fitness", "dumbbell", "gym" -> "✦"
            "run", "running", "bicycle" -> "▸"
            "book", "read" -> "▤"
            "water", "hydration" -> "💧"
            "meditation", "brain" -> "◎"
            "flame", "fire" -> "🔥"
            "heart" -> "♥"
            "leaf", "spa" -> "❖"
            "sun", "morning" -> "☀"
            "sleep", "moon" -> "☾"
            else -> "▣"
        }
    }
}
