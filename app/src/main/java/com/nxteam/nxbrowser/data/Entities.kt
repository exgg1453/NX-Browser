package com.nxteam.nxbrowser.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "history",
    indices = [Index("visitedAt"), Index("host")]
)
data class HistoryEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val title: String,
    val host: String,
    val visitedAt: Long
)

@Entity(
    tableName = "bookmarks",
    indices = [Index(value = ["url"], unique = true)]
)
data class BookmarkEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val title: String,
    val host: String,
    val pinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class TopSite(
    val url: String,
    val title: String,
    val host: String,
    val visits: Int
)
