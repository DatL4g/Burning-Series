package dev.datlag.burningseries.network.converter

import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.converter.SuspendResponseConverter
import de.jensklingenberg.ktorfit.converter.request.ResponseConverter
import de.jensklingenberg.ktorfit.internal.TypeData
import io.ktor.client.statement.*
import io.ktor.util.reflect.*
import com.hadiyarajesh.flower_core.ApiResponse
import kotlinx.coroutines.flow.flow
import kotlin.reflect.KClass
import dev.datlag.burningseries.network.common.toCommonResponse

/**
 * This is a copy of flower-ktorfit only for using a newer Ktorfit version.
 */
class FlowerResponseConverter : ResponseConverter, SuspendResponseConverter {
    override fun supportedType(typeData: TypeData, isSuspend: Boolean): Boolean {
        return if (isSuspend) {
            typeData.qualifiedName == "com.hadiyarajesh.flower_core.ApiResponse"
        } else {
            typeData.qualifiedName == "kotlinx.coroutines.flow.Flow"
        }
    }

    override fun <RequestType> wrapResponse(
        typeData: TypeData,
        requestFunction: suspend () -> Pair<TypeInfo, HttpResponse?>,
        ktorfit: Ktorfit
    ): Any {
        return flow<ApiResponse<Any>> {
            try {
                val (info, response) = requestFunction()
                if (response == null) {
                    throw IllegalArgumentException("No HttResponse object received")
                }

                val kotlinType = info.kotlinType
                    ?: throw IllegalArgumentException("Type must match Flow<ApiResponse<YourModel>>")
                val modelKTypeProjection =
                    if (kotlinType.arguments.isNotEmpty()) kotlinType.arguments[0] else throw IllegalArgumentException(
                        "Type must match Flow<ApiResponse<YourModel>>"
                    )
                val modelKType = modelKTypeProjection.type
                    ?: throw IllegalArgumentException("Could not get a KType of your model class or return type")
                val modelClass = (modelKType.classifier as? KClass<*>?)
                    ?: throw IllegalArgumentException("Could not parse your model class or return type to a KClass")

                emit(
                    ApiResponse.create(
                        response.toCommonResponse(
                            TypeInfo(
                                modelClass,
                                modelKType.platformType,
                                modelKType
                            )
                        )
                    )
                )
            } catch (e: Throwable) {
                emit(ApiResponse.create(e))
            }
        }
    }

    override suspend fun <RequestType> wrapSuspendResponse(
        typeData: TypeData,
        requestFunction: suspend () -> Pair<TypeInfo, HttpResponse>,
        ktorfit: Ktorfit
    ): Any {
        return try {
            val (info, response) = requestFunction()

            ApiResponse.create(response.toCommonResponse(info))
        } catch (e: Throwable) {
            ApiResponse.create<Any>(e)
        }
    }
}