package com.example.ecodot.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Premium Material Symbols Rounded icons extracted and curated from SimpMusic.
 * opsz 24 / wght 400 / GRAD 0 / ROND 50.
 */
object SimpIcons

// ── Shuffle ──────────────────────────────────────────────────────────────────
val SimpIcons.Shuffle: ImageVector
    get() {
        if (_Shuffle != null) return _Shuffle!!
        _Shuffle = ImageVector.Builder(
            name = "Shuffle",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(15f, 20f)
                quadToRelative(-0.42f, 0f, -0.71f, -0.29f)
                quadTo(14f, 19.43f, 14f, 19f)
                reflectiveQuadToRelative(0.29f, -0.71f)
                reflectiveQuadTo(15f, 18f)
                horizontalLineToRelative(1.6f)
                lineTo(14.13f, 15.53f)
                quadToRelative(-0.3f, -0.3f, -0.29f, -0.71f)
                reflectiveQuadTo(14.15f, 14.1f)
                reflectiveQuadToRelative(0.71f, -0.3f)
                reflectiveQuadToRelative(0.71f, 0.3f)
                lineTo(18f, 16.55f)
                verticalLineTo(15f)
                quadToRelative(0f, -0.43f, 0.29f, -0.71f)
                reflectiveQuadTo(19f, 14f)
                reflectiveQuadToRelative(0.71f, 0.29f)
                reflectiveQuadTo(20f, 15f)
                verticalLineToRelative(4f)
                quadToRelative(0f, 0.43f, -0.29f, 0.71f)
                reflectiveQuadTo(19f, 20f)
                horizontalLineTo(15f)
                close()
                moveTo(4.3f, 19.7f)
                quadTo(4.03f, 19.43f, 4.03f, 19f)
                reflectiveQuadTo(4.3f, 18.3f)
                lineTo(16.6f, 6f)
                horizontalLineTo(15f)
                quadTo(14.58f, 6f, 14.29f, 5.71f)
                quadTo(14f, 5.43f, 14f, 5f)
                reflectiveQuadTo(14.29f, 4.29f)
                reflectiveQuadTo(15f, 4f)
                horizontalLineToRelative(4f)
                quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                reflectiveQuadTo(20f, 5f)
                verticalLineTo(9f)
                quadToRelative(0f, 0.42f, -0.29f, 0.71f)
                reflectiveQuadTo(19f, 10f)
                reflectiveQuadTo(18.29f, 9.71f)
                reflectiveQuadTo(18f, 9f)
                verticalLineTo(7.4f)
                lineTo(5.7f, 19.7f)
                quadTo(5.43f, 19.98f, 5f, 19.98f)
                quadToRelative(-0.42f, 0f, -0.7f, -0.28f)
                close()
                moveTo(4.28f, 5.7f)
                quadTo(4f, 5.43f, 4f, 5f)
                reflectiveQuadTo(4.28f, 4.3f)
                reflectiveQuadTo(4.96f, 4.02f)
                reflectiveQuadTo(5.68f, 4.3f)
                lineToRelative(4.2f, 4.18f)
                quadToRelative(0.28f, 0.28f, 0.29f, 0.69f)
                reflectiveQuadTo(9.88f, 9.88f)
                quadTo(9.6f, 10.15f, 9.18f, 10.15f)
                reflectiveQuadTo(8.48f, 9.88f)
                lineTo(4.28f, 5.7f)
                close()
            }
        }.build()
        return _Shuffle!!
    }
private var _Shuffle: ImageVector? = null

// ── SkipPrevious ─────────────────────────────────────────────────────────────
val SimpIcons.SkipPrevious: ImageVector
    get() {
        if (_SkipPrevious != null) return _SkipPrevious!!
        _SkipPrevious = ImageVector.Builder(
            name = "SkipPrevious",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(5.5f, 17f)
                verticalLineTo(7f)
                quadTo(5.5f, 6.57f, 5.79f, 6.29f)
                reflectiveQuadTo(6.5f, 6f)
                reflectiveQuadTo(7.21f, 6.29f)
                reflectiveQuadTo(7.5f, 7f)
                verticalLineTo(17f)
                quadToRelative(0f, 0.43f, -0.29f, 0.71f)
                reflectiveQuadTo(6.5f, 18f)
                quadTo(6.08f, 18f, 5.79f, 17.71f)
                quadTo(5.5f, 17.43f, 5.5f, 17f)
                close()
                moveTo(16.95f, 16.98f)
                lineToRelative(-6.2f, -4.15f)
                quadTo(10.53f, 12.68f, 10.41f, 12.46f)
                reflectiveQuadTo(10.3f, 12f)
                reflectiveQuadToRelative(0.11f, -0.46f)
                reflectiveQuadToRelative(0.34f, -0.36f)
                lineToRelative(6.2f, -4.15f)
                quadTo(17.08f, 6.93f, 17.23f, 6.9f)
                quadTo(17.38f, 6.88f, 17.5f, 6.88f)
                quadToRelative(0.4f, 0f, 0.7f, 0.27f)
                quadToRelative(0.3f, 0.28f, 0.3f, 0.73f)
                verticalLineToRelative(8.25f)
                quadToRelative(0f, 0.45f, -0.3f, 0.73f)
                reflectiveQuadToRelative(-0.7f, 0.27f)
                quadToRelative(-0.13f, 0f, -0.27f, -0.02f)
                reflectiveQuadTo(16.95f, 16.98f)
                close()
            }
        }.build()
        return _SkipPrevious!!
    }
