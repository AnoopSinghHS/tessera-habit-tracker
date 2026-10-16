package app.tessera.habit.api

import android.content.Context
import android.content.Intent
import android.net.Uri
import app.tessera.habit.data.repository.TesseraRepository
import java.time.LocalDate

object DeepLinkHandler {

    sealed class DeepLinkRequest {
        data class LogHabit(
            val slugOrId: String,
            val mode: String = HabitApiContract.MODE_INCREMENT,
            val delta: Int = 1,
            val count: Int? = null,
            val date: LocalDate = LocalDate.now(),
            val note: String? = null
        ) : DeepLinkRequest()

        data class OpenHabitDetail(
            val slugOrId: String
        ) : DeepLinkRequest()
    }

    data class DeepLinkOutcome(
        val slugOrId: String,
        val openDetailOnly: Boolean,
        val updatedSuccessfully: Boolean,
        val message: String
    )

    fun parseIntent(intent: Intent?): DeepLinkRequest? {
        val uri = intent?.data ?: return null
        return parseUri(uri)
    }

    fun parseUri(uri: Uri?): DeepLinkRequest? {
        if (uri == null || !uri.scheme.equals(HabitApiContract.SCHEME_TESSERA, ignoreCase = true)) {
            return null
        }

        val host = uri.host?.lowercase().orEmpty()
        return when (host) {
            HabitApiContract.HOST_LOG -> {
                val slug = uri.getQueryParameter(HabitApiContract.EXTRA_SLUG)
                    ?: uri.getQueryParameter(HabitApiContract.EXTRA_HABIT_ID)
                    ?: uri.lastPathSegment
                    ?: return null

                val mode = uri.getQueryParameter(HabitApiContract.EXTRA_MODE)
                    ?: HabitApiContract.MODE_INCREMENT
                val delta = uri.getQueryParameter(HabitApiContract.EXTRA_DELTA)?.toIntOrNull() ?: 1
                val count = uri.getQueryParameter(HabitApiContract.EXTRA_COUNT)?.toIntOrNull()
                val date = uri.getQueryParameter(HabitApiContract.EXTRA_DATE)?.let { raw ->
                    runCatching { LocalDate.parse(raw.trim()) }.getOrNull()
                } ?: LocalDate.now()
                val note = uri.getQueryParameter(HabitApiContract.EXTRA_NOTE)

                DeepLinkRequest.LogHabit(
                    slugOrId = slug.trim(),
                    mode = mode,
                    delta = delta,
                    count = count,
                    date = date,
                    note = note
                )
            }

            HabitApiContract.HOST_HABIT -> {
                val slug = uri.lastPathSegment
                    ?: uri.getQueryParameter(HabitApiContract.EXTRA_SLUG)
                    ?: return null
                DeepLinkRequest.OpenHabitDetail(slugOrId = slug.trim())
            }

            else -> null
        }
    }

    fun parse(uri: Uri?): DeepLinkRequest? = parseUri(uri)

    suspend fun handleIntent(
        intent: Intent?,
        repository: TesseraRepository
    ): DeepLinkOutcome? {
        return processIntent(repository.appContext, intent, repository)
    }

    suspend fun processIntent(
        context: Context,
        intent: Intent?,
        repository: TesseraRepository
    ): DeepLinkOutcome? {
        val request = parseIntent(intent) ?: return null
        return when (request) {
            is DeepLinkRequest.LogHabit -> {
                val success = repository.logByProgrammaticApi(
                    slugOrId = request.slugOrId,
                    mode = request.mode,
                    delta = request.delta,
                    count = request.count,
                    date = request.date,
                    note = request.note
                )
                if (success) {
                    HabitApiReceiver.recordLastApiEvent(
                        context,
                        "DeepLink [${request.mode}] -> #${request.slugOrId} on ${request.date}"
                    )
                }
                DeepLinkOutcome(
                    slugOrId = request.slugOrId,
                    openDetailOnly = false,
                    updatedSuccessfully = success,
                    message = if (success) {
                        "Logged ritual #${request.slugOrId}"
                    } else {
                        "Ritual #${request.slugOrId} not found"
                    }
                )
            }

            is DeepLinkRequest.OpenHabitDetail -> {
                DeepLinkOutcome(
                    slugOrId = request.slugOrId,
                    openDetailOnly = true,
                    updatedSuccessfully = true,
                    message = "Opened #${request.slugOrId}"
                )
            }
        }
    }
}
