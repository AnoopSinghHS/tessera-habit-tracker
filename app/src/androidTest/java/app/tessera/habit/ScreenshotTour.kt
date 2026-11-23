package app.tessera.habit

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.provider.MediaStore
import android.view.View
import android.widget.FrameLayout
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import app.tessera.habit.data.local.HabitEntity
import app.tessera.habit.data.model.HabitWithLogs
import app.tessera.habit.widget.HabitWidgetUpdater
import app.tessera.habit.widget.WidgetPrefs
import app.tessera.habit.widget.WidgetStyle
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import kotlin.random.Random

/**
 * Not a behaviour test: seeds sample habits, walks through the main screens and saves a
 * screenshot of each to /sdcard/Download/tessera-shots, for the "UI screenshots" workflow.
 */
@RunWith(AndroidJUnit4::class)
class ScreenshotTour {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val device = UiDevice.getInstance(instrumentation)
    private val dir = "/sdcard/Download/tessera-shots"

    private class Spec(
        val slug: String,
        val title: String,
        val category: String,
        val iconKey: String,
        val target: Int,
        val unit: String,
        val todayCount: Int,
        val streakBeforeToday: Int,
        val rate: Float,
        val seed: Int,
        val identity: String = "",
        val cue: String = "",
        val micro: String = "",
        val reward: String = "",
        val colorKey: String = "emerald"
    )

    private fun seed() = runBlocking {
        val repo = (instrumentation.targetContext.applicationContext as TesseraApp).repository
        repo.clearAllData()
        val created = System.currentTimeMillis() - 150L * 24 * 3600 * 1000
        val today = LocalDate.now()
        val specs = listOf(
            Spec(
                "write-500-words", "Write 500 words", "Craft", "quill", 1, "times", 1, 22, 0.75f, 1,
                identity = "I'm someone who writes every day",
                cue = "After morning coffee",
                micro = "Write one sentence",
                reward = "Tea on the balcony",
                colorKey = "amber"
            ),
            Spec("vocal-riyaz", "Vocal riyaz", "Craft", "waveform", 1, "times", 1, 5, 0.7f, 2, colorKey = "violet"),
            Spec("log-meals", "Log meals", "Health", "bowl", 3, "meals", 2, 40, 0.8f, 3, colorKey = "emerald"),
            Spec("strength-training", "Strength training", "Health", "dumbbell", 1, "times", 0, 3, 0.55f, 4, colorKey = "rose"),
            Spec("read-20-pages", "Read 20 pages", "Mind", "book", 1, "times", 0, 0, 0.45f, 5, colorKey = "cobalt")
        )
        specs.forEachIndexed { index, s ->
            val id = repo.saveHabit(
                HabitEntity(
                    slug = s.slug,
                    title = s.title,
                    category = s.category,
                    iconKey = s.iconKey,
                    colorKey = s.colorKey,
                    targetPerDay = s.target,
                    unitLabel = s.unit,
                    cueAnchor = s.cue,
                    microStarter = s.micro,
                    identityStatement = s.identity,
                    temptationReward = s.reward,
                    sortOrder = index,
                    createdAt = created
                )
            )
            val rnd = Random(s.seed)
            if (s.todayCount > 0) repo.setHabitCount(id, today, s.todayCount)
            for (i in 1..119) {
                val done = i <= s.streakBeforeToday ||
                    (i != s.streakBeforeToday + 1 && rnd.nextFloat() < s.rate)
                if (!done) continue
                val count = if (s.target == 1 || i <= s.streakBeforeToday || rnd.nextFloat() > 0.3f) {
                    s.target
                } else {
                    1 + rnd.nextInt(s.target - 1)
                }
                repo.setHabitCount(id, today.minusDays(i.toLong()), count)
            }
        }
    }

    private fun shot(name: String) {
        device.waitForIdle()
        Thread.sleep(1500)
        device.executeShellCommand("screencap -p $dir/$name.png")
    }

    private fun clickText(text: String) {
        val obj = device.wait(Until.findObject(By.text(text)), 8000)
            ?: error("Could not find text: $text")
        obj.click()
    }

    private fun clickDesc(desc: String) {
        val obj = device.wait(Until.findObject(By.desc(desc)), 8000)
            ?: error("Could not find content description: $desc")
        obj.click()
    }

    private fun scrollUp() {
        val w = device.displayWidth
        val h = device.displayHeight
        device.swipe(w / 2, (h * 0.75).toInt(), w / 2, (h * 0.25).toInt(), 40)
    }

    // ---- Widgets: draw the real widget views off-screen and save them as PNGs ----------

