package com.nxteam.nxbrowser.data

data class SearchEngine(
    val id: String,
    val label: String,
    val queryUrl: String,
    val homeUrl: String
)

object SearchEngines {

    const val DEFAULT_ID = "google"

    val all: List<SearchEngine> = listOf(
        SearchEngine("google", "Google", "https://www.google.com/search?q=", "https://www.google.com"),
        SearchEngine("bing", "Bing", "https://www.bing.com/search?q=", "https://www.bing.com"),
        SearchEngine("duckduckgo", "DuckDuckGo", "https://duckduckgo.com/?q=", "https://duckduckgo.com"),
        SearchEngine("yandex", "Yandex", "https://yandex.com.tr/search/?text=", "https://yandex.com.tr"),
        SearchEngine("brave", "Brave Search", "https://search.brave.com/search?q=", "https://search.brave.com"),
        SearchEngine("startpage", "Startpage", "https://www.startpage.com/sp/search?query=", "https://www.startpage.com")
    )

    fun byId(id: String): SearchEngine = all.firstOrNull { it.id == id } ?: all.first()
}
