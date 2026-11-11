package app.tessera.habit

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import app.tessera.habit.data.local.HabitEntity
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
        val reward: String = ""
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
                reward = "Tea on the balcony"
            ),
            Spec("vocal-riyaz", "Vocal riyaz", "Craft", "waveform", 1, "times", 1, 5, 0.7f, 2),
            Spec("log-meals", "Log meals", "Health", "bowl", 3, "meals", 2, 40, 0.8f, 3),
            Spec("strength-training", "Strength training", "Health", "dumbbell", 1, "times", 0, 3, 0.55f, 4),
            Spec("read-20-pages", "Read 20 pages", "Mind", "book", 1, "times", 0, 0, 0.45f, 5)
        )
        specs.forEachIndexed { index, s ->
            val id = repo.saveHabit(
                HabitEntity(
                    slug = s.slug,
                    title = s.title,
                    category = s.category,
                    iconKey = s.iconKey,
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

    @Test
    fun tour() {
        seed()
        device.executeShellCommand("mkdir -p $dir")
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
