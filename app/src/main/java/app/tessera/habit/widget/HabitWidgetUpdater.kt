package app.tessera.habit.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
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

/** The words a widget shows for one habit. */
internal class WidgetTexts(
    val title: String,
    /** "23-day streak", or empty when there is no streak. */
    val streak: String,
    /** What follows the streak, e.g. " · Done today". */
    val status: String,
    /** What the check-in button shows: +, a tick, or progress like 2/3. */
    val glyph: String,
    val isDone: Boolean,
    val buttonDescription: String
)

/**
 * Redraws the home-screen widgets ([HabitHeatmapWidgetProvider] and
 * [HabitCompactWidgetProvider]) from the database. Both can be resized to any shape: the
 * heatmap is drawn at the widget's current size (see [WidgetHeatmapBitmapRenderer]).
 */
object HabitWidgetUpdater {

    private val updaterScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Used when the launcher does not report a widget's size. */
    private const val FALLBACK_COMPACT_WIDTH_DP = 158f
    private const val FALLBACK_WIDE_WIDTH_DP = 330f
    private const val FALLBACK_HEIGHT_DP = 158f

    /** Below this width (and in the 104dp title column of wide widgets) the text is shortened. */
    private const val SHORT_TEXT_BELOW_DP = 200f

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
            renderAndPushWidget(appContext, appWidgetManager, widgetId, false, allHabits)
        }
        for (widgetId in compactIds) {
            renderAndPushWidget(appContext, appWidgetManager, widgetId, true, allHabits)
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

        renderAndPushWidget(appContext, appWidgetManager, appWidgetId, resolvedCompact, allHabits)
    }

    /** The widget's current size in dp, from what the launcher reports. */
    private fun widgetSizeDp(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        isCompact: Boolean
    ): Pair<Float, Float> {
        val options = runCatching { appWidgetManager.getAppWidgetOptions(appWidgetId) }.getOrNull()
        val portrait = context.resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE
        var width = 0
        var height = 0
        if (options != null) {
            width = if (portrait) {
                options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0)
            } else {
                options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 0)
            }
            height = if (portrait) {
                options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 0)
            } else {
                options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0)
            }
        }
        if (width <= 0 || height <= 0) {
            return Pair(
                if (isCompact) FALLBACK_COMPACT_WIDTH_DP else FALLBACK_WIDE_WIDTH_DP,
                FALLBACK_HEIGHT_DP
            )
        }
        return Pair(width.toFloat(), height.toFloat())
    }

    internal fun widgetTexts(selectedHabit: HabitWithLogs?, shortText: Boolean): WidgetTexts {
        if (selectedHabit == null) {
            return WidgetTexts(
                title = "No habits yet",
                streak = "",
                status = if (shortText) "Tap to add one" else "Tap to add one in Tessera",
                glyph = "+",
                isDone = false,
                buttonDescription = "Open Tessera"
            )
        }

        val habit = selectedHabit.habit
        val streak = selectedHabit.computeStats().currentStreak
        val todayCount = selectedHabit.countForDate(LocalDate.now())
        val target = habit.targetPerDay.coerceAtLeast(1)
        val isDone = todayCount >= target
        val hasStreak = streak > 0

        val streakText = when {
            !hasStreak -> ""
            shortText -> if (streak == 1) "1 day" else "$streak days"
            else -> "$streak-day streak"
        }
        val status = when {
            target > 1 && shortText -> "$todayCount of $target"
            target > 1 -> "$todayCount of $target today"
            isDone && shortText -> "Done"
            isDone -> "Done today"
            shortText && hasStreak -> ""
            else -> "Not done yet"
        }
        val statusText = when {
            status.isEmpty() -> ""
            hasStreak -> " · $status"
            else -> status
        }
        val glyph = when {
            isDone -> "✓"
            todayCount <= 0 -> "+"
            // Part-way through a several-times-a-day habit. "2/3" fits; longer ones show the count.
            todayCount < 10 && target < 10 -> "$todayCount/$target"
            else -> todayCount.toString()
        }
        return WidgetTexts(
            title = habit.title,
            streak = streakText,
            status = statusText,
            glyph = glyph,
            isDone = isDone,
            buttonDescription = if (isDone) "${habit.title}: done today" else "Check in: ${habit.title}"
        )
    }

    private fun renderAndPushWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        isCompact: Boolean,
        allHabits: List<HabitWithLogs>
    ) {
        val (widthDp, heightDp) = widgetSizeDp(context, appWidgetManager, appWidgetId, isCompact)
        appWidgetManager.updateAppWidget(
            appWidgetId,
            buildViews(context, appWidgetId, widthDp, heightDp, allHabits)
        )
    }

    /**
     * The widget's views for a given size: the heatmap picture plus the title, streak and
     * check-in button on top. Separate from pushing them to the launcher so a test can
     * draw them.
     */
    internal fun buildViews(
        context: Context,
        appWidgetId: Int,
        widthDp: Float,
        heightDp: Float,
        allHabits: List<HabitWithLogs>
    ): RemoteViews {
        val configuredHabitId = WidgetPrefs.getHabitId(context, appWidgetId)
        val isDark = WidgetPrefs.isDarkTheme(context, appWidgetId)
        val style = WidgetPrefs.getStyle(context)

        val selectedHabit = allHabits.firstOrNull { it.habit.id == configuredHabitId }
            ?: allHabits.firstOrNull { !it.habit.isArchived }
            ?: allHabits.firstOrNull()

        // A widget placed without going through setup shows the first habit; remember it.
        if (selectedHabit != null && configuredHabitId <= 0L) {
            WidgetPrefs.setHabitId(context, appWidgetId, selectedHabit.habit.id)
        }

        val shape = WidgetHeatmapBitmapRenderer.shapeFor(widthDp, heightDp)
        val layoutRes = if (shape == WidgetShape.SIDE) {
            R.layout.widget_habit_side
        } else {
            R.layout.widget_habit_top
        }
        val views = RemoteViews(context.packageName, layoutRes)

        val colors = WidgetColors.of(isDark, style, WidgetPalette.resolve(selectedHabit?.habit?.colorKey))
        val shortText = widthDp < SHORT_TEXT_BELOW_DP || shape == WidgetShape.SIDE
        val texts = widgetTexts(selectedHabit, shortText)

        views.setImageViewBitmap(
            R.id.widget_heatmap_image,
            WidgetHeatmapBitmapRenderer.render(
                context = context,
                habitWithLogs = selectedHabit,
                widthDp = widthDp,
                heightDp = heightDp,
                isDark = isDark,
                style = style
            )
        )

        views.setTextViewText(R.id.widget_habit_title, texts.title)
        views.setTextColor(R.id.widget_habit_title, colors.textPrimary)
        views.setTextColor(R.id.widget_streak_badge, colors.accent)
        views.setTextColor(R.id.widget_habit_subtitle, colors.textSecondary)
        if (texts.streak.isEmpty()) {
            views.setViewVisibility(R.id.widget_streak_badge, View.GONE)
        } else {
            views.setTextViewText(R.id.widget_streak_badge, texts.streak)
            views.setViewVisibility(R.id.widget_streak_badge, View.VISIBLE)
        }
        views.setTextViewText(R.id.widget_habit_subtitle, texts.status)

        views.setTextViewText(R.id.widget_check_in_label, texts.glyph)
        views.setTextColor(
            R.id.widget_check_in_label,
            if (texts.isDone) colors.onAccent else colors.accent
        )
        views.setContentDescription(R.id.widget_check_in_btn, texts.buttonDescription)

        if (selectedHabit == null) {
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
            return views
        }

        val habit = selectedHabit.habit

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

        return views
    }
}