private var _SkipPrevious: ImageVector? = null

// ── PlayArrow ────────────────────────────────────────────────────────────────
val SimpIcons.PlayArrow: ImageVector
    get() {
        if (_PlayArrow != null) return _PlayArrow!!
        _PlayArrow = ImageVector.Builder(
            name = "PlayArrow",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(8f, 17.18f)
                verticalLineTo(6.82f)
                quadTo(8f, 6.4f, 8.3f, 6.11f)
                quadTo(8.6f, 5.82f, 9f, 5.82f)
                quadToRelative(0.13f, 0f, 0.26f, 0.04f)
                reflectiveQuadTo(9.53f, 5.97f)
                lineToRelative(8.15f, 5.18f)
                quadToRelative(0.23f, 0.15f, 0.34f, 0.38f)
                quadToRelative(0.11f, 0.23f, 0.11f, 0.48f)
                reflectiveQuadToRelative(-0.11f, 0.47f)
                reflectiveQuadToRelative(-0.34f, 0.38f)
                lineTo(9.53f, 18.02f)
                quadTo(9.4f, 18.1f, 9.26f, 18.14f)
                quadTo(9.13f, 18.18f, 9f, 18.18f)
                quadToRelative(-0.4f, 0f, -0.7f, -0.29f)
                reflectiveQuadTo(8f, 17.18f)
                close()
            }
        }.build()
        return _PlayArrow!!
    }
private var _PlayArrow: ImageVector? = null

// ── Pause ────────────────────────────────────────────────────────────────────
val SimpIcons.Pause: ImageVector
    get() {
        if (_Pause != null) return _Pause!!
        _Pause = ImageVector.Builder(
            name = "Pause",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(16f, 19f)
                quadToRelative(-0.82f, 0f, -1.41f, -0.59f)
                reflectiveQuadTo(14f, 17f)
                verticalLineTo(7f)
                quadTo(14f, 6.18f, 14.59f, 5.59f)
                reflectiveQuadTo(16f, 5f)
                quadToRelative(0.82f, 0f, 1.41f, 0.59f)
                quadTo(18f, 6.18f, 18f, 7f)
                verticalLineTo(17f)
                quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                reflectiveQuadTo(16f, 19f)
                close()
                moveTo(8f, 19f)
                quadTo(7.18f, 19f, 6.59f, 18.41f)
                reflectiveQuadTo(6f, 17f)
                verticalLineTo(7f)
                quadTo(6f, 6.18f, 6.59f, 5.59f)
                reflectiveQuadTo(8f, 5f)
                quadTo(8.83f, 5f, 9.41f, 5.59f)
                quadTo(10f, 6.18f, 10f, 7f)
                verticalLineTo(17f)
                quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                quadTo(8.83f, 19f, 8f, 19f)
                close()
            }
        }.build()
        return _Pause!!
    }
private var _Pause: ImageVector? = null

// ── SkipNext ─────────────────────────────────────────────────────────────────
val SimpIcons.SkipNext: ImageVector
    get() {
        if (_SkipNext != null) return _SkipNext!!
        _SkipNext = ImageVector.Builder(
            name = "SkipNext",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(16.5f, 17f)
                verticalLineTo(7f)
                quadToRelative(0f, -0.43f, 0.29f, -0.71f)
                reflectiveQuadTo(17.5f, 6f)
                reflectiveQuadToRelative(0.71f, 0.29f)
                reflectiveQuadTo(18.5f, 7f)
                verticalLineTo(17f)
                quadToRelative(0f, 0.43f, -0.29f, 0.71f)
                reflectiveQuadTo(17.5f, 18f)
                reflectiveQuadTo(16.79f, 17.71f)
                quadTo(16.5f, 17.43f, 16.5f, 17f)
                close()
                moveTo(5.5f, 16.13f)
                verticalLineTo(7.88f)
                quadTo(5.5f, 7.43f, 5.8f, 7.15f)
                quadTo(6.1f, 6.88f, 6.5f, 6.88f)
                quadToRelative(0.13f, 0f, 0.28f, 0.02f)
                quadTo(6.93f, 6.93f, 7.05f, 7.02f)
                lineToRelative(6.2f, 4.15f)
                quadToRelative(0.23f, 0.15f, 0.34f, 0.36f)
                reflectiveQuadTo(13.7f, 12f)
                reflectiveQuadToRelative(-0.11f, 0.46f)
                quadToRelative(-0.11f, 0.21f, -0.34f, 0.36f)
                lineToRelative(-6.2f, 4.15f)
                quadTo(6.93f, 17.08f, 6.78f, 17.1f)
                reflectiveQuadTo(6.5f, 17.13f)
                quadToRelative(-0.4f, 0f, -0.7f, -0.27f)
                reflectiveQuadTo(5.5f, 16.13f)
                close()
            }
        }.build()
        return _SkipNext!!
    }
private var _SkipNext: ImageVector? = null

