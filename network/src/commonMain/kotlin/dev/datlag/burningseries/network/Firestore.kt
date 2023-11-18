package dev.datlag.burningseries.network

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import dev.datlag.burningseries.model.FirestoreQuery
import dev.datlag.burningseries.model.FirestoreQueryResponse
import io.ktor.client.statement.*
import kotlinx.serialization.json.JsonElement

interface Firestore {

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @POST("databases/(default)/documents:runQuery")
    suspend fun query(@Body request: FirestoreQuery): List<FirestoreQueryResponse>
}