package com.example.ecodot.util

import androidx.compose.runtime.Immutable

@Immutable
data class AvatarCropLayout(
    val centerX: Float,
    val centerY: Float,
    val cropDiameter: Float,
    val totalScale: Float,
    val drawnWidth: Float,
    val drawnHeight: Float,
    val clampedOffsetX: Float,
    val clampedOffsetY: Float,
    val imageLeft: Float,
    val imageTop: Float
)

@Immutable
data class SourceCropRect(
    val left: Float,
    val top: Float,
    val size: Float
)

/**
 * Pure geometry helper shared between live preview rendering and final bitmap export
 * so circular pan/zoom adjustments match pixel-for-pixel.
 */
object AvatarCropMath {

    const val MIN_ZOOM = 1f
    const val MAX_ZOOM = 5f
    const val CROP_VIEWPORT_FRACTION = 0.84f

    fun computeLayout(
        sourceWidth: Int,
        sourceHeight: Int,
        containerWidth: Float,
        containerHeight: Float,
        zoom: Float,
        offsetX: Float,
        offsetY: Float
    ): AvatarCropLayout {
        val safeSrcW = sourceWidth.coerceAtLeast(1).toFloat()
        val safeSrcH = sourceHeight.coerceAtLeast(1).toFloat()
        val safeBoxW = containerWidth.coerceAtLeast(1f)
        val safeBoxH = containerHeight.coerceAtLeast(1f)

        val cropDiameter = minOf(safeBoxW, safeBoxH) * CROP_VIEWPORT_FRACTION
        val clampedZoom = zoom.coerceIn(MIN_ZOOM, MAX_ZOOM)
        val baseScale = maxOf(cropDiameter / safeSrcW, cropDiameter / safeSrcH)
        val totalScale = baseScale * clampedZoom

        val drawnW = safeSrcW * totalScale
        val drawnH = safeSrcH * totalScale
        val maxOffsetX = ((drawnW - cropDiameter) / 2f).coerceAtLeast(0f)
        val maxOffsetY = ((drawnH - cropDiameter) / 2f).coerceAtLeast(0f)

        val clampedX = offsetX.coerceIn(-maxOffsetX, maxOffsetX)
        val clampedY = offsetY.coerceIn(-maxOffsetY, maxOffsetY)

        val centerX = safeBoxW / 2f
        val centerY = safeBoxH / 2f

        return AvatarCropLayout(
            centerX = centerX,
            centerY = centerY,
            cropDiameter = cropDiameter,
            totalScale = totalScale,
            drawnWidth = drawnW,
            drawnHeight = drawnH,
            clampedOffsetX = clampedX,
            clampedOffsetY = clampedY,
            imageLeft = centerX + clampedX - drawnW / 2f,
            imageTop = centerY + clampedY - drawnH / 2f
        )
    }

    fun computeSourceCropRect(
        sourceWidth: Int,
        sourceHeight: Int,
        containerWidth: Float,
        containerHeight: Float,
        zoom: Float,
        offsetX: Float,
        offsetY: Float
    ): SourceCropRect {
        val layout = computeLayout(
            sourceWidth = sourceWidth,
            sourceHeight = sourceHeight,
            containerWidth = containerWidth,
            containerHeight = containerHeight,
            zoom = zoom,
            offsetX = offsetX,
            offsetY = offsetY
        )
        val srcW = sourceWidth.coerceAtLeast(1).toFloat()
        val srcH = sourceHeight.coerceAtLeast(1).toFloat()
        val srcSize = (layout.cropDiameter / layout.totalScale)
            .coerceIn(1f, minOf(srcW, srcH))

        val rawLeft = ((srcW - srcSize) / 2f) - (layout.clampedOffsetX / layout.totalScale)
        val rawTop = ((srcH - srcSize) / 2f) - (layout.clampedOffsetY / layout.totalScale)

        return SourceCropRect(
            left = rawLeft.coerceIn(0f, (srcW - srcSize).coerceAtLeast(0f)),
            top = rawTop.coerceIn(0f, (srcH - srcSize).coerceAtLeast(0f)),
            size = srcSize
        )
    }
}