// ── Repeat ───────────────────────────────────────────────────────────────────
val SimpIcons.Repeat: ImageVector
    get() {
        if (_Repeat != null) return _Repeat!!
        _Repeat = ImageVector.Builder(
            name = "Repeat",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(6.85f, 19f)
                lineTo(7.7f, 19.85f)
                quadToRelative(0.3f, 0.3f, 0.29f, 0.7f)
                reflectiveQuadTo(7.7f, 21.25f)
                quadToRelative(-0.3f, 0.3f, -0.71f, 0.31f)
                reflectiveQuadTo(6.28f, 21.28f)
                lineTo(3.7f, 18.7f)
                quadTo(3.55f, 18.55f, 3.49f, 18.38f)
                reflectiveQuadTo(3.43f, 18f)
                reflectiveQuadTo(3.49f, 17.63f)
                reflectiveQuadTo(3.7f, 17.3f)
                lineTo(6.28f, 14.73f)
                quadToRelative(0.3f, -0.3f, 0.71f, -0.29f)
                reflectiveQuadTo(7.7f, 14.75f)
                quadToRelative(0.28f, 0.3f, 0.29f, 0.7f)
                reflectiveQuadTo(7.7f, 16.15f)
                lineTo(6.85f, 17f)
                horizontalLineTo(17f)
                verticalLineTo(14f)
                quadToRelative(0f, -0.43f, 0.29f, -0.71f)
                reflectiveQuadTo(18f, 13f)
                reflectiveQuadToRelative(0.71f, 0.29f)
                reflectiveQuadTo(19f, 14f)
                verticalLineToRelative(3f)
                quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                reflectiveQuadTo(17f, 19f)
                horizontalLineTo(6.85f)
                close()
                moveTo(17.15f, 7f)
                horizontalLineTo(7f)
                verticalLineToRelative(3f)
                quadToRelative(0f, 0.42f, -0.29f, 0.71f)
                reflectiveQuadTo(6f, 11f)
                quadTo(5.58f, 11f, 5.29f, 10.71f)
                quadTo(5f, 10.43f, 5f, 10f)
                verticalLineTo(7f)
                quadTo(5f, 6.18f, 5.59f, 5.59f)
                reflectiveQuadTo(7f, 5f)
                horizontalLineTo(17.15f)
                lineTo(16.3f, 4.15f)
                quadTo(16f, 3.85f, 16.01f, 3.45f)
                quadToRelative(0.01f, -0.4f, 0.29f, -0.7f)
                quadToRelative(0.3f, -0.3f, 0.71f, -0.31f)
                quadToRelative(0.41f, -0.01f, 0.71f, 0.29f)
                lineTo(20.3f, 5.3f)
                quadToRelative(0.15f, 0.15f, 0.21f, 0.32f)
                reflectiveQuadTo(20.58f, 6f)
                reflectiveQuadTo(20.51f, 6.38f)
                reflectiveQuadTo(20.3f, 6.7f)
                lineTo(17.73f, 9.27f)
                quadToRelative(-0.3f, 0.3f, -0.71f, 0.29f)
                reflectiveQuadTo(16.3f, 9.25f)
                quadTo(16.03f, 8.95f, 16.01f, 8.55f)
                reflectiveQuadTo(16.3f, 7.85f)
                lineTo(17.15f, 7f)
                close()
            }
        }.build()
        return _Repeat!!
    }
private var _Repeat: ImageVector? = null

// ── RepeatOne ────────────────────────────────────────────────────────────────
val SimpIcons.RepeatOne: ImageVector
    get() {
        if (_RepeatOne != null) return _RepeatOne!!
        _RepeatOne = ImageVector.Builder(
            name = "RepeatOne",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(11.5f, 10.5f)
                horizontalLineTo(10.75f)
                quadToRelative(-0.32f, 0f, -0.54f, -0.21f)
                reflectiveQuadTo(10f, 9.75f)
                quadTo(10f, 9.42f, 10.21f, 9.21f)
                reflectiveQuadTo(10.75f, 9f)
                horizontalLineTo(12f)
                quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                reflectiveQuadTo(13f, 10f)
                verticalLineToRelative(4.25f)
                quadToRelative(0f, 0.32f, -0.21f, 0.54f)
                reflectiveQuadTo(12.25f, 15f)
                reflectiveQuadTo(11.71f, 14.79f)
                reflectiveQuadTo(11.5f, 14.25f)
                verticalLineTo(10.5f)
                close()
                moveTo(6.85f, 19f)
                lineTo(7.7f, 19.85f)
                quadToRelative(0.3f, 0.3f, 0.29f, 0.7f)
                reflectiveQuadTo(7.7f, 21.25f)
                quadToRelative(-0.3f, 0.3f, -0.71f, 0.31f)
                reflectiveQuadTo(6.28f, 21.28f)
                lineTo(3.7f, 18.7f)
                quadTo(3.55f, 18.55f, 3.49f, 18.38f)
                reflectiveQuadTo(3.43f, 18f)
                reflectiveQuadTo(3.49f, 17.63f)
                reflectiveQuadTo(3.7f, 17.3f)
                lineTo(6.28f, 14.73f)
                quadToRelative(0.3f, -0.3f, 0.71f, -0.29f)
                reflectiveQuadTo(7.7f, 14.75f)
                quadToRelative(0.28f, 0.3f, 0.29f, 0.7f)
                reflectiveQuadTo(7.7f, 16.15f)
                lineTo(6.85f, 17f)
                horizontalLineTo(17f)
                verticalLineTo(14f)
                quadToRelative(0f, -0.43f, 0.29f, -0.71f)
                reflectiveQuadTo(18f, 13f)
                reflectiveQuadToRelative(0.71f, 0.29f)
                reflectiveQuadTo(19f, 14f)
                verticalLineToRelative(3f)
                quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                reflectiveQuadTo(17f, 19f)
                horizontalLineTo(6.85f)
                close()
                moveTo(17.15f, 7f)
                horizontalLineTo(7f)
                verticalLineToRelative(3f)
                quadToRelative(0f, 0.42f, -0.29f, 0.71f)
                reflectiveQuadTo(6f, 11f)
                quadTo(5.58f, 11f, 5.29f, 10.71f)
                quadTo(5f, 10.43f, 5f, 10f)
                verticalLineTo(7f)
                quadTo(5f, 6.18f, 5.59f, 5.59f)
                reflectiveQuadTo(7f, 5f)
                horizontalLineTo(17.15f)
                lineTo(16.3f, 4.15f)
                quadTo(16f, 3.85f, 16.01f, 3.45f)
                quadToRelative(0.01f, -0.4f, 0.29f, -0.7f)
                quadToRelative(0.3f, -0.3f, 0.71f, -0.31f)
                quadToRelative(0.41f, -0.01f, 0.71f, 0.29f)
                lineTo(20.3f, 5.3f)
                quadToRelative(0.15f, 0.15f, 0.21f, 0.32f)
                reflectiveQuadTo(20.58f, 6f)
                reflectiveQuadTo(20.51f, 6.38f)
                reflectiveQuadTo(20.3f, 6.7f)
                lineTo(17.73f, 9.27f)
                quadToRelative(-0.3f, 0.3f, -0.71f, 0.29f)
                reflectiveQuadTo(16.3f, 9.25f)
                quadTo(16.03f, 8.95f, 16.01f, 8.55f)
                reflectiveQuadTo(16.3f, 7.85f)
                lineTo(17.15f, 7f)
                close()
            }
        }.build()
        return _RepeatOne!!
    }
