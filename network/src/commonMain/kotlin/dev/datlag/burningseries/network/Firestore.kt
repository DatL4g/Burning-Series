package dev.datlag.burningseries.network

import de.jensklingenberg.ktorfit.http.*
import dev.datlag.burningseries.model.FirestoreDocument
import dev.datlag.burningseries.model.FirestoreQuery
import io.ktor.client.statement.*

interface Firestore {

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @POST("databases/(default)/documents:runQuery")
    suspend fun query(@Body request: FirestoreQuery): List<FirestoreQuery.Response>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @PATCH("databases/(default)/documents/{collection}")
    suspend fun patch(@Header("Authorization") token: String, @Path("collection") collection: String, @Body request: FirestoreDocument): HttpResponse

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @POST("databases/(default)/documents/{collection}")
    suspend fun create(@Header("Authorization") token: String, @Path("collection") collection: String, @Body request: FirestoreDocument): HttpResponse
}