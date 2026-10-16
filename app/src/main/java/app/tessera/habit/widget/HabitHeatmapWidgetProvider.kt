package app.tessera.habit.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle

/**
 * 4x2 Wide Architectural GitHub Heatmap Widget Provider for Samsung One UI Home Screen.
 * Renders an 18-week contribution matrix with streak badge and 1-tap check-in button.
 */
class HabitHeatmapWidgetProvider : AppWidgetProvider() {

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        HabitWidgetUpdater.updateAllWidgets(context)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        for (appWidgetId in appWidgetIds) {
            HabitWidgetUpdater.updateSingleWidget(context, appWidgetId, isCompact = false)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        HabitWidgetUpdater.updateSingleWidget(context, appWidgetId, isCompact = false)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        for (appWidgetId in appWidgetIds) {
            WidgetPrefs.clearWidgetConfig(context, appWidgetId)
        }
    }
}
