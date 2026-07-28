package com.rmstu.brsdd.detector

/**
 * A single decoded detection, in the coordinate space of the ORIGINAL image
 * (already mapped back from the 640x640 letterboxed model input).
 */
data class Detection(
    val classId: Int,
    val label: String,
    val confidence: Float,
    /** Bounding box in pixel coordinates of the original image: left, top, right, bottom. */
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
)
