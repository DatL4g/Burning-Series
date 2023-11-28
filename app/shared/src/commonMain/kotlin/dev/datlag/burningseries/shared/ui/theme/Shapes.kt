package dev.datlag.burningseries.shared.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun TopLeftBottomRightRoundedShape(baseShape: CornerBasedShape = CircleShape, otherSideRounding: Dp = 2.dp) = baseShape.copy(
    topEnd = CornerSize(otherSideRounding),
    bottomStart = CornerSize(otherSideRounding)
)

fun TopRightBottomLeftRoundedShape(baseShape: CornerBasedShape = CircleShape, otherSideRounding: Dp = 2.dp) = baseShape.copy(
    topStart = CornerSize(otherSideRounding),
    bottomEnd = CornerSize(otherSideRounding)
)

val BottomLeftRoundedShape = CircleShape.copy(
    topStart = CornerSize(2.dp),
    topEnd = CornerSize(2.dp),
    bottomEnd = CornerSize(2.dp)
)

val BottomRightRoundedShape = CircleShape.copy(
    topStart = CornerSize(2.dp),
    topEnd = CornerSize(2.dp),
    bottomStart = CornerSize(2.dp)
)

fun LeftRoundedShape(rightSideRounding: Dp = 2.dp): Shape {
    return CircleShape.copy(
        topEnd = CornerSize(rightSideRounding),
        bottomEnd = CornerSize(rightSideRounding)
    )
}

fun RightRoundedShape(leftSideRounding: Dp = 2.dp): Shape {
    return CircleShape.copy(
        topStart = CornerSize(leftSideRounding),
        bottomStart = CornerSize(leftSideRounding)
    )
}

fun MiddleRoundedShape(sideRounding: Dp = 2.dp): Shape {
    return RoundedCornerShape(sideRounding)
}