import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max

@Composable
fun CustomFlowRow(
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp = 8.dp,
    verticalSpacing: Dp = 8.dp,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val horizontalSpacingPx = horizontalSpacing.roundToPx()
        val verticalSpacingPx = verticalSpacing.roundToPx()

        var currentX = 0
        var currentY = 0
        var rowHeight = 0
        val placeables = measurables.map { measurable ->
            val placeable = measurable.measure(constraints.copy(minWidth = 0))
            if (currentX + placeable.width > constraints.maxWidth && currentX > 0) {
                currentX = 0
                currentY += rowHeight + verticalSpacingPx
                rowHeight = 0
            }
            val x = currentX
            val y = currentY
            currentX += placeable.width + horizontalSpacingPx
            rowHeight = max(rowHeight, placeable.height)
            Triple(placeable, x, y)
        }
        
        val width = constraints.maxWidth
        val height = if (placeables.isEmpty()) 0 else currentY + rowHeight
        
        layout(width, height) {
            placeables.forEach { (placeable, x, y) ->
                placeable.placeRelative(x, y)
            }
        }
    }
}
