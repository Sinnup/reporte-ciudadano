package com.espert.reporteciudadano.ui.adaptive

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp

/**
 * Returns true when the window width is >= 840dp (Expanded breakpoint).
 *
 * Uses [LocalWindowInfo.current.containerSize] which is available across all CMP targets
 * (Android, iOS, Web) without requiring the Android-only adaptive library.
 *
 * Breakpoints follow Material3 guidelines:
 *   Compact  < 600dp
 *   Medium   600dp – 839dp
 *   Expanded >= 840dp
 */
@Composable
fun isExpandedWidth(): Boolean {
    val density = LocalDensity.current
    val containerWidth = LocalWindowInfo.current.containerSize.width
    val widthDp = with(density) { containerWidth.toDp() }
    return widthDp >= 840.dp
}

/**
 * Returns true when the window width is >= 600dp (Medium or Expanded breakpoint).
 * Used to switch from NavigationBar to NavigationRail.
 */
@Composable
fun isMediumOrLargerWidth(): Boolean {
    val density = LocalDensity.current
    val containerWidth = LocalWindowInfo.current.containerSize.width
    val widthDp = with(density) { containerWidth.toDp() }
    return widthDp >= 600.dp
}
