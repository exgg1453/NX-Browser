package com.nxteam.nxbrowser.data

import android.content.Context
import com.nxteam.nxbrowser.browser.TabManager
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class SessionStore(context: Context) {

    private val file = File(context.filesDir, "session.json")

    fun save(tabManager: TabManager) {
        try {
            val tabsArray = JSONArray()
            tabManager.tabs.filter { !it.incognito }.forEach { tab ->
                val obj = JSONObject()
                obj.put("id", tab.id)
                obj.put("url", tab.url)
                obj.put("title", tab.title)
                obj.put("groupId", tab.groupId ?: JSONObject.NULL)
                obj.put("desktopMode", tab.desktopMode)
                tabsArray.put(obj)
            }

            val groupsArray = JSONArray()
            tabManager.groups.forEach { group ->
                val obj = JSONObject()
                obj.put("id", group.id)
                obj.put("name", group.name)
                obj.put("color", group.color)
                groupsArray.put(obj)
            }

            val root = JSONObject()
            root.put("tabs", tabsArray)
            root.put("groups", groupsArray)
            val current = tabManager.currentTab
            root.put(
                "currentTabId",
                if (current != null && !current.incognito) current.id else JSONObject.NULL
            )

            file.writeText(root.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun restore(tabManager: TabManager): Boolean {
        if (!file.exists()) return false
        return try {
            val root = JSONObject(file.readText())

            val groupsArray = root.optJSONArray("groups") ?: JSONArray()
            for (index in 0 until groupsArray.length()) {
                val obj = groupsArray.getJSONObject(index)
                tabManager.restoreGroup(
                    obj.getString("id"),
                    obj.getString("name"),
                    obj.getLong("color")
                )
            }

            val tabsArray = root.optJSONArray("tabs") ?: JSONArray()
            for (index in 0 until tabsArray.length()) {
                val obj = tabsArray.getJSONObject(index)
                tabManager.restoreTab(
                    id = obj.getString("id"),
                    url = obj.optString("url", ""),
                    title = obj.optString("title", ""),
                    groupId = if (obj.isNull("groupId")) null else obj.getString("groupId"),
                    desktopMode = obj.optBoolean("desktopMode", false)
                )
            }

            if (tabManager.tabs.isEmpty()) return false

            val currentId = if (root.isNull("currentTabId")) null else root.getString("currentTabId")
            tabManager.restoreCurrentTab(currentId)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun clear() {
        try {
            if (file.exists()) file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
