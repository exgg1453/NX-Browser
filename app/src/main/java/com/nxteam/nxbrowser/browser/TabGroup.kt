package com.nxteam.nxbrowser.browser

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class TabGroup(
    val id: String,
    name: String,
    color: Long
) {

    var name by mutableStateOf(name)

    var color by mutableStateOf(color)

    var collapsed by mutableStateOf(false)

    companion object {
        val COLORS = listOf(
            0xFF5B8CFFL,
            0xFF7C5CFFL,
            0xFF00B3A4L,
            0xFFE0703AL,
            0xFFD9455FL,
            0xFF3AA655L,
            0xFF9B51E0L,
            0xFFF2C94CL
        )
    }
}
