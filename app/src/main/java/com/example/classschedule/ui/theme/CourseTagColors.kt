package com.example.classschedule.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.example.classschedule.domain.model.ColorTag
import com.example.classschedule.domain.model.Course

data class CourseColor(
    val background: Color,
    val content: Color
)

/**
 * Fixed, theme-independent palette of 4 light pastel + 3 deep colours. Because the
 * swatches no longer flip to dark variants in dark mode, they always look distinct.
 */
private val PRESET: Map<ColorTag, CourseColor> = mapOf(
    ColorTag.BLUE to CourseColor(Color(0xFFD8E6FF), Color(0xFF2A4A9C)),
    ColorTag.GREEN to CourseColor(Color(0xFFD5F2E2), Color(0xFF147A4E)),
    ColorTag.PINK to CourseColor(Color(0xFFFFE0ED), Color(0xFFA53770)),
    ColorTag.YELLOW to CourseColor(Color(0xFFFFF2C8), Color(0xFF8A6D00)),
    ColorTag.PURPLE to CourseColor(Color(0xFF6B4FD0), Color(0xFFFFFFFF)),
    ColorTag.TEAL to CourseColor(Color(0xFF0E8C80), Color(0xFFFFFFFF)),
    ColorTag.ORANGE to CourseColor(Color(0xFFD96A1F), Color(0xFFFFFFFF))
)

/** The four light pastel tags, shown first in the picker. */
val LIGHT_TAGS: List<ColorTag> = listOf(ColorTag.BLUE, ColorTag.GREEN, ColorTag.PINK, ColorTag.YELLOW)

/** The three deep colours. */
val DARK_TAGS: List<ColorTag> = listOf(ColorTag.PURPLE, ColorTag.TEAL, ColorTag.ORANGE)

/** Swatch background colour for a preset tag (used by the colour picker). */
fun presetSwatchColor(tag: ColorTag): Color = PRESET.getValue(tag).background

/**
 * Block colours for a course, resolving the custom colour when [ColorTag.CUSTOM] is
 * set to a stored ARGB value.
 */
fun courseBlockColors(course: Course): CourseColor {
    val argb = course.customColorArgb
    return if (course.colorTag == ColorTag.CUSTOM && argb != null) {
        val bg = Color(argb.toInt())
        CourseColor(bg, autoContentColor(bg))
    } else {
        PRESET.getOrDefault(course.colorTag, PRESET.getValue(ColorTag.BLUE))
    }
}

/** Picks a readable content (glyph) colour for a given background. */
fun autoContentColor(background: Color): Color =
    if (background.luminance() > 0.5f) Color(0xFF1A1A1A) else Color.White
