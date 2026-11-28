package com.example.ecodot.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

// ── Add (add) ──
@Suppress("CheckReturnValue")
val SimpIcons.Add: ImageVector
  get() {
    if (_GenAdd != null) {
      return _GenAdd!!
    }
    _GenAdd =
      ImageVector.Builder(
          name = "Add",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(11f, 13f)
            horizontalLineTo(6f)
            quadTo(5.58f, 13f, 5.29f, 12.71f)
            quadTo(5f, 12.43f, 5f, 12f)
            reflectiveQuadTo(5.29f, 11.29f)
            reflectiveQuadTo(6f, 11f)
            horizontalLineToRelative(5f)
            verticalLineTo(6f)
            quadTo(11f, 5.57f, 11.29f, 5.29f)
            reflectiveQuadTo(12f, 5f)
            reflectiveQuadToRelative(0.71f, 0.29f)
            reflectiveQuadTo(13f, 6f)
            verticalLineToRelative(5f)
            horizontalLineToRelative(5f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(19f, 12f)
            reflectiveQuadToRelative(-0.29f, 0.71f)
            reflectiveQuadTo(18f, 13f)
            horizontalLineTo(13f)
            verticalLineToRelative(5f)
            quadToRelative(0f, 0.43f, -0.29f, 0.71f)
            reflectiveQuadTo(12f, 19f)
            reflectiveQuadTo(11.29f, 18.71f)
            quadTo(11f, 18.43f, 11f, 18f)
            verticalLineTo(13f)
            close()
          }
        }
        .build()
    return _GenAdd!!
  }

private var _GenAdd: ImageVector? = null

// ── AddCircleOutline (add_circle) ──
@Suppress("CheckReturnValue")
val SimpIcons.AddCircleOutline: ImageVector
  get() {
    if (_GenAddCircleOutline != null) {
      return _GenAddCircleOutline!!
    }
    _GenAddCircleOutline =
      ImageVector.Builder(
          name = "AddCircleOutline",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(11f, 13f)
            verticalLineToRelative(3f)
            quadToRelative(0f, 0.43f, 0.29f, 0.71f)
            reflectiveQuadTo(12f, 17f)
            reflectiveQuadToRelative(0.71f, -0.29f)
            quadTo(13f, 16.43f, 13f, 16f)
            verticalLineTo(13f)
            horizontalLineToRelative(3f)
            quadToRelative(0.43f, 0f, 0.71f, -0.29f)
            quadTo(17f, 12.43f, 17f, 12f)
            reflectiveQuadTo(16.71f, 11.29f)
            reflectiveQuadTo(16f, 11f)
            horizontalLineTo(13f)
            verticalLineTo(8f)
            quadTo(13f, 7.57f, 12.71f, 7.29f)
            reflectiveQuadTo(12f, 7f)
            reflectiveQuadTo(11.29f, 7.29f)
            reflectiveQuadTo(11f, 8f)
            verticalLineToRelative(3f)
            horizontalLineTo(8f)
            quadTo(7.58f, 11f, 7.29f, 11.29f)
            reflectiveQuadTo(7f, 12f)
            reflectiveQuadToRelative(0.29f, 0.71f)
            reflectiveQuadTo(8f, 13f)
            horizontalLineToRelative(3f)
            close()
            moveToRelative(1f, 9f)
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
            moveToRelative(0f, -2f)
            quadToRelative(3.35f, 0f, 5.68f, -2.32f)
            reflectiveQuadTo(20f, 12f)
            reflectiveQuadTo(17.68f, 6.32f)
            reflectiveQuadTo(12f, 4f)
            reflectiveQuadTo(6.33f, 6.32f)
            reflectiveQuadTo(4f, 12f)
            reflectiveQuadToRelative(2.33f, 5.68f)
            reflectiveQuadTo(12f, 20f)
            close()
            moveToRelative(0f, -8f)
            close()
          }
        }
        .build()
    return _GenAddCircleOutline!!
  }

private var _GenAddCircleOutline: ImageVector? = null

// ── AddToQueue (add_to_queue) ──
@Suppress("CheckReturnValue")
val SimpIcons.AddToQueue: ImageVector
  get() {
    if (_GenAddToQueue != null) {
      return _GenAddToQueue!!
    }
    _GenAddToQueue =
      ImageVector.Builder(
          name = "AddToQueue",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(11f, 12f)
            verticalLineToRelative(2f)
            quadToRelative(0f, 0.42f, 0.29f, 0.71f)
            reflectiveQuadTo(12f, 15f)
            reflectiveQuadToRelative(0.71f, -0.29f)
            reflectiveQuadTo(13f, 14f)
            verticalLineTo(12f)
            horizontalLineToRelative(2f)
            quadToRelative(0.43f, 0f, 0.71f, -0.29f)
            quadTo(16f, 11.43f, 16f, 11f)
            reflectiveQuadTo(15.71f, 10.29f)
            reflectiveQuadTo(15f, 10f)
            horizontalLineTo(13f)
            verticalLineTo(8f)
            quadTo(13f, 7.57f, 12.71f, 7.29f)
            reflectiveQuadTo(12f, 7f)
            reflectiveQuadTo(11.29f, 7.29f)
            reflectiveQuadTo(11f, 8f)
            verticalLineToRelative(2f)
            horizontalLineTo(9f)
            quadTo(8.58f, 10f, 8.29f, 10.29f)
            reflectiveQuadTo(8f, 11f)
            reflectiveQuadToRelative(0.29f, 0.71f)
            quadTo(8.58f, 12f, 9f, 12f)
            horizontalLineToRelative(2f)
            close()
            moveTo(4f, 19f)
            quadTo(3.18f, 19f, 2.59f, 18.41f)
            reflectiveQuadTo(2f, 17f)
            verticalLineTo(5f)
            quadTo(2f, 4.17f, 2.59f, 3.59f)
            reflectiveQuadTo(4f, 3f)
            horizontalLineTo(20f)
            quadToRelative(0.83f, 0f, 1.41f, 0.59f)
            reflectiveQuadTo(22f, 5f)
            verticalLineTo(17f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(20f, 19f)
            horizontalLineTo(16f)
            verticalLineToRelative(1f)
            quadToRelative(0f, 0.43f, -0.29f, 0.71f)
            reflectiveQuadTo(15f, 21f)
            horizontalLineTo(9f)
            quadTo(8.58f, 21f, 8.29f, 20.71f)
            quadTo(8f, 20.43f, 8f, 20f)
            verticalLineTo(19f)
            horizontalLineTo(4f)
            close()
          }
        }
        .build()
    return _GenAddToQueue!!
  }

private var _GenAddToQueue: ImageVector? = null

// ── Album (album) ──
@Suppress("CheckReturnValue")
val SimpIcons.Album: ImageVector
  get() {
    if (_GenAlbum != null) {
      return _GenAlbum!!
    }
    _GenAlbum =
      ImageVector.Builder(
          name = "Album",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(12f, 16.5f)
            quadToRelative(1.88f, 0f, 3.19f, -1.31f)
            reflectiveQuadTo(16.5f, 12f)
            reflectiveQuadTo(15.19f, 8.81f)
            reflectiveQuadTo(12f, 7.5f)
            reflectiveQuadTo(8.81f, 8.81f)
            reflectiveQuadTo(7.5f, 12f)
            reflectiveQuadToRelative(1.31f, 3.19f)
            reflectiveQuadTo(12f, 16.5f)
            close()
            moveTo(11.29f, 12.71f)
            quadTo(11f, 12.43f, 11f, 12f)
            reflectiveQuadToRelative(0.29f, -0.71f)
            reflectiveQuadTo(12f, 11f)
            reflectiveQuadToRelative(0.71f, 0.29f)
            reflectiveQuadTo(13f, 12f)
            reflectiveQuadToRelative(-0.29f, 0.71f)
            reflectiveQuadTo(12f, 13f)
            reflectiveQuadTo(11.29f, 12.71f)
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
        }
        .build()
    return _GenAlbum!!
  }

private var _GenAlbum: ImageVector? = null

// ── ArrowBack (arrow_back) ──
@Suppress("CheckReturnValue")
val SimpIcons.ArrowBack: ImageVector
  get() {
    if (_GenArrowBack != null) {
      return _GenArrowBack!!
    }
    _GenArrowBack =
      ImageVector.Builder(
          name = "ArrowBack",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(7.83f, 13f)
            lineToRelative(4.9f, 4.9f)
            quadToRelative(0.3f, 0.3f, 0.29f, 0.7f)
            reflectiveQuadTo(12.7f, 19.3f)
            quadTo(12.4f, 19.58f, 12f, 19.59f)
            reflectiveQuadTo(11.3f, 19.3f)
            lineTo(4.7f, 12.7f)
            quadTo(4.55f, 12.55f, 4.49f, 12.38f)
            reflectiveQuadTo(4.43f, 12f)
            reflectiveQuadTo(4.49f, 11.63f)
            reflectiveQuadTo(4.7f, 11.3f)
            lineTo(11.3f, 4.7f)
            quadTo(11.58f, 4.42f, 11.99f, 4.42f)
            reflectiveQuadTo(12.7f, 4.7f)
            quadTo(13f, 5f, 13f, 5.41f)
            reflectiveQuadTo(12.7f, 6.13f)
            lineTo(7.83f, 11f)
            horizontalLineTo(19f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(20f, 12f)
            reflectiveQuadToRelative(-0.29f, 0.71f)
            reflectiveQuadTo(19f, 13f)
            horizontalLineTo(7.83f)
            close()
          }
        }
        .build()
    return _GenArrowBack!!
  }

private var _GenArrowBack: ImageVector? = null

// ── ArrowBackIos (arrow_back_ios) ──
@Suppress("CheckReturnValue")
val SimpIcons.ArrowBackIos: ImageVector
  get() {
    if (_GenArrowBackIos != null) {
      return _GenArrowBackIos!!
    }
    _GenArrowBackIos =
      ImageVector.Builder(
          name = "ArrowBackIos",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(3.55f, 12f)
            lineToRelative(7.35f, 7.35f)
            quadToRelative(0.38f, 0.38f, 0.36f, 0.88f)
            reflectiveQuadTo(10.88f, 21.1f)
            reflectiveQuadTo(10f, 21.48f)
            reflectiveQuadTo(9.13f, 21.1f)
            lineTo(1.43f, 13.43f)
            quadTo(1.13f, 13.13f, 0.98f, 12.75f)
            reflectiveQuadTo(0.83f, 12f)
            reflectiveQuadTo(0.98f, 11.25f)
            reflectiveQuadTo(1.43f, 10.58f)
            lineToRelative(7.7f, -7.7f)
            quadTo(9.5f, 2.5f, 10.01f, 2.51f)
            reflectiveQuadTo(10.9f, 2.9f)
            reflectiveQuadToRelative(0.38f, 0.88f)
            reflectiveQuadTo(10.9f, 4.65f)
            lineTo(3.55f, 12f)
            close()
          }
        }
        .build()
    return _GenArrowBackIos!!
  }

private var _GenArrowBackIos: ImageVector? = null

// ── ArrowDropDown (arrow_drop_down) ──
@Suppress("CheckReturnValue")
val SimpIcons.ArrowDropDown: ImageVector
  get() {
    if (_GenArrowDropDown != null) {
      return _GenArrowDropDown!!
    }
    _GenArrowDropDown =
      ImageVector.Builder(
          name = "ArrowDropDown",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(11.48f, 14.48f)
            lineTo(7.85f, 10.85f)
            quadTo(7.78f, 10.77f, 7.74f, 10.69f)
            reflectiveQuadTo(7.7f, 10.5f)
            quadToRelative(0f, -0.2f, 0.14f, -0.35f)
            reflectiveQuadTo(8.2f, 10f)
            horizontalLineToRelative(7.6f)
            quadToRelative(0.23f, 0f, 0.36f, 0.15f)
            reflectiveQuadTo(16.3f, 10.5f)
            quadToRelative(0f, 0.05f, -0.15f, 0.35f)
            lineToRelative(-3.63f, 3.63f)
            quadTo(12.4f, 14.6f, 12.28f, 14.65f)
            reflectiveQuadTo(12f, 14.7f)
            reflectiveQuadTo(11.73f, 14.65f)
            reflectiveQuadTo(11.48f, 14.48f)
            close()
          }
        }
        .build()
    return _GenArrowDropDown!!
  }

private var _GenArrowDropDown: ImageVector? = null

// ── AspectRatio (aspect_ratio) ──
@Suppress("CheckReturnValue")
val SimpIcons.AspectRatio: ImageVector
  get() {
    if (_GenAspectRatio != null) {
      return _GenAspectRatio!!
    }
    _GenAspectRatio =
      ImageVector.Builder(
          name = "AspectRatio",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(17f, 15f)
            horizontalLineTo(15f)
            quadToRelative(-0.42f, 0f, -0.71f, 0.29f)
            reflectiveQuadTo(14f, 16f)
            reflectiveQuadToRelative(0.29f, 0.71f)
            reflectiveQuadTo(15f, 17f)
            horizontalLineToRelative(3f)
            quadToRelative(0.43f, 0f, 0.71f, -0.29f)
            quadTo(19f, 16.43f, 19f, 16f)
            verticalLineTo(13f)
            quadToRelative(0f, -0.43f, -0.29f, -0.71f)
            reflectiveQuadTo(18f, 12f)
            reflectiveQuadToRelative(-0.71f, 0.29f)
            reflectiveQuadTo(17f, 13f)
            verticalLineToRelative(2f)
            close()
            moveTo(7f, 9f)
            horizontalLineTo(9f)
            quadTo(9.43f, 9f, 9.71f, 8.71f)
            reflectiveQuadTo(10f, 8f)
            quadTo(10f, 7.57f, 9.71f, 7.29f)
            reflectiveQuadTo(9f, 7f)
            horizontalLineTo(6f)
            quadTo(5.58f, 7f, 5.29f, 7.29f)
            reflectiveQuadTo(5f, 8f)
            verticalLineToRelative(3f)
            quadToRelative(0f, 0.42f, 0.29f, 0.71f)
            reflectiveQuadTo(6f, 12f)
            reflectiveQuadTo(6.71f, 11.71f)
            quadTo(7f, 11.43f, 7f, 11f)
            verticalLineTo(9f)
            close()
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
          }
        }
        .build()
    return _GenAspectRatio!!
  }

private var _GenAspectRatio: ImageVector? = null

// ── AutoAwesome (auto_awesome) ──
@Suppress("CheckReturnValue")
val SimpIcons.AutoAwesome: ImageVector
  get() {
    if (_GenAutoAwesome != null) {
      return _GenAutoAwesome!!
    }
    _GenAutoAwesome =
      ImageVector.Builder(
          name = "AutoAwesome",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(17.65f, 6.35f)
            lineTo(15.9f, 5.55f)
            quadTo(15.75f, 5.47f, 15.69f, 5.35f)
            reflectiveQuadTo(15.63f, 5.1f)
            reflectiveQuadTo(15.69f, 4.85f)
            reflectiveQuadTo(15.9f, 4.65f)
            lineToRelative(1.75f, -0.8f)
            lineTo(18.45f, 2.1f)
            quadToRelative(0.08f, -0.15f, 0.2f, -0.22f)
            reflectiveQuadTo(18.9f, 1.8f)
            reflectiveQuadToRelative(0.25f, 0.08f)
            reflectiveQuadToRelative(0.2f, 0.22f)
            lineToRelative(0.8f, 1.75f)
            lineToRelative(1.75f, 0.8f)
            quadToRelative(0.15f, 0.07f, 0.22f, 0.2f)
            reflectiveQuadTo(22.2f, 5.1f)
            reflectiveQuadTo(22.13f, 5.35f)
            reflectiveQuadTo(21.9f, 5.55f)
            lineToRelative(-1.75f, 0.8f)
            lineTo(19.35f, 8.1f)
            quadToRelative(-0.07f, 0.15f, -0.2f, 0.21f)
            reflectiveQuadTo(18.9f, 8.38f)
            reflectiveQuadTo(18.65f, 8.31f)
            reflectiveQuadTo(18.45f, 8.1f)
            lineTo(17.65f, 6.35f)
            close()
            moveTo(6.5f, 14.5f)
            lineToRelative(-4f, -1.83f)
            quadTo(2.28f, 12.58f, 2.18f, 12.39f)
            reflectiveQuadTo(2.08f, 12f)
            reflectiveQuadToRelative(0.1f, -0.39f)
            reflectiveQuadTo(2.5f, 11.33f)
            lineTo(6.5f, 9.5f)
            lineToRelative(1.83f, -4f)
            quadTo(8.43f, 5.27f, 8.61f, 5.18f)
            quadTo(8.8f, 5.07f, 9f, 5.07f)
            reflectiveQuadToRelative(0.39f, 0.1f)
            quadTo(9.58f, 5.27f, 9.68f, 5.5f)
            lineToRelative(1.82f, 4f)
            lineToRelative(4f, 1.82f)
            quadToRelative(0.23f, 0.1f, 0.33f, 0.29f)
            reflectiveQuadTo(15.93f, 12f)
            reflectiveQuadToRelative(-0.1f, 0.39f)
            reflectiveQuadTo(15.5f, 12.68f)
            lineToRelative(-4f, 1.83f)
            lineToRelative(-1.82f, 4f)
            quadToRelative(-0.1f, 0.23f, -0.29f, 0.32f)
            quadTo(9.2f, 18.93f, 9f, 18.93f)
            reflectiveQuadTo(8.61f, 18.83f)
            quadTo(8.43f, 18.73f, 8.33f, 18.5f)
            lineTo(6.5f, 14.5f)
            close()
            moveToRelative(11.25f, 5.75f)
            lineTo(16f, 19.45f)
            quadToRelative(-0.15f, -0.07f, -0.22f, -0.2f)
            reflectiveQuadTo(15.7f, 19f)
            reflectiveQuadToRelative(0.07f, -0.25f)
            reflectiveQuadTo(16f, 18.55f)
            lineToRelative(1.75f, -0.8f)
            lineTo(18.55f, 16f)
            quadToRelative(0.07f, -0.15f, 0.2f, -0.22f)
            reflectiveQuadTo(19f, 15.7f)
            reflectiveQuadToRelative(0.25f, 0.08f)
            reflectiveQuadTo(19.45f, 16f)
            lineToRelative(0.8f, 1.75f)
            lineTo(22f, 18.55f)
            quadToRelative(0.15f, 0.07f, 0.23f, 0.2f)
            reflectiveQuadTo(22.3f, 19f)
            reflectiveQuadToRelative(-0.07f, 0.25f)
            reflectiveQuadTo(22f, 19.45f)
            lineToRelative(-1.75f, 0.8f)
            lineTo(19.45f, 22f)
            quadToRelative(-0.08f, 0.15f, -0.2f, 0.23f)
            reflectiveQuadTo(19f, 22.3f)
            reflectiveQuadTo(18.75f, 22.23f)
            reflectiveQuadTo(18.55f, 22f)
            lineToRelative(-0.8f, -1.75f)
            close()
          }
        }
        .build()
    return _GenAutoAwesome!!
  }

private var _GenAutoAwesome: ImageVector? = null

// ── Build (build) ──
@Suppress("CheckReturnValue")
val SimpIcons.Build: ImageVector
  get() {
    if (_GenBuild != null) {
      return _GenBuild!!
    }
    _GenBuild =
      ImageVector.Builder(
          name = "Build",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(9f, 15f)
            quadTo(6.5f, 15f, 4.75f, 13.25f)
            reflectiveQuadTo(3f, 9f)
            quadTo(3f, 8.5f, 3.08f, 8f)
            reflectiveQuadTo(3.35f, 7.05f)
            quadTo(3.48f, 6.8f, 3.66f, 6.68f)
            reflectiveQuadTo(4.08f, 6.5f)
            reflectiveQuadTo(4.54f, 6.51f)
            reflectiveQuadTo(4.98f, 6.77f)
            lineTo(7.6f, 9.4f)
            lineTo(9.4f, 7.6f)
            lineTo(6.78f, 4.97f)
            quadTo(6.58f, 4.77f, 6.51f, 4.54f)
            reflectiveQuadTo(6.5f, 4.07f)
            reflectiveQuadTo(6.68f, 3.66f)
            reflectiveQuadTo(7.05f, 3.35f)
            quadTo(7.5f, 3.15f, 8f, 3.07f)
            reflectiveQuadTo(9f, 3f)
            quadToRelative(2.5f, 0f, 4.25f, 1.75f)
            reflectiveQuadTo(15f, 9f)
            quadToRelative(0f, 0.57f, -0.1f, 1.09f)
            reflectiveQuadTo(14.6f, 11.1f)
            lineToRelative(5.05f, 5f)
            quadToRelative(0.72f, 0.72f, 0.72f, 1.77f)
            reflectiveQuadToRelative(-0.72f, 1.77f)
            quadToRelative(-0.73f, 0.73f, -1.78f, 0.73f)
            reflectiveQuadTo(16.1f, 19.63f)
            lineToRelative(-5f, -5.03f)
            quadToRelative(-0.5f, 0.2f, -1.01f, 0.3f)
            quadTo(9.58f, 15f, 9f, 15f)
            close()
          }
        }
        .build()
    return _GenBuild!!
  }

private var _GenBuild: ImageVector? = null

// ── CameraAlt (photo_camera) ──
@Suppress("CheckReturnValue")
val SimpIcons.CameraAlt: ImageVector
  get() {
    if (_GenCameraAlt != null) {
      return _GenCameraAlt!!
    }
    _GenCameraAlt =
      ImageVector.Builder(
          name = "CameraAlt",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(12f, 17.5f)
            quadToRelative(1.88f, 0f, 3.19f, -1.31f)
            reflectiveQuadTo(16.5f, 13f)
            reflectiveQuadTo(15.19f, 9.81f)
            reflectiveQuadTo(12f, 8.5f)
            reflectiveQuadTo(8.81f, 9.81f)
            reflectiveQuadTo(7.5f, 13f)
            reflectiveQuadToRelative(1.31f, 3.19f)
            reflectiveQuadTo(12f, 17.5f)
            close()
            moveToRelative(0f, -2f)
            quadToRelative(-1.05f, 0f, -1.77f, -0.72f)
            reflectiveQuadTo(9.5f, 13f)
            reflectiveQuadToRelative(0.73f, -1.78f)
            reflectiveQuadTo(12f, 10.5f)
            reflectiveQuadToRelative(1.78f, 0.72f)
            reflectiveQuadTo(14.5f, 13f)
            reflectiveQuadToRelative(-0.72f, 1.78f)
            reflectiveQuadTo(12f, 15.5f)
            close()
            moveTo(4f, 21f)
            quadTo(3.18f, 21f, 2.59f, 20.41f)
            reflectiveQuadTo(2f, 19f)
            verticalLineTo(7f)
            quadTo(2f, 6.18f, 2.59f, 5.59f)
            reflectiveQuadTo(4f, 5f)
            horizontalLineTo(7.15f)
            lineTo(8.4f, 3.65f)
            quadTo(8.68f, 3.35f, 9.06f, 3.17f)
            reflectiveQuadTo(9.88f, 3f)
            horizontalLineToRelative(4.25f)
            quadToRelative(0.43f, 0f, 0.81f, 0.17f)
            reflectiveQuadTo(15.6f, 3.65f)
            lineTo(16.85f, 5f)
            horizontalLineTo(20f)
            quadToRelative(0.83f, 0f, 1.41f, 0.59f)
            quadTo(22f, 6.18f, 22f, 7f)
            verticalLineTo(19f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(20f, 21f)
            horizontalLineTo(4f)
            close()
          }
        }
        .build()
    return _GenCameraAlt!!
  }

private var _GenCameraAlt: ImageVector? = null

// ── Campaign (campaign) ──
@Suppress("CheckReturnValue")
val SimpIcons.Campaign: ImageVector
  get() {
    if (_GenCampaign != null) {
      return _GenCampaign!!
    }
    _GenCampaign =
      ImageVector.Builder(
          name = "Campaign",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(21f, 13f)
            horizontalLineTo(19f)
            quadToRelative(-0.43f, 0f, -0.71f, -0.29f)
            quadTo(18f, 12.43f, 18f, 12f)
            reflectiveQuadToRelative(0.29f, -0.71f)
            reflectiveQuadTo(19f, 11f)
            horizontalLineToRelative(2f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(22f, 12f)
            reflectiveQuadToRelative(-0.29f, 0.71f)
            reflectiveQuadTo(21f, 13f)
            close()
            moveToRelative(-4.4f, 3.8f)
            quadToRelative(0.25f, -0.35f, 0.65f, -0.4f)
            reflectiveQuadTo(18f, 16.6f)
            lineToRelative(1.6f, 1.2f)
            quadToRelative(0.35f, 0.25f, 0.4f, 0.65f)
            reflectiveQuadTo(19.8f, 19.2f)
            reflectiveQuadToRelative(-0.65f, 0.4f)
            reflectiveQuadTo(18.4f, 19.4f)
            lineTo(16.8f, 18.2f)
            quadTo(16.45f, 17.95f, 16.4f, 17.55f)
            reflectiveQuadTo(16.6f, 16.8f)
            close()
            moveToRelative(3f, -10.6f)
            lineTo(18f, 7.4f)
            quadTo(17.65f, 7.65f, 17.25f, 7.6f)
            reflectiveQuadTo(16.6f, 7.2f)
            reflectiveQuadTo(16.4f, 6.45f)
            reflectiveQuadTo(16.8f, 5.8f)
            lineTo(18.4f, 4.6f)
            quadTo(18.75f, 4.35f, 19.15f, 4.4f)
            reflectiveQuadTo(19.8f, 4.8f)
            reflectiveQuadTo(20f, 5.55f)
            reflectiveQuadTo(19.6f, 6.2f)
            close()
            moveTo(5f, 15f)
            horizontalLineTo(4f)
            quadTo(3.18f, 15f, 2.59f, 14.41f)
            reflectiveQuadTo(2f, 13f)
            verticalLineTo(11f)
            quadTo(2f, 10.17f, 2.59f, 9.59f)
            reflectiveQuadTo(4f, 9f)
            horizontalLineTo(8f)
            lineTo(11.48f, 6.9f)
            quadToRelative(0.5f, -0.3f, 1.01f, 0f)
            reflectiveQuadTo(13f, 7.77f)
            verticalLineToRelative(8.45f)
            quadToRelative(0f, 0.57f, -0.51f, 0.88f)
            reflectiveQuadToRelative(-1.01f, 0f)
            lineTo(8f, 15f)
            horizontalLineTo(7f)
            verticalLineToRelative(3f)
            quadToRelative(0f, 0.43f, -0.29f, 0.71f)
            reflectiveQuadTo(6f, 19f)
            quadTo(5.58f, 19f, 5.29f, 18.71f)
            quadTo(5f, 18.43f, 5f, 18f)
            verticalLineTo(15f)
            close()
            moveToRelative(9f, 0.35f)
            verticalLineTo(8.65f)
            quadToRelative(0.68f, 0.6f, 1.09f, 1.46f)
            quadTo(15.5f, 10.98f, 15.5f, 12f)
            quadToRelative(0f, 1.02f, -0.41f, 1.89f)
            reflectiveQuadTo(14f, 15.35f)
            close()
          }
        }
        .build()
    return _GenCampaign!!
  }

private var _GenCampaign: ImageVector? = null

// ── Cancel (cancel) ──
@Suppress("CheckReturnValue")
val SimpIcons.Cancel: ImageVector
  get() {
    if (_GenCancel != null) {
      return _GenCancel!!
    }
    _GenCancel =
      ImageVector.Builder(
          name = "Cancel",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(12f, 13.4f)
            lineToRelative(2.9f, 2.9f)
            quadToRelative(0.28f, 0.27f, 0.7f, 0.27f)
            reflectiveQuadTo(16.3f, 16.3f)
            quadToRelative(0.27f, -0.28f, 0.27f, -0.7f)
            reflectiveQuadTo(16.3f, 14.9f)
            lineTo(13.4f, 12f)
            lineTo(16.3f, 9.1f)
            quadTo(16.58f, 8.82f, 16.58f, 8.4f)
            reflectiveQuadTo(16.3f, 7.7f)
            reflectiveQuadTo(15.6f, 7.43f)
            reflectiveQuadTo(14.9f, 7.7f)
            lineTo(12f, 10.6f)
            lineTo(9.1f, 7.7f)
            quadTo(8.83f, 7.43f, 8.4f, 7.43f)
            reflectiveQuadTo(7.7f, 7.7f)
            reflectiveQuadTo(7.43f, 8.4f)
            reflectiveQuadTo(7.7f, 9.1f)
            lineTo(10.6f, 12f)
            lineTo(7.7f, 14.9f)
            quadTo(7.43f, 15.18f, 7.43f, 15.6f)
            reflectiveQuadTo(7.7f, 16.3f)
            reflectiveQuadToRelative(0.7f, 0.27f)
            quadToRelative(0.43f, 0f, 0.7f, -0.27f)
            lineTo(12f, 13.4f)
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
        }
        .build()
    return _GenCancel!!
  }

private var _GenCancel: ImageVector? = null

// ── Check (check) ──
@Suppress("CheckReturnValue")
val SimpIcons.Check: ImageVector
  get() {
    if (_GenCheck != null) {
      return _GenCheck!!
    }
    _GenCheck =
      ImageVector.Builder(
          name = "Check",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(9.55f, 15.15f)
            lineTo(18.03f, 6.68f)
            quadToRelative(0.3f, -0.3f, 0.7f, -0.3f)
            reflectiveQuadToRelative(0.7f, 0.3f)
            quadToRelative(0.3f, 0.3f, 0.3f, 0.71f)
            reflectiveQuadTo(19.43f, 8.1f)
            lineToRelative(-9.18f, 9.2f)
            quadToRelative(-0.3f, 0.3f, -0.7f, 0.3f)
            reflectiveQuadTo(8.85f, 17.3f)
            lineTo(4.55f, 13f)
            quadTo(4.25f, 12.7f, 4.26f, 12.29f)
            reflectiveQuadTo(4.58f, 11.58f)
            reflectiveQuadToRelative(0.71f, -0.3f)
            reflectiveQuadTo(6f, 11.58f)
            lineToRelative(3.55f, 3.58f)
            close()
          }
        }
        .build()
    return _GenCheck!!
  }

private var _GenCheck: ImageVector? = null

