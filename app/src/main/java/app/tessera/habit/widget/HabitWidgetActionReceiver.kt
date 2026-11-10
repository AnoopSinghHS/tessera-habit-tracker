package app.tessera.habit.widget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.tessera.habit.TesseraApp
import app.tessera.habit.data.local.TesseraDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Handles a tap on a widget's check-in button.
 *
 * The count change itself is done by the repository, so widget taps and in-app taps
 * follow the same rules. Which rule applies depends on the widget's tap setting:
 * "smart" marks done / undoes (or steps towards the daily target), "increment" adds one.
 */
class HabitWidgetActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action == ACTION_REFRESH_ALL_WIDGETS) {
            HabitWidgetUpdater.updateAllWidgets(context)
            return
        }
        if (action != ACTION_WIDGET_CHECK_IN && action != ACTION_WIDGET_CHECK_IN_ALT) return

        val appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        val explicitHabitId = intent.getLongExtra(EXTRA_HABIT_ID, -1L)

        val pendingResult = goAsync()
        val appContext = context.applicationContext

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val dao = TesseraDatabase.getInstance(appContext).habitDao()
                val configuredHabitId = if (explicitHabitId > 0L) {
                    explicitHabitId
                } else {
                    WidgetPrefs.getHabitId(appContext, appWidgetId)
                }

                val habit = if (configuredHabitId > 0L) {
                    dao.getHabitById(configuredHabitId)
                } else {
                    dao.getAllHabitsWithLogsOnce().firstOrNull { !it.habit.isArchived }?.habit
                }

                val repository = (appContext as? TesseraApp)?.repository
                if (habit != null && repository != null) {
                    val today = LocalDate.now()
                    if (WidgetPrefs.getTapMode(appContext, appWidgetId) == WidgetPrefs.TAP_MODE_INCREMENT) {
                        repository.incrementHabit(habitId = habit.id, date = today)
                    } else {
                        repository.smartCheckIn(habitId = habit.id, date = today)
                    }
                }

                HabitWidgetUpdater.updateAllWidgetsSync(appContext)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_WIDGET_CHECK_IN = "app.tessera.habit.action.WIDGET_CHECK_IN"
        const val ACTION_WIDGET_CHECK_IN_ALT = "app.tessera.habit.widget.ACTION_WIDGET_CHECK_IN"
        const val ACTION_REFRESH_ALL_WIDGETS = "app.tessera.habit.widget.ACTION_REFRESH_ALL_WIDGETS"
        const val EXTRA_HABIT_ID = "extra_habit_id"
    }
}
