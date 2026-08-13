package com.nxteam.nxbrowser.util

object UserAgents {

    const val DESKTOP =
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"

    fun mobile(defaultAgent: String): String = defaultAgent
}
