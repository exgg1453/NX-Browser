package com.nxteam.nxbrowser.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.nxteam.nxbrowser.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuSheet(
    incognito: Boolean,
    desktopMode: Boolean,
    canShare: Boolean,
    onDismiss: () -> Unit,
    onNewTab: () -> Unit,
    onNewIncognitoTab: () -> Unit,
    onAddBookmark: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenExtensions: () -> Unit,
    onShare: () -> Unit,
    onToggleDesktop: (Boolean) -> Unit,
    onOpenSettings: () -> Unit,
    onClearData: () -> Unit,
    onExit: () -> Unit
) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = state,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(bottom = 28.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MenuAction(Icons.Filled.Add, stringResource(R.string.new_tab), onNewTab)
                MenuAction(Icons.Filled.VisibilityOff, stringResource(R.string.incognito_tab), onNewIncognitoTab)
                MenuAction(Icons.Filled.StarBorder, stringResource(R.string.add_bookmark), onAddBookmark)
                if (canShare) {
                    MenuAction(Icons.Filled.Share, stringResource(R.string.share), onShare)
                }
            }

            Spacer(Modifier.height(8.dp))

            MenuRow(Icons.Filled.Extension, stringResource(R.string.extensions), onOpenExtensions)
            MenuRow(Icons.Filled.Bookmarks, stringResource(R.string.bookmarks), onOpenBookmarks)
            if (!incognito) {
                MenuRow(Icons.Filled.History, stringResource(R.string.history), onOpenHistory)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleDesktop(!desktopMode) }
                    .padding(horizontal = 22.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.DesktopWindows,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(18.dp))
                Text(
                    text = stringResource(R.string.desktop_site),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Switch(checked = desktopMode, onCheckedChange = onToggleDesktop)
            }

            MenuRow(Icons.Filled.CleaningServices, stringResource(R.string.clear_data), onClearData)
            MenuRow(Icons.Filled.Settings, stringResource(R.string.settings), onOpenSettings)
            MenuRow(Icons.Filled.PowerSettingsNew, stringResource(R.string.exit), onExit)
        }
    }
}

@Composable
private fun MenuAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MenuRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 22.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(18.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