private var _RepeatOne: ImageVector? = null

// ── Tune (Equalizer) ─────────────────────────────────────────────────────────
val SimpIcons.Tune: ImageVector
    get() {
        if (_Tune != null) return _Tune!!
        _Tune = ImageVector.Builder(
            name = "Tune",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(11.29f, 20.71f)
                quadTo(11f, 20.43f, 11f, 20f)
                verticalLineTo(16f)
                quadToRelative(0f, -0.43f, 0.29f, -0.71f)
                reflectiveQuadTo(12f, 15f)
                reflectiveQuadToRelative(0.71f, 0.29f)
                reflectiveQuadTo(13f, 16f)
                verticalLineToRelative(1f)
                horizontalLineToRelative(7f)
                quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                reflectiveQuadTo(21f, 18f)
                reflectiveQuadToRelative(-0.29f, 0.71f)
                reflectiveQuadTo(20f, 19f)
                horizontalLineTo(13f)
                verticalLineToRelative(1f)
                quadToRelative(0f, 0.43f, -0.29f, 0.71f)
                reflectiveQuadTo(12f, 21f)
                reflectiveQuadTo(11.29f, 20.71f)
                close()
                moveTo(4f, 19f)
                quadTo(3.58f, 19f, 3.29f, 18.71f)
                quadTo(3f, 18.43f, 3f, 18f)
                reflectiveQuadTo(3.29f, 17.29f)
                reflectiveQuadTo(4f, 17f)
                horizontalLineTo(8f)
                quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                reflectiveQuadTo(9f, 18f)
                reflectiveQuadTo(8.71f, 18.71f)
                reflectiveQuadTo(8f, 19f)
                horizontalLineTo(4f)
                close()
                moveTo(7.29f, 14.71f)
                quadTo(7f, 14.43f, 7f, 14f)
                verticalLineTo(13f)
                horizontalLineTo(4f)
                quadTo(3.58f, 13f, 3.29f, 12.71f)
                quadTo(3f, 12.43f, 3f, 12f)
                reflectiveQuadTo(3.29f, 11.29f)
                reflectiveQuadTo(4f, 11f)
                horizontalLineTo(7f)
                verticalLineTo(10f)
                quadTo(7f, 9.57f, 7.29f, 9.29f)
                reflectiveQuadTo(8f, 9f)
                reflectiveQuadTo(8.71f, 9.29f)
                reflectiveQuadTo(9f, 10f)
                verticalLineToRelative(4f)
                quadToRelative(0f, 0.42f, -0.29f, 0.71f)
                reflectiveQuadTo(8f, 15f)
                quadTo(7.58f, 15f, 7.29f, 14.71f)
                close()
                moveTo(12f, 13f)
                quadToRelative(-0.42f, 0f, -0.71f, -0.29f)
                quadTo(11f, 12.43f, 11f, 12f)
                reflectiveQuadToRelative(0.29f, -0.71f)
                reflectiveQuadTo(12f, 11f)
                horizontalLineToRelative(8f)
                quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                reflectiveQuadTo(21f, 12f)
                reflectiveQuadToRelative(-0.29f, 0.71f)
                reflectiveQuadTo(20f, 13f)
                horizontalLineTo(12f)
                close()
                moveTo(15.29f, 8.71f)
                quadTo(15f, 8.42f, 15f, 8f)
                verticalLineTo(4f)
                quadTo(15f, 3.57f, 15.29f, 3.29f)
                reflectiveQuadTo(16f, 3f)
                quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                reflectiveQuadTo(17f, 4f)
                verticalLineTo(5f)
                horizontalLineToRelative(3f)
                quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                reflectiveQuadTo(21f, 6f)
                reflectiveQuadTo(20.71f, 6.71f)
                reflectiveQuadTo(20f, 7f)
                horizontalLineTo(17f)
                verticalLineTo(8f)
                quadToRelative(0f, 0.42f, -0.29f, 0.71f)
                reflectiveQuadTo(16f, 9f)
                reflectiveQuadTo(15.29f, 8.71f)
                close()
                moveTo(4f, 7f)
                quadTo(3.58f, 7f, 3.29f, 6.71f)
                quadTo(3f, 6.43f, 3f, 6f)
                reflectiveQuadTo(3.29f, 5.29f)
                reflectiveQuadTo(4f, 5f)
                horizontalLineToRelative(8f)
                quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                reflectiveQuadTo(13f, 6f)
                reflectiveQuadTo(12.71f, 6.71f)
                reflectiveQuadTo(12f, 7f)
                horizontalLineTo(4f)
                close()
            }
        }.build()
        return _Tune!!
    }