// ── CheckCircle (check_circle) ──
@Suppress("CheckReturnValue")
val SimpIcons.CheckCircle: ImageVector
  get() {
    if (_GenCheckCircle != null) {
      return _GenCheckCircle!!
    }
    _GenCheckCircle =
      ImageVector.Builder(
          name = "CheckCircle",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(10.6f, 13.8f)
            lineTo(8.45f, 11.65f)
            quadTo(8.18f, 11.38f, 7.75f, 11.38f)
            quadToRelative(-0.42f, 0f, -0.7f, 0.27f)
            quadToRelative(-0.28f, 0.28f, -0.28f, 0.7f)
            quadToRelative(0f, 0.42f, 0.28f, 0.7f)
            lineTo(9.9f, 15.9f)
            quadToRelative(0.3f, 0.3f, 0.7f, 0.3f)
            reflectiveQuadToRelative(0.7f, -0.3f)
            lineToRelative(5.65f, -5.65f)
            quadToRelative(0.28f, -0.28f, 0.28f, -0.7f)
            quadToRelative(0f, -0.42f, -0.28f, -0.7f)
            quadTo(16.68f, 8.57f, 16.25f, 8.57f)
            reflectiveQuadToRelative(-0.7f, 0.28f)
            lineTo(10.6f, 13.8f)
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
        }
        .build()
    return _GenCheckCircle!!
  }

private var _GenCheckCircle: ImageVector? = null

// ── ChevronRight (chevron_right) ──
@Suppress("CheckReturnValue")
val SimpIcons.ChevronRight: ImageVector
  get() {
    if (_GenChevronRight != null) {
      return _GenChevronRight!!
    }
    _GenChevronRight =
      ImageVector.Builder(
          name = "ChevronRight",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(12.6f, 12f)
            lineTo(8.7f, 8.1f)
            quadTo(8.43f, 7.82f, 8.43f, 7.4f)
            reflectiveQuadTo(8.7f, 6.7f)
            reflectiveQuadTo(9.4f, 6.43f)
            reflectiveQuadTo(10.1f, 6.7f)
            lineToRelative(4.6f, 4.6f)
            quadToRelative(0.15f, 0.15f, 0.21f, 0.33f)
            reflectiveQuadTo(14.98f, 12f)
            reflectiveQuadToRelative(-0.06f, 0.38f)
            reflectiveQuadTo(14.7f, 12.7f)
            lineToRelative(-4.6f, 4.6f)
            quadTo(9.83f, 17.58f, 9.4f, 17.58f)
            reflectiveQuadTo(8.7f, 17.3f)
            quadTo(8.43f, 17.02f, 8.43f, 16.6f)
            reflectiveQuadTo(8.7f, 15.9f)
            lineTo(12.6f, 12f)
            close()
          }
        }
        .build()
    return _GenChevronRight!!
  }

private var _GenChevronRight: ImageVector? = null

// ── Close (close) ──
@Suppress("CheckReturnValue")
val SimpIcons.Close: ImageVector
  get() {
    if (_GenClose != null) {
      return _GenClose!!
    }
    _GenClose =
      ImageVector.Builder(
          name = "Close",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(12f, 13.4f)
            lineTo(7.1f, 18.3f)
            quadTo(6.83f, 18.58f, 6.4f, 18.58f)
            reflectiveQuadTo(5.7f, 18.3f)
            quadTo(5.43f, 18.02f, 5.43f, 17.6f)
            reflectiveQuadTo(5.7f, 16.9f)
            lineTo(10.6f, 12f)
            lineTo(5.7f, 7.1f)
            quadTo(5.43f, 6.82f, 5.43f, 6.4f)
            reflectiveQuadTo(5.7f, 5.7f)
            reflectiveQuadTo(6.4f, 5.43f)
            reflectiveQuadTo(7.1f, 5.7f)
            lineTo(12f, 10.6f)
            lineTo(16.9f, 5.7f)
            quadTo(17.18f, 5.43f, 17.6f, 5.43f)
            reflectiveQuadTo(18.3f, 5.7f)
            reflectiveQuadToRelative(0.27f, 0.7f)
            reflectiveQuadTo(18.3f, 7.1f)
            lineTo(13.4f, 12f)
            lineToRelative(4.9f, 4.9f)
            quadToRelative(0.27f, 0.28f, 0.27f, 0.7f)
            quadToRelative(0f, 0.42f, -0.27f, 0.7f)
            reflectiveQuadToRelative(-0.7f, 0.27f)
            reflectiveQuadTo(16.9f, 18.3f)
            lineTo(12f, 13.4f)
            close()
          }
        }
        .build()
    return _GenClose!!
  }

private var _GenClose: ImageVector? = null

// ── ClosedCaption (closed_caption) ──
@Suppress("CheckReturnValue")
val SimpIcons.ClosedCaption: ImageVector
  get() {
    if (_GenClosedCaption != null) {
      return _GenClosedCaption!!
    }
    _GenClosedCaption =
      ImageVector.Builder(
          name = "ClosedCaption",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(5f, 20f)
            quadTo(4.18f, 20f, 3.59f, 19.41f)
            reflectiveQuadTo(3f, 18f)
            verticalLineTo(6f)
            quadTo(3f, 5.18f, 3.59f, 4.59f)
            reflectiveQuadTo(5f, 4f)
            horizontalLineTo(19f)
            quadToRelative(0.83f, 0f, 1.41f, 0.59f)
            quadTo(21f, 5.18f, 21f, 6f)
            verticalLineTo(18f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(19f, 20f)
            horizontalLineTo(5f)
            close()
            moveTo(7f, 15f)
            horizontalLineToRelative(3f)
            quadToRelative(0.43f, 0f, 0.71f, -0.29f)
            reflectiveQuadTo(11f, 14f)
            verticalLineTo(13.5f)
            quadToRelative(0f, -0.23f, -0.15f, -0.38f)
            reflectiveQuadTo(10.48f, 12.98f)
            horizontalLineTo(10.03f)
            quadToRelative(-0.22f, 0f, -0.38f, 0.15f)
            quadTo(9.5f, 13.27f, 9.5f, 13.5f)
            horizontalLineToRelative(-2f)
            verticalLineToRelative(-3f)
            horizontalLineToRelative(2f)
            quadToRelative(0f, 0.22f, 0.15f, 0.38f)
            quadToRelative(0.15f, 0.15f, 0.38f, 0.15f)
            horizontalLineToRelative(0.45f)
            quadToRelative(0.22f, 0f, 0.38f, -0.15f)
            reflectiveQuadTo(11f, 10.5f)
            verticalLineTo(10f)
            quadTo(11f, 9.57f, 10.71f, 9.29f)
            reflectiveQuadTo(10f, 9f)
            horizontalLineTo(7f)
            quadTo(6.58f, 9f, 6.29f, 9.29f)
            reflectiveQuadTo(6f, 10f)
            verticalLineToRelative(4f)
            quadToRelative(0f, 0.42f, 0.29f, 0.71f)
            reflectiveQuadTo(7f, 15f)
            close()
            moveTo(17f, 9f)
            horizontalLineTo(14f)
            quadTo(13.58f, 9f, 13.29f, 9.29f)
            reflectiveQuadTo(13f, 10f)
            verticalLineToRelative(4f)
            quadToRelative(0f, 0.42f, 0.29f, 0.71f)
            reflectiveQuadTo(14f, 15f)
            horizontalLineToRelative(3f)
            quadToRelative(0.43f, 0f, 0.71f, -0.29f)
            reflectiveQuadTo(18f, 14f)
            verticalLineTo(13.5f)
            quadToRelative(0f, -0.23f, -0.15f, -0.38f)
            reflectiveQuadTo(17.48f, 12.98f)
            horizontalLineTo(17.03f)
            quadToRelative(-0.23f, 0f, -0.38f, 0.15f)
            quadTo(16.5f, 13.27f, 16.5f, 13.5f)
            horizontalLineToRelative(-2f)
            verticalLineToRelative(-3f)
            horizontalLineToRelative(2f)
            quadToRelative(0f, 0.22f, 0.15f, 0.38f)
            quadToRelative(0.15f, 0.15f, 0.38f, 0.15f)
            horizontalLineToRelative(0.45f)
            quadToRelative(0.22f, 0f, 0.38f, -0.15f)
            reflectiveQuadTo(18f, 10.5f)
            verticalLineTo(10f)
            quadTo(18f, 9.57f, 17.71f, 9.29f)
            reflectiveQuadTo(17f, 9f)
            close()
          }
        }
        .build()
    return _GenClosedCaption!!
  }

private var _GenClosedCaption: ImageVector? = null

// ── ClosedCaptionOff (closed_caption_disabled) ──
@Suppress("CheckReturnValue")
val SimpIcons.ClosedCaptionOff: ImageVector
  get() {
    if (_GenClosedCaptionOff != null) {
      return _GenClosedCaptionOff!!
    }
    _GenClosedCaptionOff =
      ImageVector.Builder(
          name = "ClosedCaptionOff",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(19.78f, 22.58f)
            lineTo(17.18f, 20f)
            horizontalLineTo(5f)
            quadTo(4.18f, 20f, 3.59f, 19.41f)
            reflectiveQuadTo(3f, 18f)
            verticalLineTo(6f)
            quadTo(3f, 5.95f, 3f, 5.91f)
            quadTo(3f, 5.88f, 3f, 5.82f)
            lineTo(1.38f, 4.2f)
            quadTo(1.08f, 3.9f, 1.08f, 3.49f)
            reflectiveQuadTo(1.38f, 2.77f)
            reflectiveQuadTo(2.09f, 2.47f)
            reflectiveQuadTo(2.8f, 2.77f)
            lineToRelative(18.4f, 18.4f)
            quadToRelative(0.3f, 0.3f, 0.3f, 0.7f)
            reflectiveQuadToRelative(-0.3f, 0.7f)
            reflectiveQuadToRelative(-0.71f, 0.3f)
            reflectiveQuadToRelative(-0.71f, -0.3f)
            close()
            moveTo(7f, 15f)
            horizontalLineToRelative(3f)
            quadToRelative(0.43f, 0f, 0.71f, -0.29f)
            reflectiveQuadTo(11f, 14f)
            verticalLineTo(13.38f)
            quadToRelative(0f, -0.2f, -0.15f, -0.35f)
            quadTo(10.7f, 12.88f, 10.5f, 12.88f)
            horizontalLineTo(10f)
            quadToRelative(-0.2f, 0f, -0.35f, 0.15f)
            quadTo(9.5f, 13.18f, 9.5f, 13.38f)
            verticalLineTo(13.5f)
            horizontalLineToRelative(-2f)
            verticalLineTo(10.33f)
            lineTo(6.38f, 9.2f)
            verticalLineTo(9.23f)
            quadTo(6.2f, 9.35f, 6.1f, 9.55f)
            reflectiveQuadTo(6f, 10f)
            verticalLineToRelative(4f)
            quadToRelative(0f, 0.42f, 0.29f, 0.71f)
            reflectiveQuadTo(7f, 15f)
            close()
            moveTo(21f, 6f)
            verticalLineToRelative(9.7f)
            quadToRelative(0f, 0.68f, -0.61f, 0.94f)
            reflectiveQuadTo(19.3f, 16.43f)
            lineTo(17.65f, 14.78f)
            quadToRelative(0.17f, -0.13f, 0.26f, -0.34f)
            reflectiveQuadTo(18f, 14f)
            quadToRelative(0f, -0.05f, 0f, -0.25f)
            reflectiveQuadTo(18f, 13.5f)
            quadToRelative(0f, -0.2f, -0.15f, -0.35f)
            reflectiveQuadTo(17.5f, 13f)
            quadToRelative(-0.07f, 0f, -0.26f, 0f)
            reflectiveQuadTo(17f, 13f)
            quadToRelative(-0.2f, 0f, -0.35f, 0.15f)
            reflectiveQuadTo(16.5f, 13.5f)
            horizontalLineTo(16.38f)
            lineTo(14.5f, 11.63f)
            verticalLineTo(10.5f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(0.13f)
            quadToRelative(0f, 0.2f, 0.15f, 0.35f)
            reflectiveQuadTo(17f, 11.13f)
            horizontalLineToRelative(0.5f)
            quadToRelative(0.2f, 0f, 0.35f, -0.15f)
            reflectiveQuadTo(18f, 10.63f)
            verticalLineTo(10f)
            quadTo(18f, 9.57f, 17.71f, 9.29f)
            reflectiveQuadTo(17f, 9f)
            horizontalLineTo(14f)
            quadTo(13.58f, 9f, 13.29f, 9.29f)
            reflectiveQuadTo(13f, 10f)
            verticalLineToRelative(0.13f)
            lineTo(8.58f, 5.7f)
            quadTo(8.1f, 5.22f, 8.36f, 4.61f)
            reflectiveQuadTo(9.3f, 4f)
            horizontalLineTo(19f)
            quadToRelative(0.83f, 0f, 1.41f, 0.59f)
            quadTo(21f, 5.18f, 21f, 6f)
            close()
          }
        }
        .build()
    return _GenClosedCaptionOff!!
  }

private var _GenClosedCaptionOff: ImageVector? = null

// ── CloudDownload (cloud_download) ──
@Suppress("CheckReturnValue")
val SimpIcons.CloudDownload: ImageVector
  get() {
    if (_GenCloudDownload != null) {
      return _GenCloudDownload!!
    }
    _GenCloudDownload =
      ImageVector.Builder(
          name = "CloudDownload",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(6.5f, 20f)
            quadTo(4.23f, 20f, 2.61f, 18.43f)
            reflectiveQuadTo(1f, 14.58f)
            quadTo(1f, 12.63f, 2.18f, 11.1f)
            reflectiveQuadTo(5.25f, 9.15f)
            quadTo(5.83f, 7.13f, 7.39f, 5.75f)
            reflectiveQuadTo(11f, 4.07f)
            verticalLineToRelative(8.07f)
            lineTo(10.1f, 11.27f)
            quadTo(9.83f, 11f, 9.41f, 11f)
            reflectiveQuadTo(8.7f, 11.3f)
            quadTo(8.43f, 11.58f, 8.43f, 12f)
            reflectiveQuadTo(8.7f, 12.7f)
            lineToRelative(2.6f, 2.6f)
            quadToRelative(0.3f, 0.3f, 0.7f, 0.3f)
            reflectiveQuadToRelative(0.7f, -0.3f)
            lineToRelative(2.6f, -2.6f)
            quadToRelative(0.28f, -0.28f, 0.29f, -0.69f)
            reflectiveQuadTo(15.3f, 11.3f)
            quadTo(15.03f, 11.02f, 14.61f, 11.01f)
            reflectiveQuadTo(13.9f, 11.27f)
            lineTo(13f, 12.15f)
            verticalLineTo(4.07f)
            quadToRelative(2.58f, 0.35f, 4.29f, 2.31f)
            reflectiveQuadTo(19f, 11f)
            quadToRelative(1.73f, 0.2f, 2.86f, 1.49f)
            reflectiveQuadTo(23f, 15.5f)
            quadToRelative(0f, 1.88f, -1.31f, 3.19f)
            reflectiveQuadTo(18.5f, 20f)
            horizontalLineTo(6.5f)
            close()
          }
        }
        .build()
    return _GenCloudDownload!!
  }

private var _GenCloudDownload: ImageVector? = null

// ── ContentCut (content_cut) ──
@Suppress("CheckReturnValue")
val SimpIcons.ContentCut: ImageVector
  get() {
    if (_GenContentCut != null) {
      return _GenContentCut!!
    }
    _GenContentCut =
      ImageVector.Builder(
          name = "ContentCut",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(12f, 14f)
            lineTo(9.65f, 16.35f)
            quadToRelative(0.2f, 0.38f, 0.28f, 0.8f)
            quadTo(10f, 17.58f, 10f, 18f)
            quadToRelative(0f, 1.65f, -1.17f, 2.82f)
            reflectiveQuadTo(6f, 22f)
            reflectiveQuadTo(3.18f, 20.83f)
            reflectiveQuadTo(2f, 18f)
            reflectiveQuadTo(3.18f, 15.18f)
            reflectiveQuadTo(6f, 14f)
            quadToRelative(0.43f, 0f, 0.85f, 0.07f)
            reflectiveQuadToRelative(0.8f, 0.28f)
            lineTo(10f, 12f)
            lineTo(7.65f, 9.65f)
            quadTo(7.28f, 9.85f, 6.85f, 9.92f)
            reflectiveQuadTo(6f, 10f)
            quadTo(4.35f, 10f, 3.18f, 8.82f)
            reflectiveQuadTo(2f, 6f)
            reflectiveQuadTo(3.18f, 3.17f)
            reflectiveQuadTo(6f, 2f)
            reflectiveQuadTo(8.83f, 3.17f)
            reflectiveQuadTo(10f, 6f)
            quadTo(10f, 6.43f, 9.93f, 6.85f)
            reflectiveQuadTo(9.65f, 7.65f)
            lineTo(20.6f, 18.6f)
            quadToRelative(0.68f, 0.67f, 0.3f, 1.54f)
            reflectiveQuadTo(19.58f, 21f)
            quadTo(19.3f, 21f, 19.04f, 20.89f)
            reflectiveQuadTo(18.58f, 20.58f)
            lineTo(12f, 14f)
            close()
            moveToRelative(3f, -3f)
            lineTo(13f, 9f)
            lineTo(18.58f, 3.42f)
            quadToRelative(0.2f, -0.2f, 0.46f, -0.31f)
            reflectiveQuadTo(19.58f, 3f)
            quadToRelative(0.95f, 0f, 1.31f, 0.88f)
            reflectiveQuadTo(20.58f, 5.43f)
            lineTo(15f, 11f)
            close()
            moveTo(7.41f, 7.41f)
            quadTo(8f, 6.82f, 8f, 6f)
            reflectiveQuadTo(7.41f, 4.59f)
            reflectiveQuadTo(6f, 4f)
            reflectiveQuadTo(4.59f, 4.59f)
            quadTo(4f, 5.18f, 4f, 6f)
            reflectiveQuadTo(4.59f, 7.41f)
            reflectiveQuadTo(6f, 8f)
            quadTo(6.83f, 8f, 7.41f, 7.41f)
            close()
            moveToRelative(4.94f, 4.94f)
            quadTo(12.5f, 12.2f, 12.5f, 12f)
            reflectiveQuadTo(12.35f, 11.65f)
            reflectiveQuadTo(12f, 11.5f)
            reflectiveQuadToRelative(-0.35f, 0.15f)
            reflectiveQuadTo(11.5f, 12f)
            reflectiveQuadToRelative(0.15f, 0.35f)
            reflectiveQuadTo(12f, 12.5f)
            reflectiveQuadToRelative(0.35f, -0.15f)
            close()
            moveTo(7.41f, 19.41f)
            quadTo(8f, 18.83f, 8f, 18f)
            reflectiveQuadTo(7.41f, 16.59f)
            reflectiveQuadTo(6f, 16f)
            reflectiveQuadTo(4.59f, 16.59f)
            quadTo(4f, 17.18f, 4f, 18f)
            reflectiveQuadToRelative(0.59f, 1.41f)
            reflectiveQuadTo(6f, 20f)
            quadToRelative(0.83f, 0f, 1.41f, -0.59f)
            close()
          }
        }
        .build()
    return _GenContentCut!!
  }

private var _GenContentCut: ImageVector? = null

// ── Crop (crop) ──
@Suppress("CheckReturnValue")
val SimpIcons.Crop: ImageVector
  get() {
    if (_GenCrop != null) {
      return _GenCrop!!
    }
    _GenCrop =
      ImageVector.Builder(
          name = "Crop",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(17f, 22f)
            verticalLineTo(19f)
            horizontalLineTo(7f)
            quadTo(6.18f, 19f, 5.59f, 18.41f)
            reflectiveQuadTo(5f, 17f)
            verticalLineTo(7f)
            horizontalLineTo(2f)
            quadTo(1.58f, 7f, 1.29f, 6.71f)
            quadTo(1f, 6.43f, 1f, 6f)
            reflectiveQuadTo(1.29f, 5.29f)
            reflectiveQuadTo(2f, 5f)
            horizontalLineTo(5f)
            verticalLineTo(2f)
            quadTo(5f, 1.57f, 5.29f, 1.29f)
            reflectiveQuadTo(6f, 1f)
            reflectiveQuadTo(6.71f, 1.29f)
            reflectiveQuadTo(7f, 2f)
            verticalLineTo(17f)
            horizontalLineTo(22f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(23f, 18f)
            reflectiveQuadToRelative(-0.29f, 0.71f)
            reflectiveQuadTo(22f, 19f)
            horizontalLineTo(19f)
            verticalLineToRelative(3f)
            quadToRelative(0f, 0.43f, -0.29f, 0.71f)
            reflectiveQuadTo(18f, 23f)
            reflectiveQuadTo(17.29f, 22.71f)
            quadTo(17f, 22.43f, 17f, 22f)
            close()
            moveToRelative(0f, -7f)
            verticalLineTo(7f)
            horizontalLineTo(9f)
            verticalLineTo(5f)
            horizontalLineToRelative(8f)
            quadToRelative(0.82f, 0f, 1.41f, 0.59f)
            quadTo(19f, 6.18f, 19f, 7f)
            verticalLineToRelative(8f)
            horizontalLineTo(17f)
            close()
          }
        }
        .build()
    return _GenCrop!!
  }

private var _GenCrop: ImageVector? = null

