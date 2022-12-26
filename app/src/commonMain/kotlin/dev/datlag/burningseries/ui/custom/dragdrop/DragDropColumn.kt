package dev.datlag.burningseries.ui.custom.dragdrop

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.*
import androidx.compose.material.Card
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.common.DraggableItem
import dev.datlag.burningseries.common.rememberDragDropState
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun <T : Any> DragDropColumn(
    items: List<T>,
    onSwap: (Int, Int) -> Unit,
    itemsBefore: LazyListScope.() -> Unit = {},
    itemContent: @Composable LazyItemScope.(index: Int, item: T) -> Unit,
    itemsAfter: LazyListScope.() -> Unit = {}
) {
    var overscrollJob by remember { mutableStateOf<Job?>(null) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val dragDropState = rememberDragDropState(listState) { fromIndex, toIndex ->
        onSwap(fromIndex, toIndex)
    }

    LazyColumn(
        modifier = Modifier
            .pointerInput(dragDropState) {
                detectDragGesturesAfterLongPress(
                    onDrag = { change, offset ->
                        change.consume()
                        dragDropState.onDrag(offset = offset)

                        if (overscrollJob?.isActive == true)
                            return@detectDragGesturesAfterLongPress

                        dragDropState
                            .checkForOverScroll()
                            .takeIf { it != 0f }
                            ?.let {
                                overscrollJob =
                                    scope.launch {
                                        dragDropState.state.animateScrollBy(
                                            it*1.3f, tween(easing = FastOutLinearInEasing)
                                        )
                                    }
                            }
                            ?: run { overscrollJob?.cancel() }
                    },
                    onDragStart = { offset -> dragDropState.onDragStart(offset) },
                    onDragEnd = {
                        dragDropState.onDragInterrupted()
                        overscrollJob?.cancel()
                    },
                    onDragCancel = {
                        dragDropState.onDragInterrupted()
                        overscrollJob?.cancel()
                    }
                )
            },
        state = listState,
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsBefore()

        itemsIndexed(items = items) { index, item ->
            DraggableItem(
                dragDropState = dragDropState,
                index = index
            ) { isDragging ->
                val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)
                Card(elevation = elevation) {
                    itemContent(index, item)
                }
            }
        }

        itemsAfter()
    }
}