private var _Tune: ImageVector? = null

// ── Info ─────────────────────────────────────────────────────────────────────
val SimpIcons.Info: ImageVector
    get() {
        if (_Info != null) return _Info!!
        _Info = ImageVector.Builder(
            name = "Info",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(12.71f, 16.71f)
                quadTo(13f, 16.43f, 13f, 16f)
                verticalLineTo(12f)
                quadToRelative(0f, -0.43f, -0.29f, -0.71f)
                reflectiveQuadTo(12f, 11f)
                reflectiveQuadToRelative(-0.71f, 0.29f)
                reflectiveQuadTo(11f, 12f)
                verticalLineToRelative(4f)
                quadToRelative(0f, 0.43f, 0.29f, 0.71f)
                reflectiveQuadTo(12f, 17f)
                reflectiveQuadToRelative(0.71f, -0.29f)
                close()
                moveToRelative(0f, -8f)
                quadTo(13f, 8.42f, 13f, 8f)
                quadTo(13f, 7.57f, 12.71f, 7.29f)
                reflectiveQuadTo(12f, 7f)
                reflectiveQuadTo(11.29f, 7.29f)
                reflectiveQuadTo(11f, 8f)
                quadToRelative(0f, 0.42f, 0.29f, 0.71f)
                reflectiveQuadTo(12f, 9f)
                reflectiveQuadTo(12.71f, 8.71f)
                close()
                moveTo(12f, 22f)
                quadTo(9.93f, 22f, 8.1f, 21.21f)
                quadTo(6.28f, 20.43f, 4.93f, 19.08f)
                quadTo(3.58f, 17.73f, 2.79f, 15.9f)
                reflectiveQuadTo(2f, 12f)
                quadTo(2f, 9.92f, 2.79f, 8.1f)
                quadTo(3.58f, 6.27f, 4.93f, 4.93f)
                quadTo(6.28f, 3.57f, 8.1f, 2.79f)
                quadTo(9.93f, 2f, 12f, 2f)
                reflectiveQuadToRelative(3.9f, 0.79f)
                reflectiveQuadToRelative(3.17f, 2.14f)
                quadToRelative(1.35f, 1.35f, 2.14f, 3.17f)
                quadTo(22f, 9.92f, 22f, 12f)
                reflectiveQuadToRelative(-0.79f, 3.9f)
                reflectiveQuadToRelative(-2.14f, 3.17f)
                quadToRelative(-1.35f, 1.35f, -3.17f, 2.14f)
                reflectiveQuadTo(12f, 22f)
                close()
            }
        }.build()
        return _Info!!
    }
private var _Info: ImageVector? = null

// ── PictureInPictureAlt (Video Mode) ─────────────────────────────────────────
val SimpIcons.PictureInPictureAlt: ImageVector
    get() {
        if (_PictureInPictureAlt != null) return _PictureInPictureAlt!!
        _PictureInPictureAlt = ImageVector.Builder(
            name = "PictureInPictureAlt",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(4f, 20f)
                quadTo(3.18f, 20f, 2.59f, 19.41f)
                reflectiveQuadTo(2f, 18f)
                verticalLineTo(6f)
                quadTo(2f, 5.18f, 2.59f, 4.59f)
                reflectiveQuadTo(4f, 4f)
                horizontalLineTo(20f)
                quadToRelative(0.83f, 0f, 1.41f, 0.59f)
                quadTo(22f, 5.18f, 22f, 6f)
                verticalLineTo(18f)
                quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                reflectiveQuadTo(20f, 20f)
                horizontalLineTo(4f)
                close()
                moveTo(4f, 18f)
                horizontalLineTo(20f)
                verticalLineTo(6f)
                horizontalLineTo(4f)
                verticalLineTo(18f)
                close()
                moveToRelative(8f, -1f)
                horizontalLineToRelative(6f)
                quadToRelative(0.43f, 0f, 0.71f, -0.29f)
                quadTo(19f, 16.43f, 19f, 16f)
                verticalLineTo(12f)
                quadToRelative(0f, -0.43f, -0.29f, -0.71f)
                reflectiveQuadTo(18f, 11f)
                horizontalLineTo(12f)
                quadToRelative(-0.42f, 0f, -0.71f, 0.29f)
                reflectiveQuadTo(11f, 12f)
                verticalLineToRelative(4f)
                quadToRelative(0f, 0.43f, 0.29f, 0.71f)
                reflectiveQuadTo(12f, 17f)
                close()
                moveToRelative(1f, -2f)
                verticalLineTo(13f)
                horizontalLineToRelative(4f)
                verticalLineToRelative(2f)
                horizontalLineTo(13f)
                close()
            }
        }.build()
        return _PictureInPictureAlt!!
    }
