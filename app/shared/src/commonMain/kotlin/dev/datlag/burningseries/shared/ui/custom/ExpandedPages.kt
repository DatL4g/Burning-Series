package dev.datlag.burningseries.shared.ui.custom

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.value.Value

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun <T : Any> ExpandedPages(
    pages: Value<ChildPages<*, T>>,
    pageContent: @Composable (index: Int, page: T) -> Unit
) {
    val state = pages.subscribeAsState()

    ExpandedPages(
        pages = state.value,
        pageContent = pageContent
    )
}

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun <T : Any> ExpandedPages(
    pages: ChildPages<*, T>,
    pageContent: @Composable (index: Int, page: T) -> Unit
) {
    val selectedIndex = pages.selectedIndex
    val items = pages.items

    items[selectedIndex].instance?.also { page ->
        pageContent(selectedIndex, page)
    }
}