package app.tessera.habit.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.tessera.habit.TesseraApp
import app.tessera.habit.api.DeepLinkHandler
import app.tessera.habit.api.HabitApiContract
import app.tessera.habit.api.HabitApiReceiver
import app.tessera.habit.data.backup.BackupManager
import app.tessera.habit.data.local.HabitEntity
import app.tessera.habit.data.local.TesseraDatabase
import app.tessera.habit.data.model.HabitWithLogs
import app.tessera.habit.data.repository.TesseraRepository
import app.tessera.habit.ui.theme.ThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class TesseraUiState(
    val allHabits: List<HabitWithLogs> = emptyList(),
    val activeHabits: List<HabitWithLogs> = emptyList(),
    val archivedHabits: List<HabitWithLogs> = emptyList(),
    val filteredHabits: List<HabitWithLogs> = emptyList(),
    val categories: List<String> = listOf("ALL"),
    val selectedCategory: String = "ALL",
    val isCompactBentoView: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.ALABASTER_LIGHT,
    val selectedHabitId: Long? = null,
    val isEditorOpen: Boolean = false,
    val editingHabit: HabitEntity? = null,
    val lastApiEventSummary: String = "Ready · Listening on app.tessera.habit.ACTION_LOG_HABIT",
    val statusMessage: String? = null
) {
    val selectedHabitWithLogs: HabitWithLogs?
        get() = allHabits.firstOrNull { it.habit.id == selectedHabitId }
}

private data class LocalUiControlState(
    val selectedCategory: String = "ALL",
    val isCompactBentoView: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.ALABASTER_LIGHT,
    val selectedHabitId: Long? = null,
    val isEditorOpen: Boolean = false,
    val editingHabit: HabitEntity? = null,
    val lastApiEventSummary: String = "Ready · Listening on app.tessera.habit.ACTION_LOG_HABIT",
    val statusMessage: String? = null
)

class TesseraViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext: Context = application.applicationContext

    private val repository: TesseraRepository = (application as? TesseraApp)?.repository
        ?: TesseraRepository(appContext, TesseraDatabase.getInstance(appContext).habitDao())

    private val backupManager: BackupManager = (application as? TesseraApp)?.backupManager
        ?: BackupManager(appContext, repository)

    private val uiPrefs = appContext.getSharedPreferences(UI_PREFS_NAME, Context.MODE_PRIVATE)
    private val apiTelemetryPrefs = appContext.getSharedPreferences(
        HabitApiReceiver.PREFS_API_TELEMETRY,
        Context.MODE_PRIVATE
    )

    private val controlState = MutableStateFlow(
        LocalUiControlState(
            isCompactBentoView = uiPrefs.getBoolean(KEY_COMPACT_BENTO_VIEW, false),
            themeMode = ThemeMode.fromKey(uiPrefs.getString(KEY_THEME_MODE, ThemeMode.ALABASTER_LIGHT.key)),
            lastApiEventSummary = readApiSummaryFromPrefs()
        )
    )

    val uiState: StateFlow<TesseraUiState> = combine(
        repository.allHabitsWithLogs,
        controlState
    ) { allHabits, ctrl ->
        val active = allHabits.filter { !it.habit.isArchived }
        val archived = allHabits.filter { it.habit.isArchived }

        val distinctCategories = active
            .map { it.habit.category.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }

        val categoryPills = buildList {
            add("ALL")
            addAll(distinctCategories)
            if (archived.isNotEmpty() || ctrl.selectedCategory.equals("ARCHIVED", ignoreCase = true)) {
                add("ARCHIVED")
            }
        }

        val effectiveCategory = if (categoryPills.any { it.equals(ctrl.selectedCategory, ignoreCase = true) }) {
            ctrl.selectedCategory
        } else {
            "ALL"
        }

        val filtered = when {
            effectiveCategory.equals("ALL", ignoreCase = true) -> active
            effectiveCategory.equals("ARCHIVED", ignoreCase = true) -> archived
            else -> active.filter { it.habit.category.equals(effectiveCategory, ignoreCase = true) }
        }

        TesseraUiState(
            allHabits = allHabits,
            activeHabits = active,
            archivedHabits = archived,
            filteredHabits = filtered,
            categories = categoryPills,
            selectedCategory = effectiveCategory,
            isCompactBentoView = ctrl.isCompactBentoView,
            themeMode = ctrl.themeMode,
            selectedHabitId = ctrl.selectedHabitId,
            isEditorOpen = ctrl.isEditorOpen,
            editingHabit = ctrl.editingHabit,
            lastApiEventSummary = ctrl.lastApiEventSummary,
            statusMessage = ctrl.statusMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TesseraUiState(
            isCompactBentoView = controlState.value.isCompactBentoView,
            themeMode = controlState.value.themeMode,
            lastApiEventSummary = controlState.value.lastApiEventSummary
        )
    )

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
            refreshApiTelemetrySummary()
        }
    }

    private fun readApiSummaryFromPrefs(): String {
        return apiTelemetryPrefs.getString(
            HabitApiReceiver.KEY_LAST_EVENT_SUMMARY,
            "Ready · Listening on app.tessera.habit.ACTION_LOG_HABIT"
        ) ?: "Ready · Listening on app.tessera.habit.ACTION_LOG_HABIT"
    }

    fun refreshApiTelemetrySummary() {
        controlState.update { it.copy(lastApiEventSummary = readApiSummaryFromPrefs()) }
    }

    fun selectCategory(category: String) {
        controlState.update { it.copy(selectedCategory = category) }
    }

    fun toggleViewMode() {
        val next = !controlState.value.isCompactBentoView
        uiPrefs.edit().putBoolean(KEY_COMPACT_BENTO_VIEW, next).apply()
        controlState.update { it.copy(isCompactBentoView = next) }
    }

    fun setThemeMode(mode: ThemeMode) {
        uiPrefs.edit().putString(KEY_THEME_MODE, mode.key).apply()
        controlState.update { it.copy(themeMode = mode) }
    }

    fun cycleThemeMode() {
        val next = when (controlState.value.themeMode) {
            ThemeMode.ALABASTER_LIGHT -> ThemeMode.OBSIDIAN_DARK
            ThemeMode.OBSIDIAN_DARK -> ThemeMode.ALABASTER_LIGHT
            ThemeMode.SYSTEM -> ThemeMode.ALABASTER_LIGHT
        }
        setThemeMode(next)
    }

    fun openHabitDetail(habitId: Long) {
        controlState.update { it.copy(selectedHabitId = habitId) }
    }

    fun openHabitDetailBySlug(slug: String) {
        viewModelScope.launch {
            val found = repository.getHabitWithLogsBySlugOnce(slug)
            if (found != null) {
                controlState.update { it.copy(selectedHabitId = found.habit.id) }
            }
        }
    }

    fun closeHabitDetail() {
        controlState.update { it.copy(selectedHabitId = null) }
    }

    fun openCreateHabitDialog() {
        controlState.update {
            it.copy(
                isEditorOpen = true,
                editingHabit = null
            )
        }
    }

    fun openEditHabitDialog(habit: HabitEntity) {
        controlState.update {
            it.copy(
                isEditorOpen = true,
                editingHabit = habit
            )
        }
    }

    fun closeHabitEditor() {
        controlState.update {
            it.copy(
                isEditorOpen = false,
                editingHabit = null
            )
        }
    }

    fun saveHabit(habit: HabitEntity) {
        viewModelScope.launch {
            val isNew = habit.id == 0L
            repository.saveHabit(habit)
            controlState.update {
                it.copy(
                    isEditorOpen = false,
                    editingHabit = null,
                    statusMessage = if (isNew) {
                        "Created ${habit.title}"
                    } else {
                        "Updated ${habit.title}"
                    }
                )
            }
        }
    }

    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
            controlState.update {
                it.copy(
                    selectedHabitId = if (it.selectedHabitId == habitId) null else it.selectedHabitId,
                    statusMessage = "Habit deleted"
                )
            }
        }
    }

    fun toggleArchiveHabit(habit: HabitEntity) {
        viewModelScope.launch {
            val nextArchived = !habit.isArchived
            repository.setHabitArchived(habit.id, nextArchived)
            controlState.update {
                it.copy(
                    statusMessage = if (nextArchived) {
                        "Archived ${habit.title}"
                    } else {
                        "Restored ${habit.title}"
                    }
                )
            }
        }
    }

    fun smartCheckIn(habitId: Long, date: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            repository.smartCheckIn(habitId = habitId, date = date)
        }
    }

    fun setHabitDateCountAndNote(
        habitId: Long,
        date: LocalDate,
        count: Int,
        note: String
    ) {
        viewModelScope.launch {
            repository.setHabitCount(
                habitId = habitId,
                date = date,
                count = count,
                note = note
            )
        }
    }

    /**
     * Sends an actual Android Broadcast Intent to [HabitApiReceiver] so the developer sandbox
     * tests the real exported BroadcastReceiver + Room + Home Screen Widget update pipeline.
     */
    fun fireLiveBroadcastTest(
        slug: String,
        mode: String = HabitApiContract.MODE_INCREMENT,
        delta: Int = 1,
        count: Int? = null,
        dateIso: String = LocalDate.now().toString(),
        note: String = "Logged via API"
    ) {
        viewModelScope.launch {
            val intent = HabitApiContract.buildBroadcastIntent(
                context = appContext,
                slug = slug,
                mode = mode,
                delta = delta,
                count = count,
                dateIso = dateIso,
                note = note
            )
            appContext.sendBroadcast(intent)
            delay(180)
            val summary = readApiSummaryFromPrefs()
            controlState.update {
                it.copy(
                    lastApiEventSummary = summary,
                    statusMessage = "Broadcast sent for $slug ($mode)"
                )
            }
        }
    }

    fun exportJsonToString(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = backupManager.exportToJsonString()
            onResult(json)
            postMessage("Copied JSON backup to clipboard")
        }
    }

    fun exportJsonToUri(uri: Uri) {
        viewModelScope.launch {
            val result = backupManager.exportToUri(uri)
            result.onSuccess { count ->
                postMessage("Exported $count habits to JSON file")
            }.onFailure { err ->
                postMessage("Export failed: ${err.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    fun importJsonFromString(rawJson: String, replaceExisting: Boolean = false) {
        viewModelScope.launch {
            val result = backupManager.importFromJsonString(rawJson, replaceExisting)
            result.onSuccess { count ->
                postMessage("Imported $count habits from JSON")
            }.onFailure { err ->
                postMessage("Import failed: ${err.localizedMessage ?: "Invalid JSON"}")
            }
        }
    }

    fun importJsonFromUri(uri: Uri, replaceExisting: Boolean = false) {
        viewModelScope.launch {
            val result = backupManager.importFromUri(uri, replaceExisting)
            result.onSuccess { count ->
                postMessage("Imported $count habits from backup file")
            }.onFailure { err ->
                postMessage("Import failed: ${err.localizedMessage ?: "Invalid file"}")
            }
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.resetToDemoData()
            controlState.update {
                it.copy(
                    selectedCategory = "ALL",
                    selectedHabitId = null,
                    statusMessage = "Restored sample habits"
                )
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            controlState.update {
                it.copy(
                    selectedCategory = "ALL",
                    selectedHabitId = null,
                    statusMessage = "Cleared all habits and logs"
                )
            }
        }
    }

    fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        viewModelScope.launch {
            val outcome = DeepLinkHandler.processIntent(appContext, intent, repository)
            if (outcome != null) {
                val targetHabit = repository.getHabitWithLogsBySlugOnce(outcome.slugOrId)
                controlState.update {
                    it.copy(
                        selectedHabitId = if (outcome.openDetailOnly && targetHabit != null) {
                            targetHabit.habit.id
                        } else {
                            it.selectedHabitId
                        },
                        lastApiEventSummary = readApiSummaryFromPrefs(),
                        statusMessage = outcome.message
                    )
                }
            }
        }
    }

    fun postMessage(message: String) {
        controlState.update { it.copy(statusMessage = message) }
    }

    fun clearStatusMessage() {
        controlState.update { it.copy(statusMessage = null) }
    }

    companion object {
        private const val UI_PREFS_NAME = "tessera_ui_prefs"
        private const val KEY_COMPACT_BENTO_VIEW = "compact_bento_view"
        private const val KEY_THEME_MODE = "theme_mode_v2"

        fun factory(application: Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TesseraViewModel(application) as T
                }
            }
        }
    }
}
