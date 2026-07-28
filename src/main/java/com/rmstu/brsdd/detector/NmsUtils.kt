package com.rmstu.brsdd.detector

import kotlin.math.max
import kotlin.math.min

object NmsUtils {

    private fun iou(a: Detection, b: Detection): Float {
        val interLeft = max(a.left, b.left)
        val interTop = max(a.top, b.top)
        val interRight = min(a.right, b.right)
        val interBottom = min(a.bottom, b.bottom)

        val interW = max(0f, interRight - interLeft)
        val interH = max(0f, interBottom - interTop)
        val interArea = interW * interH

        val areaA = max(0f, a.right - a.left) * max(0f, a.bottom - a.top)
        val areaB = max(0f, b.right - b.left) * max(0f, b.bottom - b.top)
        val union = areaA + areaB - interArea

        return if (union <= 0f) 0f else interArea / union
    }

    /**
     * Greedy non-maximum suppression, applied independently per class so that
     * overlapping signs of different classes are never suppressed against each other.
     */
    fun nms(detections: List<Detection>, iouThreshold: Float): List<Detection> {
        val result = mutableListOf<Detection>()
        val byClass = detections.groupBy { it.classId }

        for ((_, group) in byClass) {
            val sorted = group.sortedByDescending { it.confidence }.toMutableList()
            while (sorted.isNotEmpty()) {
                val best = sorted.removeAt(0)
                result.add(best)
                sorted.removeAll { candidate -> iou(best, candidate) > iouThreshold }
            }
        }
        return result.sortedByDescending { it.confidence }
    }
}