// ── Delete (delete) ──
@Suppress("CheckReturnValue")
val SimpIcons.Delete: ImageVector
  get() {
    if (_GenDelete != null) {
      return _GenDelete!!
    }
    _GenDelete =
      ImageVector.Builder(
          name = "Delete",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(7f, 21f)
            quadTo(6.18f, 21f, 5.59f, 20.41f)
            reflectiveQuadTo(5f, 19f)
            verticalLineTo(6f)
            quadTo(4.58f, 6f, 4.29f, 5.71f)
            quadTo(4f, 5.43f, 4f, 5f)
            reflectiveQuadTo(4.29f, 4.29f)
            reflectiveQuadTo(5f, 4f)
            horizontalLineTo(9f)
            quadTo(9f, 3.57f, 9.29f, 3.29f)
            quadTo(9.58f, 3f, 10f, 3f)
            horizontalLineToRelative(4f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(15f, 4f)
            horizontalLineToRelative(4f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(20f, 5f)
            reflectiveQuadTo(19.71f, 5.71f)
            reflectiveQuadTo(19f, 6f)
            verticalLineTo(19f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(17f, 21f)
            horizontalLineTo(7f)
            close()
            moveToRelative(3.71f, -4.29f)
            quadTo(11f, 16.43f, 11f, 16f)
            verticalLineTo(9f)
            quadTo(11f, 8.57f, 10.71f, 8.29f)
            reflectiveQuadTo(10f, 8f)
            quadTo(9.58f, 8f, 9.29f, 8.29f)
            reflectiveQuadTo(9f, 9f)
            verticalLineToRelative(7f)
            quadToRelative(0f, 0.43f, 0.29f, 0.71f)
            quadTo(9.58f, 17f, 10f, 17f)
            reflectiveQuadToRelative(0.71f, -0.29f)
            close()
            moveToRelative(4f, 0f)
            quadTo(15f, 16.43f, 15f, 16f)
            verticalLineTo(9f)
            quadTo(15f, 8.57f, 14.71f, 8.29f)
            reflectiveQuadTo(14f, 8f)
            reflectiveQuadTo(13.29f, 8.29f)
            reflectiveQuadTo(13f, 9f)
            verticalLineToRelative(7f)
            quadToRelative(0f, 0.43f, 0.29f, 0.71f)
            reflectiveQuadTo(14f, 17f)
            reflectiveQuadToRelative(0.71f, -0.29f)
            close()
          }
        }
        .build()
    return _GenDelete!!
  }

private var _GenDelete: ImageVector? = null

// ── DeleteOutline (delete) ──
@Suppress("CheckReturnValue")
val SimpIcons.DeleteOutline: ImageVector
  get() {
    if (_GenDeleteOutline != null) {
      return _GenDeleteOutline!!
    }
    _GenDeleteOutline =
      ImageVector.Builder(
          name = "DeleteOutline",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(7f, 21f)
            quadTo(6.18f, 21f, 5.59f, 20.41f)
            reflectiveQuadTo(5f, 19f)
            verticalLineTo(6f)
            quadTo(4.58f, 6f, 4.29f, 5.71f)
            quadTo(4f, 5.43f, 4f, 5f)
            reflectiveQuadTo(4.29f, 4.29f)
            reflectiveQuadTo(5f, 4f)
            horizontalLineTo(9f)
            quadTo(9f, 3.57f, 9.29f, 3.29f)
            quadTo(9.58f, 3f, 10f, 3f)
            horizontalLineToRelative(4f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(15f, 4f)
            horizontalLineToRelative(4f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(20f, 5f)
            reflectiveQuadTo(19.71f, 5.71f)
            reflectiveQuadTo(19f, 6f)
            verticalLineTo(19f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(17f, 21f)
            horizontalLineTo(7f)
            close()
            moveTo(17f, 6f)
            horizontalLineTo(7f)
            verticalLineTo(19f)
            horizontalLineTo(17f)
            verticalLineTo(6f)
            close()
            moveTo(10.71f, 16.71f)
            quadTo(11f, 16.43f, 11f, 16f)
            verticalLineTo(9f)
            quadTo(11f, 8.57f, 10.71f, 8.29f)
            reflectiveQuadTo(10f, 8f)
            quadTo(9.58f, 8f, 9.29f, 8.29f)
            reflectiveQuadTo(9f, 9f)
            verticalLineToRelative(7f)
            quadToRelative(0f, 0.43f, 0.29f, 0.71f)
            quadTo(9.58f, 17f, 10f, 17f)
            reflectiveQuadToRelative(0.71f, -0.29f)
            close()
            moveToRelative(4f, 0f)
            quadTo(15f, 16.43f, 15f, 16f)
            verticalLineTo(9f)
            quadTo(15f, 8.57f, 14.71f, 8.29f)
            reflectiveQuadTo(14f, 8f)
            reflectiveQuadTo(13.29f, 8.29f)
            reflectiveQuadTo(13f, 9f)
            verticalLineToRelative(7f)
            quadToRelative(0f, 0.43f, 0.29f, 0.71f)
            reflectiveQuadTo(14f, 17f)
            reflectiveQuadToRelative(0.71f, -0.29f)
            close()
            moveTo(7f, 6f)
            verticalLineTo(19f)
            verticalLineTo(6f)
            close()
          }
        }
        .build()
    return _GenDeleteOutline!!
  }

private var _GenDeleteOutline: ImageVector? = null

// ── DeleteSweep (delete_sweep) ──
@Suppress("CheckReturnValue")
val SimpIcons.DeleteSweep: ImageVector
  get() {
    if (_GenDeleteSweep != null) {
      return _GenDeleteSweep!!
    }
    _GenDeleteSweep =
      ImageVector.Builder(
          name = "DeleteSweep",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(5f, 19f)
            quadTo(4.18f, 19f, 3.59f, 18.41f)
            reflectiveQuadTo(3f, 17f)
            verticalLineTo(8f)
            quadTo(2.58f, 8f, 2.29f, 7.71f)
            quadTo(2f, 7.43f, 2f, 7f)
            reflectiveQuadTo(2.29f, 6.29f)
            reflectiveQuadTo(3f, 6f)
            horizontalLineTo(6f)
            verticalLineTo(5.5f)
            quadTo(6f, 5.07f, 6.29f, 4.79f)
            reflectiveQuadTo(7f, 4.5f)
            horizontalLineTo(9f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(10f, 5.5f)
            verticalLineTo(6f)
            horizontalLineToRelative(3f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(14f, 7f)
            reflectiveQuadTo(13.71f, 7.71f)
            reflectiveQuadTo(13f, 8f)
            verticalLineToRelative(9f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(11f, 19f)
            horizontalLineTo(5f)
            close()
            moveTo(16f, 18f)
            quadToRelative(-0.42f, 0f, -0.71f, -0.29f)
            quadTo(15f, 17.43f, 15f, 17f)
            reflectiveQuadToRelative(0.29f, -0.71f)
            reflectiveQuadTo(16f, 16f)
            horizontalLineToRelative(2f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(19f, 17f)
            reflectiveQuadToRelative(-0.29f, 0.71f)
            reflectiveQuadTo(18f, 18f)
            horizontalLineTo(16f)
            close()
            moveToRelative(0f, -4f)
            quadToRelative(-0.42f, 0f, -0.71f, -0.29f)
            quadTo(15f, 13.43f, 15f, 13f)
            reflectiveQuadToRelative(0.29f, -0.71f)
            reflectiveQuadTo(16f, 12f)
            horizontalLineToRelative(4f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(21f, 13f)
            reflectiveQuadToRelative(-0.29f, 0.71f)
            reflectiveQuadTo(20f, 14f)
            horizontalLineTo(16f)
            close()
            moveToRelative(0f, -4f)
            quadTo(15.58f, 10f, 15.29f, 9.71f)
            reflectiveQuadTo(15f, 9f)
            quadTo(15f, 8.57f, 15.29f, 8.29f)
            reflectiveQuadTo(16f, 8f)
            horizontalLineToRelative(5f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(22f, 9f)
            quadToRelative(0f, 0.42f, -0.29f, 0.71f)
            reflectiveQuadTo(21f, 10f)
            horizontalLineTo(16f)
            close()
          }
        }
        .build()
    return _GenDeleteSweep!!
  }

private var _GenDeleteSweep: ImageVector? = null

// ── Download (download) ──
@Suppress("CheckReturnValue")
val SimpIcons.Download: ImageVector
  get() {
    if (_GenDownload != null) {
      return _GenDownload!!
    }
    _GenDownload =
      ImageVector.Builder(
          name = "Download",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(11.63f, 15.51f)
            quadTo(11.45f, 15.45f, 11.3f, 15.3f)
            lineTo(7.7f, 11.7f)
            quadTo(7.4f, 11.4f, 7.41f, 11f)
            reflectiveQuadTo(7.7f, 10.3f)
            quadTo(8f, 10f, 8.41f, 9.99f)
            quadTo(8.83f, 9.98f, 9.13f, 10.27f)
            lineTo(11f, 12.15f)
            verticalLineTo(5f)
            quadTo(11f, 4.57f, 11.29f, 4.29f)
            reflectiveQuadTo(12f, 4f)
            reflectiveQuadToRelative(0.71f, 0.29f)
            reflectiveQuadTo(13f, 5f)
            verticalLineToRelative(7.15f)
            lineToRelative(1.88f, -1.88f)
            quadToRelative(0.3f, -0.3f, 0.71f, -0.29f)
            reflectiveQuadTo(16.3f, 10.3f)
            quadToRelative(0.27f, 0.3f, 0.29f, 0.7f)
            reflectiveQuadTo(16.3f, 11.7f)
            lineToRelative(-3.6f, 3.6f)
            quadToRelative(-0.15f, 0.15f, -0.33f, 0.21f)
            reflectiveQuadTo(12f, 15.58f)
            reflectiveQuadTo(11.63f, 15.51f)
            close()
            moveTo(6f, 20f)
            quadTo(5.18f, 20f, 4.59f, 19.41f)
            reflectiveQuadTo(4f, 18f)
            verticalLineTo(16f)
            quadTo(4f, 15.58f, 4.29f, 15.29f)
            reflectiveQuadTo(5f, 15f)
            reflectiveQuadToRelative(0.71f, 0.29f)
            reflectiveQuadTo(6f, 16f)
            verticalLineToRelative(2f)
            horizontalLineTo(18f)
            verticalLineTo(16f)
            quadToRelative(0f, -0.43f, 0.29f, -0.71f)
            reflectiveQuadTo(19f, 15f)
            reflectiveQuadToRelative(0.71f, 0.29f)
            reflectiveQuadTo(20f, 16f)
            verticalLineToRelative(2f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(18f, 20f)
            horizontalLineTo(6f)
            close()
          }
        }
        .build()
    return _GenDownload!!
  }

private var _GenDownload: ImageVector? = null

// ── DownloadDone (download_done) ──
@Suppress("CheckReturnValue")
val SimpIcons.DownloadDone: ImageVector
  get() {
    if (_GenDownloadDone != null) {
      return _GenDownloadDone!!
    }
    _GenDownloadDone =
      ImageVector.Builder(
          name = "DownloadDone",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(9.55f, 13.15f)
            lineTo(18f, 4.7f)
            quadTo(18.3f, 4.4f, 18.71f, 4.4f)
            reflectiveQuadToRelative(0.71f, 0.3f)
            reflectiveQuadToRelative(0.3f, 0.71f)
            reflectiveQuadToRelative(-0.3f, 0.71f)
            lineTo(10.25f, 15.3f)
            quadToRelative(-0.3f, 0.3f, -0.7f, 0.3f)
            reflectiveQuadTo(8.85f, 15.3f)
            lineTo(4.58f, 11.02f)
            quadTo(4.28f, 10.73f, 4.29f, 10.31f)
            reflectiveQuadTo(4.6f, 9.6f)
            reflectiveQuadTo(5.31f, 9.3f)
            reflectiveQuadTo(6.03f, 9.6f)
            lineToRelative(3.53f, 3.55f)
            close()
            moveTo(6f, 20f)
            quadTo(5.58f, 20f, 5.29f, 19.71f)
            quadTo(5f, 19.43f, 5f, 19f)
            reflectiveQuadTo(5.29f, 18.29f)
            reflectiveQuadTo(6f, 18f)
            horizontalLineTo(18f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(19f, 19f)
            reflectiveQuadToRelative(-0.29f, 0.71f)
            reflectiveQuadTo(18f, 20f)
            horizontalLineTo(6f)
            close()
          }
        }
        .build()
    return _GenDownloadDone!!
  }

private var _GenDownloadDone: ImageVector? = null

// ── DownloadForOffline (download_for_offline) ──
@Suppress("CheckReturnValue")
val SimpIcons.DownloadForOffline: ImageVector
  get() {
    if (_GenDownloadForOffline != null) {
      return _GenDownloadForOffline!!
    }
    _GenDownloadForOffline =
      ImageVector.Builder(
          name = "DownloadForOffline",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(8f, 17f)
            horizontalLineToRelative(8f)
            quadToRelative(0.43f, 0f, 0.71f, -0.29f)
            quadTo(17f, 16.43f, 17f, 16f)
            reflectiveQuadTo(16.71f, 15.29f)
            reflectiveQuadTo(16f, 15f)
            horizontalLineTo(8f)
            quadTo(7.58f, 15f, 7.29f, 15.29f)
            reflectiveQuadTo(7f, 16f)
            reflectiveQuadToRelative(0.29f, 0.71f)
            reflectiveQuadTo(8f, 17f)
            close()
            moveToRelative(3f, -6.85f)
            lineTo(10.1f, 9.27f)
            quadTo(9.83f, 9f, 9.41f, 9f)
            reflectiveQuadTo(8.7f, 9.3f)
            quadTo(8.43f, 9.57f, 8.43f, 10f)
            reflectiveQuadTo(8.7f, 10.7f)
            lineToRelative(2.6f, 2.6f)
            quadToRelative(0.3f, 0.3f, 0.7f, 0.3f)
            reflectiveQuadToRelative(0.7f, -0.3f)
            lineToRelative(2.6f, -2.6f)
            quadToRelative(0.28f, -0.28f, 0.29f, -0.69f)
            reflectiveQuadTo(15.3f, 9.3f)
            quadTo(15.03f, 9.02f, 14.61f, 9.01f)
            reflectiveQuadTo(13.9f, 9.27f)
            lineTo(13f, 10.15f)
            verticalLineTo(7f)
            quadTo(13f, 6.57f, 12.71f, 6.29f)
            reflectiveQuadTo(12f, 6f)
            reflectiveQuadTo(11.29f, 6.29f)
            reflectiveQuadTo(11f, 7f)
            verticalLineToRelative(3.15f)
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
        }
        .build()
    return _GenDownloadForOffline!!
  }

private var _GenDownloadForOffline: ImageVector? = null

// ── DragHandle (drag_handle) ──
@Suppress("CheckReturnValue")
val SimpIcons.DragHandle: ImageVector
  get() {
    if (_GenDragHandle != null) {
      return _GenDragHandle!!
    }
    _GenDragHandle =
      ImageVector.Builder(
          name = "DragHandle",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(5f, 15f)
            quadTo(4.58f, 15f, 4.29f, 14.71f)
            reflectiveQuadTo(4f, 14f)
            reflectiveQuadTo(4.29f, 13.29f)
            reflectiveQuadTo(5f, 13f)
            horizontalLineTo(19f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(20f, 14f)
            reflectiveQuadToRelative(-0.29f, 0.71f)
            reflectiveQuadTo(19f, 15f)
            horizontalLineTo(5f)
            close()
            moveTo(5f, 11f)
            quadTo(4.58f, 11f, 4.29f, 10.71f)
            quadTo(4f, 10.43f, 4f, 10f)
            quadTo(4f, 9.57f, 4.29f, 9.29f)
            reflectiveQuadTo(5f, 9f)
            horizontalLineTo(19f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(20f, 10f)
            reflectiveQuadToRelative(-0.29f, 0.71f)
            reflectiveQuadTo(19f, 11f)
            horizontalLineTo(5f)
            close()
          }
        }
        .build()
    return _GenDragHandle!!
  }

private var _GenDragHandle: ImageVector? = null

// ── DragIndicator (drag_indicator) ──
@Suppress("CheckReturnValue")
val SimpIcons.DragIndicator: ImageVector
  get() {
    if (_GenDragIndicator != null) {
      return _GenDragIndicator!!
    }
    _GenDragIndicator =
      ImageVector.Builder(
          name = "DragIndicator",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(9f, 20f)
            quadTo(8.18f, 20f, 7.59f, 19.41f)
            reflectiveQuadTo(7f, 18f)
            reflectiveQuadTo(7.59f, 16.59f)
            reflectiveQuadTo(9f, 16f)
            quadToRelative(0.83f, 0f, 1.41f, 0.59f)
            quadTo(11f, 17.18f, 11f, 18f)
            reflectiveQuadToRelative(-0.59f, 1.41f)
            reflectiveQuadTo(9f, 20f)
            close()
            moveToRelative(6f, 0f)
            quadToRelative(-0.82f, 0f, -1.41f, -0.59f)
            reflectiveQuadTo(13f, 18f)
            reflectiveQuadToRelative(0.59f, -1.41f)
            reflectiveQuadTo(15f, 16f)
            reflectiveQuadToRelative(1.41f, 0.59f)
            quadTo(17f, 17.18f, 17f, 18f)
            reflectiveQuadToRelative(-0.59f, 1.41f)
            reflectiveQuadTo(15f, 20f)
            close()
            moveTo(9f, 14f)
            quadTo(8.18f, 14f, 7.59f, 13.41f)
            reflectiveQuadTo(7f, 12f)
            reflectiveQuadTo(7.59f, 10.59f)
            reflectiveQuadTo(9f, 10f)
            quadToRelative(0.83f, 0f, 1.41f, 0.59f)
            quadTo(11f, 11.18f, 11f, 12f)
            reflectiveQuadToRelative(-0.59f, 1.41f)
            reflectiveQuadTo(9f, 14f)
            close()
            moveToRelative(6f, 0f)
            quadToRelative(-0.82f, 0f, -1.41f, -0.59f)
            reflectiveQuadTo(13f, 12f)
            reflectiveQuadToRelative(0.59f, -1.41f)
            reflectiveQuadTo(15f, 10f)
            reflectiveQuadToRelative(1.41f, 0.59f)
            quadTo(17f, 11.18f, 17f, 12f)
            reflectiveQuadToRelative(-0.59f, 1.41f)
            reflectiveQuadTo(15f, 14f)
            close()
            moveTo(9f, 8f)
            quadTo(8.18f, 8f, 7.59f, 7.41f)
            reflectiveQuadTo(7f, 6f)
            reflectiveQuadTo(7.59f, 4.59f)
            reflectiveQuadTo(9f, 4f)
            quadToRelative(0.83f, 0f, 1.41f, 0.59f)
            quadTo(11f, 5.18f, 11f, 6f)
            reflectiveQuadTo(10.41f, 7.41f)
            reflectiveQuadTo(9f, 8f)
            close()
            moveToRelative(6f, 0f)
            quadTo(14.18f, 8f, 13.59f, 7.41f)
            reflectiveQuadTo(13f, 6f)
            reflectiveQuadTo(13.59f, 4.59f)
            reflectiveQuadTo(15f, 4f)
            reflectiveQuadToRelative(1.41f, 0.59f)
            quadTo(17f, 5.18f, 17f, 6f)
            reflectiveQuadTo(16.41f, 7.41f)
            reflectiveQuadTo(15f, 8f)
            close()
          }
        }
        .build()
    return _GenDragIndicator!!
  }

private var _GenDragIndicator: ImageVector? = null

// ── Edit (edit) ──
@Suppress("CheckReturnValue")
val SimpIcons.Edit: ImageVector
  get() {
    if (_GenEdit != null) {
      return _GenEdit!!
    }
    _GenEdit =
      ImageVector.Builder(
          name = "Edit",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(4f, 21f)
            quadTo(3.58f, 21f, 3.29f, 20.71f)
            quadTo(3f, 20.43f, 3f, 20f)
            verticalLineTo(17.58f)
            quadToRelative(0f, -0.4f, 0.15f, -0.76f)
            reflectiveQuadTo(3.58f, 16.18f)
            lineTo(16.2f, 3.57f)
            quadTo(16.5f, 3.3f, 16.86f, 3.15f)
            reflectiveQuadTo(17.63f, 3f)
            quadToRelative(0.4f, 0f, 0.78f, 0.15f)
            reflectiveQuadTo(19.05f, 3.6f)
            lineTo(20.43f, 5f)
            quadToRelative(0.3f, 0.27f, 0.44f, 0.65f)
            reflectiveQuadTo(21f, 6.4f)
            quadToRelative(0f, 0.4f, -0.14f, 0.76f)
            reflectiveQuadTo(20.43f, 7.82f)
            lineTo(7.83f, 20.43f)
            quadTo(7.55f, 20.7f, 7.19f, 20.85f)
            quadTo(6.83f, 21f, 6.43f, 21f)
            horizontalLineTo(4f)
            close()
            moveTo(17.6f, 7.8f)
            lineTo(19f, 6.4f)
            lineTo(17.6f, 5f)
            lineTo(16.2f, 6.4f)
            lineToRelative(1.4f, 1.4f)
            close()
          }
        }
        .build()
    return _GenEdit!!
  }

private var _GenEdit: ImageVector? = null

// ── ErrorOutline (error) ──
@Suppress("CheckReturnValue")
val SimpIcons.ErrorOutline: ImageVector
  get() {
    if (_GenErrorOutline != null) {
      return _GenErrorOutline!!
    }
    _GenErrorOutline =
      ImageVector.Builder(
          name = "ErrorOutline",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(12.71f, 16.71f)
            quadTo(13f, 16.43f, 13f, 16f)
            reflectiveQuadTo(12.71f, 15.29f)
            reflectiveQuadTo(12f, 15f)
            reflectiveQuadToRelative(-0.71f, 0.29f)
            reflectiveQuadTo(11f, 16f)
            reflectiveQuadToRelative(0.29f, 0.71f)
            reflectiveQuadTo(12f, 17f)
            reflectiveQuadToRelative(0.71f, -0.29f)
            close()
            moveToRelative(0f, -4f)
            quadTo(13f, 12.43f, 13f, 12f)
            verticalLineTo(8f)
            quadTo(13f, 7.57f, 12.71f, 7.29f)
            reflectiveQuadTo(12f, 7f)
            reflectiveQuadTo(11.29f, 7.29f)
            reflectiveQuadTo(11f, 8f)
            verticalLineToRelative(4f)
            quadToRelative(0f, 0.42f, 0.29f, 0.71f)
            reflectiveQuadTo(12f, 13f)
            reflectiveQuadToRelative(0.71f, -0.29f)
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
            moveToRelative(0f, -2f)
            quadToRelative(3.35f, 0f, 5.68f, -2.32f)
            reflectiveQuadTo(20f, 12f)
            reflectiveQuadTo(17.68f, 6.32f)
            reflectiveQuadTo(12f, 4f)
            reflectiveQuadTo(6.33f, 6.32f)
            reflectiveQuadTo(4f, 12f)
            reflectiveQuadToRelative(2.33f, 5.68f)
            reflectiveQuadTo(12f, 20f)
            close()
            moveToRelative(0f, -8f)
            close()
          }
        }
        .build()
    return _GenErrorOutline!!
  }

private var _GenErrorOutline: ImageVector? = null

// ── FastForward (fast_forward) ──
@Suppress("CheckReturnValue")
val SimpIcons.FastForward: ImageVector
  get() {
    if (_GenFastForward != null) {
      return _GenFastForward!!
    }
    _GenFastForward =
      ImageVector.Builder(
          name = "FastForward",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(2.5f, 16.13f)
            verticalLineTo(7.88f)
            quadTo(2.5f, 7.43f, 2.8f, 7.15f)
            quadTo(3.1f, 6.88f, 3.5f, 6.88f)
            quadToRelative(0.13f, 0f, 0.28f, 0.02f)
            quadTo(3.93f, 6.93f, 4.05f, 7.02f)
            lineToRelative(6.2f, 4.15f)
            quadToRelative(0.23f, 0.15f, 0.34f, 0.36f)
            reflectiveQuadTo(10.7f, 12f)
            reflectiveQuadToRelative(-0.11f, 0.46f)
            quadToRelative(-0.11f, 0.21f, -0.34f, 0.36f)
            lineToRelative(-6.2f, 4.15f)
            quadTo(3.93f, 17.08f, 3.78f, 17.1f)
            reflectiveQuadTo(3.5f, 17.13f)
            quadToRelative(-0.4f, 0f, -0.7f, -0.27f)
            reflectiveQuadTo(2.5f, 16.13f)
            close()
            moveToRelative(10f, 0f)
            verticalLineTo(7.88f)
            quadToRelative(0f, -0.45f, 0.3f, -0.73f)
            quadTo(13.1f, 6.88f, 13.5f, 6.88f)
            quadToRelative(0.13f, 0f, 0.28f, 0.02f)
            quadToRelative(0.15f, 0.03f, 0.28f, 0.13f)
            lineToRelative(6.2f, 4.15f)
            quadToRelative(0.23f, 0.15f, 0.34f, 0.36f)
            reflectiveQuadTo(20.7f, 12f)
            reflectiveQuadToRelative(-0.11f, 0.46f)
            quadToRelative(-0.11f, 0.21f, -0.34f, 0.36f)
            lineToRelative(-6.2f, 4.15f)
            quadToRelative(-0.13f, 0.1f, -0.28f, 0.13f)
            reflectiveQuadTo(13.5f, 17.13f)
            quadToRelative(-0.4f, 0f, -0.7f, -0.27f)
            reflectiveQuadTo(12.5f, 16.13f)
            close()
          }
        }
        .build()
    return _GenFastForward!!
  }

private var _GenFastForward: ImageVector? = null

// ── FastRewind (fast_rewind) ──
@Suppress("CheckReturnValue")
val SimpIcons.FastRewind: ImageVector
  get() {
    if (_GenFastRewind != null) {
      return _GenFastRewind!!
    }
    _GenFastRewind =
      ImageVector.Builder(
          name = "FastRewind",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(19.95f, 16.98f)
            lineToRelative(-6.2f, -4.15f)
            quadTo(13.53f, 12.68f, 13.41f, 12.46f)
            reflectiveQuadTo(13.3f, 12f)
            reflectiveQuadToRelative(0.11f, -0.46f)
            reflectiveQuadToRelative(0.34f, -0.36f)
            lineToRelative(6.2f, -4.15f)
            quadTo(20.08f, 6.93f, 20.23f, 6.9f)
            quadTo(20.38f, 6.88f, 20.5f, 6.88f)
            quadToRelative(0.4f, 0f, 0.7f, 0.27f)
            quadToRelative(0.3f, 0.28f, 0.3f, 0.73f)
            verticalLineToRelative(8.25f)
            quadToRelative(0f, 0.45f, -0.3f, 0.73f)
            reflectiveQuadToRelative(-0.7f, 0.27f)
            quadToRelative(-0.13f, 0f, -0.27f, -0.02f)
            reflectiveQuadTo(19.95f, 16.98f)
            close()
            moveToRelative(-10f, 0f)
            lineTo(3.75f, 12.83f)
            quadTo(3.53f, 12.68f, 3.41f, 12.46f)
            reflectiveQuadTo(3.3f, 12f)
            reflectiveQuadTo(3.41f, 11.54f)
            reflectiveQuadTo(3.75f, 11.18f)
            lineTo(9.95f, 7.02f)
            quadTo(10.08f, 6.93f, 10.23f, 6.9f)
            quadTo(10.38f, 6.88f, 10.5f, 6.88f)
            quadToRelative(0.4f, 0f, 0.7f, 0.27f)
            quadToRelative(0.3f, 0.28f, 0.3f, 0.73f)
            verticalLineToRelative(8.25f)
            quadToRelative(0f, 0.45f, -0.3f, 0.73f)
            reflectiveQuadToRelative(-0.7f, 0.27f)
            quadToRelative(-0.13f, 0f, -0.27f, -0.02f)
            reflectiveQuadTo(9.95f, 16.98f)
            close()
          }
        }
        .build()
    return _GenFastRewind!!
  }

private var _GenFastRewind: ImageVector? = null

// ── Favorite (favorite) ──
@Suppress("CheckReturnValue")
val SimpIcons.Favorite: ImageVector
  get() {
    if (_GenFavorite != null) {
      return _GenFavorite!!
    }
    _GenFavorite =
      ImageVector.Builder(
          name = "Favorite",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(11.29f, 20.2f)
            quadTo(10.93f, 20.08f, 10.65f, 19.8f)
            lineTo(8.93f, 18.23f)
            quadTo(6.28f, 15.8f, 4.14f, 13.41f)
            quadTo(2f, 11.02f, 2f, 8.15f)
            quadTo(2f, 5.8f, 3.58f, 4.22f)
            reflectiveQuadTo(7.5f, 2.65f)
            quadToRelative(1.33f, 0f, 2.5f, 0.56f)
            reflectiveQuadToRelative(2f, 1.54f)
            quadTo(12.83f, 3.77f, 14f, 3.21f)
            reflectiveQuadTo(16.5f, 2.65f)
            quadToRelative(2.35f, 0f, 3.93f, 1.57f)
            reflectiveQuadTo(22f, 8.15f)
            quadToRelative(0f, 2.88f, -2.13f, 5.28f)
            reflectiveQuadToRelative(-4.82f, 4.83f)
            lineToRelative(-1.7f, 1.55f)
            quadToRelative(-0.28f, 0.27f, -0.64f, 0.4f)
            reflectiveQuadTo(12f, 20.33f)
            reflectiveQuadTo(11.29f, 20.2f)
            close()
          }
        }
        .build()
    return _GenFavorite!!
  }

private var _GenFavorite: ImageVector? = null

// ── FavoriteBorder (favorite) ──
@Suppress("CheckReturnValue")
val SimpIcons.FavoriteBorder: ImageVector
  get() {
    if (_GenFavoriteBorder != null) {
      return _GenFavoriteBorder!!
    }
    _GenFavoriteBorder =
      ImageVector.Builder(
          name = "FavoriteBorder",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(11.29f, 20.2f)
            quadTo(10.93f, 20.08f, 10.65f, 19.8f)
            lineTo(8.93f, 18.23f)
            quadTo(6.28f, 15.8f, 4.14f, 13.41f)
            quadTo(2f, 11.02f, 2f, 8.15f)
            quadTo(2f, 5.8f, 3.58f, 4.22f)
            reflectiveQuadTo(7.5f, 2.65f)
            quadToRelative(1.33f, 0f, 2.5f, 0.56f)
            reflectiveQuadToRelative(2f, 1.54f)
            quadTo(12.83f, 3.77f, 14f, 3.21f)
            reflectiveQuadTo(16.5f, 2.65f)
            quadToRelative(2.35f, 0f, 3.93f, 1.57f)
            reflectiveQuadTo(22f, 8.15f)
            quadToRelative(0f, 2.88f, -2.13f, 5.28f)
            reflectiveQuadToRelative(-4.82f, 4.83f)
            lineToRelative(-1.7f, 1.55f)
            quadToRelative(-0.28f, 0.27f, -0.64f, 0.4f)
            reflectiveQuadTo(12f, 20.33f)
            reflectiveQuadTo(11.29f, 20.2f)
            close()
            moveTo(11.05f, 6.75f)
            quadTo(10.33f, 5.72f, 9.5f, 5.19f)
            quadTo(8.68f, 4.65f, 7.5f, 4.65f)
            quadTo(6f, 4.65f, 5f, 5.65f)
            reflectiveQuadTo(4f, 8.15f)
            quadToRelative(0f, 1.3f, 0.93f, 2.76f)
            reflectiveQuadToRelative(2.21f, 2.84f)
            reflectiveQuadToRelative(2.65f, 2.57f)
            reflectiveQuadTo(12f, 18.3f)
            quadToRelative(0.85f, -0.78f, 2.21f, -1.98f)
            reflectiveQuadToRelative(2.65f, -2.57f)
            reflectiveQuadToRelative(2.21f, -2.84f)
            reflectiveQuadTo(20f, 8.15f)
            quadToRelative(0f, -1.5f, -1f, -2.5f)
            reflectiveQuadToRelative(-2.5f, -1f)
            quadToRelative(-1.17f, 0f, -2f, 0.54f)
            quadTo(13.68f, 5.72f, 12.95f, 6.75f)
            quadTo(12.78f, 7f, 12.53f, 7.13f)
            reflectiveQuadTo(12f, 7.25f)
            reflectiveQuadTo(11.48f, 7.13f)
            reflectiveQuadTo(11.05f, 6.75f)
            close()
            moveTo(12f, 11.48f)
            close()
          }
        }
        .build()
    return _GenFavoriteBorder!!
  }

private var _GenFavoriteBorder: ImageVector? = null

// ── FormatQuote (format_quote) ──
@Suppress("CheckReturnValue")
val SimpIcons.FormatQuote: ImageVector
  get() {
    if (_GenFormatQuote != null) {
      return _GenFormatQuote!!
    }
    _GenFormatQuote =
      ImageVector.Builder(
          name = "FormatQuote",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(6.55f, 16.5f)
            lineTo(8f, 14f)
            quadTo(6.35f, 14f, 5.18f, 12.83f)
            reflectiveQuadTo(4f, 10f)
            reflectiveQuadTo(5.18f, 7.18f)
            reflectiveQuadTo(8f, 6f)
            reflectiveQuadToRelative(2.83f, 1.18f)
            reflectiveQuadTo(12f, 10f)
            quadToRelative(0f, 0.57f, -0.14f, 1.06f)
            reflectiveQuadTo(11.45f, 12f)
            lineTo(8.28f, 17.5f)
            quadTo(8.15f, 17.73f, 7.93f, 17.86f)
            reflectiveQuadTo(7.43f, 18f)
            quadTo(6.85f, 18f, 6.56f, 17.5f)
            reflectiveQuadToRelative(-0.01f, -1f)
            close()
            moveToRelative(9f, 0f)
            lineTo(17f, 14f)
            quadToRelative(-1.65f, 0f, -2.82f, -1.18f)
            reflectiveQuadTo(13f, 10f)
            reflectiveQuadTo(14.18f, 7.18f)
            reflectiveQuadTo(17f, 6f)
            reflectiveQuadToRelative(2.83f, 1.18f)
            reflectiveQuadTo(21f, 10f)
            quadToRelative(0f, 0.57f, -0.14f, 1.06f)
            reflectiveQuadTo(20.45f, 12f)
            lineToRelative(-3.18f, 5.5f)
            quadToRelative(-0.13f, 0.23f, -0.35f, 0.36f)
            reflectiveQuadTo(16.43f, 18f)
            quadToRelative(-0.57f, 0f, -0.86f, -0.5f)
            quadTo(15.28f, 17f, 15.55f, 16.5f)
            close()
          }
        }
        .build()
    return _GenFormatQuote!!
  }

private var _GenFormatQuote: ImageVector? = null

// ── GraphicEq (graphic_eq) ──
@Suppress("CheckReturnValue")
val SimpIcons.GraphicEq: ImageVector
  get() {
    if (_GenGraphicEq != null) {
      return _GenGraphicEq!!
    }
    _GenGraphicEq =
      ImageVector.Builder(
          name = "GraphicEq",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(7f, 17f)
            verticalLineTo(7f)
            quadTo(7f, 6.57f, 7.29f, 6.29f)
            reflectiveQuadTo(8f, 6f)
            reflectiveQuadTo(8.71f, 6.29f)
            reflectiveQuadTo(9f, 7f)
            verticalLineTo(17f)
            quadToRelative(0f, 0.43f, -0.29f, 0.71f)
            reflectiveQuadTo(8f, 18f)
            quadTo(7.58f, 18f, 7.29f, 17.71f)
            quadTo(7f, 17.43f, 7f, 17f)
            close()
            moveToRelative(4f, 4f)
            verticalLineTo(3f)
            quadTo(11f, 2.57f, 11.29f, 2.29f)
            reflectiveQuadTo(12f, 2f)
            reflectiveQuadToRelative(0.71f, 0.29f)
            reflectiveQuadTo(13f, 3f)
            verticalLineTo(21f)
            quadToRelative(0f, 0.43f, -0.29f, 0.71f)
            reflectiveQuadTo(12f, 22f)
            reflectiveQuadTo(11.29f, 21.71f)
            quadTo(11f, 21.43f, 11f, 21f)
            close()
            moveTo(3f, 13f)
            verticalLineTo(11f)
            quadTo(3f, 10.58f, 3.29f, 10.29f)
            reflectiveQuadTo(4f, 10f)
            reflectiveQuadToRelative(0.71f, 0.29f)
            reflectiveQuadTo(5f, 11f)
            verticalLineToRelative(2f)
            quadToRelative(0f, 0.42f, -0.29f, 0.71f)
            reflectiveQuadTo(4f, 14f)
            reflectiveQuadTo(3.29f, 13.71f)
            quadTo(3f, 13.43f, 3f, 13f)
            close()
            moveToRelative(12f, 4f)
            verticalLineTo(7f)
            quadTo(15f, 6.57f, 15.29f, 6.29f)
            reflectiveQuadTo(16f, 6f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(17f, 7f)
            verticalLineTo(17f)
            quadToRelative(0f, 0.43f, -0.29f, 0.71f)
            reflectiveQuadTo(16f, 18f)
            reflectiveQuadTo(15.29f, 17.71f)
            quadTo(15f, 17.43f, 15f, 17f)
            close()
            moveToRelative(4f, -4f)
            verticalLineTo(11f)
            quadToRelative(0f, -0.43f, 0.29f, -0.71f)
            reflectiveQuadTo(20f, 10f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(21f, 11f)
            verticalLineToRelative(2f)
            quadToRelative(0f, 0.42f, -0.29f, 0.71f)
            reflectiveQuadTo(20f, 14f)
            reflectiveQuadTo(19.29f, 13.71f)
            quadTo(19f, 13.43f, 19f, 13f)
            close()
          }
        }
        .build()
    return _GenGraphicEq!!
  }

private var _GenGraphicEq: ImageVector? = null

// ── Groups (groups) ──
@Suppress("CheckReturnValue")
val SimpIcons.Groups: ImageVector
  get() {
    if (_GenGroups != null) {
      return _GenGroups!!
    }
    _GenGroups =
      ImageVector.Builder(
          name = "Groups",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(1f, 18f)
            quadTo(0.58f, 18f, 0.29f, 17.71f)
            quadTo(0f, 17.43f, 0f, 17f)
            verticalLineTo(16.43f)
            quadTo(0f, 15.35f, 1.1f, 14.68f)
            reflectiveQuadTo(4f, 14f)
            quadToRelative(0.33f, 0f, 0.63f, 0.01f)
            reflectiveQuadTo(5.2f, 14.08f)
            quadTo(4.85f, 14.6f, 4.68f, 15.18f)
            reflectiveQuadTo(4.5f, 16.38f)
            verticalLineTo(18f)
            horizontalLineTo(1f)
            close()
            moveToRelative(6f, 0f)
            quadTo(6.58f, 18f, 6.29f, 17.71f)
            quadTo(6f, 17.43f, 6f, 17f)
            verticalLineTo(16.38f)
            quadToRelative(0f, -0.8f, 0.44f, -1.46f)
            reflectiveQuadTo(7.68f, 13.75f)
            reflectiveQuadTo(9.59f, 13f)
            reflectiveQuadTo(12f, 12.75f)
            quadToRelative(1.33f, 0f, 2.44f, 0.25f)
            reflectiveQuadToRelative(1.91f, 0.75f)
            reflectiveQuadToRelative(1.22f, 1.16f)
            reflectiveQuadTo(18f, 16.38f)
            verticalLineTo(17f)
            quadToRelative(0f, 0.43f, -0.29f, 0.71f)
            reflectiveQuadTo(17f, 18f)
            horizontalLineTo(7f)
            close()
            moveToRelative(12.5f, 0f)
            verticalLineTo(16.38f)
            quadToRelative(0f, -0.65f, -0.16f, -1.22f)
            reflectiveQuadTo(18.85f, 14.08f)
            quadToRelative(0.27f, -0.05f, 0.56f, -0.06f)
            reflectiveQuadTo(20f, 14f)
            quadToRelative(1.8f, 0f, 2.9f, 0.66f)
            reflectiveQuadTo(24f, 16.43f)
            verticalLineTo(17f)
            quadToRelative(0f, 0.43f, -0.29f, 0.71f)
            reflectiveQuadTo(23f, 18f)
            horizontalLineTo(19.5f)
            close()
            moveTo(4f, 13f)
            quadTo(3.18f, 13f, 2.59f, 12.41f)
            reflectiveQuadTo(2f, 11f)
            quadTo(2f, 10.15f, 2.59f, 9.57f)
            reflectiveQuadTo(4f, 9f)
            quadTo(4.85f, 9f, 5.43f, 9.57f)
            reflectiveQuadTo(6f, 11f)
            quadToRelative(0f, 0.82f, -0.57f, 1.41f)
            reflectiveQuadTo(4f, 13f)
            close()
            moveToRelative(16f, 0f)
            quadToRelative(-0.82f, 0f, -1.41f, -0.59f)
            reflectiveQuadTo(18f, 11f)
            quadToRelative(0f, -0.85f, 0.59f, -1.43f)
            reflectiveQuadTo(20f, 9f)
            quadToRelative(0.85f, 0f, 1.43f, 0.57f)
            reflectiveQuadTo(22f, 11f)
            quadToRelative(0f, 0.82f, -0.57f, 1.41f)
            reflectiveQuadTo(20f, 13f)
            close()
            moveTo(12f, 12f)
            quadTo(10.75f, 12f, 9.88f, 11.13f)
            reflectiveQuadTo(9f, 9f)
            quadTo(9f, 7.72f, 9.88f, 6.86f)
            reflectiveQuadTo(12f, 6f)
            quadToRelative(1.28f, 0f, 2.14f, 0.86f)
            quadTo(15f, 7.72f, 15f, 9f)
            quadToRelative(0f, 1.25f, -0.86f, 2.13f)
            reflectiveQuadTo(12f, 12f)
            close()
          }
        }
        .build()
    return _GenGroups!!
  }

private var _GenGroups: ImageVector? = null

// ── History (history) ──
@Suppress("CheckReturnValue")
val SimpIcons.History: ImageVector
  get() {
    if (_GenHistory != null) {
      return _GenHistory!!
    }
    _GenHistory =
      ImageVector.Builder(
          name = "History",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(12f, 21f)
            quadTo(8.85f, 21f, 6.43f, 19.09f)
            quadTo(4f, 17.18f, 3.28f, 14.2f)
            quadTo(3.18f, 13.83f, 3.43f, 13.51f)
            reflectiveQuadTo(4.1f, 13.15f)
            quadTo(4.5f, 13.1f, 4.83f, 13.3f)
            reflectiveQuadToRelative(0.45f, 0.6f)
            quadToRelative(0.6f, 2.25f, 2.47f, 3.68f)
            reflectiveQuadTo(12f, 19f)
            quadToRelative(2.93f, 0f, 4.96f, -2.04f)
            quadTo(19f, 14.93f, 19f, 12f)
            quadTo(19f, 9.07f, 16.96f, 7.04f)
            reflectiveQuadTo(12f, 5f)
            quadTo(10.28f, 5f, 8.78f, 5.8f)
            reflectiveQuadTo(6.25f, 8f)
            horizontalLineTo(8f)
            quadTo(8.43f, 8f, 8.71f, 8.29f)
            reflectiveQuadTo(9f, 9f)
            quadTo(9f, 9.42f, 8.71f, 9.71f)
            reflectiveQuadTo(8f, 10f)
            horizontalLineTo(4f)
            quadTo(3.58f, 10f, 3.29f, 9.71f)
            reflectiveQuadTo(3f, 9f)
            verticalLineTo(5f)
            quadTo(3f, 4.57f, 3.29f, 4.29f)
            reflectiveQuadTo(4f, 4f)
            reflectiveQuadTo(4.71f, 4.29f)
            reflectiveQuadTo(5f, 5f)
            verticalLineTo(6.35f)
            quadTo(6.28f, 4.75f, 8.11f, 3.88f)
            reflectiveQuadTo(12f, 3f)
            quadToRelative(1.88f, 0f, 3.51f, 0.71f)
            reflectiveQuadToRelative(2.85f, 1.93f)
            reflectiveQuadToRelative(1.93f, 2.85f)
            reflectiveQuadTo(21f, 12f)
            reflectiveQuadToRelative(-0.71f, 3.51f)
            reflectiveQuadToRelative(-1.93f, 2.85f)
            reflectiveQuadToRelative(-2.85f, 1.93f)
            reflectiveQuadTo(12f, 21f)
            close()
            moveToRelative(1f, -9.4f)
            lineToRelative(2.5f, 2.5f)
            quadToRelative(0.28f, 0.28f, 0.28f, 0.7f)
            quadToRelative(0f, 0.43f, -0.28f, 0.7f)
            reflectiveQuadToRelative(-0.7f, 0.28f)
            reflectiveQuadTo(14.1f, 15.5f)
            lineTo(11.3f, 12.7f)
            quadTo(11.15f, 12.55f, 11.08f, 12.36f)
            reflectiveQuadTo(11f, 11.98f)
            verticalLineTo(8f)
            quadTo(11f, 7.57f, 11.29f, 7.29f)
            reflectiveQuadTo(12f, 7f)
            reflectiveQuadToRelative(0.71f, 0.29f)
            reflectiveQuadTo(13f, 8f)
            verticalLineToRelative(3.6f)
            close()
          }
        }
        .build()
    return _GenHistory!!
  }

private var _GenHistory: ImageVector? = null

// ── Image (image) ──
@Suppress("CheckReturnValue")
val SimpIcons.Image: ImageVector
  get() {
    if (_GenImage != null) {
      return _GenImage!!
    }
    _GenImage =
      ImageVector.Builder(
          name = "Image",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
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
            moveTo(7f, 17f)
            horizontalLineTo(17f)
            quadToRelative(0.3f, 0f, 0.45f, -0.27f)
            reflectiveQuadTo(17.4f, 16.2f)
            lineTo(14.65f, 12.52f)
            quadToRelative(-0.15f, -0.2f, -0.4f, -0.2f)
            reflectiveQuadToRelative(-0.4f, 0.2f)
            lineTo(11.25f, 16f)
            lineTo(9.4f, 13.52f)
            quadTo(9.25f, 13.33f, 9f, 13.33f)
            reflectiveQuadToRelative(-0.4f, 0.2f)
            lineToRelative(-2f, 2.68f)
            quadTo(6.4f, 16.45f, 6.55f, 16.73f)
            reflectiveQuadTo(7f, 17f)
            close()
          }
        }
        .build()
    return _GenImage!!
  }

