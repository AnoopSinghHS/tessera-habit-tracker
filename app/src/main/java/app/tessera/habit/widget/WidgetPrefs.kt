package app.tessera.habit.widget

import android.content.Context

/**
 * Per-instance SharedPreferences storage for Tessera Home Screen Widgets.
 * Tracks bound habit ID, surface theme ("dark" | "light"), and check-in tap mode ("smart" | "increment").
 */
object WidgetPrefs {

    private const val PREFS_NAME = "tessera_widget_prefs"
    private const val KEY_HABIT_ID_PREFIX = "widget_habit_id_"
    private const val KEY_THEME_PREFIX = "widget_theme_"
    private const val KEY_TAP_MODE_PREFIX = "widget_tap_mode_"

    const val THEME_DARK = "dark"
    const val THEME_LIGHT = "light"

    const val TAP_MODE_SMART = "smart"
    const val TAP_MODE_INCREMENT = "increment"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getHabitId(context: Context, appWidgetId: Int): Long {
        return prefs(context).getLong("$KEY_HABIT_ID_PREFIX$appWidgetId", -1L)
    }

    fun setHabitId(context: Context, appWidgetId: Int, habitId: Long) {
        prefs(context).edit()
            .putLong("$KEY_HABIT_ID_PREFIX$appWidgetId", habitId)
            .apply()
    }

    fun getThemeMode(context: Context, appWidgetId: Int): String {
        return prefs(context).getString("$KEY_THEME_PREFIX$appWidgetId", THEME_DARK) ?: THEME_DARK
    }

    fun isDarkTheme(context: Context, appWidgetId: Int): Boolean {
        return getThemeMode(context, appWidgetId) != THEME_LIGHT
    }

    fun setThemeMode(context: Context, appWidgetId: Int, themeMode: String) {
        prefs(context).edit()
            .putString("$KEY_THEME_PREFIX$appWidgetId", themeMode)
            .apply()
    }

    fun getTapMode(context: Context, appWidgetId: Int): String {
        return prefs(context).getString("$KEY_TAP_MODE_PREFIX$appWidgetId", TAP_MODE_SMART)
            ?: TAP_MODE_SMART
    }

    fun setTapMode(context: Context, appWidgetId: Int, tapMode: String) {
        prefs(context).edit()
            .putString("$KEY_TAP_MODE_PREFIX$appWidgetId", tapMode)
            .apply()
    }

    fun saveWidgetConfig(
        context: Context,
        appWidgetId: Int,
        habitId: Long,
        isDark: Boolean,
        tapMode: String
    ) {
        prefs(context).edit()
            .putLong("$KEY_HABIT_ID_PREFIX$appWidgetId", habitId)
            .putString("$KEY_THEME_PREFIX$appWidgetId", if (isDark) THEME_DARK else THEME_LIGHT)
            .putString("$KEY_TAP_MODE_PREFIX$appWidgetId", tapMode)
            .apply()
    }

    fun clearWidgetConfig(context: Context, appWidgetId: Int) {
        prefs(context).edit()
            .remove("$KEY_HABIT_ID_PREFIX$appWidgetId")
            .remove("$KEY_THEME_PREFIX$appWidgetId")
            .remove("$KEY_TAP_MODE_PREFIX$appWidgetId")
            .apply()
    }
}