private var _PictureInPictureAlt: ImageVector? = null

// ── PlaylistAdd ──────────────────────────────────────────────────────────────
val SimpIcons.PlaylistAdd: ImageVector
    get() {
        if (_PlaylistAdd != null) return _PlaylistAdd!!
        _PlaylistAdd = ImageVector.Builder(
            name = "PlaylistAdd",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(4f, 16f)
                quadTo(3.58f, 16f, 3.29f, 15.71f)
                reflectiveQuadTo(3f, 15f)
                reflectiveQuadTo(3.29f, 14.29f)
                reflectiveQuadTo(4f, 14f)
                horizontalLineTo(9f)
                quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                reflectiveQuadTo(10f, 15f)
                reflectiveQuadTo(9.71f, 15.71f)
                reflectiveQuadTo(9f, 16f)
                horizontalLineTo(4f)
                close()
                moveTo(4f, 12f)
                quadTo(3.58f, 12f, 3.29f, 11.71f)
                quadTo(3f, 11.43f, 3f, 11f)
                reflectiveQuadTo(3.29f, 10.29f)
                reflectiveQuadTo(4f, 10f)
                horizontalLineToRelative(9f)
                quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                reflectiveQuadTo(14f, 11f)
                reflectiveQuadToRelative(-0.29f, 0.71f)
                reflectiveQuadTo(13f, 12f)
                horizontalLineTo(4f)
                close()
                moveTo(4f, 8f)
                quadTo(3.58f, 8f, 3.29f, 7.71f)
                quadTo(3f, 7.43f, 3f, 7f)
                reflectiveQuadTo(3.29f, 6.29f)
                reflectiveQuadTo(4f, 6f)
                horizontalLineToRelative(9f)
                quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                reflectiveQuadTo(14f, 7f)
                reflectiveQuadTo(13.71f, 7.71f)
                reflectiveQuadTo(13f, 8f)
                horizontalLineTo(4f)
                close()
                moveTo(16.29f, 19.71f)
                quadTo(16f, 19.43f, 16f, 19f)
                verticalLineTo(16f)
                horizontalLineTo(13f)
                quadToRelative(-0.42f, 0f, -0.71f, -0.29f)
                reflectiveQuadTo(12f, 15f)
                reflectiveQuadToRelative(0.29f, -0.71f)
                reflectiveQuadTo(13f, 14f)
                horizontalLineToRelative(3f)
                verticalLineTo(11f)
                quadToRelative(0f, -0.43f, 0.29f, -0.71f)
                reflectiveQuadTo(17f, 10f)
                reflectiveQuadToRelative(0.71f, 0.29f)
                reflectiveQuadTo(18f, 11f)
                verticalLineToRelative(3f)
                horizontalLineToRelative(3f)
                quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                reflectiveQuadTo(22f, 15f)
                reflectiveQuadToRelative(-0.29f, 0.71f)
                reflectiveQuadTo(21f, 16f)
                horizontalLineTo(18f)
                verticalLineToRelative(3f)
                quadToRelative(0f, 0.43f, -0.29f, 0.71f)
                reflectiveQuadTo(17f, 20f)
                reflectiveQuadTo(16.29f, 19.71f)
                close()
            }
        }.build()
        return _PlaylistAdd!!
    }
private var _PlaylistAdd: ImageVector? = null

// ── AccessAlarm (Sleep Timer) ────────────────────────────────────────────────
val SimpIcons.AccessAlarm: ImageVector
    get() {
        if (_AccessAlarm != null) return _AccessAlarm!!
        _AccessAlarm = ImageVector.Builder(
            name = "AccessAlarm",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(13f, 12.6f)
                verticalLineTo(9f)
                quadTo(13f, 8.57f, 12.71f, 8.29f)
                reflectiveQuadTo(12f, 8f)
                reflectiveQuadTo(11.29f, 8.29f)
                reflectiveQuadTo(11f, 9f)
                verticalLineToRelative(3.97f)
                quadToRelative(0f, 0.2f, 0.08f, 0.39f)
                reflectiveQuadTo(11.3f, 13.7f)
                lineToRelative(2.8f, 2.8f)
                quadToRelative(0.27f, 0.27f, 0.7f, 0.27f)
                reflectiveQuadTo(15.5f, 16.5f)
                quadToRelative(0.28f, -0.27f, 0.28f, -0.7f)
                quadToRelative(0f, -0.42f, -0.28f, -0.7f)
                lineTo(13f, 12.6f)
                close()
                moveTo(8.49f, 21.29f)
                quadTo(6.85f, 20.58f, 5.64f, 19.36f)
                reflectiveQuadTo(3.71f, 16.51f)
                reflectiveQuadTo(3f, 13f)
                reflectiveQuadTo(3.71f, 9.49f)
                reflectiveQuadTo(5.64f, 6.64f)
                reflectiveQuadTo(8.49f, 4.71f)
                reflectiveQuadTo(12f, 4f)
                reflectiveQuadToRelative(3.51f, 0.71f)
                quadToRelative(1.64f, 0.71f, 2.85f, 1.93f)
                reflectiveQuadToRelative(1.93f, 2.85f)
                reflectiveQuadTo(21f, 13f)
                reflectiveQuadToRelative(-0.71f, 3.51f)
                reflectiveQuadToRelative(-1.93f, 2.85f)
                reflectiveQuadToRelative(-2.85f, 1.93f)
                reflectiveQuadTo(12f, 22f)
                reflectiveQuadTo(8.49f, 21.29f)
                close()
                moveTo(2.05f, 7.3f)
                quadTo(1.78f, 7.02f, 1.78f, 6.6f)
                quadToRelative(0f, -0.42f, 0.28f, -0.7f)
                lineTo(4.9f, 3.05f)
                quadTo(5.18f, 2.77f, 5.6f, 2.77f)
                reflectiveQuadTo(6.3f, 3.05f)
                reflectiveQuadToRelative(0.27f, 0.7f)
                quadToRelative(0f, 0.42f, -0.27f, 0.7f)
                lineTo(3.45f, 7.3f)
                quadTo(3.18f, 7.57f, 2.75f, 7.57f)
                quadTo(2.33f, 7.57f, 2.05f, 7.3f)
                close()
                moveToRelative(19.9f, 0f)
                quadToRelative(-0.28f, 0.27f, -0.7f, 0.27f)
                reflectiveQuadTo(20.55f, 7.3f)
                lineTo(17.7f, 4.45f)
                quadTo(17.43f, 4.17f, 17.43f, 3.75f)
                quadToRelative(0f, -0.43f, 0.27f, -0.7f)
                quadTo(17.98f, 2.77f, 18.4f, 2.77f)
                reflectiveQuadToRelative(0.7f, 0.27f)
                lineTo(21.95f, 5.9f)
                quadToRelative(0.27f, 0.28f, 0.27f, 0.7f)
                reflectiveQuadTo(21.95f, 7.3f)
                close()
            }
        }.build()
        return _AccessAlarm!!
    }