    private fun savePng(name: String, bitmap: Bitmap) {
        val resolver = instrumentation.targetContext.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, "$name.png")
            put(MediaStore.Downloads.MIME_TYPE, "image/png")
            put(MediaStore.Downloads.RELATIVE_PATH, "Download/tessera-shots")
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return
        resolver.openOutputStream(uri)?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private fun drawWidget(
        widthDp: Float,
        heightDp: Float,
        widgetId: Int,
        habits: List<HabitWithLogs>
    ): Bitmap {
        val ctx = instrumentation.targetContext
        val density = ctx.resources.displayMetrics.density
        val widthPx = (widthDp * density).toInt()
        val heightPx = (heightDp * density).toInt()
        var result: Bitmap? = null
        instrumentation.runOnMainSync {
            val views = HabitWidgetUpdater.buildViews(ctx, widgetId, widthDp, heightDp, habits)
            val view = views.apply(ctx, FrameLayout(ctx))
            view.measure(
                View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(heightPx, View.MeasureSpec.EXACTLY)
            )
            view.layout(0, 0, widthPx, heightPx)
            val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            result = bitmap
        }
        return result!!
    }

    private fun sheet(rows: List<List<Bitmap>>, background: Int): Bitmap {
        val gap = 48
        val width = rows.maxOf { row -> row.sumOf { it.width } + gap * (row.size + 1) }
        val height = rows.sumOf { row -> row.maxOf { it.height } + gap } + gap
        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawColor(background)
        var y = gap
        for (row in rows) {
            var x = gap
            for (bitmap in row) {
                canvas.drawBitmap(bitmap, x.toFloat(), y.toFloat(), null)
                x += bitmap.width + gap
            }
            y += row.maxOf { it.height } + gap
        }
        return out
    }

    private fun widgetSheets() {
        val ctx = instrumentation.targetContext
        val habits = runBlocking {
            (ctx.applicationContext as TesseraApp).repository.getAllHabitsWithLogsOnce()
        }.sortedBy { it.habit.sortOrder }
        val wallpaper = 0xFF1B1450.toInt()

        fun configure(widgetId: Int, habit: HabitWithLogs, dark: Boolean) {
            WidgetPrefs.saveWidgetConfig(ctx, widgetId, habit.habit.id, dark, WidgetPrefs.TAP_MODE_SMART)
        }

        fun shapes(style: WidgetStyle, dark: Boolean, name: String) {
            WidgetPrefs.setStyle(ctx, style)
            configure(9000, habits[0], dark)
            val sizes = listOf(
                158f to 158f, 158f to 236f, 330f to 158f, 244f to 158f, 330f to 236f
            )
            val bitmaps = sizes.map { (w, h) -> drawWidget(w, h, 9000, habits) }
            savePng(name, sheet(listOf(bitmaps.take(3), bitmaps.drop(3)), wallpaper))
        }

        fun habitsRow(style: WidgetStyle, dark: Boolean, name: String) {
            WidgetPrefs.setStyle(ctx, style)
            val bitmaps = habits.mapIndexed { i, habit ->
                configure(9100 + i, habit, dark)
                drawWidget(158f, 158f, 9100 + i, habits)
            }
            savePng(name, sheet(listOf(bitmaps.take(3), bitmaps.drop(3)), wallpaper))
        }

        shapes(WidgetStyle.WASH, true, "w1-shapes-wash-dark")
        shapes(WidgetStyle.TINTED, true, "w2-shapes-tinted-dark")
        habitsRow(WidgetStyle.WASH, true, "w3-habits-wash-dark")
        habitsRow(WidgetStyle.WASH, false, "w4-habits-wash-light")
        habitsRow(WidgetStyle.TINTED, false, "w5-habits-tinted-light")
        WidgetPrefs.setStyle(ctx, WidgetStyle.Default)
    }

    @Test
    fun tour() {
        device.executeShellCommand("mkdir -p $dir")
        // Fresh install: the app should open with the owner's own starter habits, not demo data.
        ActivityScenario.launch(MainActivity::class.java).use {
            device.wait(Until.findObject(By.textContains("bathroom by 6:30")), 15000)
            shot("00-starter-habits")
        }
        seed()
        widgetSheets()
        ActivityScenario.launch(MainActivity::class.java).use {
            device.wait(Until.findObject(By.text("Write 500 words")), 15000)
            shot("01-today")

            clickDesc("Show as grid")
            shot("02-today-grid")
            clickDesc("Show as list")

            clickText("Write 500 words")
            shot("03-detail-top")
            scrollUp()
            shot("04-detail-middle")
            scrollUp()
            shot("05-detail-bottom")
            device.pressBack()

            clickDesc("Add a habit")
            shot("06-editor")
            scrollUp()
            shot("07-editor-lower")
            device.pressBack()

            clickText("Progress")
            shot("08-progress")
            scrollUp()
            shot("09-progress-lower")

            clickText("Settings")
            shot("10-settings")
            scrollUp()
            shot("11-settings-2")
            scrollUp()
            shot("12-settings-3")
        }
    }
}
