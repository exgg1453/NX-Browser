package com.nxteam.nxbrowser.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nxteam.nxbrowser.data.HistoryEntry
import com.nxteam.nxbrowser.privacy.ClearOptions
import com.nxteam.nxbrowser.util.UrlUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    entries: List<HistoryEntry>,
    onOpenUrl: (String) -> Unit,
    onDeleteEntry: (Long) -> Unit,
    onClear: (ClearOptions) -> Unit,
    onBack: () -> Unit
) {
    var search by remember { mutableStateOf("") }
    var showClearDialog by remember { mutableStateOf(false) }

    val filtered = if (search.isBlank()) {
        entries
    } else {
        entries.filter {
            it.title.contains(search, true) || it.url.contains(search, true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        ScreenTopBar(
            title = "Geçmiş",
            onBack = onBack,
            action = {
                IconButton(onClick = { showClearDialog = true }) {
                    Icon(
                        imageVector = Icons.Filled.DeleteSweep,
                        contentDescription = "Temizle",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        )

        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            singleLine = true,
            placeholder = { Text("Geçmişte ara") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        )

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Geçmiş boş",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filtered, key = { it.id }) { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenUrl(entry.url) }
                            .padding(horizontal = 18.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SiteAvatar(host = entry.host, label = entry.title, size = 36.dp, corner = 12.dp)
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = entry.title.ifBlank { UrlUtils.prettyUrl(entry.url) },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = entry.host + "  ·  " + formatTime(entry.visitedAt),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = { onDeleteEntry(entry.id) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Sil",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        ClearDataDialog(
            onDismiss = { showClearDialog = false },
            onConfirm = {
                onClear(it)
                showClearDialog = false
            }
        )
    }
}

@Composable
fun ClearDataDialog(
    onDismiss: () -> Unit,
    onConfirm: (ClearOptions) -> Unit
) {
    var history by remember { mutableStateOf(true) }
    var cookies by remember { mutableStateOf(true) }
    var cache by remember { mutableStateOf(true) }
    var siteStorage by remember { mutableStateOf(true) }
    var formData by remember { mutableStateOf(true) }
    var bookmarks by remember { mutableStateOf(false) }
    var rangeIndex by remember { mutableStateOf(3) }

    val ranges = listOf(
        "Son 1 saat" to 3600_000L,
        "Son 24 saat" to 86_400_000L,
        "Son 7 gün" to 604_800_000L,
        "Tüm zamanlar" to -1L
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Verileri temizle") },
        text = {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ranges.forEachIndexed { index, pair ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { rangeIndex = index },
                            color = if (rangeIndex == index) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ) {
                            Text(
                                text = pair.first,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (rangeIndex == index) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                CheckRow("Gezinme geçmişi", history) { history = it }
                CheckRow("Çerezler ve oturumlar", cookies) { cookies = it }
                CheckRow("Önbellek", cache) { cache = it }
                CheckRow("Site verileri", siteStorage) { siteStorage = it }
                CheckRow("Form ve şifre verileri", formData) { formData = it }
                CheckRow("Yer imleri", bookmarks) { bookmarks = it }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val millis = ranges[rangeIndex].second
                onConfirm(
                    ClearOptions(
                        history = history,
                        cookies = cookies,
                        cache = cache,
                        siteStorage = siteStorage,
                        formData = formData,
                        bookmarks = bookmarks,
                        sinceMillis = if (millis < 0) null else System.currentTimeMillis() - millis
                    )
                )
            }) {
                Text("Temizle", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}

@Composable
private fun CheckRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onChange)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ScreenTopBar(
    title: String,
    onBack: () -> Unit,
    action: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Geri",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.weight(1f))
        action()
    }
}

private fun formatTime(millis: Long): String {
    val formatter = SimpleDateFormat("dd MMM HH:mm", Locale("tr"))
    return formatter.format(Date(millis))
}
