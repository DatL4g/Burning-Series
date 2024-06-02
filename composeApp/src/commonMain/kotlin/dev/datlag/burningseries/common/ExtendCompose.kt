package dev.datlag.burningseries.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.max

@Composable
operator fun PaddingValues.plus(other: PaddingValues): PaddingValues {
    val direction = LocalLayoutDirection.current

    return PaddingValues(
        start = this.calculateStartPadding(direction) + other.calculateStartPadding(direction),
        top = this.calculateTopPadding() + other.calculateTopPadding(),
        end = this.calculateEndPadding(direction) + other.calculateEndPadding(direction),
        bottom = this.calculateBottomPadding() + other.calculateBottomPadding()
    )
}

@Composable
operator fun PaddingValues.plus(all: Dp): PaddingValues {
    val direction = LocalLayoutDirection.current
    val other = PaddingValues(all)

    return PaddingValues(
        start = this.calculateStartPadding(direction) + other.calculateStartPadding(direction),
        top = this.calculateTopPadding() + other.calculateTopPadding(),
        end = this.calculateEndPadding(direction) + other.calculateEndPadding(direction),
        bottom = this.calculateBottomPadding() + other.calculateBottomPadding()
    )
}

@Composable
fun PaddingValues.merge(other: PaddingValues): PaddingValues {
    val direction = LocalLayoutDirection.current

    return PaddingValues(
        start = max(this.calculateStartPadding(direction), other.calculateStartPadding(direction)),
        top = max(this.calculateTopPadding(), other.calculateTopPadding()),
        end = max(this.calculateEndPadding(direction), other.calculateEndPadding(direction)),
        bottom = max(this.calculateBottomPadding(), other.calculateBottomPadding())
    )
}

@Composable
fun PaddingValues.merge(all: Dp): PaddingValues {
    val direction = LocalLayoutDirection.current
    val other = PaddingValues(all)

    return PaddingValues(
        start = max(this.calculateStartPadding(direction), other.calculateStartPadding(direction)),
        top = max(this.calculateTopPadding(), other.calculateTopPadding()),
        end = max(this.calculateEndPadding(direction), other.calculateEndPadding(direction)),
        bottom = max(this.calculateBottomPadding(), other.calculateBottomPadding())
    )
}
