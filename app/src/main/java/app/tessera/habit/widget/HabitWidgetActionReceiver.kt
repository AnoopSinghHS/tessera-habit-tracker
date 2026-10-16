package app.tessera.habit.widget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.tessera.habit.data.local.HabitLogEntity
import app.tessera.habit.data.local.TesseraDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Handles 1-tap check-in actions dispatched directly from Samsung One UI Home Screen Widgets.
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

                if (habit != null) {
                    val todayEpoch = LocalDate.now().toEpochDay()
                    val existingLog = dao.getLogForDate(habit.id, todayEpoch)
                    val currentCount = existingLog?.count ?: 0
                    val target = habit.targetPerDay.coerceAtLeast(1)
                    val tapMode = WidgetPrefs.getTapMode(appContext, appWidgetId)

                    val nextCount = if (tapMode == WidgetPrefs.TAP_MODE_INCREMENT) {
                        currentCount + 1
                    } else {
                        // Smart Toggle / Quartile Cycle:
                        // Binary habits toggle 0 <-> 1.
                        // Multi-step habits increment 0 -> 1 -> ... -> target -> 0.
                        if (target <= 1) {
                            if (currentCount >= 1) 0 else 1
                        } else {
                            if (currentCount >= target) 0 else currentCount + 1
                        }
                    }

                    if (nextCount <= 0) {
                        dao.deleteLogForDate(habit.id, todayEpoch)
                    } else {
                        dao.upsertLog(
                            HabitLogEntity(
                                id = existingLog?.id ?: 0L,
                                habitId = habit.id,
                                dateEpochDay = todayEpoch,
                                count = nextCount,
                                note = existingLog?.note.orEmpty(),
                                updatedAt = System.currentTimeMillis()
                            )
                        )
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
