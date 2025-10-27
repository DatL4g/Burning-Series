package dev.datlag.mimasu.extension.kache

interface CachePool {

    suspend fun clear(): Boolean
}