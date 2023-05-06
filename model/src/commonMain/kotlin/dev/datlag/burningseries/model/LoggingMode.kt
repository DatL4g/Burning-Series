package dev.datlag.burningseries.model

sealed class LoggingMode(val value: Int) {

    private object _NONE : LoggingMode(0)
    private object _HOME : LoggingMode(1)
    private object _SEARCH : LoggingMode(2)
    private object _SERIES : LoggingMode(3)
    private object _STREAMS : LoggingMode(4)

    companion object {
        val NONE: Int
            get() = _NONE.value

        val HOME: Int
            get() = _HOME.value

        val SEARCH: Int
            get() = _SEARCH.value

        val SERIES: Int
            get() = _SERIES.value

        val STREAMS: Int
            get() = _STREAMS.value
    }

}