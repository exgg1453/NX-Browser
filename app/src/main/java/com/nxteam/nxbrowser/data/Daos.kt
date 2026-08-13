package com.nxteam.nxbrowser.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: HistoryEntry)

    @Query("SELECT * FROM history ORDER BY visitedAt DESC LIMIT :limit")
    fun recent(limit: Int): Flow<List<HistoryEntry>>

    @Query("SELECT * FROM history WHERE title LIKE '%' || :query || '%' OR url LIKE '%' || :query || '%' ORDER BY visitedAt DESC LIMIT :limit")
    suspend fun search(query: String, limit: Int): List<HistoryEntry>

    @Query("SELECT url, title, host, COUNT(*) AS visits FROM history GROUP BY host ORDER BY visits DESC, visitedAt DESC LIMIT :limit")
    fun topSites(limit: Int): Flow<List<TopSite>>

    @Query("DELETE FROM history")
    suspend fun clearAll()

    @Query("DELETE FROM history WHERE visitedAt >= :since")
    suspend fun clearSince(since: Long)

    @Query("DELETE FROM history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM history WHERE host = :host")
    suspend fun deleteByHost(host: String)
}

@Dao
interface BookmarkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: BookmarkEntry)

    @Delete
    suspend fun delete(entry: BookmarkEntry)

    @Query("DELETE FROM bookmarks WHERE url = :url")
    suspend fun deleteByUrl(url: String)

    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun all(): Flow<List<BookmarkEntry>>

    @Query("SELECT * FROM bookmarks WHERE pinned = 1 ORDER BY createdAt ASC LIMIT :limit")
    fun pinned(limit: Int): Flow<List<BookmarkEntry>>

    @Query("SELECT COUNT(*) FROM bookmarks WHERE url = :url")
    suspend fun countByUrl(url: String): Int

    @Query("UPDATE bookmarks SET pinned = :pinned WHERE url = :url")
    suspend fun setPinned(url: String, pinned: Boolean)

    @Query("DELETE FROM bookmarks")
    suspend fun clearAll()
}
