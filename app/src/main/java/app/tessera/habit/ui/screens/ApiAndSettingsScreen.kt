package app.tessera.habit.ui.screens

import android.appwidget.AppWidgetManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import app.tessera.habit.BuildConfig
import app.tessera.habit.api.HabitApiContract
import app.tessera.habit.api.HabitApiReceiver
import app.tessera.habit.ui.components.SectionHeading
import app.tessera.habit.ui.components.TesseraCard
import app.tessera.habit.ui.components.TesseraChip
import app.tessera.habit.ui.components.TesseraDimens
import app.tessera.habit.ui.components.TesseraIconButton
import app.tessera.habit.ui.components.TesseraSecondaryButton
import app.tessera.habit.ui.components.tesseraTextFieldColors
import app.tessera.habit.ui.theme.TesseraTheme
import app.tessera.habit.ui.theme.TesseraType
import app.tessera.habit.ui.viewmodel.TesseraUiState
import app.tessera.habit.widget.HabitCompactWidgetProvider
import app.tessera.habit.widget.HabitHeatmapWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate

private const val LATEST_RELEASE_API_URL =
    "https://api.github.com/repos/AnoopSinghHS/tessera-habit-tracker/releases/latest"
private const val LATEST_RELEASE_PAGE_URL =
    "https://github.com/AnoopSinghHS/tessera-habit-tracker/releases/latest"

// File name of the release APK that CI attaches to each GitHub release.
private const val RELEASE_APK_ASSET_NAME = "tessera-s26-ultra-release.apk"

private const val TEST_NOTE = "Test from Settings"

private class LatestRelease(val tag: String, val downloadUrl: String)

private sealed interface UpdateStatus {
    data object NotChecked : UpdateStatus
    data object Checking : UpdateStatus
    data object UpToDate : UpdateStatus
    data class Available(val version: String, val downloadUrl: String) : UpdateStatus
    data class UnknownTag(val tag: String, val downloadUrl: String) : UpdateStatus
    data object Failed : UpdateStatus
}

/**
 * Splits "v2.1.0" or "2.1.0-beta" into [2, 1, 0]. Returns null when the text is not a
 * dotted list of numbers.
 */
internal fun parseVersionParts(text: String): List<Int>? {
    val core = text.trim()
        .removePrefix("v")
        .removePrefix("V")
        .substringBefore('-')
        .substringBefore('+')
    if (core.isEmpty()) return null
    val parts = ArrayList<Int>()
    for (piece in core.split('.')) {
        val number = piece.toIntOrNull() ?: return null
        if (number < 0) return null
        parts.add(number)
    }
    return parts
}

/**
 * True when [latestTag] (for example "v2.1.0") is a higher version than [installed]
 * (for example "2.0.0"). Compares part by part as numbers; missing parts count as 0.
 * False when either one cannot be parsed.
 */
internal fun isNewerVersion(latestTag: String, installed: String): Boolean {
    val latest = parseVersionParts(latestTag) ?: return false
    val current = parseVersionParts(installed) ?: return false
    for (i in 0 until maxOf(latest.size, current.size)) {
        val a = latest.getOrElse(i) { 0 }
        val b = current.getOrElse(i) { 0 }
        if (a != b) return a > b
    }
    return false
}

private suspend fun fetchLatestRelease(): Result<LatestRelease> = withContext(Dispatchers.IO) {
    runCatching {
        val conn = (URL(LATEST_RELEASE_API_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Accept", "application/vnd.github.v3+json")
            connectTimeout = 8000
            readTimeout = 8000
        }
        try {
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            val tag = json.optString("tag_name", "").trim()
            if (tag.isEmpty()) error("Release has no tag")
            var downloadUrl = json.optString("html_url", "").ifBlank { LATEST_RELEASE_PAGE_URL }
            val assets = json.optJSONArray("assets")
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    if (asset.optString("name") == RELEASE_APK_ASSET_NAME) {
                        downloadUrl = asset.optString("browser_download_url", "").ifBlank { downloadUrl }
                        break
                    }
                }
            }
            LatestRelease(tag = tag, downloadUrl = downloadUrl)
        } finally {
            conn.disconnect()
        }
    }
}

