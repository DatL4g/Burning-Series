@file:Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")

package browser.events

public inline fun <C, A> Rule(block: Rule<C, A>.() -> Unit): Rule<C, A> = (js("{}") as Rule<C,
        A>).apply(block)

public inline fun UrlFilter(block: UrlFilter.() -> Unit): UrlFilter = (js("{}") as
        UrlFilter).apply(block)