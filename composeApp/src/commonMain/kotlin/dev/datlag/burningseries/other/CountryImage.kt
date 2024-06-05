package dev.datlag.burningseries.other

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.datlag.tooling.compose.ifTrue
import dev.icerock.moko.resources.ImageResource
import kotlinx.collections.immutable.ImmutableCollection
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import dev.icerock.moko.resources.compose.painterResource
import dev.datlag.burningseries.MokoRes

data object CountryImage {

    fun getByCode(code: String): ImageResource {
        val bestCode = code.split("[-_]".toRegex()).firstOrNull() ?: code

        return when {
            bestCode.equals(
                "EN",
                ignoreCase = true
            ) || bestCode.equals(
                "US",
                ignoreCase = true
            ) -> MokoRes.images.US

            bestCode.equals("DE", ignoreCase = true) -> MokoRes.images.DE
            bestCode.equals("JP", ignoreCase = true) -> MokoRes.images.JP
            else -> MokoRes.images.COUNTRY_UNKNOWN
        }
    }

    fun getByFlag(code: String?): ImmutableSet<ImageResource> {
        return when {
            code == null -> persistentSetOf(
                MokoRes.images.COUNTRY_UNKNOWN
            )
            code.equals("DES", ignoreCase = true) -> persistentSetOf(
                MokoRes.images.US,
                MokoRes.images.DE
            )
            code.equals("JPS", ignoreCase = true) -> persistentSetOf(
                MokoRes.images.JP,
                MokoRes.images.US
            )
            else -> persistentSetOf(getByCode(code))
        }
    }

    @Composable
    fun showFlags(
        code: String?,
        description: String? = null,
        iconSize: Dp? = null,
        showBorder: Boolean = false,
        shape: Shape = MaterialTheme.shapes.extraSmall
    ) {
        code?.let {
            val flags = getByFlag(code)

            if (flags.isNotEmpty()) {
                showFlags(
                    collection = flags,
                    description = description,
                    iconSize = iconSize,
                    showBorder = showBorder,
                    shape = shape
                )
            }
        }
    }

    @Composable
    fun showFlags(
        collection: ImmutableCollection<ImageResource>,
        description: String? = null,
        iconSize: Dp? = null,
        showBorder: Boolean = false,
        shape: Shape = MaterialTheme.shapes.extraSmall
    ) {
        var iconWidth by remember { mutableFloatStateOf(iconSize?.value ?: 24.dp.value) }
        var iconHeight by remember { mutableFloatStateOf(iconSize?.value ?: 24.dp.value) }

        Box(
            modifier = Modifier.ifTrue(iconSize == null) {
                this.onSizeChanged {
                    val thirdWidth = it.width.toFloat() - (it.width.toFloat() / 2F)
                    val thirdHeight = it.height.toFloat() - (it.height.toFloat() / 2F)
                    iconWidth = thirdWidth
                    iconHeight = thirdHeight
                }
            }.ifTrue(collection.size >= 2) {
                size(width = (iconWidth * 1.5).dp, height = (iconHeight * 1.5).dp)
            },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(collection.last()),
                contentDescription = description,
                modifier = Modifier
                    .size(width = iconWidth.dp, height = iconHeight.dp)
                    .clip(shape)
                    .ifTrue(showBorder) {
                        border(1.dp, LocalContentColor.current, shape)
                    }
                    .ifTrue(collection.size >= 2) { this.align(Alignment.TopStart).alpha(0.75F) }
            )
            if (collection.size >= 2) {
                Image(
                    painter = painterResource(collection.first()),
                    contentDescription = description,
                    modifier = Modifier
                        .size(width = iconWidth.dp, height = iconHeight.dp)
                        .clip(shape)
                        .ifTrue(showBorder) {
                            border(1.dp, LocalContentColor.current, shape)
                        }
                        .align(Alignment.BottomEnd)
                )
            }
        }
    }
}