private var _GenImage: ImageVector? = null

// ── KeyboardArrowDown (keyboard_arrow_down) ──
@Suppress("CheckReturnValue")
val SimpIcons.KeyboardArrowDown: ImageVector
  get() {
    if (_GenKeyboardArrowDown != null) {
      return _GenKeyboardArrowDown!!
    }
    _GenKeyboardArrowDown =
      ImageVector.Builder(
          name = "KeyboardArrowDown",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(11.63f, 14.91f)
            quadTo(11.45f, 14.85f, 11.3f, 14.7f)
            lineTo(6.7f, 10.1f)
            quadTo(6.43f, 9.82f, 6.43f, 9.4f)
            quadTo(6.43f, 8.98f, 6.7f, 8.7f)
            reflectiveQuadTo(7.4f, 8.42f)
            reflectiveQuadTo(8.1f, 8.7f)
            lineTo(12f, 12.6f)
            lineTo(15.9f, 8.7f)
            quadTo(16.18f, 8.42f, 16.6f, 8.42f)
            reflectiveQuadTo(17.3f, 8.7f)
            reflectiveQuadToRelative(0.27f, 0.7f)
            reflectiveQuadTo(17.3f, 10.1f)
            lineToRelative(-4.6f, 4.6f)
            quadToRelative(-0.15f, 0.15f, -0.33f, 0.21f)
            reflectiveQuadTo(12f, 14.98f)
            reflectiveQuadTo(11.63f, 14.91f)
            close()
          }
        }
        .build()
    return _GenKeyboardArrowDown!!
  }

private var _GenKeyboardArrowDown: ImageVector? = null

// ── KeyboardArrowRight (keyboard_arrow_right) ──
@Suppress("CheckReturnValue")
val SimpIcons.KeyboardArrowRight: ImageVector
  get() {
    if (_GenKeyboardArrowRight != null) {
      return _GenKeyboardArrowRight!!
    }
    _GenKeyboardArrowRight =
      ImageVector.Builder(
          name = "KeyboardArrowRight",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(12.6f, 12f)
            lineTo(8.7f, 8.1f)
            quadTo(8.43f, 7.82f, 8.43f, 7.4f)
            reflectiveQuadTo(8.7f, 6.7f)
            reflectiveQuadTo(9.4f, 6.43f)
            reflectiveQuadTo(10.1f, 6.7f)
            lineToRelative(4.6f, 4.6f)
            quadToRelative(0.15f, 0.15f, 0.21f, 0.33f)
            reflectiveQuadTo(14.98f, 12f)
            reflectiveQuadToRelative(-0.06f, 0.38f)
            reflectiveQuadTo(14.7f, 12.7f)
            lineToRelative(-4.6f, 4.6f)
            quadTo(9.83f, 17.58f, 9.4f, 17.58f)
            reflectiveQuadTo(8.7f, 17.3f)
            quadTo(8.43f, 17.02f, 8.43f, 16.6f)
            reflectiveQuadTo(8.7f, 15.9f)
            lineTo(12.6f, 12f)
            close()
          }
        }
        .build()
    return _GenKeyboardArrowRight!!
  }

private var _GenKeyboardArrowRight: ImageVector? = null

// ── KeyboardDoubleArrowLeft (keyboard_double_arrow_left) ──
@Suppress("CheckReturnValue")
val SimpIcons.KeyboardDoubleArrowLeft: ImageVector
  get() {
    if (_GenKeyboardDoubleArrowLeft != null) {
      return _GenKeyboardDoubleArrowLeft!!
    }
    _GenKeyboardDoubleArrowLeft =
      ImageVector.Builder(
          name = "KeyboardDoubleArrowLeft",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(7.83f, 12f)
            lineToRelative(3.88f, 3.9f)
            quadToRelative(0.28f, 0.28f, 0.29f, 0.69f)
            reflectiveQuadTo(11.7f, 17.3f)
            quadTo(11.43f, 17.58f, 11f, 17.58f)
            reflectiveQuadTo(10.3f, 17.3f)
            lineTo(5.7f, 12.7f)
            quadTo(5.55f, 12.55f, 5.49f, 12.38f)
            reflectiveQuadTo(5.43f, 12f)
            reflectiveQuadTo(5.49f, 11.63f)
            reflectiveQuadTo(5.7f, 11.3f)
            lineTo(10.3f, 6.7f)
            quadTo(10.58f, 6.43f, 10.99f, 6.41f)
            reflectiveQuadTo(11.7f, 6.7f)
            quadToRelative(0.28f, 0.27f, 0.28f, 0.7f)
            reflectiveQuadTo(11.7f, 8.1f)
            lineTo(7.83f, 12f)
            close()
            moveToRelative(6.6f, 0f)
            lineToRelative(3.88f, 3.9f)
            quadToRelative(0.27f, 0.28f, 0.29f, 0.69f)
            reflectiveQuadTo(18.3f, 17.3f)
            quadToRelative(-0.27f, 0.27f, -0.7f, 0.27f)
            reflectiveQuadTo(16.9f, 17.3f)
            lineTo(12.3f, 12.7f)
            quadTo(12.15f, 12.55f, 12.09f, 12.38f)
            reflectiveQuadTo(12.03f, 12f)
            reflectiveQuadToRelative(0.06f, -0.38f)
            reflectiveQuadTo(12.3f, 11.3f)
            lineTo(16.9f, 6.7f)
            quadTo(17.18f, 6.43f, 17.59f, 6.41f)
            reflectiveQuadTo(18.3f, 6.7f)
            quadToRelative(0.27f, 0.27f, 0.27f, 0.7f)
            reflectiveQuadTo(18.3f, 8.1f)
            lineTo(14.43f, 12f)
            close()
          }
        }
        .build()
    return _GenKeyboardDoubleArrowLeft!!
  }

private var _GenKeyboardDoubleArrowLeft: ImageVector? = null

// ── KeyboardDoubleArrowRight (keyboard_double_arrow_right) ──
@Suppress("CheckReturnValue")
val SimpIcons.KeyboardDoubleArrowRight: ImageVector
  get() {
    if (_GenKeyboardDoubleArrowRight != null) {
      return _GenKeyboardDoubleArrowRight!!
    }
    _GenKeyboardDoubleArrowRight =
      ImageVector.Builder(
          name = "KeyboardDoubleArrowRight",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(9.58f, 12f)
            lineTo(5.7f, 8.1f)
            quadTo(5.43f, 7.82f, 5.41f, 7.41f)
            reflectiveQuadTo(5.7f, 6.7f)
            quadTo(5.98f, 6.43f, 6.4f, 6.43f)
            reflectiveQuadTo(7.1f, 6.7f)
            lineToRelative(4.6f, 4.6f)
            quadToRelative(0.15f, 0.15f, 0.21f, 0.33f)
            reflectiveQuadTo(11.98f, 12f)
            reflectiveQuadToRelative(-0.06f, 0.38f)
            reflectiveQuadTo(11.7f, 12.7f)
            lineTo(7.1f, 17.3f)
            quadTo(6.83f, 17.58f, 6.41f, 17.59f)
            reflectiveQuadTo(5.7f, 17.3f)
            quadTo(5.43f, 17.02f, 5.43f, 16.6f)
            reflectiveQuadTo(5.7f, 15.9f)
            lineTo(9.58f, 12f)
            close()
            moveToRelative(6.6f, 0f)
            lineTo(12.3f, 8.1f)
            quadTo(12.03f, 7.82f, 12.01f, 7.41f)
            reflectiveQuadTo(12.3f, 6.7f)
            quadTo(12.58f, 6.43f, 13f, 6.43f)
            reflectiveQuadTo(13.7f, 6.7f)
            lineToRelative(4.6f, 4.6f)
            quadToRelative(0.15f, 0.15f, 0.21f, 0.33f)
            reflectiveQuadTo(18.58f, 12f)
            reflectiveQuadToRelative(-0.06f, 0.38f)
            reflectiveQuadTo(18.3f, 12.7f)
            lineToRelative(-4.6f, 4.6f)
            quadToRelative(-0.28f, 0.27f, -0.69f, 0.29f)
            reflectiveQuadTo(12.3f, 17.3f)
            quadTo(12.03f, 17.02f, 12.03f, 16.6f)
            reflectiveQuadTo(12.3f, 15.9f)
            lineTo(16.18f, 12f)
            close()
          }
        }
        .build()
    return _GenKeyboardDoubleArrowRight!!
  }

private var _GenKeyboardDoubleArrowRight: ImageVector? = null

// ── Language (language) ──
@Suppress("CheckReturnValue")
val SimpIcons.Language: ImageVector
  get() {
    if (_GenLanguage != null) {
      return _GenLanguage!!
    }
    _GenLanguage =
      ImageVector.Builder(
          name = "Language",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(8.13f, 21.21f)
            quadTo(6.3f, 20.43f, 4.94f, 19.06f)
            quadTo(3.58f, 17.7f, 2.79f, 15.88f)
            reflectiveQuadTo(2f, 11.99f)
            reflectiveQuadTo(2.79f, 8.11f)
            reflectiveQuadTo(4.94f, 4.94f)
            reflectiveQuadTo(8.13f, 2.79f)
            reflectiveQuadTo(12.01f, 2f)
            reflectiveQuadToRelative(3.88f, 0.79f)
            reflectiveQuadToRelative(3.17f, 2.15f)
            reflectiveQuadToRelative(2.15f, 3.17f)
            reflectiveQuadTo(22f, 11.99f)
            reflectiveQuadToRelative(-0.79f, 3.89f)
            reflectiveQuadToRelative(-2.15f, 3.19f)
            reflectiveQuadToRelative(-3.17f, 2.15f)
            reflectiveQuadTo(12.01f, 22f)
            reflectiveQuadTo(8.13f, 21.21f)
            close()
            moveTo(12f, 19.95f)
            quadToRelative(0.65f, -0.9f, 1.13f, -1.88f)
            reflectiveQuadTo(13.9f, 16f)
            horizontalLineTo(10.1f)
            quadToRelative(0.3f, 1.1f, 0.77f, 2.07f)
            reflectiveQuadTo(12f, 19.95f)
            close()
            moveTo(9.4f, 19.55f)
            quadTo(8.95f, 18.73f, 8.61f, 17.84f)
            reflectiveQuadTo(8.05f, 16f)
            horizontalLineTo(5.1f)
            quadToRelative(0.72f, 1.25f, 1.81f, 2.18f)
            reflectiveQuadTo(9.4f, 19.55f)
            close()
            moveToRelative(5.2f, 0f)
            quadTo(16f, 19.1f, 17.09f, 18.18f)
            reflectiveQuadTo(18.9f, 16f)
            horizontalLineTo(15.95f)
            quadToRelative(-0.22f, 0.95f, -0.56f, 1.84f)
            quadToRelative(-0.34f, 0.89f, -0.79f, 1.71f)
            close()
            moveTo(4.25f, 14f)
            horizontalLineToRelative(3.4f)
            quadTo(7.58f, 13.5f, 7.54f, 13.01f)
            reflectiveQuadTo(7.5f, 12f)
            reflectiveQuadTo(7.54f, 10.99f)
            reflectiveQuadTo(7.65f, 10f)
            horizontalLineTo(4.25f)
            quadTo(4.13f, 10.5f, 4.06f, 10.99f)
            quadTo(4f, 11.48f, 4f, 12f)
            quadToRelative(0f, 0.52f, 0.06f, 1.01f)
            reflectiveQuadTo(4.25f, 14f)
            close()
            moveToRelative(5.4f, 0f)
            horizontalLineToRelative(4.7f)
            quadToRelative(0.07f, -0.5f, 0.11f, -0.99f)
            reflectiveQuadTo(14.5f, 12f)
            reflectiveQuadTo(14.46f, 10.99f)
            reflectiveQuadTo(14.35f, 10f)
            horizontalLineTo(9.65f)
            quadTo(9.58f, 10.5f, 9.54f, 10.99f)
            quadTo(9.5f, 11.48f, 9.5f, 12f)
            quadToRelative(0f, 0.52f, 0.04f, 1.01f)
            quadTo(9.58f, 13.5f, 9.65f, 14f)
            close()
            moveToRelative(6.7f, 0f)
            horizontalLineToRelative(3.4f)
            quadToRelative(0.13f, -0.5f, 0.19f, -0.99f)
            reflectiveQuadTo(20f, 12f)
            reflectiveQuadTo(19.94f, 10.99f)
            reflectiveQuadTo(19.75f, 10f)
            horizontalLineToRelative(-3.4f)
            quadToRelative(0.07f, 0.5f, 0.11f, 0.99f)
            quadTo(16.5f, 11.48f, 16.5f, 12f)
            quadToRelative(0f, 0.52f, -0.04f, 1.01f)
            reflectiveQuadTo(16.35f, 14f)
            close()
            moveTo(15.95f, 8f)
            horizontalLineTo(18.9f)
            quadTo(18.18f, 6.75f, 17.09f, 5.82f)
            reflectiveQuadTo(14.6f, 4.45f)
            quadToRelative(0.45f, 0.82f, 0.79f, 1.71f)
            reflectiveQuadTo(15.95f, 8f)
            close()
            moveTo(10.1f, 8f)
            horizontalLineToRelative(3.8f)
            quadTo(13.6f, 6.9f, 13.13f, 5.93f)
            reflectiveQuadTo(12f, 4.05f)
            quadToRelative(-0.65f, 0.9f, -1.13f, 1.88f)
            reflectiveQuadTo(10.1f, 8f)
            close()
            moveToRelative(-5f, 0f)
            horizontalLineTo(8.05f)
            quadTo(8.28f, 7.05f, 8.61f, 6.16f)
            reflectiveQuadTo(9.4f, 4.45f)
            quadTo(8f, 4.9f, 6.91f, 5.82f)
            reflectiveQuadTo(5.1f, 8f)
            close()
          }
        }
        .build()
    return _GenLanguage!!
  }

private var _GenLanguage: ImageVector? = null

// ── LibraryMusic (library_music) ──
@Suppress("CheckReturnValue")
val SimpIcons.LibraryMusic: ImageVector
  get() {
    if (_GenLibraryMusic != null) {
      return _GenLibraryMusic!!
    }
    _GenLibraryMusic =
      ImageVector.Builder(
          name = "LibraryMusic",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(12.5f, 15f)
            quadToRelative(1.05f, 0f, 1.78f, -0.73f)
            reflectiveQuadTo(15f, 12.5f)
            verticalLineTo(7f)
            horizontalLineToRelative(2f)
            quadToRelative(0.43f, 0f, 0.71f, -0.29f)
            quadTo(18f, 6.43f, 18f, 6f)
            reflectiveQuadTo(17.71f, 5.29f)
            reflectiveQuadTo(17f, 5f)
            horizontalLineTo(15f)
            quadTo(14.58f, 5f, 14.29f, 5.29f)
            reflectiveQuadTo(14f, 6f)
            verticalLineToRelative(4.5f)
            quadTo(13.68f, 10.25f, 13.3f, 10.13f)
            reflectiveQuadTo(12.5f, 10f)
            quadToRelative(-1.05f, 0f, -1.77f, 0.72f)
            reflectiveQuadTo(10f, 12.5f)
            reflectiveQuadToRelative(0.73f, 1.77f)
            reflectiveQuadTo(12.5f, 15f)
            close()
            moveTo(8f, 18f)
            quadTo(7.18f, 18f, 6.59f, 17.41f)
            reflectiveQuadTo(6f, 16f)
            verticalLineTo(4f)
            quadTo(6f, 3.17f, 6.59f, 2.59f)
            reflectiveQuadTo(8f, 2f)
            horizontalLineTo(20f)
            quadToRelative(0.83f, 0f, 1.41f, 0.59f)
            reflectiveQuadTo(22f, 4f)
            verticalLineTo(16f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(20f, 18f)
            horizontalLineTo(8f)
            close()
            moveTo(4f, 22f)
            quadTo(3.18f, 22f, 2.59f, 21.41f)
            reflectiveQuadTo(2f, 20f)
            verticalLineTo(7f)
            quadTo(2f, 6.57f, 2.29f, 6.29f)
            reflectiveQuadTo(3f, 6f)
            reflectiveQuadTo(3.71f, 6.29f)
            reflectiveQuadTo(4f, 7f)
            verticalLineTo(20f)
            horizontalLineTo(17f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(18f, 21f)
            reflectiveQuadToRelative(-0.29f, 0.71f)
            reflectiveQuadTo(17f, 22f)
            horizontalLineTo(4f)
            close()
          }
        }
        .build()
    return _GenLibraryMusic!!
  }

private var _GenLibraryMusic: ImageVector? = null

// ── Lock (lock) ──
@Suppress("CheckReturnValue")
val SimpIcons.Lock: ImageVector
  get() {
    if (_GenLock != null) {
      return _GenLock!!
    }
    _GenLock =
      ImageVector.Builder(
          name = "Lock",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(6f, 22f)
            quadTo(5.18f, 22f, 4.59f, 21.41f)
            reflectiveQuadTo(4f, 20f)
            verticalLineTo(10f)
            quadTo(4f, 9.17f, 4.59f, 8.59f)
            reflectiveQuadTo(6f, 8f)
            horizontalLineTo(7f)
            verticalLineTo(6f)
            quadTo(7f, 3.92f, 8.46f, 2.46f)
            reflectiveQuadTo(12f, 1f)
            reflectiveQuadToRelative(3.54f, 1.46f)
            reflectiveQuadTo(17f, 6f)
            verticalLineTo(8f)
            horizontalLineToRelative(1f)
            quadToRelative(0.82f, 0f, 1.41f, 0.59f)
            reflectiveQuadTo(20f, 10f)
            verticalLineTo(20f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(18f, 22f)
            horizontalLineTo(6f)
            close()
            moveToRelative(7.41f, -5.59f)
            quadTo(14f, 15.83f, 14f, 15f)
            reflectiveQuadTo(13.41f, 13.59f)
            reflectiveQuadTo(12f, 13f)
            reflectiveQuadToRelative(-1.41f, 0.59f)
            quadTo(10f, 14.18f, 10f, 15f)
            reflectiveQuadToRelative(0.59f, 1.41f)
            reflectiveQuadTo(12f, 17f)
            reflectiveQuadToRelative(1.41f, -0.59f)
            close()
            moveTo(9f, 8f)
            horizontalLineToRelative(6f)
            verticalLineTo(6f)
            quadTo(15f, 4.75f, 14.13f, 3.88f)
            reflectiveQuadTo(12f, 3f)
            reflectiveQuadTo(9.88f, 3.88f)
            reflectiveQuadTo(9f, 6f)
            verticalLineTo(8f)
            close()
          }
        }
        .build()
    return _GenLock!!
  }

private var _GenLock: ImageVector? = null

// ── Lyrics (lyrics) ──
@Suppress("CheckReturnValue")
val SimpIcons.Lyrics: ImageVector
  get() {
    if (_GenLyrics != null) {
      return _GenLyrics!!
    }
    _GenLyrics =
      ImageVector.Builder(
          name = "Lyrics",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(7f, 14f)
            horizontalLineTo(9f)
            quadToRelative(0.43f, 0f, 0.71f, -0.29f)
            quadTo(10f, 13.43f, 10f, 13f)
            reflectiveQuadTo(9.71f, 12.29f)
            reflectiveQuadTo(9f, 12f)
            horizontalLineTo(7f)
            quadTo(6.58f, 12f, 6.29f, 12.29f)
            reflectiveQuadTo(6f, 13f)
            reflectiveQuadToRelative(0.29f, 0.71f)
            reflectiveQuadTo(7f, 14f)
            close()
            moveTo(19f, 12f)
            quadToRelative(-1.25f, 0f, -2.13f, -0.88f)
            reflectiveQuadTo(16f, 9f)
            reflectiveQuadTo(16.88f, 6.88f)
            reflectiveQuadTo(19f, 6f)
            quadToRelative(0.28f, 0f, 0.53f, 0.05f)
            reflectiveQuadTo(20f, 6.18f)
            verticalLineTo(2f)
            quadTo(20f, 1.57f, 20.29f, 1.29f)
            reflectiveQuadTo(21f, 1f)
            horizontalLineToRelative(2f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(24f, 2f)
            quadToRelative(0f, 0.42f, -0.29f, 0.71f)
            reflectiveQuadTo(23f, 3f)
            horizontalLineTo(22f)
            verticalLineTo(9f)
            quadToRelative(0f, 1.25f, -0.88f, 2.13f)
            reflectiveQuadTo(19f, 12f)
            close()
            moveTo(7f, 11f)
            horizontalLineToRelative(5f)
            quadToRelative(0.43f, 0f, 0.71f, -0.29f)
            quadTo(13f, 10.43f, 13f, 10f)
            quadTo(13f, 9.57f, 12.71f, 9.29f)
            reflectiveQuadTo(12f, 9f)
            horizontalLineTo(7f)
            quadTo(6.58f, 9f, 6.29f, 9.29f)
            reflectiveQuadTo(6f, 10f)
            reflectiveQuadToRelative(0.29f, 0.71f)
            reflectiveQuadTo(7f, 11f)
            close()
            moveTo(7f, 8f)
            horizontalLineToRelative(5f)
            quadToRelative(0.43f, 0f, 0.71f, -0.29f)
            quadTo(13f, 7.43f, 13f, 7f)
            reflectiveQuadTo(12.71f, 6.29f)
            reflectiveQuadTo(12f, 6f)
            horizontalLineTo(7f)
            quadTo(6.58f, 6f, 6.29f, 6.29f)
            reflectiveQuadTo(6f, 7f)
            reflectiveQuadTo(6.29f, 7.71f)
            reflectiveQuadTo(7f, 8f)
            close()
            moveTo(6f, 18f)
            lineTo(3.7f, 20.3f)
            quadTo(3.55f, 20.45f, 3.38f, 20.53f)
            reflectiveQuadTo(3f, 20.6f)
            quadToRelative(-0.4f, 0f, -0.7f, -0.29f)
            reflectiveQuadTo(2f, 19.58f)
            verticalLineTo(4f)
            quadTo(2f, 3.17f, 2.59f, 2.59f)
            reflectiveQuadTo(4f, 2f)
            horizontalLineTo(15f)
            quadToRelative(0.78f, 0f, 1.36f, 0.47f)
            reflectiveQuadTo(16.95f, 3.7f)
            quadToRelative(0f, 0.35f, -0.16f, 0.63f)
            reflectiveQuadTo(16.35f, 4.77f)
            quadToRelative(-1.1f, 0.68f, -1.73f, 1.8f)
            reflectiveQuadTo(14f, 9f)
            quadToRelative(0f, 1.35f, 0.66f, 2.5f)
            reflectiveQuadToRelative(1.84f, 1.82f)
            quadToRelative(0.55f, 0.32f, 0.88f, 0.86f)
            reflectiveQuadToRelative(0.32f, 1.19f)
            quadToRelative(0f, 1.13f, -0.79f, 1.88f)
            reflectiveQuadTo(15f, 18f)
            horizontalLineTo(6f)
            close()
          }
        }
        .build()
    return _GenLyrics!!
  }

private var _GenLyrics: ImageVector? = null

// ── Mic (mic) ──
@Suppress("CheckReturnValue")
val SimpIcons.Mic: ImageVector
  get() {
    if (_GenMic != null) {
      return _GenMic!!
    }
    _GenMic =
      ImageVector.Builder(
          name = "Mic",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(9.88f, 13.13f)
            quadTo(9f, 12.25f, 9f, 11f)
            verticalLineTo(5f)
            quadTo(9f, 3.75f, 9.88f, 2.88f)
            reflectiveQuadTo(12f, 2f)
            reflectiveQuadToRelative(2.13f, 0.88f)
            reflectiveQuadTo(15f, 5f)
            verticalLineToRelative(6f)
            quadToRelative(0f, 1.25f, -0.88f, 2.13f)
            reflectiveQuadTo(12f, 14f)
            reflectiveQuadTo(9.88f, 13.13f)
            close()
            moveTo(11f, 20f)
            verticalLineTo(17.93f)
            quadTo(8.7f, 17.6f, 7.06f, 15.98f)
            quadTo(5.43f, 14.35f, 5.08f, 12.02f)
            quadTo(5.03f, 11.6f, 5.3f, 11.3f)
            reflectiveQuadTo(6f, 11f)
            reflectiveQuadToRelative(0.71f, 0.29f)
            reflectiveQuadTo(7.1f, 12f)
            quadToRelative(0.35f, 1.75f, 1.74f, 2.88f)
            reflectiveQuadTo(12f, 16f)
            quadToRelative(1.8f, 0f, 3.18f, -1.14f)
            quadTo(16.55f, 13.73f, 16.9f, 12f)
            quadTo(17f, 11.58f, 17.29f, 11.29f)
            reflectiveQuadTo(18f, 11f)
            reflectiveQuadToRelative(0.7f, 0.3f)
            quadToRelative(0.28f, 0.3f, 0.23f, 0.72f)
            quadToRelative(-0.35f, 2.28f, -1.98f, 3.93f)
            reflectiveQuadTo(13f, 17.93f)
            verticalLineTo(20f)
            quadToRelative(0f, 0.43f, -0.29f, 0.71f)
            reflectiveQuadTo(12f, 21f)
            reflectiveQuadTo(11.29f, 20.71f)
            quadTo(11f, 20.43f, 11f, 20f)
            close()
          }
        }
        .build()
    return _GenMic!!
  }

