package com.example.classschedule.domain.model

/**
 * Colour tag for a course block.
 *
 * Four light pastel colours and three deep colours form a fixed, theme-independent
 * palette so swatches always look distinct (they no longer flip to dark variants in
 * dark mode). [CUSTOM] lets the user pick an arbitrary colour, stored in
 * [Course.customColorArgb].
 */
enum class ColorTag {
    // Light pastel colours
    BLUE,
    GREEN,
    PINK,
    YELLOW,

    // Deep/saturated colours
    PURPLE,
    TEAL,
    ORANGE,

    // User-defined colour (see Course.customColorArgb)
    CUSTOM
}
