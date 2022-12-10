package dev.datlag.burningseries.ui.screen.home

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.stackAnimation

@Composable
actual fun HomeViewPager(component: HomeComponent) {
    Children(
        stack = component.childStack.value,
        animation = stackAnimation(fade())
    ) { child ->
        child.instance.render()
    }
}