private var _GenMic: ImageVector? = null

// ── MicOff (mic_off) ──
@Suppress("CheckReturnValue")
val SimpIcons.MicOff: ImageVector
  get() {
    if (_GenMicOff != null) {
      return _GenMicOff!!
    }
    _GenMicOff =
      ImageVector.Builder(
          name = "MicOff",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(16.8f, 14.58f)
            quadToRelative(-0.35f, -0.2f, -0.45f, -0.61f)
            reflectiveQuadToRelative(0.1f, -0.76f)
            quadToRelative(0.18f, -0.28f, 0.29f, -0.59f)
            reflectiveQuadTo(16.9f, 11.98f)
            quadToRelative(0.1f, -0.43f, 0.39f, -0.7f)
            reflectiveQuadTo(18f, 11f)
            reflectiveQuadToRelative(0.7f, 0.3f)
            quadToRelative(0.28f, 0.3f, 0.23f, 0.72f)
            quadToRelative(-0.07f, 0.58f, -0.26f, 1.13f)
            reflectiveQuadTo(18.18f, 14.2f)
            quadToRelative(-0.2f, 0.35f, -0.61f, 0.46f)
            reflectiveQuadTo(16.8f, 14.58f)
            close()
            moveTo(13.3f, 10.45f)
            lineTo(9.58f, 6.72f)
            quadTo(9.3f, 6.45f, 9.15f, 6.09f)
            reflectiveQuadTo(9f, 5.32f)
            verticalLineTo(5f)
            quadTo(9f, 3.75f, 9.88f, 2.88f)
            reflectiveQuadTo(12f, 2f)
            reflectiveQuadToRelative(2.13f, 0.88f)
            reflectiveQuadTo(15f, 5f)
            verticalLineTo(9.73f)
            quadToRelative(0f, 0.67f, -0.61f, 0.94f)
            reflectiveQuadTo(13.3f, 10.45f)
            close()
            moveTo(11f, 20f)
            verticalLineTo(17.9f)
            quadTo(8.7f, 17.6f, 7.06f, 15.98f)
            quadTo(5.43f, 14.35f, 5.08f, 12.02f)
            quadTo(5.03f, 11.6f, 5.3f, 11.3f)
            reflectiveQuadTo(6f, 11f)
            reflectiveQuadToRelative(0.71f, 0.29f)
            reflectiveQuadTo(7.1f, 12f)
            quadToRelative(0.35f, 1.75f, 1.74f, 2.88f)
            reflectiveQuadTo(12f, 16f)
            quadToRelative(0.85f, 0f, 1.61f, -0.26f)
            quadTo(14.38f, 15.48f, 15f, 15f)
            lineToRelative(1.43f, 1.43f)
            quadTo(15.7f, 17f, 14.84f, 17.39f)
            reflectiveQuadTo(13f, 17.9f)
            verticalLineTo(20f)
            quadToRelative(0f, 0.43f, -0.29f, 0.71f)
            reflectiveQuadTo(12f, 21f)
            reflectiveQuadTo(11.29f, 20.71f)
            quadTo(11f, 20.43f, 11f, 20f)
            close()
            moveToRelative(8.1f, 1.9f)
            lineTo(2.1f, 4.9f)
            quadTo(1.83f, 4.63f, 1.83f, 4.2f)
            reflectiveQuadTo(2.1f, 3.5f)
            quadTo(2.38f, 3.22f, 2.8f, 3.22f)
            reflectiveQuadTo(3.5f, 3.5f)
            lineToRelative(17f, 17f)
            quadToRelative(0.28f, 0.27f, 0.28f, 0.7f)
            reflectiveQuadTo(20.5f, 21.9f)
            quadToRelative(-0.27f, 0.28f, -0.7f, 0.28f)
            reflectiveQuadTo(19.1f, 21.9f)
            close()
          }
        }
        .build()
    return _GenMicOff!!
  }

private var _GenMicOff: ImageVector? = null

// ── MoreVert (more_vert) ──
@Suppress("CheckReturnValue")
val SimpIcons.MoreVert: ImageVector
  get() {
    if (_GenMoreVert != null) {
      return _GenMoreVert!!
    }
    _GenMoreVert =
      ImageVector.Builder(
          name = "MoreVert",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(12f, 20f)
            quadToRelative(-0.82f, 0f, -1.41f, -0.59f)
            reflectiveQuadTo(10f, 18f)
            reflectiveQuadToRelative(0.59f, -1.41f)
            reflectiveQuadTo(12f, 16f)
            reflectiveQuadToRelative(1.41f, 0.59f)
            quadTo(14f, 17.18f, 14f, 18f)
            reflectiveQuadToRelative(-0.59f, 1.41f)
            reflectiveQuadTo(12f, 20f)
            close()
            moveToRelative(0f, -6f)
            quadToRelative(-0.82f, 0f, -1.41f, -0.59f)
            reflectiveQuadTo(10f, 12f)
            reflectiveQuadToRelative(0.59f, -1.41f)
            reflectiveQuadTo(12f, 10f)
            reflectiveQuadToRelative(1.41f, 0.59f)
            quadTo(14f, 11.18f, 14f, 12f)
            reflectiveQuadToRelative(-0.59f, 1.41f)
            reflectiveQuadTo(12f, 14f)
            close()
            moveTo(12f, 8f)
            quadTo(11.18f, 8f, 10.59f, 7.41f)
            reflectiveQuadTo(10f, 6f)
            reflectiveQuadTo(10.59f, 4.59f)
            reflectiveQuadTo(12f, 4f)
            reflectiveQuadToRelative(1.41f, 0.59f)
            quadTo(14f, 5.18f, 14f, 6f)
            reflectiveQuadTo(13.41f, 7.41f)
            reflectiveQuadTo(12f, 8f)
            close()
          }
        }
        .build()
    return _GenMoreVert!!
  }

private var _GenMoreVert: ImageVector? = null

// ── Movie (movie) ──
@Suppress("CheckReturnValue")
val SimpIcons.Movie: ImageVector
  get() {
    if (_GenMovie != null) {
      return _GenMovie!!
    }
    _GenMovie =
      ImageVector.Builder(
          name = "Movie",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(4f, 4f)
            lineTo(5.63f, 7.25f)
            quadTo(5.8f, 7.6f, 6.13f, 7.8f)
            reflectiveQuadTo(6.83f, 8f)
            quadTo(7.58f, 8f, 7.98f, 7.36f)
            quadTo(8.38f, 6.72f, 8.03f, 6.05f)
            lineTo(7f, 4f)
            horizontalLineTo(9f)
            lineToRelative(1.63f, 3.25f)
            quadTo(10.8f, 7.6f, 11.13f, 7.8f)
            reflectiveQuadTo(11.83f, 8f)
            quadToRelative(0.75f, 0f, 1.15f, -0.64f)
            quadToRelative(0.4f, -0.64f, 0.05f, -1.31f)
            lineTo(12f, 4f)
            horizontalLineToRelative(2f)
            lineToRelative(1.63f, 3.25f)
            quadTo(15.8f, 7.6f, 16.13f, 7.8f)
            reflectiveQuadTo(16.83f, 8f)
            quadToRelative(0.75f, 0f, 1.15f, -0.64f)
            quadToRelative(0.4f, -0.64f, 0.05f, -1.31f)
            lineTo(17f, 4f)
            horizontalLineToRelative(3f)
            quadToRelative(0.83f, 0f, 1.41f, 0.59f)
            quadTo(22f, 5.18f, 22f, 6f)
            verticalLineTo(18f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(20f, 20f)
            horizontalLineTo(4f)
            quadTo(3.18f, 20f, 2.59f, 19.41f)
            reflectiveQuadTo(2f, 18f)
            verticalLineTo(6f)
            quadTo(2f, 5.18f, 2.59f, 4.59f)
            reflectiveQuadTo(4f, 4f)
            close()
          }
        }
        .build()
    return _GenMovie!!
  }

private var _GenMovie: ImageVector? = null

// ── MusicNote (music_note) ──
@Suppress("CheckReturnValue")
val SimpIcons.MusicNote: ImageVector
  get() {
    if (_GenMusicNote != null) {
      return _GenMusicNote!!
    }
    _GenMusicNote =
      ImageVector.Builder(
          name = "MusicNote",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(7.18f, 19.83f)
            quadTo(6f, 18.65f, 6f, 17f)
            reflectiveQuadTo(7.18f, 14.18f)
            reflectiveQuadTo(10f, 13f)
            quadToRelative(0.58f, 0f, 1.06f, 0.14f)
            reflectiveQuadTo(12f, 13.55f)
            verticalLineTo(4f)
            quadTo(12f, 3.57f, 12.29f, 3.29f)
            reflectiveQuadTo(13f, 3f)
            horizontalLineToRelative(4f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(18f, 4f)
            verticalLineTo(6f)
            quadToRelative(0f, 0.43f, -0.29f, 0.71f)
            reflectiveQuadTo(17f, 7f)
            horizontalLineTo(14f)
            verticalLineTo(17f)
            quadToRelative(0f, 1.65f, -1.17f, 2.82f)
            reflectiveQuadTo(10f, 21f)
            reflectiveQuadTo(7.18f, 19.83f)
            close()
          }
        }
        .build()
    return _GenMusicNote!!
  }

private var _GenMusicNote: ImageVector? = null

// ── MusicOff (music_off) ──
@Suppress("CheckReturnValue")
val SimpIcons.MusicOff: ImageVector
  get() {
    if (_GenMusicOff != null) {
      return _GenMusicOff!!
    }
    _GenMusicOff =
      ImageVector.Builder(
          name = "MusicOff",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(19.1f, 21.9f)
            lineTo(2.1f, 4.9f)
            quadTo(1.83f, 4.63f, 1.83f, 4.2f)
            reflectiveQuadTo(2.1f, 3.5f)
            quadTo(2.38f, 3.22f, 2.8f, 3.22f)
            reflectiveQuadTo(3.5f, 3.5f)
            lineToRelative(17f, 17f)
            quadToRelative(0.28f, 0.27f, 0.28f, 0.7f)
            reflectiveQuadTo(20.5f, 21.9f)
            quadToRelative(-0.27f, 0.28f, -0.7f, 0.28f)
            reflectiveQuadTo(19.1f, 21.9f)
            close()
            moveTo(14f, 7f)
            verticalLineTo(8.75f)
            quadToRelative(0f, 0.5f, -0.31f, 0.74f)
            quadTo(13.38f, 9.73f, 13f, 9.73f)
            reflectiveQuadTo(12.31f, 9.48f)
            reflectiveQuadTo(12f, 8.73f)
            verticalLineTo(4f)
            quadTo(12f, 3.57f, 12.29f, 3.29f)
            reflectiveQuadTo(13f, 3f)
            horizontalLineToRelative(4f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(18f, 4f)
            verticalLineTo(6f)
            quadToRelative(0f, 0.43f, -0.29f, 0.71f)
            reflectiveQuadTo(17f, 7f)
            horizontalLineTo(14f)
            close()
            moveTo(10f, 21f)
            quadTo(8.35f, 21f, 7.18f, 19.83f)
            reflectiveQuadTo(6f, 17f)
            reflectiveQuadTo(7.18f, 14.18f)
            reflectiveQuadTo(10f, 13f)
            quadToRelative(0.58f, 0f, 1.06f, 0.14f)
            reflectiveQuadTo(12f, 13.55f)
            verticalLineTo(12f)
            lineToRelative(2f, 2f)
            verticalLineToRelative(3f)
            quadToRelative(0f, 1.65f, -1.17f, 2.82f)
            reflectiveQuadTo(10f, 21f)
            close()
          }
        }
        .build()
    return _GenMusicOff!!
  }

private var _GenMusicOff: ImageVector? = null

// ── NewReleases (new_releases) ──
@Suppress("CheckReturnValue")
val SimpIcons.NewReleases: ImageVector
  get() {
    if (_GenNewReleases != null) {
      return _GenNewReleases!!
    }
    _GenNewReleases =
      ImageVector.Builder(
          name = "NewReleases",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(10.95f, 12.7f)
            lineTo(9.5f, 11.27f)
            quadTo(9.23f, 11f, 8.81f, 11f)
            reflectiveQuadTo(8.1f, 11.3f)
            quadTo(7.83f, 11.58f, 7.83f, 12f)
            reflectiveQuadTo(8.1f, 12.7f)
            lineToRelative(2.15f, 2.15f)
            quadToRelative(0.3f, 0.3f, 0.7f, 0.3f)
            reflectiveQuadToRelative(0.7f, -0.3f)
            lineTo(15.9f, 10.6f)
            quadToRelative(0.3f, -0.3f, 0.29f, -0.7f)
            reflectiveQuadTo(15.9f, 9.2f)
            quadTo(15.6f, 8.9f, 15.19f, 8.89f)
            quadTo(14.78f, 8.88f, 14.48f, 9.17f)
            lineTo(10.95f, 12.7f)
            close()
            moveToRelative(-2.8f, 9.05f)
            lineTo(6.7f, 19.3f)
            lineTo(3.95f, 18.7f)
            quadTo(3.58f, 18.63f, 3.35f, 18.31f)
            reflectiveQuadTo(3.18f, 17.63f)
            lineTo(3.45f, 14.8f)
            lineTo(1.58f, 12.65f)
            quadTo(1.33f, 12.38f, 1.33f, 12f)
            reflectiveQuadTo(1.58f, 11.35f)
            lineTo(3.45f, 9.2f)
            lineTo(3.18f, 6.38f)
            quadTo(3.13f, 6f, 3.35f, 5.69f)
            reflectiveQuadTo(3.95f, 5.3f)
            lineTo(6.7f, 4.7f)
            lineTo(8.15f, 2.25f)
            quadTo(8.35f, 1.92f, 8.7f, 1.81f)
            reflectiveQuadTo(9.4f, 1.85f)
            lineTo(12f, 2.95f)
            lineToRelative(2.6f, -1.1f)
            quadTo(14.95f, 1.7f, 15.3f, 1.81f)
            quadToRelative(0.35f, 0.11f, 0.55f, 0.44f)
            lineTo(17.3f, 4.7f)
            lineToRelative(2.75f, 0.6f)
            quadToRelative(0.38f, 0.07f, 0.6f, 0.39f)
            reflectiveQuadToRelative(0.18f, 0.69f)
            lineTo(20.55f, 9.2f)
            lineToRelative(1.88f, 2.15f)
            quadToRelative(0.25f, 0.28f, 0.25f, 0.65f)
            reflectiveQuadToRelative(-0.25f, 0.65f)
            lineTo(20.55f, 14.8f)
            lineToRelative(0.28f, 2.83f)
            quadTo(20.88f, 18f, 20.65f, 18.31f)
            reflectiveQuadToRelative(-0.6f, 0.39f)
            lineTo(17.3f, 19.3f)
            lineToRelative(-1.45f, 2.45f)
            quadToRelative(-0.2f, 0.32f, -0.55f, 0.44f)
            reflectiveQuadTo(14.6f, 22.15f)
            lineTo(12f, 21.05f)
            lineToRelative(-2.6f, 1.1f)
            quadTo(9.05f, 22.3f, 8.7f, 22.19f)
            reflectiveQuadTo(8.15f, 21.75f)
            close()
          }
        }
        .build()
    return _GenNewReleases!!
  }

private var _GenNewReleases: ImageVector? = null

// ── OpenInFull (open_in_full) ──
@Suppress("CheckReturnValue")
val SimpIcons.OpenInFull: ImageVector
  get() {
    if (_GenOpenInFull != null) {
      return _GenOpenInFull!!
    }
    _GenOpenInFull =
      ImageVector.Builder(
          name = "OpenInFull",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(4f, 21f)
            quadTo(3.58f, 21f, 3.29f, 20.71f)
            quadTo(3f, 20.43f, 3f, 20f)
            verticalLineTo(14f)
            quadTo(3f, 13.58f, 3.29f, 13.29f)
            reflectiveQuadTo(4f, 13f)
            reflectiveQuadToRelative(0.71f, 0.29f)
            reflectiveQuadTo(5f, 14f)
            verticalLineToRelative(3.6f)
            lineTo(17.6f, 5f)
            horizontalLineTo(14f)
            quadTo(13.58f, 5f, 13.29f, 4.71f)
            reflectiveQuadTo(13f, 4f)
            quadTo(13f, 3.57f, 13.29f, 3.29f)
            reflectiveQuadTo(14f, 3f)
            horizontalLineToRelative(6f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(21f, 4f)
            verticalLineToRelative(6f)
            quadToRelative(0f, 0.42f, -0.29f, 0.71f)
            reflectiveQuadTo(20f, 11f)
            reflectiveQuadTo(19.29f, 10.71f)
            quadTo(19f, 10.43f, 19f, 10f)
            verticalLineTo(6.4f)
            lineTo(6.4f, 19f)
            horizontalLineTo(10f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(11f, 20f)
            reflectiveQuadToRelative(-0.29f, 0.71f)
            reflectiveQuadTo(10f, 21f)
            horizontalLineTo(4f)
            close()
          }
        }
        .build()
    return _GenOpenInFull!!
  }

private var _GenOpenInFull: ImageVector? = null

// ── Person (person) ──
@Suppress("CheckReturnValue")
val SimpIcons.Person: ImageVector
  get() {
    if (_GenPerson != null) {
      return _GenPerson!!
    }
    _GenPerson =
      ImageVector.Builder(
          name = "Person",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(9.18f, 10.83f)
            quadTo(8f, 9.65f, 8f, 8f)
            reflectiveQuadTo(9.18f, 5.18f)
            reflectiveQuadTo(12f, 4f)
            reflectiveQuadToRelative(2.83f, 1.18f)
            reflectiveQuadTo(16f, 8f)
            reflectiveQuadToRelative(-1.17f, 2.82f)
            reflectiveQuadTo(12f, 12f)
            reflectiveQuadTo(9.18f, 10.83f)
            close()
            moveTo(4f, 18f)
            verticalLineTo(17.2f)
            quadTo(4f, 16.35f, 4.44f, 15.64f)
            quadTo(4.88f, 14.93f, 5.6f, 14.55f)
            quadTo(7.15f, 13.77f, 8.75f, 13.39f)
            reflectiveQuadTo(12f, 13f)
            reflectiveQuadToRelative(3.25f, 0.39f)
            reflectiveQuadToRelative(3.15f, 1.16f)
            quadToRelative(0.72f, 0.38f, 1.16f, 1.09f)
            reflectiveQuadTo(20f, 17.2f)
            verticalLineTo(18f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(18f, 20f)
            horizontalLineTo(6f)
            quadTo(5.18f, 20f, 4.59f, 19.41f)
            reflectiveQuadTo(4f, 18f)
            close()
          }
        }
        .build()
    return _GenPerson!!
  }

private var _GenPerson: ImageVector? = null

// ── PhotoLibrary (photo_library) ──
@Suppress("CheckReturnValue")
val SimpIcons.PhotoLibrary: ImageVector
  get() {
    if (_GenPhotoLibrary != null) {
      return _GenPhotoLibrary!!
    }
    _GenPhotoLibrary =
      ImageVector.Builder(
          name = "PhotoLibrary",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(9f, 14f)
            horizontalLineTo(19f)
            lineTo(15.55f, 9.5f)
            lineToRelative(-2.3f, 3f)
            lineToRelative(-1.55f, -2f)
            lineTo(9f, 14f)
            close()
            moveTo(8f, 18f)
            quadTo(7.18f, 18f, 6.59f, 17.41f)
            reflectiveQuadTo(6f, 16f)
            verticalLineTo(4f)
            quadTo(6f, 3.17f, 6.59f, 2.59f)
            reflectiveQuadTo(8f, 2f)
            horizontalLineTo(20f)
            quadToRelative(0.83f, 0f, 1.41f, 0.59f)
            reflectiveQuadTo(22f, 4f)
            verticalLineTo(16f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(20f, 18f)
            horizontalLineTo(8f)
            close()
            moveTo(8f, 16f)
            horizontalLineTo(20f)
            verticalLineTo(4f)
            horizontalLineTo(8f)
            verticalLineTo(16f)
            close()
            moveTo(4f, 22f)
            quadTo(3.18f, 22f, 2.59f, 21.41f)
            reflectiveQuadTo(2f, 20f)
            verticalLineTo(6f)
            horizontalLineTo(4f)
            verticalLineTo(20f)
            horizontalLineTo(18f)
            verticalLineToRelative(2f)
            horizontalLineTo(4f)
            close()
            moveTo(8f, 4f)
            horizontalLineTo(20f)
            verticalLineTo(16f)
            horizontalLineTo(8f)
            verticalLineTo(4f)
            close()
          }
        }
        .build()
    return _GenPhotoLibrary!!
  }

private var _GenPhotoLibrary: ImageVector? = null

// ── PlayCircle (play_circle) ──
@Suppress("CheckReturnValue")
val SimpIcons.PlayCircle: ImageVector
  get() {
    if (_GenPlayCircle != null) {
      return _GenPlayCircle!!
    }
    _GenPlayCircle =
      ImageVector.Builder(
          name = "PlayCircle",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(10.65f, 15.75f)
            lineToRelative(4.88f, -3.13f)
            quadTo(15.88f, 12.4f, 15.88f, 12f)
            reflectiveQuadTo(15.53f, 11.38f)
            lineTo(10.65f, 8.25f)
            quadTo(10.28f, 8f, 9.89f, 8.21f)
            reflectiveQuadTo(9.5f, 8.88f)
            verticalLineToRelative(6.25f)
            quadToRelative(0f, 0.45f, 0.39f, 0.66f)
            reflectiveQuadToRelative(0.76f, -0.04f)
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
        }
        .build()
    return _GenPlayCircle!!
  }

private var _GenPlayCircle: ImageVector? = null

// ── PlaylistPlay (playlist_play) ──
@Suppress("CheckReturnValue")
val SimpIcons.PlaylistPlay: ImageVector
  get() {
    if (_GenPlaylistPlay != null) {
      return _GenPlaylistPlay!!
    }
    _GenPlaylistPlay =
      ImageVector.Builder(
          name = "PlaylistPlay",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
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
            moveTo(16.78f, 20.48f)
            quadToRelative(-0.13f, 0.07f, -0.25f, 0.07f)
            reflectiveQuadTo(16.28f, 20.5f)
            reflectiveQuadToRelative(-0.2f, -0.16f)
            quadTo(16f, 20.23f, 16f, 20.08f)
            verticalLineTo(13.93f)
            quadToRelative(0f, -0.15f, 0.07f, -0.26f)
            reflectiveQuadToRelative(0.2f, -0.16f)
            reflectiveQuadToRelative(0.25f, -0.05f)
            reflectiveQuadToRelative(0.25f, 0.07f)
            lineToRelative(4.6f, 3.05f)
            quadToRelative(0.13f, 0.07f, 0.18f, 0.19f)
            reflectiveQuadTo(21.6f, 17f)
            reflectiveQuadToRelative(-0.05f, 0.24f)
            reflectiveQuadToRelative(-0.18f, 0.19f)
            lineToRelative(-4.6f, 3.05f)
            close()
          }
        }
        .build()
    return _GenPlaylistPlay!!
  }

private var _GenPlaylistPlay: ImageVector? = null

// ── PushPin (keep) ──
@Suppress("CheckReturnValue")
val SimpIcons.PushPin: ImageVector
  get() {
    if (_GenPushPin != null) {
      return _GenPushPin!!
    }
    _GenPushPin =
      ImageVector.Builder(
          name = "PushPin",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(16f, 5f)
            verticalLineToRelative(7f)
            lineToRelative(1.7f, 1.7f)
            quadToRelative(0.15f, 0.15f, 0.23f, 0.34f)
            reflectiveQuadTo(18f, 14.43f)
            verticalLineTo(15f)
            quadToRelative(0f, 0.42f, -0.29f, 0.71f)
            reflectiveQuadTo(17f, 16f)
            horizontalLineTo(13f)
            verticalLineToRelative(5.85f)
            quadToRelative(0f, 0.42f, -0.29f, 0.71f)
            reflectiveQuadTo(12f, 22.85f)
            reflectiveQuadTo(11.29f, 22.56f)
            reflectiveQuadTo(11f, 21.85f)
            verticalLineTo(16f)
            horizontalLineTo(7f)
            quadTo(6.58f, 16f, 6.29f, 15.71f)
            reflectiveQuadTo(6f, 15f)
            verticalLineTo(14.43f)
            quadToRelative(0f, -0.2f, 0.08f, -0.39f)
            reflectiveQuadTo(6.3f, 13.7f)
            lineTo(8f, 12f)
            verticalLineTo(5f)
            quadTo(7.58f, 5f, 7.29f, 4.71f)
            reflectiveQuadTo(7f, 4f)
            quadTo(7f, 3.57f, 7.29f, 3.29f)
            reflectiveQuadTo(8f, 3f)
            horizontalLineToRelative(8f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(17f, 4f)
            quadToRelative(0f, 0.42f, -0.29f, 0.71f)
            reflectiveQuadTo(16f, 5f)
            close()
          }
        }
        .build()
    return _GenPushPin!!
  }

private var _GenPushPin: ImageVector? = null

// ── QueuePlayNext (queue_play_next) ──
@Suppress("CheckReturnValue")
val SimpIcons.QueuePlayNext: ImageVector
  get() {
    if (_GenQueuePlayNext != null) {
      return _GenQueuePlayNext!!
    }
    _GenQueuePlayNext =
      ImageVector.Builder(
          name = "QueuePlayNext",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(11f, 12f)
            verticalLineToRelative(2f)
            quadToRelative(0f, 0.42f, 0.29f, 0.71f)
            reflectiveQuadTo(12f, 15f)
            reflectiveQuadToRelative(0.71f, -0.29f)
            reflectiveQuadTo(13f, 14f)
            verticalLineTo(12f)
            horizontalLineToRelative(2f)
            quadToRelative(0.43f, 0f, 0.71f, -0.29f)
            quadTo(16f, 11.43f, 16f, 11f)
            reflectiveQuadTo(15.71f, 10.29f)
            reflectiveQuadTo(15f, 10f)
            horizontalLineTo(13f)
            verticalLineTo(8f)
            quadTo(13f, 7.57f, 12.71f, 7.29f)
            reflectiveQuadTo(12f, 7f)
            reflectiveQuadTo(11.29f, 7.29f)
            reflectiveQuadTo(11f, 8f)
            verticalLineToRelative(2f)
            horizontalLineTo(9f)
            quadTo(8.58f, 10f, 8.29f, 10.29f)
            reflectiveQuadTo(8f, 11f)
            reflectiveQuadToRelative(0.29f, 0.71f)
            quadTo(8.58f, 12f, 9f, 12f)
            horizontalLineToRelative(2f)
            close()
            moveToRelative(10f, 6f)
            lineTo(18.75f, 15.75f)
            quadTo(18.43f, 15.43f, 18.43f, 15f)
            reflectiveQuadToRelative(0.32f, -0.75f)
            reflectiveQuadTo(19.5f, 13.93f)
            reflectiveQuadToRelative(0.75f, 0.33f)
            lineTo(23.3f, 17.3f)
            quadToRelative(0.3f, 0.3f, 0.3f, 0.7f)
            reflectiveQuadToRelative(-0.3f, 0.7f)
            lineToRelative(-3.05f, 3.05f)
            quadToRelative(-0.32f, 0.32f, -0.75f, 0.32f)
            reflectiveQuadTo(18.75f, 21.75f)
            reflectiveQuadTo(18.43f, 21f)
            reflectiveQuadToRelative(0.32f, -0.75f)
            lineTo(21f, 18f)
            close()
            moveTo(9f, 21f)
            quadTo(8.58f, 21f, 8.29f, 20.71f)
            quadTo(8f, 20.43f, 8f, 20f)
            verticalLineTo(19f)
            horizontalLineTo(4f)
            quadTo(3.18f, 19f, 2.59f, 18.41f)
            reflectiveQuadTo(2f, 17f)
            verticalLineTo(5f)
            quadTo(2f, 4.17f, 2.59f, 3.59f)
            reflectiveQuadTo(4f, 3f)
            horizontalLineTo(20f)
            quadToRelative(0.83f, 0f, 1.41f, 0.59f)
            reflectiveQuadTo(22f, 5f)
            verticalLineToRelative(6f)
            quadToRelative(0f, 0.42f, -0.29f, 0.71f)
            reflectiveQuadTo(21f, 12f)
            horizontalLineTo(19f)
            quadToRelative(-0.82f, 0f, -1.41f, 0.59f)
            quadTo(17f, 13.18f, 17f, 14f)
            verticalLineToRelative(4f)
            quadToRelative(0f, 0.43f, -0.29f, 0.71f)
            reflectiveQuadTo(16f, 19f)
            horizontalLineTo(15f)
            verticalLineToRelative(1f)
            quadToRelative(0f, 0.43f, -0.29f, 0.71f)
            reflectiveQuadTo(14f, 21f)
            horizontalLineTo(9f)
            close()
          }
        }
        .build()
    return _GenQueuePlayNext!!
  }

private var _GenQueuePlayNext: ImageVector? = null

