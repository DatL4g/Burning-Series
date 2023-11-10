/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.datlag.burningseries.color.dynamiccolor

import dev.datlag.burningseries.color.utils.MathUtils


/**
 * A class containing the contrast curve for a dynamic color on its background.
 *
 * <p>The four values correspond to contrast requirements for contrast levels -1.0, 0.0, 0.5, and
 * 1.0, respectively.
 *
 * @param low Contrast requirement for contrast level -1.0
 * @param normal Contrast requirement for contrast level 0.0
 * @param medium Contrast requirement for contrast level 0.5
 * @param high Contrast requirement for contrast level 1.0
 */
data class ContrastCurve(
    val low: Double,
    val normal: Double,
    val medium: Double,
    val high: Double
) {

    fun getContrast(contrastLevel: Double): Double {
        return when {
            contrastLevel <= -1.0 -> low
            contrastLevel < 0.0 -> MathUtils.lerp(low, normal, (contrastLevel - -1) / 1)
            contrastLevel < 0.5 -> MathUtils.lerp(normal, medium, (contrastLevel - 0) / 0.5)
            contrastLevel < 1.0 -> MathUtils.lerp(medium, high, (contrastLevel - 0.5) / 0.5)
            else -> high
        }
    }
}