@Composable
fun ApiAndSettingsScreen(
    uiState: TesseraUiState,
    onFireLiveBroadcast: (slug: String, mode: String, delta: Int, count: Int?, note: String) -> Unit,
    onExportJsonToClipboard: () -> Unit,
    onExportJsonToUri: (Uri) -> Unit,
    onImportJsonFromUri: (Uri) -> Unit,
    onImportJsonFromString: (String) -> Unit,
    onClearAllData: () -> Unit,
    onPostMessage: (String) -> Unit
) {
    val colors = TesseraTheme.colors
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showPasteDialog by remember { mutableStateOf(false) }
    var pasteDraft by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var updateStatus by remember { mutableStateOf<UpdateStatus>(UpdateStatus.NotChecked) }
    var pickedHabitId by remember { mutableStateOf<Long?>(null) }

    val createDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            onExportJsonToUri(uri)
        }
    }

    val openDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onImportJsonFromUri(uri)
        }
    }

    if (showPasteDialog) {
        AlertDialog(
            onDismissRequest = { showPasteDialog = false },
            containerColor = colors.surfaceCard,
            shape = RoundedCornerShape(TesseraDimens.RadiusCard),
            title = {
                Text("Paste backup text", style = TesseraType.Title, color = colors.textPrimary)
            },
            text = {
                OutlinedTextField(
                    value = pasteDraft,
                    onValueChange = { pasteDraft = it },
                    placeholder = {
                        Text("Paste the backup text here", style = TesseraType.Body)
                    },
                    textStyle = TesseraType.Code,
                    colors = tesseraTextFieldColors(),
                    shape = RoundedCornerShape(TesseraDimens.RadiusControl),
                    minLines = 6,
                    maxLines = 12,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                DialogButton(
                    text = "Load backup",
                    color = colors.accent,
                    onClick = {
                        showPasteDialog = false
                        if (pasteDraft.isNotBlank()) {
                            onImportJsonFromString(pasteDraft)
                            pasteDraft = ""
                        }
                    }
                )
            },
            dismissButton = {
                DialogButton(
                    text = "Cancel",
                    color = colors.textSecondary,
                    onClick = { showPasteDialog = false }
                )
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = colors.surfaceCard,
            shape = RoundedCornerShape(TesseraDimens.RadiusCard),
            title = {
                Text(
                    "Delete all habits and history?",
                    style = TesseraType.Title,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    "This removes every habit and its history from this phone. It can't be " +
                        "undone. Save a backup first if you might want them back.",
                    style = TesseraType.Body,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                DialogButton(
                    text = "Delete everything",
                    color = colors.statusDanger,
                    onClick = {
                        showDeleteDialog = false
                        onClearAllData()
                    }
                )
            },
            dismissButton = {
                DialogButton(
                    text = "Cancel",
                    color = colors.textSecondary,
                    onClick = { showDeleteDialog = false }
                )
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surfaceAbyss)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = TesseraDimens.ScreenPadding)
    ) {
        Spacer(modifier = Modifier.height(TesseraDimens.SpaceL))
        Text(
            text = "Settings",
            style = TesseraType.Headline,
            color = colors.textPrimary,
            modifier = Modifier.padding(horizontal = TesseraDimens.SpaceXs)
        )

        // Widgets
        SettingsSection(heading = "Widgets") {
            Text(
                text = "Put a habit's heatmap on your home screen and check in without opening the app.",
                style = TesseraType.Body,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(TesseraDimens.SpaceL))
            TesseraSecondaryButton(
                text = "Add large widget (4×2)",
                onClick = {
                    requestPinWidget(
                        context = context,
                        providerClass = HabitHeatmapWidgetProvider::class.java,
                        onFeedback = onPostMessage
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(TesseraDimens.SpaceS))
            TesseraSecondaryButton(
                text = "Add small widget (2×2)",
                onClick = {
                    requestPinWidget(
                        context = context,
                        providerClass = HabitCompactWidgetProvider::class.java,
                        onFeedback = onPostMessage
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Backup
        SettingsSection(heading = "Backup") {
            TesseraSecondaryButton(
                text = "Save backup to a file",
                onClick = { createDocLauncher.launch("tessera-backup-${LocalDate.now()}.json") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(TesseraDimens.SpaceS))
            TesseraSecondaryButton(
                text = "Load backup from a file",
                onClick = {
                    openDocLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(TesseraDimens.SpaceS))
            TesseraSecondaryButton(
                text = "Copy backup as text",
                onClick = onExportJsonToClipboard,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(TesseraDimens.SpaceS))
            TesseraSecondaryButton(
                text = "Paste backup text",
                onClick = { showPasteDialog = true },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(TesseraDimens.SpaceL))
            Text(
                text = "Loading a backup adds to what's here. If a habit has the same " +
                    "automation ID as one in the backup, the backup's details and its " +
                    "days replace yours; days the backup doesn't mention are kept.",
                style = TesseraType.Supporting,
                color = colors.textSecondary
            )
        }

        // Updates
        SettingsSection(heading = "Updates") {
            Text(
                text = "Installed version ${BuildConfig.VERSION_NAME}",
                style = TesseraType.Body,
                color = colors.textPrimary
            )
            val status = updateStatus
            val statusLine: String? = when (status) {
                UpdateStatus.NotChecked -> null
                UpdateStatus.Checking -> "Checking…"
                UpdateStatus.UpToDate -> "You're up to date"
                is UpdateStatus.Available -> "Version ${status.version} is available"
                is UpdateStatus.UnknownTag -> "Latest release: ${status.tag}"
                UpdateStatus.Failed -> "Couldn't check for updates. Check your connection."
            }
            if (statusLine != null) {
                Spacer(modifier = Modifier.height(TesseraDimens.SpaceXs))
                Text(
                    text = statusLine,
                    style = TesseraType.BodyStrong,
                    color = when (status) {
                        is UpdateStatus.Available -> colors.accent
                        UpdateStatus.Failed -> colors.statusWarning
                        else -> colors.textPrimary
                    }
                )
            }
            Spacer(modifier = Modifier.height(TesseraDimens.SpaceL))
            TesseraSecondaryButton(
                text = "Check for updates",
                enabled = status != UpdateStatus.Checking,
                onClick = {
                    updateStatus = UpdateStatus.Checking
                    coroutineScope.launch {
                        val result = fetchLatestRelease()
                        val release = result.getOrNull()
                        updateStatus = when {
                            release == null -> UpdateStatus.Failed
                            else -> {
                                val latestParts = parseVersionParts(release.tag)
                                val installedParts = parseVersionParts(BuildConfig.VERSION_NAME)
                                when {
                                    latestParts == null || installedParts == null ->
                                        UpdateStatus.UnknownTag(release.tag, release.downloadUrl)
                                    isNewerVersion(release.tag, BuildConfig.VERSION_NAME) ->
                                        UpdateStatus.Available(
                                            version = latestParts.joinToString("."),
                                            downloadUrl = release.downloadUrl
                                        )
                                    else -> UpdateStatus.UpToDate
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            val downloadUrl: String? = when (status) {
                is UpdateStatus.Available -> status.downloadUrl
                is UpdateStatus.UnknownTag -> status.downloadUrl
                else -> null
            }
            if (downloadUrl != null) {
                Spacer(modifier = Modifier.height(TesseraDimens.SpaceS))
                TesseraSecondaryButton(
                    text = "Download",
                    onClick = {
                        runCatching {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        }.onFailure {
                            onPostMessage("Couldn't open the download link")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(TesseraDimens.SpaceL))
            Text(
                text = "If the installer refuses to update over the existing app, save a " +
                    "backup first, uninstall, then install the new version and load the backup.",
                style = TesseraType.Supporting,
                color = colors.textSecondary
            )
        }

        // Automation
        SettingsSection(heading = "Automation") {
            Text(
                text = "Other apps (Tasker, Samsung Routines, NFC tags) can log a habit for you.",
                style = TesseraType.Body,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(TesseraDimens.SpaceL))
            Text(text = "Last event", style = TesseraType.Label, color = colors.textPrimary)
            Spacer(modifier = Modifier.height(TesseraDimens.SpaceXs))
            Text(
                text = uiState.lastApiEventSummary,
                style = TesseraType.Supporting,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(TesseraDimens.SpaceL))

            val pickedHabit = uiState.activeHabits.firstOrNull { it.habit.id == pickedHabitId }
                ?: uiState.activeHabits.firstOrNull()

            if (pickedHabit == null) {
                Text(
                    text = "Add a habit to see the commands for it.",
                    style = TesseraType.Body,
                    color = colors.textSecondary
                )
            } else {
                val slug = pickedHabit.habit.slug

                Text(
                    text = "Show examples for",
                    style = TesseraType.Label,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(TesseraDimens.SpaceS))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(TesseraDimens.SpaceS)
                ) {
                    uiState.activeHabits.forEach { item ->
                        TesseraChip(
                            text = item.habit.title,
                            selected = item.habit.id == pickedHabit.habit.id,
                            onClick = { pickedHabitId = item.habit.id },
                            modifier = Modifier.heightIn(min = TesseraDimens.TouchTarget)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(TesseraDimens.SpaceM))

                val deepLink = remember(slug) {
                    HabitApiContract.buildLogDeepLinkUri(slug = slug).toString()
                }
                val adbCommand = remember(slug) { buildAdbExample(slug) }
                val kotlinSnippet = remember(slug) { buildKotlinBroadcastExample(slug) }

                CopyableExample(
                    label = "Deep link",
                    code = deepLink,
                    onCopy = {
                        copyText(context, "Tessera deep link", deepLink)
                        onPostMessage("Copied the deep link")
                    }
                )
                Spacer(modifier = Modifier.height(TesseraDimens.SpaceS))
                CopyableExample(
                    label = "ADB command",
                    code = adbCommand,
                    onCopy = {
                        copyText(context, "Tessera ADB command", adbCommand)
                        onPostMessage("Copied the ADB command")
                    }
                )
                Spacer(modifier = Modifier.height(TesseraDimens.SpaceS))
                CopyableExample(
                    label = "Broadcast (Kotlin)",
                    code = kotlinSnippet,
                    onCopy = {
                        copyText(context, "Tessera broadcast code", kotlinSnippet)
                        onPostMessage("Copied the Kotlin code")
                    }
                )
                Spacer(modifier = Modifier.height(TesseraDimens.SpaceL))
                TesseraSecondaryButton(
                    text = "Send a test",
                    onClick = {
                        onFireLiveBroadcast(
                            slug,
                            HabitApiContract.MODE_INCREMENT,
                            1,
                            null,
                            TEST_NOTE
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(TesseraDimens.SpaceS))
                Text(
                    text = "Sending a test adds one check-in to ${pickedHabit.habit.title} today.",
                    style = TesseraType.Supporting,
                    color = colors.textSecondary
                )
            }
        }

        // Delete everything
        SettingsSection(heading = "Delete everything") {
            Text(
                text = "Removes every habit and its history from this phone.",
                style = TesseraType.Body,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(TesseraDimens.SpaceL))
            TesseraSecondaryButton(
                text = "Delete all habits and history",
                onClick = { showDeleteDialog = true },
                danger = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // About
        SettingsSection(heading = "About") {
            Text(text = "Tessera", style = TesseraType.BodyStrong, color = colors.textPrimary)
            Text(
                text = "Version ${BuildConfig.VERSION_NAME}",
                style = TesseraType.Body,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(TesseraDimens.SpaceS))
            Text(
                text = "Fonts: Fraunces and Libre Franklin, SIL Open Font License.",
                style = TesseraType.Supporting,
                color = colors.textSecondary
            )
        }

        // Clears the bottom bar.
        Spacer(modifier = Modifier.height(TesseraDimens.SpaceXxl * 3))
    }
}

@Composable
private fun SettingsSection(
    heading: String,
    content: @Composable () -> Unit
) {
    Spacer(modifier = Modifier.height(TesseraDimens.SpaceXl))
    SectionHeading(text = heading)
    Spacer(modifier = Modifier.height(TesseraDimens.SpaceS))
    TesseraCard {
        content()
    }
}

@Composable
private fun CopyableExample(
    label: String,
    code: String,
    onCopy: () -> Unit
) {
    val colors = TesseraTheme.colors
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = TesseraType.Label,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            TesseraIconButton(
                icon = Icons.Rounded.ContentCopy,
                contentDescription = "Copy: $label",
                onClick = onCopy,
                filled = false
            )
        }
        Text(
            text = code,
            style = TesseraType.Code,
            color = colors.textPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(TesseraDimens.RadiusControl))
                .background(colors.surfaceElevated)
                .padding(TesseraDimens.SpaceM)
        )
    }
}

@Composable
private fun DialogButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.heightIn(min = TesseraDimens.TouchTarget)
    ) {
        Text(text = text, style = TesseraType.Label, color = color)
    }
}

/**
 * The contract's ADB command, minus its fixed date. The receiver logs to today when no
 * date is sent, which is what a saved command should do.
 */
private fun buildAdbExample(slug: String): String {
    val today = LocalDate.now().toString()
    return HabitApiContract.buildAdbBroadcastCommand(
        slug = slug,
        dateIso = today,
        note = "Automated"
    ).replace(" --es ${HabitApiContract.EXTRA_DATE} \"$today\"", "")
}

/**
 * Kotlin another app can paste. It names the receiver class explicitly, as
 * [HabitApiContract.buildBroadcastIntent] does, so the broadcast is delivered to the
 * manifest-registered receiver on Android 8 and later.
 */
private fun buildKotlinBroadcastExample(slug: String): String {
    val q = "\""
    return buildString {
        append("val intent = Intent(").append(q).append(HabitApiContract.ACTION_LOG_HABIT)
            .append(q).append(").apply {\n")
        append("    setClassName(").append(q).append(HabitApiContract.PACKAGE_NAME).append(q)
            .append(", ").append(q).append(HabitApiReceiver::class.java.name).append(q)
            .append(")\n")
        append("    putExtra(").append(q).append(HabitApiContract.EXTRA_SLUG).append(q)
            .append(", ").append(q).append(slug).append(q).append(")\n")
        append("    putExtra(").append(q).append(HabitApiContract.EXTRA_MODE).append(q)
            .append(", ").append(q).append(HabitApiContract.MODE_INCREMENT).append(q)
            .append(")\n")
        append("    putExtra(").append(q).append(HabitApiContract.EXTRA_DELTA).append(q)
            .append(", 1)\n")
        append("}\n")
        append("context.sendBroadcast(intent)")
    }
}

private fun copyText(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
}

private fun requestPinWidget(
    context: Context,
    providerClass: Class<*>,
    onFeedback: (String) -> Unit
) {
    val fallback = "Long-press the home screen, then Widgets, then Tessera to place the widget"
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val provider = ComponentName(context, providerClass)
    if (appWidgetManager.isRequestPinAppWidgetSupported) {
        val requested = appWidgetManager.requestPinAppWidget(provider, null, null)
        if (requested) {
            onFeedback("Confirm the widget on your home screen")
        } else {
            onFeedback(fallback)
        }
    } else {
        onFeedback(fallback)
    }
}