// ── Radio (radio) ──
@Suppress("CheckReturnValue")
val SimpIcons.Radio: ImageVector
  get() {
    if (_GenRadio != null) {
      return _GenRadio!!
    }
    _GenRadio =
      ImageVector.Builder(
          name = "Radio",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(4f, 22f)
            quadTo(3.18f, 22f, 2.59f, 21.41f)
            reflectiveQuadTo(2f, 20f)
            verticalLineTo(6.65f)
            lineTo(15.08f, 1.32f)
            quadTo(15.43f, 1.2f, 15.76f, 1.34f)
            reflectiveQuadToRelative(0.46f, 0.49f)
            quadToRelative(0.13f, 0.35f, -0.01f, 0.69f)
            reflectiveQuadTo(15.73f, 2.97f)
            lineTo(8.3f, 6f)
            horizontalLineTo(20f)
            quadToRelative(0.83f, 0f, 1.41f, 0.59f)
            quadTo(22f, 7.18f, 22f, 8f)
            verticalLineTo(20f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(20f, 22f)
            horizontalLineTo(4f)
            close()
            moveTo(9.78f, 18.27f)
            quadTo(10.5f, 17.55f, 10.5f, 16.5f)
            reflectiveQuadTo(9.78f, 14.73f)
            reflectiveQuadTo(8f, 14f)
            reflectiveQuadTo(6.23f, 14.73f)
            reflectiveQuadTo(5.5f, 16.5f)
            reflectiveQuadToRelative(0.73f, 1.77f)
            reflectiveQuadTo(8f, 19f)
            reflectiveQuadTo(9.78f, 18.27f)
            close()
            moveTo(4f, 11f)
            horizontalLineTo(16f)
            verticalLineTo(10f)
            quadTo(16f, 9.57f, 16.29f, 9.29f)
            reflectiveQuadTo(17f, 9f)
            reflectiveQuadToRelative(0.71f, 0.29f)
            reflectiveQuadTo(18f, 10f)
            verticalLineToRelative(1f)
            horizontalLineToRelative(2f)
            verticalLineTo(8f)
            horizontalLineTo(4f)
            verticalLineToRelative(3f)
            close()
          }
        }
        .build()
    return _GenRadio!!
  }

private var _GenRadio: ImageVector? = null

// ── Refresh (refresh) ──
@Suppress("CheckReturnValue")
val SimpIcons.Refresh: ImageVector
  get() {
    if (_GenRefresh != null) {
      return _GenRefresh!!
    }
    _GenRefresh =
      ImageVector.Builder(
          name = "Refresh",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(12f, 20f)
            quadTo(8.65f, 20f, 6.33f, 17.68f)
            reflectiveQuadTo(4f, 12f)
            reflectiveQuadTo(6.33f, 6.32f)
            reflectiveQuadTo(12f, 4f)
            quadToRelative(1.73f, 0f, 3.3f, 0.71f)
            quadTo(16.88f, 5.43f, 18f, 6.75f)
            verticalLineTo(5f)
            quadTo(18f, 4.57f, 18.29f, 4.29f)
            reflectiveQuadTo(19f, 4f)
            reflectiveQuadToRelative(0.71f, 0.29f)
            reflectiveQuadTo(20f, 5f)
            verticalLineToRelative(5f)
            quadToRelative(0f, 0.42f, -0.29f, 0.71f)
            reflectiveQuadTo(19f, 11f)
            horizontalLineTo(14f)
            quadToRelative(-0.42f, 0f, -0.71f, -0.29f)
            quadTo(13f, 10.43f, 13f, 10f)
            quadTo(13f, 9.57f, 13.29f, 9.29f)
            reflectiveQuadTo(14f, 9f)
            horizontalLineToRelative(3.2f)
            quadTo(16.4f, 7.6f, 15.01f, 6.8f)
            reflectiveQuadTo(12f, 6f)
            quadTo(9.5f, 6f, 7.75f, 7.75f)
            reflectiveQuadTo(6f, 12f)
            reflectiveQuadToRelative(1.75f, 4.25f)
            reflectiveQuadTo(12f, 18f)
            quadToRelative(1.7f, 0f, 3.11f, -0.86f)
            quadToRelative(1.41f, -0.86f, 2.19f, -2.31f)
            quadToRelative(0.2f, -0.35f, 0.56f, -0.49f)
            reflectiveQuadTo(18.6f, 14.33f)
            quadToRelative(0.4f, 0.13f, 0.57f, 0.53f)
            reflectiveQuadTo(19.15f, 15.6f)
            quadToRelative(-1.03f, 2f, -2.93f, 3.2f)
            reflectiveQuadTo(12f, 20f)
            close()
          }
        }
        .build()
    return _GenRefresh!!
  }

private var _GenRefresh: ImageVector? = null

// ── RemoveCircleOutline (do_not_disturb_on) ──
@Suppress("CheckReturnValue")
val SimpIcons.RemoveCircleOutline: ImageVector
  get() {
    if (_GenRemoveCircleOutline != null) {
      return _GenRemoveCircleOutline!!
    }
    _GenRemoveCircleOutline =
      ImageVector.Builder(
          name = "RemoveCircleOutline",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(8f, 13f)
            horizontalLineToRelative(8f)
            quadToRelative(0.43f, 0f, 0.71f, -0.29f)
            quadTo(17f, 12.43f, 17f, 12f)
            reflectiveQuadTo(16.71f, 11.29f)
            reflectiveQuadTo(16f, 11f)
            horizontalLineTo(8f)
            quadTo(7.58f, 11f, 7.29f, 11.29f)
            reflectiveQuadTo(7f, 12f)
            reflectiveQuadToRelative(0.29f, 0.71f)
            reflectiveQuadTo(8f, 13f)
            close()
            moveToRelative(4f, 9f)
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
            moveToRelative(0f, -2f)
            quadToRelative(3.35f, 0f, 5.68f, -2.32f)
            reflectiveQuadTo(20f, 12f)
            reflectiveQuadTo(17.68f, 6.32f)
            reflectiveQuadTo(12f, 4f)
            reflectiveQuadTo(6.33f, 6.32f)
            reflectiveQuadTo(4f, 12f)
            reflectiveQuadToRelative(2.33f, 5.68f)
            reflectiveQuadTo(12f, 20f)
            close()
            moveToRelative(0f, -8f)
            close()
          }
        }
        .build()
    return _GenRemoveCircleOutline!!
  }

private var _GenRemoveCircleOutline: ImageVector? = null

// ── Replay (replay) ──
@Suppress("CheckReturnValue")
val SimpIcons.Replay: ImageVector
  get() {
    if (_GenReplay != null) {
      return _GenReplay!!
    }
    _GenReplay =
      ImageVector.Builder(
          name = "Replay",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(8.49f, 21.29f)
            quadTo(6.85f, 20.58f, 5.64f, 19.36f)
            reflectiveQuadTo(3.71f, 16.51f)
            reflectiveQuadTo(3f, 13f)
            quadTo(3f, 12.58f, 3.29f, 12.29f)
            reflectiveQuadTo(4f, 12f)
            reflectiveQuadToRelative(0.71f, 0.29f)
            reflectiveQuadTo(5f, 13f)
            quadToRelative(0f, 2.92f, 2.04f, 4.96f)
            reflectiveQuadTo(12f, 20f)
            reflectiveQuadToRelative(4.96f, -2.04f)
            quadTo(19f, 15.93f, 19f, 13f)
            quadTo(19f, 10.07f, 16.96f, 8.04f)
            reflectiveQuadTo(12f, 6f)
            horizontalLineTo(11.85f)
            lineTo(12.7f, 6.85f)
            quadToRelative(0.3f, 0.3f, 0.29f, 0.7f)
            reflectiveQuadTo(12.7f, 8.25f)
            quadToRelative(-0.3f, 0.3f, -0.71f, 0.31f)
            quadTo(11.58f, 8.57f, 11.28f, 8.27f)
            lineTo(8.7f, 5.7f)
            quadTo(8.4f, 5.4f, 8.4f, 5f)
            reflectiveQuadTo(8.7f, 4.3f)
            lineTo(11.28f, 1.72f)
            quadToRelative(0.3f, -0.3f, 0.71f, -0.29f)
            reflectiveQuadTo(12.7f, 1.75f)
            quadToRelative(0.28f, 0.3f, 0.29f, 0.7f)
            reflectiveQuadTo(12.7f, 3.15f)
            lineTo(11.85f, 4f)
            horizontalLineTo(12f)
            quadToRelative(1.88f, 0f, 3.51f, 0.71f)
            quadToRelative(1.64f, 0.71f, 2.85f, 1.93f)
            reflectiveQuadToRelative(1.93f, 2.85f)
            reflectiveQuadTo(21f, 13f)
            reflectiveQuadToRelative(-0.71f, 3.51f)
            reflectiveQuadToRelative(-1.93f, 2.85f)
            reflectiveQuadToRelative(-2.85f, 1.93f)
            reflectiveQuadTo(12f, 22f)
            reflectiveQuadTo(8.49f, 21.29f)
            close()
          }
        }
        .build()
    return _GenReplay!!
  }

private var _GenReplay: ImageVector? = null

// ── RotateRight (rotate_right) ──
@Suppress("CheckReturnValue")
val SimpIcons.RotateRight: ImageVector
  get() {
    if (_GenRotateRight != null) {
      return _GenRotateRight!!
    }
    _GenRotateRight =
      ImageVector.Builder(
          name = "RotateRight",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(19.75f, 12.05f)
            quadToRelative(-0.3f, 0f, -0.59f, -0.21f)
            quadTo(18.88f, 11.63f, 18.8f, 11.3f)
            quadTo(18.68f, 10.83f, 18.49f, 10.36f)
            reflectiveQuadTo(18.05f, 9.48f)
            quadTo(17.88f, 9.2f, 17.93f, 8.86f)
            reflectiveQuadTo(18.2f, 8.3f)
            quadTo(18.58f, 7.95f, 19.06f, 8f)
            reflectiveQuadTo(19.8f, 8.48f)
            quadToRelative(0.32f, 0.55f, 0.57f, 1.13f)
            quadToRelative(0.25f, 0.57f, 0.4f, 1.2f)
            quadToRelative(0.13f, 0.5f, -0.19f, 0.88f)
            reflectiveQuadToRelative(-0.84f, 0.38f)
            close()
            moveToRelative(-6.7f, 8.7f)
            quadToRelative(0f, -0.3f, 0.21f, -0.59f)
            reflectiveQuadTo(13.8f, 19.8f)
            quadToRelative(0.47f, -0.13f, 0.94f, -0.31f)
            reflectiveQuadToRelative(0.89f, -0.44f)
            quadToRelative(0.28f, -0.18f, 0.61f, -0.13f)
            reflectiveQuadTo(16.8f, 19.2f)
            quadToRelative(0.35f, 0.38f, 0.3f, 0.86f)
            reflectiveQuadTo(16.63f, 20.8f)
            quadToRelative(-0.57f, 0.32f, -1.15f, 0.56f)
            reflectiveQuadTo(14.3f, 21.78f)
            quadTo(13.8f, 21.9f, 13.43f, 21.6f)
            reflectiveQuadTo(13.05f, 20.75f)
            close()
            moveTo(18.2f, 17.8f)
            quadTo(17.98f, 17.6f, 17.93f, 17.25f)
            reflectiveQuadToRelative(0.13f, -0.63f)
            quadTo(18.3f, 16.2f, 18.49f, 15.74f)
            quadTo(18.68f, 15.28f, 18.8f, 14.8f)
            quadToRelative(0.07f, -0.32f, 0.35f, -0.54f)
            reflectiveQuadToRelative(0.6f, -0.21f)
            quadToRelative(0.55f, 0f, 0.85f, 0.38f)
            reflectiveQuadToRelative(0.18f, 0.88f)
            quadToRelative(-0.18f, 0.6f, -0.41f, 1.18f)
            reflectiveQuadTo(19.8f, 17.63f)
            quadToRelative(-0.25f, 0.43f, -0.74f, 0.48f)
            reflectiveQuadTo(18.2f, 17.8f)
            close()
            moveTo(9.83f, 21.75f)
            quadTo(6.85f, 20.95f, 4.95f, 18.55f)
            reflectiveQuadToRelative(-1.9f, -5.5f)
            quadTo(3.05f, 9.3f, 5.68f, 6.68f)
            reflectiveQuadTo(12.05f, 4.05f)
            horizontalLineTo(12.2f)
            lineTo(11.33f, 3.17f)
            quadTo(11.05f, 2.9f, 11.05f, 2.47f)
            reflectiveQuadToRelative(0.3f, -0.72f)
            quadToRelative(0.27f, -0.28f, 0.7f, -0.28f)
            reflectiveQuadToRelative(0.72f, 0.28f)
            lineToRelative(2.58f, 2.6f)
            quadTo(15.5f, 4.5f, 15.56f, 4.67f)
            reflectiveQuadToRelative(0.06f, 0.38f)
            reflectiveQuadTo(15.56f, 5.43f)
            reflectiveQuadTo(15.35f, 5.75f)
            lineToRelative(-2.6f, 2.6f)
            quadToRelative(-0.27f, 0.28f, -0.7f, 0.28f)
            reflectiveQuadTo(11.35f, 8.35f)
            reflectiveQuadTo(11.08f, 7.65f)
            reflectiveQuadToRelative(0.28f, -0.7f)
            lineToRelative(0.9f, -0.9f)
            horizontalLineToRelative(-0.2f)
            quadToRelative(-2.93f, 0f, -4.96f, 2.04f)
            quadTo(5.05f, 10.13f, 5.05f, 13.05f)
            quadToRelative(0f, 2.43f, 1.47f, 4.29f)
            reflectiveQuadTo(10.3f, 19.8f)
            quadToRelative(0.32f, 0.07f, 0.54f, 0.35f)
            quadToRelative(0.21f, 0.28f, 0.21f, 0.6f)
            quadToRelative(0f, 0.52f, -0.36f, 0.82f)
            reflectiveQuadTo(9.83f, 21.75f)
            close()
          }
        }
        .build()
    return _GenRotateRight!!
  }

private var _GenRotateRight: ImageVector? = null

// ── Schedule (schedule) ──
@Suppress("CheckReturnValue")
val SimpIcons.Schedule: ImageVector
  get() {
    if (_GenSchedule != null) {
      return _GenSchedule!!
    }
    _GenSchedule =
      ImageVector.Builder(
          name = "Schedule",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(13f, 11.6f)
            verticalLineTo(8f)
            quadTo(13f, 7.57f, 12.71f, 7.29f)
            reflectiveQuadTo(12f, 7f)
            reflectiveQuadTo(11.29f, 7.29f)
            reflectiveQuadTo(11f, 8f)
            verticalLineToRelative(3.97f)
            quadToRelative(0f, 0.2f, 0.08f, 0.39f)
            reflectiveQuadTo(11.3f, 12.7f)
            lineTo(14.6f, 16f)
            quadToRelative(0.27f, 0.27f, 0.7f, 0.27f)
            reflectiveQuadTo(16f, 16f)
            quadToRelative(0.28f, -0.28f, 0.28f, -0.7f)
            quadToRelative(0f, -0.42f, -0.28f, -0.7f)
            lineToRelative(-3f, -3f)
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
        }
        .build()
    return _GenSchedule!!
  }

private var _GenSchedule: ImageVector? = null

// ── SearchOff (search_off) ──
@Suppress("CheckReturnValue")
val SimpIcons.SearchOff: ImageVector
  get() {
    if (_GenSearchOff != null) {
      return _GenSearchOff!!
    }
    _GenSearchOff =
      ImageVector.Builder(
          name = "SearchOff",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(7f, 17.7f)
            lineToRelative(1.4f, 1.43f)
            quadToRelative(0.15f, 0.15f, 0.35f, 0.15f)
            reflectiveQuadTo(9.1f, 19.13f)
            reflectiveQuadTo(9.25f, 18.76f)
            reflectiveQuadTo(9.1f, 18.4f)
            lineTo(7.7f, 17f)
            lineTo(9.13f, 15.58f)
            quadTo(9.28f, 15.43f, 9.28f, 15.23f)
            reflectiveQuadTo(9.13f, 14.88f)
            reflectiveQuadTo(8.78f, 14.73f)
            reflectiveQuadTo(8.43f, 14.88f)
            lineTo(7f, 16.3f)
            lineTo(5.58f, 14.88f)
            quadTo(5.43f, 14.73f, 5.23f, 14.73f)
            reflectiveQuadTo(4.88f, 14.88f)
            quadTo(4.73f, 15.03f, 4.73f, 15.23f)
            reflectiveQuadToRelative(0.15f, 0.35f)
            lineTo(6.3f, 17f)
            lineTo(4.88f, 18.43f)
            quadTo(4.73f, 18.58f, 4.73f, 18.77f)
            quadToRelative(0f, 0.2f, 0.15f, 0.35f)
            quadToRelative(0.15f, 0.15f, 0.35f, 0.15f)
            reflectiveQuadTo(5.58f, 19.13f)
            lineTo(7f, 17.7f)
            close()
            moveTo(3.46f, 20.54f)
            quadTo(2f, 19.08f, 2f, 17f)
            reflectiveQuadTo(3.46f, 13.46f)
            reflectiveQuadTo(7f, 12f)
            quadToRelative(2.08f, 0f, 3.54f, 1.46f)
            quadTo(12f, 14.93f, 12f, 17f)
            reflectiveQuadToRelative(-1.46f, 3.54f)
            reflectiveQuadTo(7f, 22f)
            quadTo(4.93f, 22f, 3.46f, 20.54f)
            close()
            moveTo(14.2f, 14.6f)
            quadTo(13.9f, 14.27f, 13.56f, 13.94f)
            reflectiveQuadTo(12.9f, 13.3f)
            quadToRelative(0.95f, -0.6f, 1.53f, -1.6f)
            reflectiveQuadTo(15f, 9.5f)
            quadTo(15f, 7.63f, 13.69f, 6.31f)
            reflectiveQuadTo(10.5f, 5f)
            reflectiveQuadTo(7.31f, 6.31f)
            reflectiveQuadTo(6f, 9.5f)
            quadTo(6f, 9.65f, 6.01f, 9.79f)
            quadToRelative(0.01f, 0.14f, 0.04f, 0.29f)
            quadTo(5.6f, 10.13f, 5.06f, 10.27f)
            quadTo(4.53f, 10.43f, 4.1f, 10.63f)
            quadTo(4.05f, 10.35f, 4.03f, 10.07f)
            reflectiveQuadTo(4f, 9.5f)
            quadTo(4f, 6.77f, 5.89f, 4.89f)
            reflectiveQuadTo(10.5f, 3f)
            reflectiveQuadToRelative(4.61f, 1.89f)
            reflectiveQuadTo(17f, 9.5f)
            quadToRelative(0f, 1.07f, -0.34f, 2.04f)
            reflectiveQuadTo(15.73f, 13.3f)
            lineToRelative(5.57f, 5.6f)
            quadToRelative(0.28f, 0.28f, 0.29f, 0.69f)
            reflectiveQuadTo(21.3f, 20.3f)
            quadToRelative(-0.27f, 0.27f, -0.7f, 0.27f)
            reflectiveQuadTo(19.9f, 20.3f)
            lineTo(14.2f, 14.6f)
            close()
          }
        }
        .build()
    return _GenSearchOff!!
  }

private var _GenSearchOff: ImageVector? = null

