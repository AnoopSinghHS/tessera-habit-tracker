package app.tessera.habit.api

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import app.tessera.habit.TesseraApp
import app.tessera.habit.data.local.TesseraDatabase
import app.tessera.habit.data.repository.TesseraRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate

class HabitApiReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action != HabitApiContract.ACTION_LOG_HABIT &&
            action != HabitApiContract.ACTION_LOG_HABIT_SHORT
        ) {
            return
        }

        val pendingResult = goAsync()
        val appContext = context.applicationContext

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val repository = (appContext as? TesseraApp)?.repository
                    ?: TesseraRepository(
                        appContext,
                        TesseraDatabase.getInstance(appContext).habitDao()
                    )

                val slugExtra = intent.getStringExtra(HabitApiContract.EXTRA_SLUG)?.trim()
                val idExtra: String? = when {
                    intent.hasExtra(HabitApiContract.EXTRA_HABIT_ID) -> {
                        intent.getStringExtra(HabitApiContract.EXTRA_HABIT_ID)
                            ?: intent.getLongExtra(HabitApiContract.EXTRA_HABIT_ID, -1L)
                                .takeIf { it > 0L }?.toString()
                            ?: intent.getIntExtra(HabitApiContract.EXTRA_HABIT_ID, -1)
                                .takeIf { it > 0 }?.toString()
                    }
                    else -> null
                }

                val targetKey = slugExtra?.takeIf { it.isNotEmpty() } ?: idExtra
                if (targetKey.isNullOrBlank()) {
                    Log.w(TAG, "Ignored ACTION_LOG_HABIT broadcast: missing 'slug' or 'habit_id' extra")
                    return@launch
                }

                val mode = intent.getStringExtra(HabitApiContract.EXTRA_MODE)
                    ?: HabitApiContract.MODE_INCREMENT

                val delta = intent.getIntExtra(HabitApiContract.EXTRA_DELTA, 1)
                val count: Int? = if (intent.hasExtra(HabitApiContract.EXTRA_COUNT)) {
                    intent.getIntExtra(HabitApiContract.EXTRA_COUNT, 1)
                } else {
                    null
                }

                val rawDate = intent.getStringExtra(HabitApiContract.EXTRA_DATE)
                val parsedDate = if (!rawDate.isNullOrBlank()) {
                    runCatching { LocalDate.parse(rawDate.trim()) }.getOrElse { LocalDate.now() }
                } else {
                    LocalDate.now()
                }

                val note = intent.getStringExtra(HabitApiContract.EXTRA_NOTE)

                val updated = repository.logByProgrammaticApi(
                    slugOrId = targetKey,
                    mode = mode,
                    delta = delta,
                    count = count,
                    date = parsedDate,
                    note = note
                )

                if (updated) {
                    recordLastApiEvent(
                        appContext,
                        "Broadcast [$mode] -> #$targetKey on $parsedDate"
                    )
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Error handling HabitApiReceiver broadcast", t)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "HabitApiReceiver"
        const val PREFS_API_TELEMETRY = "tessera_api_telemetry"
        const val KEY_LAST_EVENT_SUMMARY = "last_api_event_summary"
        const val KEY_LAST_EVENT_TIMESTAMP = "last_api_event_timestamp"

        fun recordLastApiEvent(context: Context, summary: String) {
            context.applicationContext
                .getSharedPreferences(PREFS_API_TELEMETRY, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_LAST_EVENT_SUMMARY, summary)
                .putLong(KEY_LAST_EVENT_TIMESTAMP, System.currentTimeMillis())
                .apply()
        }
    }
}