private var _AccessAlarm: ImageVector? = null

// ── QueueMusic ───────────────────────────────────────────────────────────────
val SimpIcons.QueueMusic: ImageVector
    get() {
        if (_QueueMusic != null) return _QueueMusic!!
        _QueueMusic = ImageVector.Builder(
            name = "QueueMusic",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
            autoMirror = true
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(16f, 20f)
                quadToRelative(-1.25f, 0f, -2.13f, -0.88f)
                reflectiveQuadTo(13f, 17f)
                reflectiveQuadToRelative(0.88f, -2.13f)
                reflectiveQuadTo(16f, 14f)
                quadToRelative(0.28f, 0f, 0.53f, 0.04f)
                reflectiveQuadTo(17f, 14.2f)
                verticalLineTo(7f)
                quadTo(17f, 6.57f, 17.29f, 6.29f)
                reflectiveQuadTo(18f, 6f)
                horizontalLineToRelative(3f)
                quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                reflectiveQuadTo(22f, 7f)
                reflectiveQuadTo(21.71f, 7.71f)
                reflectiveQuadTo(21f, 8f)
                horizontalLineTo(19f)
                verticalLineToRelative(9f)
                quadToRelative(0f, 1.25f, -0.88f, 2.13f)
                reflectiveQuadTo(16f, 20f)
                close()
                moveTo(4f, 16f)
                quadTo(3.58f, 16f, 3.29f, 15.71f)
                reflectiveQuadTo(3f, 15f)
                reflectiveQuadTo(3.29f, 14.29f)
                reflectiveQuadTo(4f, 14f)
                horizontalLineToRelative(6f)
                quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                reflectiveQuadTo(11f, 15f)
                reflectiveQuadToRelative(-0.29f, 0.71f)
                reflectiveQuadTo(10f, 16f)
                horizontalLineTo(4f)
                close()
                moveTo(4f, 12f)
                quadTo(3.58f, 12f, 3.29f, 11.71f)
                quadTo(3f, 11.43f, 3f, 11f)
                reflectiveQuadTo(3.29f, 10.29f)
                reflectiveQuadTo(4f, 10f)
                horizontalLineTo(14f)
                quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                reflectiveQuadTo(15f, 11f)
                reflectiveQuadToRelative(-0.29f, 0.71f)
                reflectiveQuadTo(14f, 12f)
                horizontalLineTo(4f)
                close()
                moveTo(4f, 8f)
                quadTo(3.58f, 8f, 3.29f, 7.71f)
                quadTo(3f, 7.43f, 3f, 7f)
                reflectiveQuadTo(3.29f, 6.29f)
                reflectiveQuadTo(4f, 6f)
                horizontalLineTo(14f)
                quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                reflectiveQuadTo(15f, 7f)
                reflectiveQuadTo(14.71f, 7.71f)
                reflectiveQuadTo(14f, 8f)
                horizontalLineTo(4f)
                close()
            }
        }.build()
        return _QueueMusic!!
    }
private var _QueueMusic: ImageVector? = null