// ── SelectAll (select_all) ──
@Suppress("CheckReturnValue")
val SimpIcons.SelectAll: ImageVector
  get() {
    if (_GenSelectAll != null) {
      return _GenSelectAll!!
    }
    _GenSelectAll =
      ImageVector.Builder(
          name = "SelectAll",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(3.29f, 4.71f)
            quadTo(3f, 4.42f, 3f, 4f)
            quadTo(3f, 3.57f, 3.29f, 3.29f)
            reflectiveQuadTo(4f, 3f)
            reflectiveQuadTo(4.71f, 3.29f)
            reflectiveQuadTo(5f, 4f)
            quadTo(5f, 4.42f, 4.71f, 4.71f)
            reflectiveQuadTo(4f, 5f)
            reflectiveQuadTo(3.29f, 4.71f)
            close()
            moveToRelative(4f, 0f)
            quadTo(7f, 4.42f, 7f, 4f)
            quadTo(7f, 3.57f, 7.29f, 3.29f)
            reflectiveQuadTo(8f, 3f)
            reflectiveQuadTo(8.71f, 3.29f)
            reflectiveQuadTo(9f, 4f)
            quadTo(9f, 4.42f, 8.71f, 4.71f)
            reflectiveQuadTo(8f, 5f)
            quadTo(7.58f, 5f, 7.29f, 4.71f)
            close()
            moveToRelative(4f, 0f)
            quadTo(11f, 4.42f, 11f, 4f)
            quadTo(11f, 3.57f, 11.29f, 3.29f)
            reflectiveQuadTo(12f, 3f)
            reflectiveQuadToRelative(0.71f, 0.29f)
            reflectiveQuadTo(13f, 4f)
            quadToRelative(0f, 0.42f, -0.29f, 0.71f)
            reflectiveQuadTo(12f, 5f)
            reflectiveQuadTo(11.29f, 4.71f)
            close()
            moveToRelative(4f, 0f)
            quadTo(15f, 4.42f, 15f, 4f)
            quadTo(15f, 3.57f, 15.29f, 3.29f)
            reflectiveQuadTo(16f, 3f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(17f, 4f)
            quadToRelative(0f, 0.42f, -0.29f, 0.71f)
            reflectiveQuadTo(16f, 5f)
            reflectiveQuadTo(15.29f, 4.71f)
            close()
            moveToRelative(4f, 0f)
            quadTo(19f, 4.42f, 19f, 4f)
            quadTo(19f, 3.57f, 19.29f, 3.29f)
            reflectiveQuadTo(20f, 3f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(21f, 4f)
            quadToRelative(0f, 0.42f, -0.29f, 0.71f)
            reflectiveQuadTo(20f, 5f)
            reflectiveQuadTo(19.29f, 4.71f)
            close()
            moveToRelative(-16f, 4f)
            quadTo(3f, 8.42f, 3f, 8f)
            quadTo(3f, 7.57f, 3.29f, 7.29f)
            reflectiveQuadTo(4f, 7f)
            reflectiveQuadTo(4.71f, 7.29f)
            reflectiveQuadTo(5f, 8f)
            quadTo(5f, 8.42f, 4.71f, 8.71f)
            reflectiveQuadTo(4f, 9f)
            reflectiveQuadTo(3.29f, 8.71f)
            close()
            moveToRelative(16f, 0f)
            quadTo(19f, 8.42f, 19f, 8f)
            quadTo(19f, 7.57f, 19.29f, 7.29f)
            reflectiveQuadTo(20f, 7f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(21f, 8f)
            quadToRelative(0f, 0.42f, -0.29f, 0.71f)
            reflectiveQuadTo(20f, 9f)
            reflectiveQuadTo(19.29f, 8.71f)
            close()
            moveToRelative(-16f, 4f)
            quadTo(3f, 12.43f, 3f, 12f)
            reflectiveQuadTo(3.29f, 11.29f)
            reflectiveQuadTo(4f, 11f)
            reflectiveQuadToRelative(0.71f, 0.29f)
            reflectiveQuadTo(5f, 12f)
            reflectiveQuadTo(4.71f, 12.71f)
            reflectiveQuadTo(4f, 13f)
            reflectiveQuadTo(3.29f, 12.71f)
            close()
            moveToRelative(16f, 0f)
            quadTo(19f, 12.43f, 19f, 12f)
            reflectiveQuadToRelative(0.29f, -0.71f)
            reflectiveQuadTo(20f, 11f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(21f, 12f)
            reflectiveQuadToRelative(-0.29f, 0.71f)
            reflectiveQuadTo(20f, 13f)
            reflectiveQuadTo(19.29f, 12.71f)
            close()
            moveToRelative(-16f, 4f)
            quadTo(3f, 16.43f, 3f, 16f)
            reflectiveQuadTo(3.29f, 15.29f)
            reflectiveQuadTo(4f, 15f)
            reflectiveQuadToRelative(0.71f, 0.29f)
            reflectiveQuadTo(5f, 16f)
            reflectiveQuadTo(4.71f, 16.71f)
            reflectiveQuadTo(4f, 17f)
            reflectiveQuadTo(3.29f, 16.71f)
            close()
            moveToRelative(16f, 0f)
            quadTo(19f, 16.43f, 19f, 16f)
            reflectiveQuadToRelative(0.29f, -0.71f)
            reflectiveQuadTo(20f, 15f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(21f, 16f)
            reflectiveQuadToRelative(-0.29f, 0.71f)
            reflectiveQuadTo(20f, 17f)
            reflectiveQuadTo(19.29f, 16.71f)
            close()
            moveToRelative(-16f, 4f)
            quadTo(3f, 20.43f, 3f, 20f)
            reflectiveQuadTo(3.29f, 19.29f)
            reflectiveQuadTo(4f, 19f)
            reflectiveQuadToRelative(0.71f, 0.29f)
            reflectiveQuadTo(5f, 20f)
            reflectiveQuadTo(4.71f, 20.71f)
            reflectiveQuadTo(4f, 21f)
            reflectiveQuadTo(3.29f, 20.71f)
            close()
            moveToRelative(4f, 0f)
            quadTo(7f, 20.43f, 7f, 20f)
            reflectiveQuadTo(7.29f, 19.29f)
            reflectiveQuadTo(8f, 19f)
            reflectiveQuadToRelative(0.71f, 0.29f)
            reflectiveQuadTo(9f, 20f)
            reflectiveQuadTo(8.71f, 20.71f)
            reflectiveQuadTo(8f, 21f)
            quadTo(7.58f, 21f, 7.29f, 20.71f)
            close()
            moveToRelative(4f, 0f)
            quadTo(11f, 20.43f, 11f, 20f)
            reflectiveQuadToRelative(0.29f, -0.71f)
            reflectiveQuadTo(12f, 19f)
            reflectiveQuadToRelative(0.71f, 0.29f)
            reflectiveQuadTo(13f, 20f)
            reflectiveQuadToRelative(-0.29f, 0.71f)
            reflectiveQuadTo(12f, 21f)
            reflectiveQuadTo(11.29f, 20.71f)
            close()
            moveToRelative(4f, 0f)
            quadTo(15f, 20.43f, 15f, 20f)
            reflectiveQuadToRelative(0.29f, -0.71f)
            reflectiveQuadTo(16f, 19f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(17f, 20f)
            reflectiveQuadToRelative(-0.29f, 0.71f)
            reflectiveQuadTo(16f, 21f)
            reflectiveQuadTo(15.29f, 20.71f)
            close()
            moveToRelative(4f, 0f)
            quadTo(19f, 20.43f, 19f, 20f)
            reflectiveQuadToRelative(0.29f, -0.71f)
            reflectiveQuadTo(20f, 19f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(21f, 20f)
            reflectiveQuadToRelative(-0.29f, 0.71f)
            reflectiveQuadTo(20f, 21f)
            reflectiveQuadTo(19.29f, 20.71f)
            close()
            moveTo(9f, 17f)
            quadTo(8.18f, 17f, 7.59f, 16.41f)
            reflectiveQuadTo(7f, 15f)
            verticalLineTo(9f)
            quadTo(7f, 8.17f, 7.59f, 7.59f)
            reflectiveQuadTo(9f, 7f)
            horizontalLineToRelative(6f)
            quadToRelative(0.83f, 0f, 1.41f, 0.59f)
            reflectiveQuadTo(17f, 9f)
            verticalLineToRelative(6f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(15f, 17f)
            horizontalLineTo(9f)
            close()
            moveTo(9f, 15f)
            horizontalLineToRelative(6f)
            verticalLineTo(9f)
            horizontalLineTo(9f)
            verticalLineToRelative(6f)
            close()
          }
        }
        .build()
    return _GenSelectAll!!
  }

private var _GenSelectAll: ImageVector? = null

// ── Settings (settings) ──
@Suppress("CheckReturnValue")
val SimpIcons.Settings: ImageVector
  get() {
    if (_GenSettings != null) {
      return _GenSettings!!
    }
    _GenSettings =
      ImageVector.Builder(
          name = "Settings",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(10.83f, 22f)
            quadTo(10.15f, 22f, 9.66f, 21.55f)
            reflectiveQuadTo(9.08f, 20.45f)
            lineTo(8.85f, 18.8f)
            quadTo(8.53f, 18.68f, 8.24f, 18.5f)
            reflectiveQuadTo(7.68f, 18.13f)
            lineTo(6.13f, 18.77f)
            quadTo(5.5f, 19.05f, 4.88f, 18.83f)
            reflectiveQuadTo(3.9f, 18.02f)
            lineTo(2.73f, 15.98f)
            quadTo(2.38f, 15.4f, 2.53f, 14.75f)
            reflectiveQuadTo(3.2f, 13.68f)
            lineToRelative(1.33f, -1f)
            quadTo(4.5f, 12.5f, 4.5f, 12.34f)
            quadToRelative(0f, -0.16f, 0f, -0.34f)
            reflectiveQuadToRelative(0f, -0.34f)
            reflectiveQuadTo(4.53f, 11.33f)
            lineToRelative(-1.33f, -1f)
            quadTo(2.68f, 9.9f, 2.53f, 9.25f)
            reflectiveQuadTo(2.73f, 8.02f)
            lineTo(3.9f, 5.97f)
            quadTo(4.25f, 5.4f, 4.88f, 5.18f)
            reflectiveQuadTo(6.13f, 5.22f)
            lineTo(7.68f, 5.88f)
            quadTo(7.95f, 5.68f, 8.25f, 5.5f)
            reflectiveQuadTo(8.85f, 5.2f)
            lineTo(9.08f, 3.55f)
            quadTo(9.18f, 2.9f, 9.66f, 2.45f)
            reflectiveQuadTo(10.83f, 2f)
            horizontalLineToRelative(2.35f)
            quadToRelative(0.68f, 0f, 1.16f, 0.45f)
            reflectiveQuadToRelative(0.59f, 1.1f)
            lineTo(15.15f, 5.2f)
            quadToRelative(0.33f, 0.13f, 0.61f, 0.3f)
            reflectiveQuadToRelative(0.56f, 0.38f)
            lineTo(17.88f, 5.22f)
            quadTo(18.5f, 4.95f, 19.13f, 5.18f)
            reflectiveQuadToRelative(0.98f, 0.8f)
            lineToRelative(1.18f, 2.05f)
            quadToRelative(0.35f, 0.58f, 0.2f, 1.23f)
            reflectiveQuadTo(20.8f, 10.33f)
            lineToRelative(-1.32f, 1f)
            quadToRelative(0.02f, 0.18f, 0.02f, 0.34f)
            reflectiveQuadToRelative(0f, 0.34f)
            reflectiveQuadToRelative(0f, 0.34f)
            reflectiveQuadToRelative(-0.05f, 0.34f)
            lineToRelative(1.32f, 1f)
            quadToRelative(0.52f, 0.43f, 0.68f, 1.08f)
            reflectiveQuadToRelative(-0.2f, 1.22f)
            lineToRelative(-1.2f, 2.05f)
            quadToRelative(-0.35f, 0.58f, -0.98f, 0.8f)
            reflectiveQuadTo(17.83f, 18.77f)
            lineToRelative(-1.5f, -0.65f)
            quadToRelative(-0.27f, 0.2f, -0.57f, 0.38f)
            reflectiveQuadToRelative(-0.6f, 0.3f)
            lineToRelative(-0.22f, 1.65f)
            quadToRelative(-0.1f, 0.65f, -0.59f, 1.1f)
            reflectiveQuadTo(13.18f, 22f)
            horizontalLineTo(10.83f)
            close()
            moveToRelative(1.22f, -6.5f)
            quadToRelative(1.45f, 0f, 2.47f, -1.03f)
            reflectiveQuadTo(15.55f, 12f)
            reflectiveQuadTo(14.53f, 9.52f)
            reflectiveQuadTo(12.05f, 8.5f)
            quadToRelative(-1.47f, 0f, -2.49f, 1.02f)
            reflectiveQuadTo(8.55f, 12f)
            reflectiveQuadToRelative(1.01f, 2.47f)
            reflectiveQuadToRelative(2.49f, 1.03f)
            close()
          }
        }
        .build()
    return _GenSettings!!
  }

private var _GenSettings: ImageVector? = null

// ── SettingsEthernet (settings_ethernet) ──
@Suppress("CheckReturnValue")
val SimpIcons.SettingsEthernet: ImageVector
  get() {
    if (_GenSettingsEthernet != null) {
      return _GenSettingsEthernet!!
    }
    _GenSettingsEthernet =
      ImageVector.Builder(
          name = "SettingsEthernet",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(20.15f, 12f)
            lineTo(16.3f, 8.1f)
            quadTo(16.03f, 7.82f, 16.01f, 7.41f)
            reflectiveQuadTo(16.3f, 6.7f)
            quadTo(16.58f, 6.43f, 17f, 6.43f)
            reflectiveQuadTo(17.7f, 6.7f)
            lineToRelative(4.6f, 4.6f)
            quadToRelative(0.15f, 0.15f, 0.21f, 0.33f)
            reflectiveQuadTo(22.58f, 12f)
            reflectiveQuadToRelative(-0.06f, 0.38f)
            reflectiveQuadTo(22.3f, 12.7f)
            lineToRelative(-4.6f, 4.6f)
            quadToRelative(-0.27f, 0.27f, -0.69f, 0.29f)
            reflectiveQuadTo(16.3f, 17.3f)
            quadTo(16.03f, 17.02f, 16.03f, 16.6f)
            reflectiveQuadTo(16.3f, 15.9f)
            lineTo(20.15f, 12f)
            close()
            moveTo(3.85f, 12f)
            lineTo(7.7f, 15.9f)
            quadToRelative(0.28f, 0.28f, 0.29f, 0.69f)
            reflectiveQuadTo(7.7f, 17.3f)
            quadTo(7.43f, 17.58f, 7f, 17.58f)
            quadToRelative(-0.42f, 0f, -0.7f, -0.27f)
            lineTo(1.7f, 12.7f)
            quadTo(1.55f, 12.55f, 1.49f, 12.38f)
            reflectiveQuadTo(1.43f, 12f)
            reflectiveQuadTo(1.49f, 11.63f)
            reflectiveQuadTo(1.7f, 11.3f)
            lineTo(6.3f, 6.7f)
            quadTo(6.58f, 6.43f, 6.99f, 6.41f)
            reflectiveQuadTo(7.7f, 6.7f)
            quadTo(7.98f, 6.97f, 7.98f, 7.4f)
            reflectiveQuadTo(7.7f, 8.1f)
            lineTo(3.85f, 12f)
            close()
            moveToRelative(3.44f, 0.71f)
            quadTo(7f, 12.43f, 7f, 12f)
            reflectiveQuadTo(7.29f, 11.29f)
            reflectiveQuadTo(8f, 11f)
            reflectiveQuadToRelative(0.71f, 0.29f)
            reflectiveQuadTo(9f, 12f)
            reflectiveQuadTo(8.71f, 12.71f)
            reflectiveQuadTo(8f, 13f)
            quadTo(7.58f, 13f, 7.29f, 12.71f)
            close()
            moveToRelative(4f, 0f)
            quadTo(11f, 12.43f, 11f, 12f)
            reflectiveQuadToRelative(0.29f, -0.71f)
            reflectiveQuadTo(12f, 11f)
            reflectiveQuadToRelative(0.71f, 0.29f)
            reflectiveQuadTo(13f, 12f)
            reflectiveQuadToRelative(-0.29f, 0.71f)
            reflectiveQuadTo(12f, 13f)
            reflectiveQuadTo(11.29f, 12.71f)
            close()
            moveToRelative(4f, 0f)
            quadTo(15f, 12.43f, 15f, 12f)
            reflectiveQuadToRelative(0.29f, -0.71f)
            reflectiveQuadTo(16f, 11f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(17f, 12f)
            reflectiveQuadToRelative(-0.29f, 0.71f)
            reflectiveQuadTo(16f, 13f)
            reflectiveQuadTo(15.29f, 12.71f)
            close()
          }
        }
        .build()
    return _GenSettingsEthernet!!
  }

private var _GenSettingsEthernet: ImageVector? = null

// ── Share (share) ──
@Suppress("CheckReturnValue")
val SimpIcons.Share: ImageVector
  get() {
    if (_GenShare != null) {
      return _GenShare!!
    }
    _GenShare =
      ImageVector.Builder(
          name = "Share",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(17f, 22f)
            quadToRelative(-1.25f, 0f, -2.13f, -0.88f)
            reflectiveQuadTo(14f, 19f)
            quadToRelative(0f, -0.15f, 0.08f, -0.7f)
            lineTo(7.05f, 14.2f)
            quadToRelative(-0.4f, 0.38f, -0.93f, 0.59f)
            reflectiveQuadTo(5f, 15f)
            quadTo(3.75f, 15f, 2.88f, 14.13f)
            reflectiveQuadTo(2f, 12f)
            reflectiveQuadTo(2.88f, 9.88f)
            reflectiveQuadTo(5f, 9f)
            quadTo(5.6f, 9f, 6.13f, 9.21f)
            reflectiveQuadTo(7.05f, 9.8f)
            lineTo(14.08f, 5.7f)
            quadTo(14.03f, 5.52f, 14.01f, 5.36f)
            reflectiveQuadTo(14f, 5f)
            quadTo(14f, 3.75f, 14.88f, 2.88f)
            reflectiveQuadTo(17f, 2f)
            reflectiveQuadToRelative(2.13f, 0.88f)
            reflectiveQuadTo(20f, 5f)
            reflectiveQuadTo(19.13f, 7.13f)
            reflectiveQuadTo(17f, 8f)
            quadTo(16.4f, 8f, 15.88f, 7.79f)
            reflectiveQuadTo(14.95f, 7.2f)
            lineTo(7.93f, 11.3f)
            quadToRelative(0.05f, 0.18f, 0.06f, 0.34f)
            reflectiveQuadTo(8f, 12f)
            reflectiveQuadTo(7.99f, 12.36f)
            reflectiveQuadTo(7.93f, 12.7f)
            lineToRelative(7.03f, 4.1f)
            quadToRelative(0.4f, -0.38f, 0.92f, -0.59f)
            reflectiveQuadTo(17f, 16f)
            quadToRelative(1.25f, 0f, 2.13f, 0.88f)
            reflectiveQuadTo(20f, 19f)
            reflectiveQuadToRelative(-0.88f, 2.13f)
            reflectiveQuadTo(17f, 22f)
            close()
          }
        }
        .build()
    return _GenShare!!
  }

private var _GenShare: ImageVector? = null

// ── Star (star) ──
@Suppress("CheckReturnValue")
val SimpIcons.Star: ImageVector
  get() {
    if (_GenStar != null) {
      return _GenStar!!
    }
    _GenStar =
      ImageVector.Builder(
          name = "Star",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(12f, 17.27f)
            lineToRelative(-4.15f, 2.5f)
            quadTo(7.58f, 19.95f, 7.28f, 19.93f)
            reflectiveQuadTo(6.75f, 19.73f)
            reflectiveQuadTo(6.4f, 19.29f)
            quadTo(6.28f, 19.02f, 6.35f, 18.7f)
            lineToRelative(1.1f, -4.72f)
            lineTo(3.78f, 10.8f)
            quadTo(3.53f, 10.58f, 3.46f, 10.29f)
            reflectiveQuadTo(3.5f, 9.73f)
            reflectiveQuadTo(3.8f, 9.27f)
            reflectiveQuadTo(4.35f, 9.05f)
            lineTo(9.2f, 8.63f)
            lineTo(11.08f, 4.17f)
            quadTo(11.2f, 3.88f, 11.46f, 3.72f)
            reflectiveQuadTo(12f, 3.57f)
            quadToRelative(0.28f, 0f, 0.54f, 0.15f)
            quadToRelative(0.26f, 0.15f, 0.39f, 0.45f)
            lineTo(14.8f, 8.63f)
            lineToRelative(4.85f, 0.42f)
            quadTo(20f, 9.1f, 20.2f, 9.27f)
            reflectiveQuadToRelative(0.3f, 0.45f)
            reflectiveQuadToRelative(0.04f, 0.56f)
            reflectiveQuadTo(20.23f, 10.8f)
            lineToRelative(-3.68f, 3.18f)
            lineToRelative(1.1f, 4.72f)
            quadToRelative(0.07f, 0.32f, -0.05f, 0.59f)
            reflectiveQuadToRelative(-0.35f, 0.44f)
            quadToRelative(-0.22f, 0.17f, -0.52f, 0.2f)
            reflectiveQuadTo(16.15f, 19.77f)
            lineTo(12f, 17.27f)
            close()
          }
        }
        .build()
    return _GenStar!!
  }

private var _GenStar: ImageVector? = null

// ── StarBorder (star) ──
@Suppress("CheckReturnValue")
val SimpIcons.StarBorder: ImageVector
  get() {
    if (_GenStarBorder != null) {
      return _GenStarBorder!!
    }
    _GenStarBorder =
      ImageVector.Builder(
          name = "StarBorder",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(8.85f, 16.83f)
            lineTo(12f, 14.93f)
            lineToRelative(3.15f, 1.93f)
            lineToRelative(-0.82f, -3.6f)
            lineToRelative(2.78f, -2.4f)
            lineTo(13.45f, 10.52f)
            lineTo(12f, 7.13f)
            lineTo(10.55f, 10.5f)
            lineTo(6.9f, 10.83f)
            lineToRelative(2.78f, 2.43f)
            lineTo(8.85f, 16.83f)
            close()
            moveTo(12f, 17.27f)
            lineToRelative(-4.15f, 2.5f)
            quadTo(7.58f, 19.95f, 7.28f, 19.93f)
            reflectiveQuadTo(6.75f, 19.73f)
            reflectiveQuadTo(6.4f, 19.29f)
            quadTo(6.28f, 19.02f, 6.35f, 18.7f)
            lineToRelative(1.1f, -4.72f)
            lineTo(3.78f, 10.8f)
            quadTo(3.53f, 10.58f, 3.46f, 10.29f)
            reflectiveQuadTo(3.5f, 9.73f)
            reflectiveQuadTo(3.8f, 9.27f)
            reflectiveQuadTo(4.35f, 9.05f)
            lineTo(9.2f, 8.63f)
            lineTo(11.08f, 4.17f)
            quadTo(11.2f, 3.88f, 11.46f, 3.72f)
            reflectiveQuadTo(12f, 3.57f)
            quadToRelative(0.28f, 0f, 0.54f, 0.15f)
            quadToRelative(0.26f, 0.15f, 0.39f, 0.45f)
            lineTo(14.8f, 8.63f)
            lineToRelative(4.85f, 0.42f)
            quadTo(20f, 9.1f, 20.2f, 9.27f)
            reflectiveQuadToRelative(0.3f, 0.45f)
            reflectiveQuadToRelative(0.04f, 0.56f)
            reflectiveQuadTo(20.23f, 10.8f)
            lineToRelative(-3.68f, 3.18f)
            lineToRelative(1.1f, 4.72f)
            quadToRelative(0.07f, 0.32f, -0.05f, 0.59f)
            reflectiveQuadToRelative(-0.35f, 0.44f)
            quadToRelative(-0.22f, 0.17f, -0.52f, 0.2f)
            reflectiveQuadTo(16.15f, 19.77f)
            lineTo(12f, 17.27f)
            close()
            moveToRelative(0f, -5.02f)
            close()
          }
        }
        .build()
    return _GenStarBorder!!
  }

private var _GenStarBorder: ImageVector? = null

// ── Style (style) ──
@Suppress("CheckReturnValue")
val SimpIcons.Style: ImageVector
  get() {
    if (_GenStyle != null) {
      return _GenStyle!!
    }
    _GenStyle =
      ImageVector.Builder(
          name = "Style",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(3.98f, 19.8f)
            lineTo(3.13f, 19.45f)
            quadTo(2.35f, 19.13f, 2.09f, 18.33f)
            quadTo(1.83f, 17.52f, 2.18f, 16.75f)
            lineToRelative(1.8f, -3.9f)
            verticalLineTo(19.8f)
            close()
            moveToRelative(4f, 2.2f)
            quadTo(7.15f, 22f, 6.56f, 21.41f)
            reflectiveQuadTo(5.98f, 20f)
            verticalLineTo(14f)
            lineToRelative(2.65f, 7.35f)
            quadToRelative(0.08f, 0.17f, 0.15f, 0.34f)
            reflectiveQuadTo(8.98f, 22f)
            horizontalLineToRelative(-1f)
            close()
            moveToRelative(5.15f, -0.1f)
            quadToRelative(-0.8f, 0.3f, -1.55f, -0.07f)
            reflectiveQuadTo(10.53f, 20.65f)
            lineTo(6.08f, 8.45f)
            quadTo(5.78f, 7.65f, 6.13f, 6.89f)
            reflectiveQuadTo(7.28f, 5.85f)
            lineTo(14.83f, 3.1f)
            quadToRelative(0.8f, -0.3f, 1.55f, 0.07f)
            reflectiveQuadToRelative(1.05f, 1.18f)
            lineToRelative(4.45f, 12.2f)
            quadToRelative(0.3f, 0.8f, -0.05f, 1.56f)
            reflectiveQuadToRelative(-1.15f, 1.04f)
            lineTo(13.13f, 21.9f)
            close()
            moveTo(11.69f, 9.71f)
            quadTo(11.98f, 9.42f, 11.98f, 9f)
            quadToRelative(0f, -0.43f, -0.29f, -0.71f)
            reflectiveQuadTo(10.98f, 8f)
            reflectiveQuadTo(10.26f, 8.29f)
            reflectiveQuadTo(9.98f, 9f)
            quadToRelative(0f, 0.42f, 0.29f, 0.71f)
            reflectiveQuadTo(10.98f, 10f)
            reflectiveQuadTo(11.69f, 9.71f)
            close()
          }
        }
        .build()
    return _GenStyle!!
  }

private var _GenStyle: ImageVector? = null

// ── SurroundSound (surround_sound) ──
@Suppress("CheckReturnValue")
val SimpIcons.SurroundSound: ImageVector
  get() {
    if (_GenSurroundSound != null) {
      return _GenSurroundSound!!
    }
    _GenSurroundSound =
      ImageVector.Builder(
          name = "SurroundSound",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(14.13f, 14.13f)
            quadTo(15f, 13.25f, 15f, 12f)
            reflectiveQuadTo(14.13f, 9.88f)
            reflectiveQuadTo(12f, 9f)
            reflectiveQuadTo(9.88f, 9.88f)
            reflectiveQuadTo(9f, 12f)
            reflectiveQuadToRelative(0.88f, 2.13f)
            reflectiveQuadTo(12f, 15f)
            reflectiveQuadToRelative(2.13f, -0.88f)
            close()
            moveTo(17f, 12f)
            quadToRelative(0f, 0.75f, -0.2f, 1.45f)
            reflectiveQuadToRelative(-0.63f, 1.33f)
            quadToRelative(-0.22f, 0.35f, -0.21f, 0.77f)
            quadToRelative(0.01f, 0.43f, 0.29f, 0.7f)
            quadToRelative(0.3f, 0.3f, 0.7f, 0.29f)
            quadToRelative(0.4f, -0.01f, 0.65f, -0.34f)
            quadToRelative(0.68f, -0.9f, 1.04f, -1.97f)
            reflectiveQuadTo(19f, 12f)
            reflectiveQuadTo(18.64f, 9.77f)
            quadTo(18.28f, 8.7f, 17.6f, 7.8f)
            quadTo(17.35f, 7.47f, 16.95f, 7.46f)
            reflectiveQuadToRelative(-0.7f, 0.29f)
            quadToRelative(-0.27f, 0.27f, -0.29f, 0.7f)
            reflectiveQuadToRelative(0.21f, 0.78f)
            quadToRelative(0.43f, 0.63f, 0.63f, 1.32f)
            reflectiveQuadTo(17f, 12f)
            close()
            moveTo(7.2f, 10.55f)
            quadTo(7.4f, 9.85f, 7.83f, 9.23f)
            quadTo(8.05f, 8.88f, 8.04f, 8.45f)
            quadTo(8.03f, 8.02f, 7.75f, 7.75f)
            quadTo(7.45f, 7.45f, 7.05f, 7.46f)
            reflectiveQuadTo(6.4f, 7.8f)
            quadTo(5.73f, 8.7f, 5.36f, 9.77f)
            reflectiveQuadTo(5f, 12f)
            reflectiveQuadToRelative(0.36f, 2.22f)
            reflectiveQuadTo(6.4f, 16.2f)
            quadToRelative(0.25f, 0.32f, 0.65f, 0.34f)
            reflectiveQuadToRelative(0.7f, -0.29f)
            quadToRelative(0.28f, -0.28f, 0.29f, -0.7f)
            quadTo(8.05f, 15.13f, 7.83f, 14.78f)
            quadTo(7.4f, 14.15f, 7.2f, 13.45f)
            reflectiveQuadTo(7f, 12f)
            reflectiveQuadTo(7.2f, 10.55f)
            close()
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
          }
        }
        .build()
    return _GenSurroundSound!!
  }

private var _GenSurroundSound: ImageVector? = null

// ── SwapVert (swap_vert) ──
@Suppress("CheckReturnValue")
val SimpIcons.SwapVert: ImageVector
  get() {
    if (_GenSwapVert != null) {
      return _GenSwapVert!!
    }
    _GenSwapVert =
      ImageVector.Builder(
          name = "SwapVert",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(8.29f, 12.71f)
            quadTo(8f, 12.43f, 8f, 12f)
            verticalLineTo(5.82f)
            lineTo(6.13f, 7.7f)
            quadTo(5.85f, 7.97f, 5.44f, 7.97f)
            quadTo(5.03f, 7.97f, 4.73f, 7.7f)
            quadTo(4.43f, 7.4f, 4.43f, 6.99f)
            quadToRelative(0f, -0.41f, 0.3f, -0.71f)
            lineTo(8.3f, 2.7f)
            quadTo(8.45f, 2.55f, 8.63f, 2.49f)
            reflectiveQuadTo(9f, 2.42f)
            reflectiveQuadTo(9.38f, 2.49f)
            reflectiveQuadTo(9.7f, 2.7f)
            lineToRelative(3.6f, 3.6f)
            quadTo(13.6f, 6.6f, 13.59f, 7f)
            reflectiveQuadTo(13.28f, 7.7f)
            quadToRelative(-0.3f, 0.27f, -0.7f, 0.29f)
            reflectiveQuadTo(11.88f, 7.7f)
            lineTo(10f, 5.82f)
            verticalLineTo(12f)
            quadToRelative(0f, 0.42f, -0.29f, 0.71f)
            reflectiveQuadTo(9f, 13f)
            quadTo(8.58f, 13f, 8.29f, 12.71f)
            close()
            moveToRelative(6.34f, 8.8f)
            quadTo(14.45f, 21.45f, 14.3f, 21.3f)
            lineTo(10.7f, 17.7f)
            quadTo(10.4f, 17.4f, 10.41f, 17f)
            reflectiveQuadToRelative(0.31f, -0.7f)
            quadToRelative(0.3f, -0.28f, 0.7f, -0.29f)
            reflectiveQuadToRelative(0.7f, 0.29f)
            lineTo(14f, 18.18f)
            verticalLineTo(12f)
            quadToRelative(0f, -0.43f, 0.29f, -0.71f)
            reflectiveQuadTo(15f, 11f)
            reflectiveQuadToRelative(0.71f, 0.29f)
            reflectiveQuadTo(16f, 12f)
            verticalLineToRelative(6.18f)
            lineTo(17.88f, 16.3f)
            quadToRelative(0.28f, -0.28f, 0.69f, -0.28f)
            quadToRelative(0.41f, 0f, 0.71f, 0.28f)
            quadToRelative(0.3f, 0.3f, 0.3f, 0.71f)
            quadToRelative(0f, 0.41f, -0.3f, 0.71f)
            lineTo(15.7f, 21.3f)
            quadToRelative(-0.15f, 0.15f, -0.33f, 0.21f)
            reflectiveQuadTo(15f, 21.58f)
            reflectiveQuadTo(14.63f, 21.51f)
            close()
          }
        }
        .build()
    return _GenSwapVert!!
  }

private var _GenSwapVert: ImageVector? = null

// ── SystemUpdate (system_update) ──
@Suppress("CheckReturnValue")
val SimpIcons.SystemUpdate: ImageVector
  get() {
    if (_GenSystemUpdate != null) {
      return _GenSystemUpdate!!
    }
    _GenSystemUpdate =
      ImageVector.Builder(
          name = "SystemUpdate",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(7f, 23f)
            quadTo(6.18f, 23f, 5.59f, 22.41f)
            reflectiveQuadTo(5f, 21f)
            verticalLineTo(3f)
            quadTo(5f, 2.17f, 5.59f, 1.59f)
            reflectiveQuadTo(7f, 1f)
            horizontalLineTo(17f)
            quadToRelative(0.82f, 0f, 1.41f, 0.59f)
            reflectiveQuadTo(19f, 3f)
            verticalLineTo(6.1f)
            quadToRelative(0.45f, 0.18f, 0.73f, 0.55f)
            reflectiveQuadTo(20f, 7.5f)
            verticalLineToRelative(2f)
            quadToRelative(0f, 0.47f, -0.27f, 0.85f)
            reflectiveQuadTo(19f, 10.9f)
            verticalLineTo(21f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(17f, 23f)
            horizontalLineTo(7f)
            close()
            moveToRelative(5.38f, -7.49f)
            quadTo(12.55f, 15.45f, 12.7f, 15.3f)
            lineToRelative(2.6f, -2.6f)
            quadToRelative(0.28f, -0.28f, 0.29f, -0.69f)
            reflectiveQuadTo(15.3f, 11.3f)
            quadTo(15.03f, 11.02f, 14.61f, 11.01f)
            reflectiveQuadTo(13.9f, 11.27f)
            lineTo(13f, 12.15f)
            verticalLineTo(9f)
            quadTo(13f, 8.57f, 12.71f, 8.29f)
            reflectiveQuadTo(12f, 8f)
            reflectiveQuadTo(11.29f, 8.29f)
            reflectiveQuadTo(11f, 9f)
            verticalLineToRelative(3.15f)
            lineTo(10.1f, 11.27f)
            quadTo(9.83f, 11f, 9.41f, 11f)
            reflectiveQuadTo(8.7f, 11.3f)
            quadTo(8.43f, 11.58f, 8.43f, 12f)
            reflectiveQuadTo(8.7f, 12.7f)
            lineToRelative(2.6f, 2.6f)
            quadToRelative(0.15f, 0.15f, 0.32f, 0.21f)
            reflectiveQuadTo(12f, 15.58f)
            reflectiveQuadToRelative(0.38f, -0.06f)
            close()
          }
        }
        .build()
    return _GenSystemUpdate!!
  }

private var _GenSystemUpdate: ImageVector? = null

// ── Timer (timer) ──
@Suppress("CheckReturnValue")
val SimpIcons.Timer: ImageVector
  get() {
    if (_GenTimer != null) {
      return _GenTimer!!
    }
    _GenTimer =
      ImageVector.Builder(
          name = "Timer",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(10f, 3f)
            quadTo(9.58f, 3f, 9.29f, 2.71f)
            reflectiveQuadTo(9f, 2f)
            quadTo(9f, 1.57f, 9.29f, 1.29f)
            quadTo(9.58f, 1f, 10f, 1f)
            horizontalLineToRelative(4f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(15f, 2f)
            quadToRelative(0f, 0.42f, -0.29f, 0.71f)
            reflectiveQuadTo(14f, 3f)
            horizontalLineTo(10f)
            close()
            moveToRelative(2.71f, 10.71f)
            quadTo(13f, 13.43f, 13f, 13f)
            verticalLineTo(9f)
            quadTo(13f, 8.57f, 12.71f, 8.29f)
            reflectiveQuadTo(12f, 8f)
            reflectiveQuadTo(11.29f, 8.29f)
            reflectiveQuadTo(11f, 9f)
            verticalLineToRelative(4f)
            quadToRelative(0f, 0.42f, 0.29f, 0.71f)
            reflectiveQuadTo(12f, 14f)
            reflectiveQuadToRelative(0.71f, -0.29f)
            close()
            moveToRelative(-4.2f, 7.58f)
            quadTo(6.88f, 20.58f, 5.65f, 19.35f)
            reflectiveQuadTo(3.71f, 16.49f)
            reflectiveQuadTo(3f, 13f)
            reflectiveQuadTo(3.71f, 9.51f)
            reflectiveQuadTo(5.65f, 6.65f)
            quadTo(6.88f, 5.43f, 8.51f, 4.71f)
            reflectiveQuadTo(12f, 4f)
            quadToRelative(1.55f, 0f, 2.98f, 0.5f)
            reflectiveQuadToRelative(2.68f, 1.45f)
            lineToRelative(0.7f, -0.7f)
            quadToRelative(0.27f, -0.28f, 0.7f, -0.28f)
            reflectiveQuadToRelative(0.7f, 0.28f)
            quadToRelative(0.28f, 0.27f, 0.28f, 0.7f)
            reflectiveQuadToRelative(-0.28f, 0.7f)
            lineToRelative(-0.7f, 0.7f)
            quadTo(20f, 8.6f, 20.5f, 10.02f)
            reflectiveQuadTo(21f, 13f)
            quadToRelative(0f, 1.85f, -0.71f, 3.49f)
            reflectiveQuadToRelative(-1.94f, 2.86f)
            reflectiveQuadToRelative(-2.86f, 1.94f)
            reflectiveQuadTo(12f, 22f)
            reflectiveQuadTo(8.51f, 21.29f)
            close()
          }
        }
        .build()
    return _GenTimer!!
  }

private var _GenTimer: ImageVector? = null

// ── TrendingUp (trending_up) ──
@Suppress("CheckReturnValue")
val SimpIcons.TrendingUp: ImageVector
  get() {
    if (_GenTrendingUp != null) {
      return _GenTrendingUp!!
    }
    _GenTrendingUp =
      ImageVector.Builder(
          name = "TrendingUp",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(2.7f, 17.63f)
            quadTo(2.4f, 17.33f, 2.41f, 16.91f)
            quadTo(2.43f, 16.5f, 2.7f, 16.23f)
            lineTo(7.98f, 10.88f)
            quadTo(8.55f, 10.3f, 9.4f, 10.3f)
            reflectiveQuadToRelative(1.43f, 0.58f)
            lineToRelative(2.57f, 2.6f)
            lineTo(18.6f, 8.32f)
            horizontalLineTo(17f)
            quadToRelative(-0.43f, 0f, -0.71f, -0.29f)
            reflectiveQuadTo(16f, 7.32f)
            reflectiveQuadTo(16.29f, 6.61f)
            quadTo(16.58f, 6.32f, 17f, 6.32f)
            horizontalLineToRelative(4f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(22f, 7.32f)
            verticalLineToRelative(4f)
            quadToRelative(0f, 0.43f, -0.29f, 0.71f)
            reflectiveQuadTo(21f, 12.33f)
            reflectiveQuadTo(20.29f, 12.04f)
            reflectiveQuadTo(20f, 11.33f)
            verticalLineTo(9.73f)
            lineTo(14.83f, 14.9f)
            quadToRelative(-0.58f, 0.57f, -1.43f, 0.57f)
            reflectiveQuadTo(11.98f, 14.9f)
            lineTo(9.4f, 12.33f)
            lineToRelative(-5.3f, 5.3f)
            quadTo(3.83f, 17.9f, 3.4f, 17.9f)
            reflectiveQuadTo(2.7f, 17.63f)
            close()
          }
        }
        .build()
    return _GenTrendingUp!!
  }

private var _GenTrendingUp: ImageVector? = null

