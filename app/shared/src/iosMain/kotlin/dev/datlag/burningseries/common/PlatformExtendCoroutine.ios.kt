package dev.datlag.burningseries.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.MainCoroutineDispatcher

actual val Dispatchers.DeviceMain: MainCoroutineDispatcher
    get() = Main

actual val Dispatchers.DeviceIO: CoroutineDispatcher
    get() = IO

actual val Dispatchers.DeviceDefault : CoroutineDispatcher
    get() = Default