// ── Home ─────────────────────────────────────────────────────────────────────
val SimpIcons.Home: ImageVector
    get() {
        if (_Home != null) return _Home!!
        _Home = ImageVector.Builder(
            name = "Home",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(4f, 19f)
                verticalLineTo(10f)
                quadTo(4f, 9.52f, 4.21f, 9.1f)
                quadTo(4.43f, 8.67f, 4.8f, 8.4f)
                lineToRelative(6f, -4.5f)
                quadTo(11.33f, 3.5f, 12f, 3.5f)
                reflectiveQuadToRelative(1.2f, 0.4f)
                lineToRelative(6f, 4.5f)
                quadToRelative(0.38f, 0.28f, 0.59f, 0.7f)
                quadTo(20f, 9.52f, 20f, 10f)
                verticalLineToRelative(9f)
                quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                reflectiveQuadTo(18f, 21f)
                horizontalLineTo(15f)
                quadToRelative(-0.42f, 0f, -0.71f, -0.29f)
                quadTo(14f, 20.43f, 14f, 20f)
                verticalLineTo(15f)
                quadToRelative(0f, -0.43f, -0.29f, -0.71f)
                reflectiveQuadTo(13f, 14f)
                horizontalLineTo(11f)
                quadToRelative(-0.42f, 0f, -0.71f, 0.29f)
                reflectiveQuadTo(10f, 15f)
                verticalLineToRelative(5f)
                quadToRelative(0f, 0.43f, -0.29f, 0.71f)
                reflectiveQuadTo(9f, 21f)
                horizontalLineTo(6f)
                quadTo(5.18f, 21f, 4.59f, 20.41f)
                reflectiveQuadTo(4f, 19f)
                close()
            }
        }.build()
        return _Home!!
    }
private var _Home: ImageVector? = null

// ── Search ───────────────────────────────────────────────────────────────────
val SimpIcons.Search: ImageVector
    get() {
        if (_Search != null) return _Search!!
        _Search = ImageVector.Builder(
            name = "Search",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(9.5f, 16f)
                quadTo(6.78f, 16f, 4.89f, 14.11f)
                quadTo(3f, 12.23f, 3f, 9.5f)
                quadTo(3f, 6.77f, 4.89f, 4.89f)
                reflectiveQuadTo(9.5f, 3f)
                reflectiveQuadToRelative(4.61f, 1.89f)
                reflectiveQuadTo(16f, 9.5f)
                quadToRelative(0f, 1.1f, -0.35f, 2.07f)
                reflectiveQuadTo(14.7f, 13.3f)
                lineToRelative(5.6f, 5.6f)
                quadToRelative(0.28f, 0.28f, 0.28f, 0.7f)
                quadToRelative(0f, 0.42f, -0.28f, 0.7f)
                quadToRelative(-0.27f, 0.27f, -0.7f, 0.27f)
                reflectiveQuadTo(18.9f, 20.3f)
                lineTo(13.3f, 14.7f)
                quadToRelative(-0.75f, 0.6f, -1.72f, 0.95f)
                reflectiveQuadTo(9.5f, 16f)
                close()
                moveToRelative(0f, -2f)
                quadToRelative(1.88f, 0f, 3.19f, -1.31f)
                reflectiveQuadTo(14f, 9.5f)
                reflectiveQuadTo(12.69f, 6.31f)
                reflectiveQuadTo(9.5f, 5f)
                reflectiveQuadTo(6.31f, 6.31f)
                reflectiveQuadTo(5f, 9.5f)
                reflectiveQuadToRelative(1.31f, 3.19f)
                reflectiveQuadTo(9.5f, 14f)
                close()
            }
        }.build()
        return _Search!!
    }
private var _Search: ImageVector? = null

// ── Explicit ──────────────────────────────────────────────────────────────────
val SimpIcons.Explicit: ImageVector
    get() {
        if (_Explicit != null) return _Explicit!!
        _Explicit = ImageVector.Builder(
            name = "Explicit",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(11f, 15f)
                verticalLineTo(13f)
                horizontalLineToRelative(3f)
                quadToRelative(0.43f, 0f, 0.71f, -0.29f)
                quadTo(15f, 12.43f, 15f, 12f)
                reflectiveQuadTo(14.71f, 11.29f)
                reflectiveQuadTo(14f, 11f)
                horizontalLineTo(11f)
                verticalLineTo(9f)
                horizontalLineToRelative(3f)
                quadToRelative(0.43f, 0f, 0.71f, -0.29f)
                reflectiveQuadTo(15f, 8f)
                quadTo(15f, 7.57f, 14.71f, 7.29f)
                reflectiveQuadTo(14f, 7f)
                horizontalLineTo(10f)
                quadTo(9.58f, 7f, 9.29f, 7.29f)
                reflectiveQuadTo(9f, 8f)
                verticalLineToRelative(8f)
                quadToRelative(0f, 0.43f, 0.29f, 0.71f)
                quadTo(9.58f, 17f, 10f, 17f)
                horizontalLineToRelative(4f)
                quadToRelative(0.43f, 0f, 0.71f, -0.29f)
                quadTo(15f, 16.43f, 15f, 16f)
                reflectiveQuadTo(14.71f, 15.29f)
                reflectiveQuadTo(14f, 15f)
                horizontalLineTo(11f)
                close()
                moveTo(5f, 21f)
                quadTo(4.18f, 21f, 3.59f, 20.41f)
                reflectiveQuadTo(3f, 19f)
                verticalLineTo(5f)
                quadTo(3f, 4.17f, 3.59f, 3.59f)
                reflectiveQuadTo(5f, 3f)
                horizontalLineTo(19f)
                quadToRelative(0.83f, 0f, 1.41f, 0.59f)
                reflectiveQuadTo(21f, 5f)
                verticalLineTo(19f)
                quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                reflectiveQuadTo(19f, 21f)
                horizontalLineTo(5f)
                close()
            }
        }.build()
        return _Explicit!!
    }
private var _Explicit: ImageVector? = null
