package com.rmstu.brsdd.ui.theme

import androidx.compose.ui.graphics.Color

val BrsddGreen = Color(0xFF1B5E3C)
val BrsddGreenDark = Color(0xFF0D2E1D)
val BrsddGreenLight = Color(0xFF2E8B57)
val BrsddAmber = Color(0xFFF2A900)
val BrsddInk = Color(0xFF0E1512)
val BrsddSurface = Color(0xFF152820)
val BrsddSurfaceVariant = Color(0xFF1E332A)
val BrsddOnSurface = Color(0xFFE7F3EC)
val BrsddMuted = Color(0xFF8FAE9E)
val BrsddDanger = Color(0xFFE05555)
val BrsddInfoBlue = Color(0xFF4FA5D8)

/** Deterministic palette used to color-code bounding boxes per class id. */
val ClassPalette = listOf(
    Color(0xFFF2A900), Color(0xFF4FA5D8), Color(0xFFE05555), Color(0xFF2E8B57),
    Color(0xFFB388FF), Color(0xFFFF8A65), Color(0xFF80DEEA), Color(0xFFFFD54F),
    Color(0xFFA1887F), Color(0xFF90A4AE), Color(0xFFAED581), Color(0xFFF06292),
    Color(0xFF64B5F6), Color(0xFFFFB74D), Color(0xFF9575CD), Color(0xFF4DB6AC),
    Color(0xFFDCE775), Color(0xFFE57373), Color(0xFF7986CB), Color(0xFF4DD0E1),
    Color(0xFFFFF176), Color(0xFF81C784), Color(0xFFBA68C8), Color(0xFF4FC3F7),
    Color(0xFFFF8A80), Color(0xFFA5D6A7), Color(0xFFFFCC80), Color(0xFF9FA8DA),
    Color(0xFF80CBC4),
)

fun colorForClass(classId: Int): Color = ClassPalette[classId % ClassPalette.size]
