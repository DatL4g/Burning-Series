package dev.datlag.burningseries.network.bs

actual object Jsoup {
    actual fun connect(url: String): Connection {
        return Connection()
    }
}

actual class Connection {
    actual fun followRedirects(follow: Boolean): Connection {
        return this
    }

    actual fun get(): Document? {
        return null
    }

}

actual class Document : Element {
    override fun select(url: String): List<Element> {
        return emptyList()
    }

    override fun selectFirst(url: String): Element? {
        return null
    }

    override fun attr(key: String): String {
        return String()
    }

    override fun hasAttr(key: String): Boolean {
        return false
    }

    override fun text(): String {
        return String()
    }

    override fun wholeText(): String {
        return String()
    }

    override fun location(): String {
        return String()
    }

    override fun classNames(): List<String> {
        return emptyList()
    }

    override fun className(): String {
        return String()
    }

}

actual interface Element {
    actual fun select(url: String): List<Element>
    actual fun selectFirst(url: String): Element?
    actual fun attr(key: String): String
    actual fun hasAttr(key: String): Boolean
    actual fun text(): String
    actual fun wholeText(): String
    actual fun location(): String
    actual fun classNames(): List<String>
    actual fun className(): String

}