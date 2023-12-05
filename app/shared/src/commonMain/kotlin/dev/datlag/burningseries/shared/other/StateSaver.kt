package dev.datlag.burningseries.shared.other

data object StateSaver {
    var homeGridIndex: Int = 0
    var homeGridOffset: Int = 0

    var seriesListIndex: Int = 0
    var seriesListOffset: Int = 0

    var sekretLibraryLoaded: Boolean = false
}