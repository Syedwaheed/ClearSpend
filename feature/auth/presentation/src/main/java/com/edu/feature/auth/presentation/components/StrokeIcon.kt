package com.edu.feature.auth.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp

/** One stroked path in a 24×24 viewBox, as in the prototype's inline SVGs. */
data class StrokePath(
    val pathData: String,
    val color: Color,
    val strokeWidth: Float,
)

/**
 * Draws the design's outline icons exactly (paths copied from
 * designs/ClearSpend.dc.html) so the glyphs match the screenshots.
 */
@Composable
fun StrokeIcon(
    paths: List<StrokePath>,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val parsed = remember(paths) {
        paths.map { PathParser().parsePathString(it.pathData).toPath() to it }
    }
    Canvas(modifier = modifier.size(size)) {
        val factor = this.size.minDimension / VIEWBOX
        scale(factor, pivot = androidx.compose.ui.geometry.Offset.Zero) {
            parsed.forEach { (path, spec) ->
                drawPath(
                    path = path,
                    color = spec.color,
                    style = Stroke(
                        width = spec.strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    ),
                )
            }
        }
    }
}

private const val VIEWBOX = 24f
