package dev.datlag.burningseries.ui.navigation.screen.activate

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import dev.datlag.burningseries.composeapp.generated.resources.Res
import dev.datlag.burningseries.composeapp.generated.resources.load_to_activate_episode
import dev.datlag.burningseries.network.state.SaveState
import dev.datlag.tooling.decompose.lifecycle.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Toolbar(component: ActivateComponent) {
    TopAppBar(
        navigationIcon = {
            IconButton(
                onClick = {
                    component.back()
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBackIosNew,
                    contentDescription = null
                )
            }
        },
        title = {
            Text(
                text = stringResource(Res.string.load_to_activate_episode)
            )
        },
        actions = {
            val saveState by component.saveState.collectAsStateWithLifecycle()
            when (val current = saveState) {
                is SaveState.Success -> {
                    component.success(current.stream)
                }
                is SaveState.Error -> {
                    component.error(current.stream)
                }
                else -> { }
            }

            val isSaving = remember(saveState) {
                when (saveState) {
                    is SaveState.Saving -> true
                    else -> false
                }
            }

            AnimatedVisibility(
                visible = isSaving,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(
                    onClick = {}
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Save,
                        contentDescription = null
                    )
                }
            }
        }
    )
}