// ── Verified (verified) ──
@Suppress("CheckReturnValue")
val SimpIcons.Verified: ImageVector
  get() {
    if (_GenVerified != null) {
      return _GenVerified!!
    }
    _GenVerified =
      ImageVector.Builder(
          name = "Verified",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(10.95f, 12.7f)
            lineTo(9.5f, 11.27f)
            quadTo(9.23f, 11f, 8.81f, 11f)
            reflectiveQuadTo(8.1f, 11.3f)
            quadTo(7.83f, 11.58f, 7.83f, 12f)
            reflectiveQuadTo(8.1f, 12.7f)
            lineToRelative(2.15f, 2.15f)
            quadToRelative(0.3f, 0.3f, 0.7f, 0.3f)
            reflectiveQuadToRelative(0.7f, -0.3f)
            lineTo(15.9f, 10.6f)
            quadToRelative(0.3f, -0.3f, 0.29f, -0.7f)
            reflectiveQuadTo(15.9f, 9.2f)
            quadTo(15.6f, 8.9f, 15.19f, 8.89f)
            quadTo(14.78f, 8.88f, 14.48f, 9.17f)
            lineTo(10.95f, 12.7f)
            close()
            moveToRelative(-2.8f, 9.05f)
            lineTo(6.7f, 19.3f)
            lineTo(3.95f, 18.7f)
            quadTo(3.58f, 18.63f, 3.35f, 18.31f)
            reflectiveQuadTo(3.18f, 17.63f)
            lineTo(3.45f, 14.8f)
            lineTo(1.58f, 12.65f)
            quadTo(1.33f, 12.38f, 1.33f, 12f)
            reflectiveQuadTo(1.58f, 11.35f)
            lineTo(3.45f, 9.2f)
            lineTo(3.18f, 6.38f)
            quadTo(3.13f, 6f, 3.35f, 5.69f)
            reflectiveQuadTo(3.95f, 5.3f)
            lineTo(6.7f, 4.7f)
            lineTo(8.15f, 2.25f)
            quadTo(8.35f, 1.92f, 8.7f, 1.81f)
            reflectiveQuadTo(9.4f, 1.85f)
            lineTo(12f, 2.95f)
            lineToRelative(2.6f, -1.1f)
            quadTo(14.95f, 1.7f, 15.3f, 1.81f)
            quadToRelative(0.35f, 0.11f, 0.55f, 0.44f)
            lineTo(17.3f, 4.7f)
            lineToRelative(2.75f, 0.6f)
            quadToRelative(0.38f, 0.07f, 0.6f, 0.39f)
            reflectiveQuadToRelative(0.18f, 0.69f)
            lineTo(20.55f, 9.2f)
            lineToRelative(1.88f, 2.15f)
            quadToRelative(0.25f, 0.28f, 0.25f, 0.65f)
            reflectiveQuadToRelative(-0.25f, 0.65f)
            lineTo(20.55f, 14.8f)
            lineToRelative(0.28f, 2.83f)
            quadTo(20.88f, 18f, 20.65f, 18.31f)
            reflectiveQuadToRelative(-0.6f, 0.39f)
            lineTo(17.3f, 19.3f)
            lineToRelative(-1.45f, 2.45f)
            quadToRelative(-0.2f, 0.32f, -0.55f, 0.44f)
            reflectiveQuadTo(14.6f, 22.15f)
            lineTo(12f, 21.05f)
            lineToRelative(-2.6f, 1.1f)
            quadTo(9.05f, 22.3f, 8.7f, 22.19f)
            reflectiveQuadTo(8.15f, 21.75f)
            close()
          }
        }
        .build()
    return _GenVerified!!
  }

private var _GenVerified: ImageVector? = null

// ── VolumeMute (volume_mute) ──
@Suppress("CheckReturnValue")
val SimpIcons.VolumeMute: ImageVector
  get() {
    if (_GenVolumeMute != null) {
      return _GenVolumeMute!!
    }
    _GenVolumeMute =
      ImageVector.Builder(
          name = "VolumeMute",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(11f, 15f)
            horizontalLineTo(8f)
            quadTo(7.58f, 15f, 7.29f, 14.71f)
            reflectiveQuadTo(7f, 14f)
            verticalLineTo(10f)
            quadTo(7f, 9.57f, 7.29f, 9.29f)
            reflectiveQuadTo(8f, 9f)
            horizontalLineToRelative(3f)
            lineTo(14.3f, 5.7f)
            quadTo(14.78f, 5.22f, 15.39f, 5.49f)
            reflectiveQuadTo(16f, 6.43f)
            verticalLineTo(17.58f)
            quadToRelative(0f, 0.68f, -0.61f, 0.94f)
            reflectiveQuadTo(14.3f, 18.3f)
            lineTo(11f, 15f)
            close()
          }
        }
        .build()
    return _GenVolumeMute!!
  }

private var _GenVolumeMute: ImageVector? = null

// ── VolumeUp (volume_up) ──
@Suppress("CheckReturnValue")
val SimpIcons.VolumeUp: ImageVector
  get() {
    if (_GenVolumeUp != null) {
      return _GenVolumeUp!!
    }
    _GenVolumeUp =
      ImageVector.Builder(
          name = "VolumeUp",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(19f, 11.98f)
            quadTo(19f, 9.9f, 17.9f, 8.19f)
            quadTo(16.8f, 6.47f, 14.95f, 5.63f)
            quadTo(14.58f, 5.45f, 14.4f, 5.09f)
            reflectiveQuadTo(14.35f, 4.35f)
            quadTo(14.5f, 3.95f, 14.89f, 3.77f)
            reflectiveQuadToRelative(0.79f, 0f)
            quadToRelative(2.43f, 1.07f, 3.88f, 3.29f)
            quadTo(21f, 9.27f, 21f, 11.98f)
            reflectiveQuadToRelative(-1.45f, 4.91f)
            reflectiveQuadToRelative(-3.88f, 3.29f)
            quadToRelative(-0.4f, 0.18f, -0.79f, 0f)
            reflectiveQuadTo(14.35f, 19.6f)
            quadTo(14.23f, 19.23f, 14.4f, 18.86f)
            reflectiveQuadToRelative(0.55f, -0.54f)
            quadTo(16.8f, 17.48f, 17.9f, 15.76f)
            reflectiveQuadTo(19f, 11.98f)
            close()
            moveTo(7f, 15f)
            horizontalLineTo(4f)
            quadTo(3.58f, 15f, 3.29f, 14.71f)
            reflectiveQuadTo(3f, 14f)
            verticalLineTo(10f)
            quadTo(3f, 9.57f, 3.29f, 9.29f)
            reflectiveQuadTo(4f, 9f)
            horizontalLineTo(7f)
            lineTo(10.3f, 5.7f)
            quadTo(10.78f, 5.22f, 11.39f, 5.49f)
            reflectiveQuadTo(12f, 6.43f)
            verticalLineTo(17.58f)
            quadToRelative(0f, 0.68f, -0.61f, 0.94f)
            reflectiveQuadTo(10.3f, 18.3f)
            lineTo(7f, 15f)
            close()
            moveToRelative(9.5f, -3f)
            quadToRelative(0f, 1.05f, -0.47f, 1.99f)
            reflectiveQuadToRelative(-1.25f, 1.54f)
            quadToRelative(-0.25f, 0.15f, -0.51f, 0.01f)
            reflectiveQuadTo(14f, 15.1f)
            verticalLineTo(8.85f)
            quadToRelative(0f, -0.3f, 0.26f, -0.44f)
            quadToRelative(0.26f, -0.14f, 0.51f, 0.01f)
            quadTo(15.55f, 9.05f, 16.03f, 10f)
            reflectiveQuadToRelative(0.47f, 2f)
            close()
          }
        }
        .build()
    return _GenVolumeUp!!
  }

private var _GenVolumeUp: ImageVector? = null

// ── Wifi (wifi) ──
@Suppress("CheckReturnValue")
val SimpIcons.Wifi: ImageVector
  get() {
    if (_GenWifi != null) {
      return _GenWifi!!
    }
    _GenWifi =
      ImageVector.Builder(
          name = "Wifi",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(10.23f, 20.27f)
            quadTo(9.5f, 19.55f, 9.5f, 18.5f)
            reflectiveQuadToRelative(0.73f, -1.77f)
            reflectiveQuadTo(12f, 16f)
            reflectiveQuadToRelative(1.78f, 0.73f)
            reflectiveQuadTo(14.5f, 18.5f)
            reflectiveQuadToRelative(-0.72f, 1.77f)
            reflectiveQuadTo(12f, 21f)
            reflectiveQuadTo(10.23f, 20.27f)
            close()
            moveTo(15.56f, 10.6f)
            quadToRelative(1.69f, 0.6f, 3.06f, 1.65f)
            quadToRelative(0.5f, 0.38f, 0.51f, 0.99f)
            reflectiveQuadTo(18.7f, 14.3f)
            quadToRelative(-0.42f, 0.43f, -1.05f, 0.44f)
            reflectiveQuadTo(16.53f, 14.4f)
            quadToRelative(-0.95f, -0.65f, -2.1f, -1.02f)
            reflectiveQuadTo(12f, 13f)
            reflectiveQuadTo(9.58f, 13.38f)
            reflectiveQuadTo(7.48f, 14.4f)
            quadToRelative(-0.5f, 0.35f, -1.13f, 0.33f)
            reflectiveQuadTo(5.3f, 14.27f)
            quadTo(4.88f, 13.83f, 4.88f, 13.21f)
            reflectiveQuadToRelative(0.5f, -0.99f)
            quadTo(6.75f, 11.18f, 8.44f, 10.59f)
            reflectiveQuadTo(12f, 10f)
            reflectiveQuadToRelative(3.56f, 0.6f)
            close()
            moveTo(17.89f, 5.02f)
            quadToRelative(2.76f, 1.03f, 4.96f, 2.9f)
            quadToRelative(0.5f, 0.42f, 0.52f, 1.05f)
            reflectiveQuadToRelative(-0.42f, 1.07f)
            quadToRelative(-0.43f, 0.43f, -1.05f, 0.44f)
            reflectiveQuadTo(20.78f, 10.1f)
            quadTo(18.98f, 8.63f, 16.74f, 7.81f)
            reflectiveQuadTo(12f, 7f)
            reflectiveQuadTo(7.26f, 7.81f)
            reflectiveQuadTo(3.23f, 10.1f)
            quadTo(2.73f, 10.5f, 2.1f, 10.49f)
            quadTo(1.48f, 10.48f, 1.05f, 10.05f)
            quadTo(0.6f, 9.6f, 0.63f, 8.98f)
            reflectiveQuadTo(1.15f, 7.93f)
            quadTo(3.35f, 6.05f, 6.11f, 5.02f)
            reflectiveQuadTo(12f, 4f)
            reflectiveQuadToRelative(5.89f, 1.02f)
            close()
          }
        }
        .build()
    return _GenWifi!!
  }

private var _GenWifi: ImageVector? = null

// ── WifiOff (wifi_off) ──
@Suppress("CheckReturnValue")
val SimpIcons.WifiOff: ImageVector
  get() {
    if (_GenWifiOff != null) {
      return _GenWifiOff!!
    }
    _GenWifiOff =
      ImageVector.Builder(
          name = "WifiOff",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(19.05f, 21.9f)
            lineToRelative(-8.7f, -8.75f)
            quadTo(9.58f, 13.33f, 8.86f, 13.63f)
            reflectiveQuadTo(7.53f, 14.35f)
            quadTo(7f, 14.7f, 6.36f, 14.71f)
            reflectiveQuadTo(5.3f, 14.27f)
            quadTo(4.85f, 13.83f, 4.89f, 13.19f)
            reflectiveQuadTo(5.43f, 12.18f)
            quadTo(6f, 11.75f, 6.64f, 11.4f)
            reflectiveQuadTo(7.95f, 10.75f)
            lineTo(5.7f, 8.5f)
            quadTo(5.05f, 8.85f, 4.44f, 9.24f)
            quadTo(3.83f, 9.63f, 3.25f, 10.07f)
            quadToRelative(-0.5f, 0.4f, -1.14f, 0.4f)
            reflectiveQuadTo(1.05f, 10.02f)
            quadTo(0.6f, 9.57f, 0.63f, 8.95f)
            reflectiveQuadTo(1.15f, 7.93f)
            quadTo(1.7f, 7.47f, 2.28f, 7.06f)
            reflectiveQuadTo(3.5f, 6.3f)
            lineTo(2.1f, 4.9f)
            quadTo(1.83f, 4.63f, 1.83f, 4.2f)
            reflectiveQuadTo(2.1f, 3.5f)
            quadTo(2.38f, 3.22f, 2.8f, 3.22f)
            reflectiveQuadTo(3.5f, 3.5f)
            lineTo(20.48f, 20.48f)
            quadToRelative(0.3f, 0.3f, 0.3f, 0.71f)
            reflectiveQuadToRelative(-0.3f, 0.71f)
            quadToRelative(-0.3f, 0.28f, -0.71f, 0.29f)
            reflectiveQuadTo(19.05f, 21.9f)
            close()
            moveTo(10.23f, 20.26f)
            quadTo(9.5f, 19.52f, 9.5f, 18.5f)
            quadToRelative(0f, -1.05f, 0.73f, -1.77f)
            reflectiveQuadTo(12f, 16f)
            reflectiveQuadToRelative(1.78f, 0.73f)
            reflectiveQuadTo(14.5f, 18.5f)
            quadToRelative(0f, 1.02f, -0.72f, 1.76f)
            reflectiveQuadTo(12f, 21f)
            reflectiveQuadTo(10.23f, 20.26f)
            close()
            moveToRelative(8.6f, -6.14f)
            quadToRelative(-0.4f, 0.4f, -0.94f, 0.39f)
            reflectiveQuadTo(16.95f, 14.1f)
            quadTo(16.83f, 13.98f, 16.7f, 13.85f)
            reflectiveQuadTo(16.45f, 13.6f)
            lineToRelative(-2.4f, -2.4f)
            quadTo(13.73f, 10.88f, 13.93f, 10.52f)
            reflectiveQuadToRelative(0.7f, -0.22f)
            quadToRelative(1.13f, 0.28f, 2.14f, 0.78f)
            quadToRelative(1.01f, 0.5f, 1.89f, 1.18f)
            quadToRelative(0.45f, 0.35f, 0.51f, 0.91f)
            reflectiveQuadToRelative(-0.34f, 0.96f)
            close()
            moveToRelative(4.13f, -4.1f)
            quadToRelative(-0.43f, 0.45f, -1.05f, 0.46f)
            reflectiveQuadTo(20.78f, 10.1f)
            quadTo(18.98f, 8.63f, 16.74f, 7.81f)
            reflectiveQuadTo(12f, 7f)
            quadTo(11.48f, 7f, 10.99f, 7.04f)
            reflectiveQuadTo(10f, 7.15f)
            quadTo(9.38f, 7.25f, 8.88f, 6.89f)
            reflectiveQuadTo(8.28f, 5.9f)
            reflectiveQuadTo(8.55f, 4.77f)
            reflectiveQuadToRelative(1f, -0.6f)
            quadToRelative(0.6f, -0.1f, 1.21f, -0.14f)
            reflectiveQuadTo(12f, 4f)
            quadToRelative(3.13f, 0f, 5.89f, 1.04f)
            reflectiveQuadTo(22.85f, 7.9f)
            quadToRelative(0.5f, 0.43f, 0.52f, 1.05f)
            reflectiveQuadToRelative(-0.42f, 1.07f)
            close()
          }
        }
        .build()
    return _GenWifiOff!!
  }

private var _GenWifiOff: ImageVector? = null

// ── ZoomIn (zoom_in) ──
@Suppress("CheckReturnValue")
val SimpIcons.ZoomIn: ImageVector
  get() {
    if (_GenZoomIn != null) {
      return _GenZoomIn!!
    }
    _GenZoomIn =
      ImageVector.Builder(
          name = "ZoomIn",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(8.5f, 10.5f)
            horizontalLineToRelative(-1f)
            quadToRelative(-0.42f, 0f, -0.71f, -0.29f)
            reflectiveQuadTo(6.5f, 9.5f)
            quadTo(6.5f, 9.07f, 6.79f, 8.79f)
            reflectiveQuadTo(7.5f, 8.5f)
            horizontalLineToRelative(1f)
            verticalLineToRelative(-1f)
            quadTo(8.5f, 7.07f, 8.79f, 6.79f)
            quadTo(9.08f, 6.5f, 9.5f, 6.5f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(10.5f, 7.5f)
            verticalLineToRelative(1f)
            horizontalLineToRelative(1f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(12.5f, 9.5f)
            quadToRelative(0f, 0.42f, -0.29f, 0.71f)
            reflectiveQuadTo(11.5f, 10.5f)
            horizontalLineToRelative(-1f)
            verticalLineToRelative(1f)
            quadToRelative(0f, 0.42f, -0.29f, 0.71f)
            reflectiveQuadTo(9.5f, 12.5f)
            quadToRelative(-0.42f, 0f, -0.71f, -0.29f)
            quadTo(8.5f, 11.93f, 8.5f, 11.5f)
            verticalLineToRelative(-1f)
            close()
            moveToRelative(1f, 5.5f)
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
        }
        .build()
    return _GenZoomIn!!
  }

private var _GenZoomIn: ImageVector? = null

// ── ZoomOut (zoom_out) ──
@Suppress("CheckReturnValue")
val SimpIcons.ZoomOut: ImageVector
  get() {
    if (_GenZoomOut != null) {
      return _GenZoomOut!!
    }
    _GenZoomOut =
      ImageVector.Builder(
          name = "ZoomOut",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(8f, 10.5f)
            quadToRelative(-0.42f, 0f, -0.71f, -0.29f)
            reflectiveQuadTo(7f, 9.5f)
            quadTo(7f, 9.07f, 7.29f, 8.79f)
            reflectiveQuadTo(8f, 8.5f)
            horizontalLineToRelative(3f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(12f, 9.5f)
            quadToRelative(0f, 0.42f, -0.29f, 0.71f)
            reflectiveQuadTo(11f, 10.5f)
            horizontalLineTo(8f)
            close()
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
        }
        .build()
    return _GenZoomOut!!
  }

private var _GenZoomOut: ImageVector? = null

// ── RestartAlt (restart_alt) ──
@Suppress("CheckReturnValue")
val SimpIcons.RestartAlt: ImageVector
  get() {
    if (_GenRestartAlt != null) {
      return _GenRestartAlt!!
    }
    _GenRestartAlt =
      ImageVector.Builder(
          name = "RestartAlt",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(9.83f, 20.7f)
            quadTo(7.25f, 19.98f, 5.63f, 17.86f)
            reflectiveQuadTo(4f, 13f)
            quadTo(4f, 11.58f, 4.48f, 10.29f)
            reflectiveQuadTo(5.83f, 7.93f)
            quadTo(6.1f, 7.63f, 6.5f, 7.61f)
            reflectiveQuadTo(7.23f, 7.93f)
            quadTo(7.5f, 8.2f, 7.51f, 8.6f)
            reflectiveQuadTo(7.25f, 9.35f)
            quadToRelative(-0.6f, 0.78f, -0.92f, 1.7f)
            quadTo(6f, 11.98f, 6f, 13f)
            quadToRelative(0f, 2.03f, 1.19f, 3.61f)
            reflectiveQuadToRelative(3.06f, 2.16f)
            quadToRelative(0.33f, 0.1f, 0.54f, 0.38f)
            quadTo(11f, 19.43f, 11f, 19.75f)
            quadToRelative(0f, 0.5f, -0.35f, 0.79f)
            reflectiveQuadTo(9.83f, 20.7f)
            close()
            moveToRelative(4.35f, 0f)
            quadTo(13.7f, 20.83f, 13.35f, 20.53f)
            quadTo(13f, 20.23f, 13f, 19.73f)
            quadToRelative(0f, -0.3f, 0.21f, -0.58f)
            quadToRelative(0.21f, -0.27f, 0.54f, -0.38f)
            quadToRelative(1.88f, -0.6f, 3.06f, -2.17f)
            reflectiveQuadTo(18f, 13f)
            quadTo(18f, 10.5f, 16.25f, 8.75f)
            reflectiveQuadTo(12f, 7f)
            horizontalLineTo(11.93f)
            lineToRelative(0.4f, 0.4f)
            quadTo(12.6f, 7.68f, 12.6f, 8.1f)
            reflectiveQuadTo(12.33f, 8.8f)
            reflectiveQuadToRelative(-0.7f, 0.28f)
            reflectiveQuadTo(10.93f, 8.8f)
            lineTo(8.83f, 6.7f)
            quadTo(8.68f, 6.55f, 8.61f, 6.38f)
            reflectiveQuadTo(8.55f, 6f)
            reflectiveQuadTo(8.61f, 5.63f)
            reflectiveQuadTo(8.83f, 5.3f)
            lineToRelative(2.1f, -2.1f)
            quadTo(11.2f, 2.92f, 11.63f, 2.92f)
            reflectiveQuadToRelative(0.7f, 0.28f)
            quadTo(12.6f, 3.47f, 12.6f, 3.9f)
            reflectiveQuadTo(12.33f, 4.6f)
            lineTo(11.93f, 5f)
            horizontalLineTo(12f)
            quadToRelative(3.35f, 0f, 5.68f, 2.32f)
            reflectiveQuadTo(20f, 13f)
            quadToRelative(0f, 2.72f, -1.63f, 4.85f)
            reflectiveQuadToRelative(-4.2f, 2.85f)
            close()
          }
        }
        .build()
    return _GenRestartAlt!!
  }

private var _GenRestartAlt: ImageVector? = null

// ── Bookmark (bookmark) ──
@Suppress("CheckReturnValue")
val SimpIcons.Bookmark: ImageVector
  get() {
    if (_GenBookmark != null) {
      return _GenBookmark!!
    }
    _GenBookmark =
      ImageVector.Builder(
          name = "Bookmark",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(12f, 18f)
            lineTo(7.8f, 19.8f)
            quadToRelative(-1f, 0.43f, -1.9f, -0.16f)
            reflectiveQuadTo(5f, 17.98f)
            verticalLineTo(5f)
            quadTo(5f, 4.17f, 5.59f, 3.59f)
            reflectiveQuadTo(7f, 3f)
            horizontalLineTo(17f)
            quadToRelative(0.82f, 0f, 1.41f, 0.59f)
            reflectiveQuadTo(19f, 5f)
            verticalLineTo(17.98f)
            quadToRelative(0f, 1.07f, -0.9f, 1.66f)
            quadToRelative(-0.9f, 0.59f, -1.9f, 0.16f)
            lineTo(12f, 18f)
            close()
          }
        }
        .build()
    return _GenBookmark!!
  }

private var _GenBookmark: ImageVector? = null

// ── BookmarkBorder (bookmark unfilled) ──
@Suppress("CheckReturnValue")
val SimpIcons.BookmarkBorder: ImageVector
  get() {
    if (_GenBookmarkBorder != null) {
      return _GenBookmarkBorder!!
    }
    _GenBookmarkBorder =
      ImageVector.Builder(
          name = "BookmarkBorder",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(12f, 18f)
            lineTo(7.8f, 19.8f)
            quadToRelative(-1f, 0.43f, -1.9f, -0.16f)
            reflectiveQuadTo(5f, 17.98f)
            verticalLineTo(5f)
            quadTo(5f, 4.17f, 5.59f, 3.59f)
            reflectiveQuadTo(7f, 3f)
            horizontalLineTo(17f)
            quadToRelative(0.82f, 0f, 1.41f, 0.59f)
            reflectiveQuadTo(19f, 5f)
            verticalLineTo(17.98f)
            quadToRelative(0f, 1.07f, -0.9f, 1.66f)
            quadToRelative(-0.9f, 0.59f, -1.9f, 0.16f)
            lineTo(12f, 18f)
            close()
            moveToRelative(0f, -2.2f)
            lineToRelative(5f, 2.15f)
            verticalLineTo(5f)
            horizontalLineTo(7f)
            verticalLineTo(17.95f)
            lineTo(12f, 15.8f)
            close()
            moveTo(12f, 5f)
            horizontalLineTo(7f)
            horizontalLineTo(17f)
            horizontalLineTo(12f)
            close()
          }
        }
        .build()
    return _GenBookmarkBorder!!
  }

private var _GenBookmarkBorder: ImageVector? = null

// ── Backup (backup) ──
@Suppress("CheckReturnValue")
val SimpIcons.Backup: ImageVector
  get() {
    if (_GenBackup != null) {
      return _GenBackup!!
    }
    _GenBackup =
      ImageVector.Builder(
          name = "Backup",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(6.5f, 20f)
            quadTo(4.23f, 20f, 2.61f, 18.43f)
            reflectiveQuadTo(1f, 14.58f)
            quadTo(1f, 12.63f, 2.18f, 11.1f)
            reflectiveQuadTo(5.25f, 9.15f)
            quadTo(5.88f, 6.85f, 7.75f, 5.43f)
            reflectiveQuadTo(12f, 4f)
            quadToRelative(2.93f, 0f, 4.96f, 2.04f)
            reflectiveQuadTo(19f, 11f)
            quadToRelative(1.73f, 0.2f, 2.86f, 1.49f)
            reflectiveQuadTo(23f, 15.5f)
            quadToRelative(0f, 1.88f, -1.31f, 3.19f)
            reflectiveQuadTo(18.5f, 20f)
            horizontalLineTo(13f)
            verticalLineTo(12.85f)
            lineToRelative(0.9f, 0.88f)
            quadTo(14.18f, 14f, 14.59f, 14f)
            reflectiveQuadTo(15.3f, 13.7f)
            quadTo(15.58f, 13.43f, 15.58f, 13f)
            reflectiveQuadTo(15.3f, 12.3f)
            lineTo(12.7f, 9.7f)
            quadTo(12.4f, 9.4f, 12f, 9.4f)
            reflectiveQuadTo(11.3f, 9.7f)
            lineTo(8.7f, 12.3f)
            quadTo(8.43f, 12.58f, 8.41f, 12.99f)
            reflectiveQuadTo(8.7f, 13.7f)
            quadToRelative(0.27f, 0.28f, 0.69f, 0.29f)
            reflectiveQuadTo(10.1f, 13.73f)
            lineTo(11f, 12.85f)
            verticalLineTo(20f)
            horizontalLineTo(6.5f)
            close()
          }
        }
        .build()
    return _GenBackup!!
  }

private var _GenBackup: ImageVector? = null

// ── Restore (restore) ──
@Suppress("CheckReturnValue")
val SimpIcons.Restore: ImageVector
  get() {
    if (_GenRestore != null) {
      return _GenRestore!!
    }
    _GenRestore =
      ImageVector.Builder(
          name = "Restore",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(12f, 21f)
            quadTo(8.85f, 21f, 6.43f, 19.09f)
            quadTo(4f, 17.18f, 3.28f, 14.2f)
            quadTo(3.18f, 13.83f, 3.43f, 13.51f)
            reflectiveQuadTo(4.1f, 13.15f)
            quadTo(4.5f, 13.1f, 4.83f, 13.3f)
            reflectiveQuadToRelative(0.45f, 0.6f)
            quadToRelative(0.6f, 2.25f, 2.47f, 3.68f)
            reflectiveQuadTo(12f, 19f)
            quadToRelative(2.93f, 0f, 4.96f, -2.04f)
            quadTo(19f, 14.93f, 19f, 12f)
            quadTo(19f, 9.07f, 16.96f, 7.04f)
            reflectiveQuadTo(12f, 5f)
            quadTo(10.28f, 5f, 8.78f, 5.8f)
            reflectiveQuadTo(6.25f, 8f)
            horizontalLineTo(8f)
            quadTo(8.43f, 8f, 8.71f, 8.29f)
            reflectiveQuadTo(9f, 9f)
            quadTo(9f, 9.42f, 8.71f, 9.71f)
            reflectiveQuadTo(8f, 10f)
            horizontalLineTo(4f)
            quadTo(3.58f, 10f, 3.29f, 9.71f)
            reflectiveQuadTo(3f, 9f)
            verticalLineTo(5f)
            quadTo(3f, 4.57f, 3.29f, 4.29f)
            reflectiveQuadTo(4f, 4f)
            reflectiveQuadTo(4.71f, 4.29f)
            reflectiveQuadTo(5f, 5f)
            verticalLineTo(6.35f)
            quadTo(6.28f, 4.75f, 8.11f, 3.88f)
            reflectiveQuadTo(12f, 3f)
            quadToRelative(1.88f, 0f, 3.51f, 0.71f)
            reflectiveQuadToRelative(2.85f, 1.93f)
            reflectiveQuadToRelative(1.93f, 2.85f)
            reflectiveQuadTo(21f, 12f)
            reflectiveQuadToRelative(-0.71f, 3.51f)
            reflectiveQuadToRelative(-1.93f, 2.85f)
            reflectiveQuadToRelative(-2.85f, 1.93f)
            reflectiveQuadTo(12f, 21f)
            close()
            moveToRelative(1f, -9.4f)
            lineToRelative(2.5f, 2.5f)
            quadToRelative(0.28f, 0.28f, 0.28f, 0.7f)
            quadToRelative(0f, 0.43f, -0.28f, 0.7f)
            reflectiveQuadToRelative(-0.7f, 0.28f)
            reflectiveQuadTo(14.1f, 15.5f)
            lineTo(11.3f, 12.7f)
            quadTo(11.15f, 12.55f, 11.08f, 12.36f)
            reflectiveQuadTo(11f, 11.98f)
            verticalLineTo(8f)
            quadTo(11f, 7.57f, 11.29f, 7.29f)
            reflectiveQuadTo(12f, 7f)
            reflectiveQuadToRelative(0.71f, 0.29f)
            reflectiveQuadTo(13f, 8f)
            verticalLineToRelative(3.6f)
            close()
          }
        }
        .build()
    return _GenRestore!!
  }

private var _GenRestore: ImageVector? = null

// ── FolderOpen (folder_open) ──
@Suppress("CheckReturnValue")
val SimpIcons.FolderOpen: ImageVector
  get() {
    if (_GenFolderOpen != null) {
      return _GenFolderOpen!!
    }
    _GenFolderOpen =
      ImageVector.Builder(
          name = "FolderOpen",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(4f, 20f)
            quadTo(3.18f, 20f, 2.59f, 19.41f)
            reflectiveQuadTo(2f, 18f)
            verticalLineTo(6f)
            quadTo(2f, 5.18f, 2.59f, 4.59f)
            reflectiveQuadTo(4f, 4f)
            horizontalLineTo(9.18f)
            quadToRelative(0.4f, 0f, 0.76f, 0.15f)
            reflectiveQuadToRelative(0.64f, 0.43f)
            lineTo(12f, 6f)
            horizontalLineToRelative(9f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(22f, 7f)
            reflectiveQuadTo(21.71f, 7.71f)
            reflectiveQuadTo(21f, 8f)
            horizontalLineTo(7.85f)
            quadTo(6.3f, 8f, 5.15f, 8.98f)
            reflectiveQuadTo(4f, 11.45f)
            verticalLineTo(18f)
            lineTo(5.98f, 11.43f)
            quadToRelative(0.2f, -0.65f, 0.74f, -1.04f)
            reflectiveQuadTo(7.9f, 10f)
            horizontalLineTo(20.8f)
            quadToRelative(1.03f, 0f, 1.61f, 0.81f)
            reflectiveQuadToRelative(0.31f, 1.76f)
            lineToRelative(-1.8f, 6f)
            quadToRelative(-0.2f, 0.65f, -0.74f, 1.04f)
            reflectiveQuadTo(19f, 20f)
            horizontalLineTo(4f)
            close()
          }
        }
        .build()
    return _GenFolderOpen!!
  }

private var _GenFolderOpen: ImageVector? = null

