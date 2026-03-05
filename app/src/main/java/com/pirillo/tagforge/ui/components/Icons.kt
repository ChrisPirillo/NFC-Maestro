package com.pirillo.tagforge.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pirillo.tagforge.ui.theme.Accent

@Composable
fun NfcIcon(modifier: Modifier = Modifier, size: Dp = 24.dp, color: Color = Accent) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.minDimension
        val cx = s / 2f; val cy = s / 2f
        drawCircle(color, radius = s * 0.065f, center = Offset(cx, cy))
        val arcs = listOf(1f to 1f, 0.55f to 1.3f, 0.25f to 1.6f)
        arcs.forEach { (alpha, scale) ->
            val r = s * 0.18f * scale
            drawArc(color.copy(alpha = alpha), startAngle = 200f, sweepAngle = 70f, useCenter = false,
                topLeft = Offset(cx - r, cy - r), size = Size(r * 2, r * 2),
                style = Stroke(width = s * 0.055f, cap = StrokeCap.Round))
        }
    }
}

@Composable
fun ScanIcon(modifier: Modifier = Modifier, size: Dp = 24.dp, color: Color = Color.White) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.minDimension; val g = s * 0.08f; val cl = s * 0.2f; val sw = s * 0.07f
        listOf(
            Offset(g, g + cl) to Offset(g, g), Offset(g, g) to Offset(g + cl, g),
            Offset(s - g - cl, g) to Offset(s - g, g), Offset(s - g, g) to Offset(s - g, g + cl),
            Offset(s - g, s - g - cl) to Offset(s - g, s - g), Offset(s - g, s - g) to Offset(s - g - cl, s - g),
            Offset(g + cl, s - g) to Offset(g, s - g), Offset(g, s - g) to Offset(g, s - g - cl),
        ).forEach { (a, b) -> drawLine(color, a, b, strokeWidth = sw, cap = StrokeCap.Round) }
        drawCircle(color, radius = s * 0.12f, center = Offset(s / 2, s / 2), style = Stroke(s * 0.06f))
        drawCircle(color, radius = s * 0.03f, center = Offset(s / 2, s / 2))
    }
}

