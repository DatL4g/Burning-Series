package dev.datlag.mimasu.extension.matcher.wanakana

sealed interface IMEMode {

    data object DISABLED : IMEMode
    data object ENABLED : IMEMode

}