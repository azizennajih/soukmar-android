package com.soukmar.app.ui.model

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser

/**
 * Hand-ported from soukmar/src/app/components/cat-icon/cat-icon.component.html
 * (Lucide-style stroke icons, 24x24 viewBox, stroke-width 2, round caps/joins) —
 * replaces the plain category emoji at the handful of spots the web app itself
 * moved to real icons (commit fe5ad55): home category grid + interests
 * section, listings filter sidebar, deposer-annonce category step.
 * Everywhere else (listing cards, listing-detail badge, mes-annonces, saved
 * searches) both apps deliberately kept the plain emoji — CATEGORIES.emoji is
 * still used there. Keep this in sync with the web file above if the
 * category set or icon shapes change.
 */
@Composable
fun CategoryIcon(category: String, tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        scale(size.width / 24f, size.height / 24f, pivot = Offset.Zero) {
            val stroke = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            fun path(d: String, style: Stroke = stroke) {
                drawPath(PathParser().parsePathString(d).toPath(), color = tint, style = style)
            }
            fun rect(x: Float, y: Float, w: Float, h: Float, r: Float = 0f) {
                drawRoundRect(color = tint, topLeft = Offset(x, y), size = Size(w, h), cornerRadius = CornerRadius(r, r), style = stroke)
            }
            fun circle(cx: Float, cy: Float, r: Float) {
                drawCircle(color = tint, radius = r, center = Offset(cx, cy), style = stroke)
            }
            fun line(x1: Float, y1: Float, x2: Float, y2: Float) {
                drawLine(color = tint, start = Offset(x1, y1), end = Offset(x2, y2), strokeWidth = 2f, cap = StrokeCap.Round)
            }
            fun polyline(vararg pts: Float) {
                val p = Path()
                p.moveTo(pts[0], pts[1])
                var i = 2
                while (i < pts.size) {
                    p.lineTo(pts[i], pts[i + 1])
                    i += 2
                }
                drawPath(p, color = tint, style = stroke)
            }

            when (category) {
                "VEHICLES" -> {
                    path("M5 11l1.5-4.5A2 2 0 0 1 8.4 5h7.2a2 2 0 0 1 1.9 1.5L19 11")
                    rect(3f, 11f, 18f, 6f, 2f)
                    circle(7.5f, 17f, 1.5f)
                    circle(16.5f, 17f, 1.5f)
                }
                "REAL_ESTATE" -> {
                    path("M3 10.5 12 3l9 7.5")
                    path("M5 9.5V20a1 1 0 0 0 1 1h4v-6h4v6h4a1 1 0 0 0 1-1V9.5")
                }
                "JOBS" -> {
                    rect(2f, 7f, 20f, 13f, 2f)
                    path("M8 7V5a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2")
                    line(2f, 12f, 22f, 12f)
                }
                "ELECTRONICS" -> {
                    rect(6f, 2f, 12f, 20f, 2f)
                    line(11f, 18f, 13f, 18f)
                }
                "HOME_GARDEN" -> {
                    path("M12 3c-2 2-2 5 0 7 2-2 2-5 0-7z")
                    path("M12 10c-3-1-6 1-6 4h12c0-3-3-5-6-4z")
                    line(12f, 10f, 12f, 21f)
                    path("M8 21h8")
                }
                "FASHION" -> {
                    path("M20.38 3.46 16 2a4 4 0 0 1-8 0L3.62 3.46a2 2 0 0 0-1.34 2.23l.58 3.47a1 1 0 0 0 .99.84H6v10a2 2 0 0 0 2 2h8a2 2 0 0 0 2-2V10h2.15a1 1 0 0 0 .99-.84l.58-3.47a2 2 0 0 0-1.34-2.23z")
                }
                "SERVICES" -> {
                    path("M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94z")
                }
                "OTHER" -> {
                    path("M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z")
                    polyline(3.29f, 7f, 12f, 12f, 20.71f, 7f)
                    line(12f, 22f, 12f, 12f)
                }
                "BABY_KIDS" -> {
                    path("M10 2h4")
                    line(12f, 2f, 12f, 5f)
                    path("M9 8.5a3 3 0 0 1 6 0V19a2.5 2.5 0 0 1-2.5 2.5h-1A2.5 2.5 0 0 1 9 19z")
                    line(9f, 13f, 15f, 13f)
                }
                "PETS" -> {
                    circle(7f, 9f, 2f)
                    circle(12f, 6.5f, 2f)
                    circle(17f, 9f, 2f)
                    path("M12 12c-3.5 0-6.5 2.5-6.5 5.8 0 2 1.7 3.2 3.6 2.6.9-.3 1.9-.4 2.9-.4s2 .1 2.9.4c1.9.6 3.6-.6 3.6-2.6 0-3.3-3-5.8-6.5-5.8z")
                }
                "SPORTS_LEISURE" -> {
                    line(6f, 12f, 18f, 12f)
                    rect(2f, 9f, 4f, 6f, 1f)
                    rect(18f, 9f, 4f, 6f, 1f)
                    rect(6f, 10.5f, 2f, 3f, 0.5f)
                    rect(16f, 10.5f, 2f, 3f, 0.5f)
                }
                "LESSONS_COURSES" -> {
                    path("M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z")
                    path("M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z")
                }
                "CARPOOLING" -> {
                    circle(4f, 5f, 1.5f)
                    circle(20f, 5f, 1.5f)
                    path(
                        "M5.5 5h13",
                        style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.5f, 2.5f)))
                    )
                    path("M5 14l1.5-4.5A2 2 0 0 1 8.4 8h7.2a2 2 0 0 1 1.9 1.5L19 14")
                    rect(3f, 14f, 18f, 6f, 2f)
                    circle(7.5f, 20f, 1.5f)
                    circle(16.5f, 20f, 1.5f)
                }
                "TRANSPORT" -> {
                    rect(1f, 7f, 13f, 9f, 1f)
                    path("M14 10h4l4 3.5V16h-2")
                    circle(5.5f, 18f, 1.5f)
                    circle(16.5f, 18f, 1.5f)
                    line(7f, 18f, 15f, 18f)
                }
                "RENTAL" -> {
                    path("M3 12l1-3a1.5 1.5 0 0 1 1.4-1h5.2a1.5 1.5 0 0 1 1.4 1l1 3")
                    rect(2f, 12f, 12f, 5f, 1.5f)
                    circle(5.5f, 17f, 1.3f)
                    circle(10.5f, 17f, 1.3f)
                    circle(19f, 7f, 2.2f)
                    path("M19 4.3v-1M19 10.7v1M21.7 7h1M15.3 7h1M20.9 5.1l.7-.7M16.4 8.9l.7-.7M20.9 8.9l.7.7M16.4 5.1l.7.7")
                }
                "TICKETS" -> {
                    path("M2 9a3 3 0 0 1 0 6v2a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-2a3 3 0 0 1 0-6V7a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2Z")
                    line(13f, 5f, 13f, 7f)
                    line(13f, 17f, 13f, 19f)
                    line(13f, 11f, 13f, 13f)
                }
                "GIVEAWAY_SWAP" -> {
                    rect(3f, 8f, 18f, 4f, 1f)
                    line(12f, 8f, 12f, 21f)
                    path("M19 12v7a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2v-7")
                    path("M7.5 8a2.5 2.5 0 0 1 0-5C11 3 12 8 12 8s1-5 4.5-5a2.5 2.5 0 0 1 0 5")
                }
                "MOVING" -> {
                    rect(3f, 11f, 8f, 8f, 1f)
                    rect(13f, 7f, 8f, 12f, 1f)
                    line(7f, 11f, 7f, 8f)
                    line(5f, 9.5f, 9f, 9.5f)
                }
            }
        }
    }
}
