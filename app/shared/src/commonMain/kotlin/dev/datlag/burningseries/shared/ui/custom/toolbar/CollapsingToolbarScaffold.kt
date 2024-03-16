package dev.datlag.burningseries.shared.ui.custom.toolbar

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import dev.datlag.burningseries.model.common.safeCast
import kotlin.math.max
import kotlin.math.roundToInt

@Stable
class CollapsingToolbarScaffoldState(
    val toolbarState: CollapsingToolbarState,
    initialOffsetY: Int = 0
) {
    val offsetY: Int
        get() = offsetYState.value

    internal val offsetYState = mutableStateOf(initialOffsetY)
}

private class CollapsingToolbarScaffoldStateSaver: Saver<CollapsingToolbarScaffoldState, List<Any>> {
    override fun restore(value: List<Any>): CollapsingToolbarScaffoldState =
        CollapsingToolbarScaffoldState(
            CollapsingToolbarState(
                value[0] as Int,
                value[1] as Int,
                value[2] as Int
            ),
            value[3] as Int
        )

    override fun SaverScope.save(value: CollapsingToolbarScaffoldState): List<Any> =
        listOf(
            value.toolbarState.height,
            value.toolbarState.minHeight,
            value.toolbarState.maxHeight,
            value.offsetY
        )
}

@Composable
fun rememberCollapsingToolbarScaffoldState(
    toolbarState: CollapsingToolbarState = rememberCollapsingToolbarState()
): CollapsingToolbarScaffoldState {
    return rememberSaveable(toolbarState, saver = CollapsingToolbarScaffoldStateSaver()) {
        CollapsingToolbarScaffoldState(toolbarState)
    }
}

interface CollapsingToolbarScaffoldScope {
    fun Modifier.align(alignment: Alignment): Modifier
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsingToolbarScaffold(
    modifier: Modifier,
    state: CollapsingToolbarScaffoldState,
    scrollStrategy: ScrollStrategy,
    enabled: Boolean = true,
    toolbarModifier: Modifier = Modifier,
    toolbarPadding: Int = TopAppBarDefaults.windowInsets.asPaddingValues().calculateTopPadding().value.roundToInt(),
    toolbarClipToBounds: Boolean = true,
    toolbarScrollable: Boolean = false,
    toolbar: @Composable CollapsingToolbarScope.() -> Unit,
    body: @Composable CollapsingToolbarScaffoldScope.() -> Unit
) {
    val flingBehavior = ScrollableDefaults.flingBehavior()
    val layoutDirection = LocalLayoutDirection.current

    val nestedScrollConnection = remember(scrollStrategy, state) {
        scrollStrategy.create(state.offsetYState, state.toolbarState, flingBehavior)
    }

    val toolbarState = state.toolbarState
    val toolbarScrollState = rememberScrollState()

    Layout(
        content = {
            CollapsingToolbar(
                modifier = toolbarModifier,
                clipToBounds = toolbarClipToBounds,
                collapsingToolbarState = toolbarState,
            ) {
                ToolbarScrollableBox(
                    enabled,
                    toolbarScrollable,
                    toolbarState,
                    toolbarScrollState
                )

                toolbar()
            }

            CollapsingToolbarScaffoldScopeInstance.body()
        },
        modifier = modifier
            .then(
                if (enabled) {
                    Modifier.nestedScroll(nestedScrollConnection)
                } else {
                    Modifier
                }
            )
    ) { measurables, constraints ->
        check(measurables.size >= 2) {
            "the number of children should be at least 2: toolbar, (at least one) body"
        }

        val toolbarConstraints = constraints.copy(
            minWidth = 0,
            minHeight = 0
        )

        val toolbarPlaceable = measurables[0].measure(toolbarConstraints)
        val toolbarHeight = toolbarPlaceable.height + toolbarPadding

        val bodyConstraints = constraints.copy(
            minWidth = 0,
            minHeight = 0,
            maxHeight = when (scrollStrategy) {
                ScrollStrategy.ExitUntilCollapsed ->
                    (constraints.maxHeight - toolbarHeight).coerceAtLeast(0)

                ScrollStrategy.EnterAlways, ScrollStrategy.EnterAlwaysCollapsed ->
                    constraints.maxHeight
            }
        )

        val bodyMeasurables = measurables.subList(1, measurables.size)
        val childrenAlignments = bodyMeasurables.map {
            it.parentData.safeCast<ScaffoldParentData>()?.alignment
        }
        val bodyPlaceables = bodyMeasurables.map {
            it.measure(bodyConstraints)
        }

        val width = max(
            toolbarPlaceable.width,
            bodyPlaceables.maxOfOrNull { it.width } ?: 0
        ).coerceIn(constraints.minWidth, constraints.maxWidth)
        val height = max(
            toolbarHeight,
            bodyPlaceables.maxOfOrNull { it.height } ?: 0
        ).coerceIn(constraints.minHeight, constraints.maxHeight)

        layout(width, height) {
            bodyPlaceables.forEachIndexed { index, placeable ->
                val alignment = childrenAlignments[index]

                if (alignment == null) {
                    placeable.placeRelative(0, toolbarHeight + state.offsetY)
                } else {
                    val offset = alignment.align(
                        size = IntSize(placeable.width, placeable.height),
                        space = IntSize(width, height),
                        layoutDirection = layoutDirection
                    )
                    placeable.place(offset)
                }
            }
            toolbarPlaceable.placeRelative(0, state.offsetY)
        }
    }
}

@Composable
private fun ToolbarScrollableBox(
    enabled: Boolean,
    toolbarScrollable: Boolean,
    toolbarState: CollapsingToolbarState,
    toolbarScrollState: ScrollState
) {
    val toolbarScrollableEnabled = enabled && toolbarScrollable

    if (toolbarScrollableEnabled && toolbarState.height != Constraints.Infinity) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(with(LocalDensity.current) { toolbarState.height.toDp() })
                .verticalScroll(state = toolbarScrollState)
        )
    }
}

internal object CollapsingToolbarScaffoldScopeInstance: CollapsingToolbarScaffoldScope {
    override fun Modifier.align(alignment: Alignment): Modifier =
        this.then(ScaffoldChildAlignmentModifier(alignment))
}

private class ScaffoldChildAlignmentModifier(
    private val alignment: Alignment
) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?): Any {
        return parentData.safeCast<ScaffoldParentData>()?.alignment ?: ScaffoldParentData(alignment)
    }
}

private data class ScaffoldParentData(
    var alignment: Alignment? = null
)