package app.tessera.habit.widget

/**
 * How a widget uses the habit's colour. The same choice applies to every widget and is
 * changed in Settings.
 *
 * - [TINTED]: a neutral card; the colour shows on the heatmap, the check-in button and the streak.
 * - [WASH]: the whole card is a deep tint of the colour.
 */
enum class WidgetStyle(val key: String, val label: String) {
    TINTED("tinted", "Tinted"),
    WASH("wash", "Wash");

    companion object {
        val Default = WASH

        fun fromKey(key: String?): WidgetStyle =
            entries.firstOrNull { it.key == key } ?: Default
    }
}
