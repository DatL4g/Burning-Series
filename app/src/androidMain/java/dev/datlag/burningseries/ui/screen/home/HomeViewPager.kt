package dev.datlag.burningseries.ui.screen.home

import androidx.compose.runtime.*
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
actual fun HomeViewPager(component: HomeComponent) {
    val scope = rememberCoroutineScope()
    val pagerItems by component.pagerList
    val initialPage = remember { component.childIndex.value }
    val state = rememberPagerState(initialPage)

    LaunchedEffect(state) {
        snapshotFlow { state.currentPage }.collect {
            component.childIndex.value = it
        }
    }

    component.childIndex.subscribe {
        scope.launch {
            state.scrollToPage(it)
        }
    }

    HorizontalPager(count = pagerItems.size, state = state) {
        pagerItems[it].render()
    }
}