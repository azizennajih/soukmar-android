package com.soukmar.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** Spacing scale (4-dp grid). Screen edge padding is [lg]. */
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}

/** Corner radii: badges 8, fields and buttons 12, cards and sheets 16, chips and search fully round. */
object Radius {
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val pill = 999.dp
}

val ShapeBadge = RoundedCornerShape(Radius.sm)
val ShapeControl = RoundedCornerShape(Radius.md)
val ShapeCard = RoundedCornerShape(Radius.lg)
val ShapePill = RoundedCornerShape(Radius.pill)

/** Icon sizes: inline with text 16, standard 24 (top bar, bottom bar, list rows), feature 28. Touch targets are at least 48 dp. */
object IconSize {
    val inline = 16.dp
    val small = 20.dp
    val standard = 24.dp
    val feature = 28.dp
}

/** Height of the main buttons and input fields. */
val ControlHeight = 52.dp