@Composable
fun WriteIcon(modifier: Modifier = Modifier, size: Dp = 24.dp, color: Color = Color.White) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.minDimension; val sw = s * 0.07f
        val p = Path().apply { moveTo(s * 0.7f, s * 0.12f); lineTo(s * 0.88f, s * 0.3f); lineTo(s * 0.35f, s * 0.83f); lineTo(s * 0.12f, s * 0.88f); lineTo(s * 0.17f, s * 0.65f); close() }
        drawPath(p, color.copy(alpha = 0.15f)); drawPath(p, color, style = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun CloneIcon(modifier: Modifier = Modifier, size: Dp = 24.dp, color: Color = Color.White) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.minDimension; val sw = s * 0.07f; val r = CornerRadius(s * 0.06f)
        drawRoundRect(color.copy(alpha = 0.4f), Offset(s * 0.22f, s * 0.08f), Size(s * 0.58f, s * 0.58f), r, style = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawRoundRect(color, Offset(s * 0.2f, s * 0.34f), Size(s * 0.58f, s * 0.58f), r, style = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
        val cx = s * 0.49f; val cy = s * 0.63f; val pl = s * 0.12f
        drawLine(color, Offset(cx - pl, cy), Offset(cx + pl, cy), strokeWidth = sw, cap = StrokeCap.Round)
        drawLine(color, Offset(cx, cy - pl), Offset(cx, cy + pl), strokeWidth = sw, cap = StrokeCap.Round)
    }
}

@Composable
fun HistoryIcon(modifier: Modifier = Modifier, size: Dp = 24.dp, color: Color = Color.White) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.minDimension; val cx = s / 2f; val cy = s / 2f; val r = s * 0.38f; val sw = s * 0.07f
        drawCircle(color, radius = r, center = Offset(cx, cy), style = Stroke(sw))
        drawLine(color, Offset(cx, cy), Offset(cx, cy - r * 0.55f), strokeWidth = sw, cap = StrokeCap.Round)
        drawLine(color, Offset(cx, cy), Offset(cx + r * 0.45f, cy + r * 0.1f), strokeWidth = sw * 0.7f, cap = StrokeCap.Round)
        val a = Path().apply { moveTo(s * 0.14f, s * 0.32f); lineTo(s * 0.14f, s * 0.12f); lineTo(s * 0.34f, s * 0.12f) }
        drawPath(a, color, style = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun EraseIcon(modifier: Modifier = Modifier, size: Dp = 24.dp, color: Color = Color.White) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.minDimension; val sw = s * 0.07f
        drawLine(color, Offset(s * 0.2f, s * 0.25f), Offset(s * 0.8f, s * 0.25f), strokeWidth = sw, cap = StrokeCap.Round)
        drawLine(color, Offset(s * 0.38f, s * 0.25f), Offset(s * 0.42f, s * 0.13f), strokeWidth = sw, cap = StrokeCap.Round)
        drawLine(color, Offset(s * 0.42f, s * 0.13f), Offset(s * 0.58f, s * 0.13f), strokeWidth = sw, cap = StrokeCap.Round)
        drawLine(color, Offset(s * 0.58f, s * 0.13f), Offset(s * 0.62f, s * 0.25f), strokeWidth = sw, cap = StrokeCap.Round)
        val b = Path().apply { moveTo(s * 0.25f, s * 0.25f); lineTo(s * 0.3f, s * 0.87f); lineTo(s * 0.7f, s * 0.87f); lineTo(s * 0.75f, s * 0.25f) }
        drawPath(b, color, style = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawLine(color.copy(alpha = 0.5f), Offset(s * 0.42f, s * 0.38f), Offset(s * 0.43f, s * 0.74f), strokeWidth = sw * 0.6f, cap = StrokeCap.Round)
        drawLine(color.copy(alpha = 0.5f), Offset(s * 0.58f, s * 0.38f), Offset(s * 0.57f, s * 0.74f), strokeWidth = sw * 0.6f, cap = StrokeCap.Round)
    }
}

@Composable
fun LockIcon(modifier: Modifier = Modifier, size: Dp = 24.dp, color: Color = Color.White) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.minDimension; val sw = s * 0.07f
        drawArc(color, 180f, 180f, false, Offset(s * 0.3f, s * 0.1f), Size(s * 0.4f, s * 0.4f), style = Stroke(sw, cap = StrokeCap.Round))
        drawLine(color, Offset(s * 0.3f, s * 0.3f), Offset(s * 0.3f, s * 0.45f), strokeWidth = sw, cap = StrokeCap.Round)
        drawLine(color, Offset(s * 0.7f, s * 0.3f), Offset(s * 0.7f, s * 0.45f), strokeWidth = sw, cap = StrokeCap.Round)
        drawRoundRect(color, Offset(s * 0.2f, s * 0.45f), Size(s * 0.6f, s * 0.45f), CornerRadius(s * 0.06f), style = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawCircle(color, radius = s * 0.06f, center = Offset(s * 0.5f, s * 0.62f))
        drawLine(color, Offset(s * 0.5f, s * 0.62f), Offset(s * 0.5f, s * 0.76f), strokeWidth = sw * 0.8f, cap = StrokeCap.Round)
    }
}

@Composable
fun ToolsIcon(modifier: Modifier = Modifier, size: Dp = 24.dp, color: Color = Color.White) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.minDimension; val sw = s * 0.08f
        // Wrench shape
        val p = Path().apply {
            moveTo(s * 0.62f, s * 0.26f); lineTo(s * 0.74f, s * 0.14f)
            lineTo(s * 0.86f, s * 0.26f); lineTo(s * 0.38f, s * 0.74f)
            lineTo(s * 0.26f, s * 0.86f); lineTo(s * 0.14f, s * 0.74f)
            close()
        }
        drawPath(p, color.copy(alpha = 0.1f))
        drawPath(p, color, style = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
        // Bolt head circle
        drawCircle(color, radius = s * 0.14f, center = Offset(s * 0.74f, s * 0.26f), style = Stroke(sw * 0.7f))
    }
}

