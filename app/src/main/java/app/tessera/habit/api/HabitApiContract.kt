package app.tessera.habit.api

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.time.LocalDate

object HabitApiContract {

    const val PACKAGE_NAME = "app.tessera.habit"

    // Broadcast Actions (supports both canonical and shorthand action strings)
    const val ACTION_LOG_HABIT = "app.tessera.habit.ACTION_LOG_HABIT"
    const val ACTION_LOG_HABIT_SHORT = "app.tessera.action.LOG_HABIT"

    // Intent & Deep-Link Parameter Keys
    const val EXTRA_SLUG = "slug"
    const val EXTRA_HABIT_ID = "habit_id"
    const val EXTRA_MODE = "mode"
    const val EXTRA_DELTA = "delta"
    const val EXTRA_COUNT = "count"
    const val EXTRA_DATE = "date"
    const val EXTRA_NOTE = "note"

    // Supported Update Modes
    const val MODE_INCREMENT = "increment"
    const val MODE_SET = "set"
    const val MODE_TOGGLE = "toggle"

    // Deep Link URI Constants
    const val SCHEME_TESSERA = "tessera"
    const val HOST_LOG = "log"
    const val HOST_HABIT = "habit"

    fun buildBroadcastIntent(
        context: Context,
        slug: String,
        mode: String = MODE_INCREMENT,
        delta: Int = 1,
        count: Int? = null,
        dateIso: String = LocalDate.now().toString(),
        note: String? = null
    ): Intent {
        return Intent(context, HabitApiReceiver::class.java).apply {
            action = ACTION_LOG_HABIT
            putExtra(EXTRA_SLUG, slug)
            putExtra(EXTRA_MODE, mode)
            putExtra(EXTRA_DELTA, delta)
            if (count != null) {
                putExtra(EXTRA_COUNT, count)
            }
            putExtra(EXTRA_DATE, dateIso)
            if (!note.isNullOrBlank()) {
                putExtra(EXTRA_NOTE, note)
            }
        }
    }

    fun buildAdbBroadcastCommand(
        slug: String,
        mode: String = MODE_INCREMENT,
        delta: Int = 1,
        dateIso: String = LocalDate.now().toString(),
        note: String = "Automated via ADB"
    ): String {
        return buildString {
            append("adb shell am broadcast -a ")
            append(ACTION_LOG_HABIT)
            append(" -n ")
            append(PACKAGE_NAME)
            append("/.api.HabitApiReceiver")
            append(" --es slug \"")
            append(slug)
            append("\" --es mode \"")
            append(mode)
            append("\" --ei delta ")
            append(delta)
            append(" --es date \"")
            append(dateIso)
            append("\" --es note \"")
            append(note)
            append("\"")
        }
    }

    fun buildLogDeepLinkUri(
        slug: String,
        mode: String = MODE_INCREMENT,
        delta: Int = 1,
        count: Int? = null,
        dateIso: String? = null,
        note: String? = null
    ): Uri {
        return Uri.Builder()
            .scheme(SCHEME_TESSERA)
            .authority(HOST_LOG)
            .appendQueryParameter(EXTRA_SLUG, slug)
            .appendQueryParameter(EXTRA_MODE, mode)
            .appendQueryParameter(EXTRA_DELTA, delta.toString())
            .apply {
                if (count != null) appendQueryParameter(EXTRA_COUNT, count.toString())
                if (!dateIso.isNullOrBlank()) appendQueryParameter(EXTRA_DATE, dateIso)
                if (!note.isNullOrBlank()) appendQueryParameter(EXTRA_NOTE, note)
            }
            .build()
    }

    fun buildHabitDetailDeepLinkUri(slug: String): Uri {
        return Uri.Builder()
            .scheme(SCHEME_TESSERA)
            .authority(HOST_HABIT)
            .appendPath(slug)
            .build()
    }
}
