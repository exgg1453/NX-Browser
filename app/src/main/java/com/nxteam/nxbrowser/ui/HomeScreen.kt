package com.nxteam.nxbrowser.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nxteam.nxbrowser.data.BookmarkEntry
import com.nxteam.nxbrowser.data.HistoryEntry
import com.nxteam.nxbrowser.data.TopSite
import com.nxteam.nxbrowser.ui.theme.NXBlue
import com.nxteam.nxbrowser.ui.theme.NXTeal
import com.nxteam.nxbrowser.ui.theme.NXViolet
import com.nxteam.nxbrowser.util.UrlUtils

data class HomeTile(
    val title: String,
    val url: String,
    val host: String,
    val pinned: Boolean
)

@Composable
fun HomeScreen(
    incognito: Boolean,
    searchEngineLabel: String,
    topSites: List<TopSite>,
    pinned: List<BookmarkEntry>,
    recent: List<HistoryEntry>,
    onSearchBarClick: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onRemoveTile: (String) -> Unit,
    onOpenIncognito: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenExtensions: () -> Unit,
    onQuickClean: () -> Unit
) {
    val tiles = buildTiles(pinned, topSites)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 32.dp)
    ) {
        item {
            HomeHeader(incognito = incognito)
        }

        item {
            Spacer(Modifier.height(18.dp))
            HomeSearchBar(
                incognito = incognito,
                searchEngineLabel = searchEngineLabel,
                onClick = onSearchBarClick
            )
        }

        item {
            Spacer(Modifier.height(14.dp))
            QuickActionRow(
                incognito = incognito,
                onOpenIncognito = onOpenIncognito,
                onOpenHistory = onOpenHistory,
                onOpenBookmarks = onOpenBookmarks,
                onOpenExtensions = onOpenExtensions,
                onQuickClean = onQuickClean
            )
        }

        if (tiles.isNotEmpty()) {
            item {
                Spacer(Modifier.height(26.dp))
                SectionTitle(
                    text = "Kısayollar",
                    modifier = Modifier.padding(horizontal = 22.dp)
                )
                Spacer(Modifier.height(10.dp))
                ShortcutGrid(
                    tiles = tiles,
                    onOpenUrl = onOpenUrl,
                    onRemoveTile = onRemoveTile
                )
            }
        }

        if (recent.isNotEmpty() && !incognito) {
            item {
                Spacer(Modifier.height(26.dp))
                SectionTitle(
                    text = "Kaldığın yerden devam et",
                    modifier = Modifier.padding(horizontal = 22.dp)
                )
                Spacer(Modifier.height(8.dp))
            }
            items(recent, key = { it.id }) { entry ->
                RecentRow(entry = entry, onOpenUrl = onOpenUrl)
            }
        }

        if (incognito) {
            item {
                Spacer(Modifier.height(26.dp))
                IncognitoNotice()
            }
        }
    }
}

@Composable
private fun HomeHeader(incognito: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 46.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.linearGradient(
                        colors = if (incognito) {
                            listOf(Color(0xFF6E5AA8), Color(0xFF3D3266))
                        } else {
                            listOf(NXBlue, NXViolet)
                        }
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "NX",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black
            )
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = if (incognito) "Gizli Mod" else "NX Browser",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (incognito) {
                "Geçmiş, çerez ve site verisi kaydedilmiyor"
            } else {
                "Hızlı, temiz ve tamamen senin kontrolünde"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 40.dp)
        )
    }
}

@Composable
private fun HomeSearchBar(
    incognito: Boolean,
    searchEngineLabel: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(14.dp))
            Text(
                text = if (incognito) "Gizli olarak ara" else "$searchEngineLabel ile ara veya adres yaz",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun QuickActionRow(
    incognito: Boolean,
    onOpenIncognito: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenExtensions: () -> Unit,
    onQuickClean: () -> Unit
) {
    LazyRow(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            QuickChip(
                icon = Icons.Filled.VisibilityOff,
                label = if (incognito) "Normal sekme" else "Gizli sekme",
                accent = NXViolet,
                onClick = onOpenIncognito
            )
        }
        item {
            QuickChip(
                icon = Icons.Filled.Extension,
                label = "Eklentiler",
                accent = NXTeal,
                onClick = onOpenExtensions
            )
        }
        item {
            QuickChip(
                icon = Icons.Filled.Bookmarks,
                label = "Yer imleri",
                accent = NXBlue,
                onClick = onOpenBookmarks
            )
        }
        item {
            QuickChip(
                icon = Icons.Filled.History,
                label = "Geçmiş",
                accent = Color(0xFFE0703A),
                onClick = onOpenHistory
            )
        }
        item {
            QuickChip(
                icon = Icons.Filled.CleaningServices,
                label = "Hızlı temizlik",
                accent = Color(0xFFD9455F),
                onClick = onQuickClean
            )
        }
    }
}

@Composable
private fun QuickChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    accent: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ShortcutGrid(
    tiles: List<HomeTile>,
    onOpenUrl: (String) -> Unit,
    onRemoveTile: (String) -> Unit
) {
    val rows = tiles.chunked(4)
    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
        rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { tile ->
                    ShortcutTile(
                        tile = tile,
                        modifier = Modifier.weight(1f),
                        onOpenUrl = onOpenUrl,
                        onRemoveTile = onRemoveTile
                    )
                }
                repeat(4 - row.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(14.dp))
        }
    }
}

@Composable
private fun ShortcutTile(
    tile: HomeTile,
    modifier: Modifier,
    onOpenUrl: (String) -> Unit,
    onRemoveTile: (String) -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onOpenUrl(tile.url) }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SiteAvatar(host = tile.host, label = tile.title, size = 48.dp, corner = 16.dp)
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (tile.host.isNotBlank()) tile.host else tile.title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
private fun RecentRow(entry: HistoryEntry, onOpenUrl: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenUrl(entry.url) }
            .padding(horizontal = 22.dp, vertical = 10.dp),
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
                text = entry.host.ifBlank { entry.url },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun IncognitoNotice() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(18.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Gizli modda neler olmuyor",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Gezinme geçmişi kaydedilmez, çerezler ve site verileri ayrı bir profilde tutulur ve tüm gizli sekmeleri kapattığında silinir. Form verisi ve önbellek de saklanmaz.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun buildTiles(pinned: List<BookmarkEntry>, topSites: List<TopSite>): List<HomeTile> {
    val result = mutableListOf<HomeTile>()
    pinned.forEach {
        result.add(HomeTile(it.title, it.url, it.host, true))
    }
    topSites.forEach { site ->
        if (result.none { it.host == site.host }) {
            result.add(HomeTile(site.title, site.url, site.host, false))
        }
    }
    return result.take(8)
}