@Composable
fun LaunchIcon(modifier: Modifier = Modifier, size: Dp = 24.dp, color: Color = Color.White) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.minDimension; val sw = s * 0.07f
        // External link icon
        drawRoundRect(color, Offset(s * 0.12f, s * 0.3f), Size(s * 0.55f, s * 0.58f), CornerRadius(s * 0.06f), style = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawLine(color, Offset(s * 0.5f, s * 0.12f), Offset(s * 0.88f, s * 0.12f), strokeWidth = sw, cap = StrokeCap.Round)
        drawLine(color, Offset(s * 0.88f, s * 0.12f), Offset(s * 0.88f, s * 0.5f), strokeWidth = sw, cap = StrokeCap.Round)
        drawLine(color, Offset(s * 0.88f, s * 0.12f), Offset(s * 0.45f, s * 0.55f), strokeWidth = sw, cap = StrokeCap.Round)
    }
}

@Composable
fun ShareIcon(modifier: Modifier = Modifier, size: Dp = 24.dp, color: Color = Color.White) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.minDimension; val sw = s * 0.07f
        drawCircle(color, s * 0.09f, Offset(s * 0.75f, s * 0.2f))
        drawCircle(color, s * 0.09f, Offset(s * 0.25f, s * 0.5f))
        drawCircle(color, s * 0.09f, Offset(s * 0.75f, s * 0.8f))
        drawLine(color, Offset(s * 0.34f, s * 0.44f), Offset(s * 0.66f, s * 0.26f), strokeWidth = sw * 0.7f)
        drawLine(color, Offset(s * 0.34f, s * 0.56f), Offset(s * 0.66f, s * 0.74f), strokeWidth = sw * 0.7f)
    }
}

@Composable
fun LocationIcon(modifier: Modifier = Modifier, size: Dp = 24.dp, color: Color = Color.White) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.minDimension; val sw = s * 0.07f
        val p = Path().apply {
            moveTo(s * 0.5f, s * 0.9f)
            cubicTo(s * 0.5f, s * 0.9f, s * 0.15f, s * 0.55f, s * 0.15f, s * 0.38f)
            cubicTo(s * 0.15f, s * 0.15f, s * 0.85f, s * 0.15f, s * 0.85f, s * 0.38f)
            cubicTo(s * 0.85f, s * 0.55f, s * 0.5f, s * 0.9f, s * 0.5f, s * 0.9f)
            close()
        }
        drawPath(p, color, style = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawCircle(color, s * 0.1f, Offset(s * 0.5f, s * 0.38f), style = Stroke(sw * 0.8f))
    }
}

@Composable
fun ContactIcon(modifier: Modifier = Modifier, size: Dp = 24.dp, color: Color = Color.White) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.minDimension; val sw = s * 0.07f
        drawCircle(color, s * 0.15f, Offset(s * 0.5f, s * 0.3f), style = Stroke(sw))
        val p = Path().apply {
            moveTo(s * 0.18f, s * 0.88f)
            cubicTo(s * 0.18f, s * 0.6f, s * 0.82f, s * 0.6f, s * 0.82f, s * 0.88f)
        }
        drawPath(p, color, style = Stroke(sw, cap = StrokeCap.Round))
    }
}

@Composable
fun AppIcon(modifier: Modifier = Modifier, size: Dp = 24.dp, color: Color = Color.White) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.minDimension; val sw = s * 0.07f
        drawRoundRect(color, Offset(s * 0.22f, s * 0.08f), Size(s * 0.56f, s * 0.84f), CornerRadius(s * 0.08f), style = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawCircle(color, s * 0.04f, Offset(s * 0.5f, s * 0.78f))
    }
}

@Composable
fun PosterIcon(modifier: Modifier = Modifier, size: Dp = 24.dp, color: Color = Color.White) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.minDimension; val sw = s * 0.07f
        drawRoundRect(color, Offset(s * 0.12f, s * 0.12f), Size(s * 0.76f, s * 0.76f), CornerRadius(s * 0.06f), style = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
        val p = Path().apply { moveTo(s * 0.12f, s * 0.65f); lineTo(s * 0.3f, s * 0.48f); lineTo(s * 0.45f, s * 0.6f); lineTo(s * 0.65f, s * 0.4f); lineTo(s * 0.88f, s * 0.65f) }
        drawPath(p, color, style = Stroke(sw * 0.8f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}
