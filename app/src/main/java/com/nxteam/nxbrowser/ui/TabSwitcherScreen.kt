package com.nxteam.nxbrowser.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LayersClear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nxteam.nxbrowser.browser.BrowserTab
import com.nxteam.nxbrowser.browser.TabGroup
import com.nxteam.nxbrowser.browser.TabManager
import com.nxteam.nxbrowser.util.UrlUtils

@Composable
fun TabSwitcherScreen(
    tabManager: TabManager,
    incognito: Boolean,
    onSelectTab: (String) -> Unit,
    onNewTab: () -> Unit,
    onSwitchMode: (Boolean) -> Unit,
    onClose: () -> Unit
) {
    var selection by remember { mutableStateOf(setOf<String>()) }
    var showGroupDialog by remember { mutableStateOf(false) }
    var renameGroupId by remember { mutableStateOf<String?>(null) }

    val groups = tabManager.groupsFor(incognito)
    val ungrouped = tabManager.ungroupedTabs(incognito)
    val total = tabManager.visibleTabs(incognito).size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ModeToggle(
                incognito = incognito,
                normalCount = tabManager.normalTabs.size,
                incognitoCount = tabManager.incognitoTabs.size,
                onSwitchMode = onSwitchMode
            )
            Spacer(Modifier.weight(1f))
            if (selection.isNotEmpty()) {
                Text(
                    text = selection.size.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = { showGroupDialog = true }) {
                    Icon(
                        imageVector = Icons.Filled.CreateNewFolder,
                        contentDescription = "Grup oluştur",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = {
                    tabManager.assignToGroup(selection, null)
                    selection = emptySet()
                }) {
                    Icon(
                        imageVector = Icons.Filled.LayersClear,
                        contentDescription = "Gruptan çıkar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = {
                    tabManager.closeTabs(selection)
                    selection = emptySet()
                }) {
                    Icon(
                        imageVector = Icons.Filled.DeleteSweep,
                        contentDescription = "Seçilenleri kapat",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                IconButton(onClick = { selection = emptySet() }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Seçimi bırak",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                IconButton(onClick = { tabManager.closeAll(incognito) }) {
                    Icon(
                        imageVector = Icons.Filled.DeleteSweep,
                        contentDescription = "Tümünü kapat",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                IconButton(onClick = onNewTab) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Yeni sekme",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Kapat",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        if (total == 0) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (incognito) "Açık gizli sekme yok" else "Açık sekme yok",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Column
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 10.dp,
                end = 10.dp,
                bottom = 28.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            groups.forEach { group ->
                val groupTabs = tabManager.tabsInGroup(group.id, incognito)

                item(
                    key = "header_" + group.id,
                    span = { GridItemSpan(maxLineSpan) }
                ) {
                    GroupHeader(
                        group = group,
                        count = groupTabs.size,
                        onToggle = { group.collapsed = !group.collapsed },
                        onRename = { renameGroupId = group.id },
                        onCloseGroup = { tabManager.closeGroup(group.id) },
                        onUngroup = { tabManager.ungroup(group.id) }
                    )
                }

                if (!group.collapsed) {
                    items(
                        count = groupTabs.size,
                        key = { index -> "gt_" + groupTabs[index].id }
                    ) { index ->
                        val tab = groupTabs[index]
                        TabCard(
                            tab = tab,
                            accent = Color(group.color),
                            selected = selection.contains(tab.id),
                            selectionMode = selection.isNotEmpty(),
                            isCurrent = tabManager.currentTabId == tab.id,
                            onClick = {
                                if (selection.isNotEmpty()) {
                                    selection = toggle(selection, tab.id)
                                } else {
                                    onSelectTab(tab.id)
                                }
                            },
                            onLongClick = { selection = toggle(selection, tab.id) },
                            onClose = { tabManager.closeTab(tab.id) }
                        )
                    }
                }
            }

            if (ungrouped.isNotEmpty()) {
                if (groups.isNotEmpty()) {
                    item(
                        key = "header_ungrouped",
                        span = { GridItemSpan(maxLineSpan) }
                    ) {
                        SectionTitle(
                            text = "Gruplanmamış",
                            modifier = Modifier.padding(start = 6.dp, top = 8.dp)
                        )
                    }
                }

                items(
                    count = ungrouped.size,
                    key = { index -> "ut_" + ungrouped[index].id }
                ) { index ->
                    val tab = ungrouped[index]
                    TabCard(
                        tab = tab,
                        accent = MaterialTheme.colorScheme.primary,
                        selected = selection.contains(tab.id),
                        selectionMode = selection.isNotEmpty(),
                        isCurrent = tabManager.currentTabId == tab.id,
                        onClick = {
                            if (selection.isNotEmpty()) {
                                selection = toggle(selection, tab.id)
                            } else {
                                onSelectTab(tab.id)
                            }
                        },
                        onLongClick = { selection = toggle(selection, tab.id) },
                        onClose = { tabManager.closeTab(tab.id) }
                    )
                }
            }
        }
    }

    if (showGroupDialog) {
        GroupDialog(
            initialName = "Yeni grup",
            initialColor = TabGroup.COLORS.first(),
            title = "Grup oluştur",
            onDismiss = { showGroupDialog = false },
            onConfirm = { name, color ->
                val group = tabManager.createGroup(name, color)
                tabManager.assignToGroup(selection, group.id)
                selection = emptySet()
                showGroupDialog = false
            }
        )
    }

    val renaming = renameGroupId
    if (renaming != null) {
        val group = tabManager.groups.firstOrNull { it.id == renaming }
        GroupDialog(
            initialName = group?.name ?: "",
            initialColor = group?.color ?: TabGroup.COLORS.first(),
            title = "Grubu düzenle",
            onDismiss = { renameGroupId = null },
            onConfirm = { name, color ->
                tabManager.renameGroup(renaming, name)
                tabManager.recolorGroup(renaming, color)
                renameGroupId = null
            }
        )
    }
}

private fun toggle(selection: Set<String>, id: String): Set<String> =
    if (selection.contains(id)) selection - id else selection + id

@Composable
private fun ModeToggle(
    incognito: Boolean,
    normalCount: Int,
    incognitoCount: Int,
    onSwitchMode: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .height(38.dp)
            .clip(RoundedCornerShape(19.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ModeChip(
                label = "Sekmeler $normalCount",
                active = !incognito,
                onClick = { onSwitchMode(false) }
            )
            ModeChip(
                label = "Gizli $incognitoCount",
                active = incognito,
                onClick = { onSwitchMode(true) }
            )
        }
    }
}

@Composable
private fun ModeChip(label: String, active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(38.dp)
            .clip(RoundedCornerShape(19.dp))
            .background(if (active) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (active) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
private fun GroupHeader(
    group: TabGroup,
    count: Int,
    onToggle: () -> Unit,
    onRename: () -> Unit,
    onCloseGroup: () -> Unit,
    onUngroup: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, start = 6.dp, end = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(group.color))
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = group.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onRename, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = "Düzenle",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
        IconButton(onClick = onUngroup, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Filled.LayersClear,
                contentDescription = "Grubu dağıt",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
        IconButton(onClick = onCloseGroup, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Filled.DeleteSweep,
                contentDescription = "Grubu kapat",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
            )
        }
        IconButton(onClick = onToggle, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = if (group.collapsed) Icons.Filled.ExpandMore else Icons.Filled.ExpandLess,
                contentDescription = "Aç kapa",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TabCard(
    tab: BrowserTab,
    accent: Color,
    selected: Boolean,
    selectionMode: Boolean,
    isCurrent: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onClose: () -> Unit
) {
    val host = UrlUtils.host(tab.url)
    val borderColor = when {
        selected -> MaterialTheme.colorScheme.primary
        isCurrent -> accent
        else -> Color.Transparent
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = if (selected || isCurrent) 2.dp else 1.dp,
                color = if (borderColor == Color.Transparent) {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                } else {
                    borderColor
                },
                shape = RoundedCornerShape(18.dp)
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 4.dp, top = 8.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (tab.incognito) {
                    Icon(
                        imageVector = Icons.Filled.VisibilityOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    SiteAvatar(
                        host = host,
                        label = tab.displayTitle,
                        size = 18.dp,
                        corner = 6.dp
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = tab.displayTitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (!selectionMode) {
                    IconButton(onClick = onClose, modifier = Modifier.size(26.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Sekmeyi kapat",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    if (tab.hasPage) {
                        SiteAvatar(host = host, label = tab.displayTitle, size = 44.dp, corner = 14.dp)
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (UrlUtils.isSecure(tab.url)) {
                                    Icons.Filled.Lock
                                } else {
                                    Icons.Filled.Public
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = host.ifBlank { tab.url },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (tab.showHome) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Ana sayfada duruyor",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1
                            )
                        }
                        if (tab.desktopMode) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Masaüstü",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                maxLines = 1
                            )
                        }
                    } else {
                        Text(
                            text = if (tab.incognito) "Gizli sekme" else "Yeni sekme",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupDialog(
    initialName: String,
    initialColor: Long,
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (String, Long) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var color by remember { mutableStateOf(initialColor) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    label = { Text("Grup adı") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TabGroup.COLORS.forEach { option ->
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(Color(option))
                                .border(
                                    width = if (option == color) 3.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = RoundedCornerShape(13.dp)
                                )
                                .clickable { color = option }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name.ifBlank { "Grup" }, color) }) {
                Text("Tamam")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}
