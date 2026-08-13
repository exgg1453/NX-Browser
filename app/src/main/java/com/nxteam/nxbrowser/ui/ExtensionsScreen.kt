package com.nxteam.nxbrowser.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.nxteam.nxbrowser.R
import com.nxteam.nxbrowser.extensions.ExtensionManager
import kotlinx.coroutines.launch

@Composable
fun ExtensionsScreen(
    manager: ExtensionManager,
    onInstallFromFile: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showStoreDialog by remember { mutableStateOf(false) }
    var storeUrl by remember { mutableStateOf("") }
    var installing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        ScreenTopBar(
            title = stringResource(R.string.extensions),
            onBack = onBack,
            action = {
                IconButton(onClick = { showStoreDialog = true }) {
                    Icon(
                        imageVector = Icons.Filled.Link,
                        contentDescription = stringResource(R.string.install_from_store),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onInstallFromFile) {
                    Icon(
                        imageVector = Icons.Filled.FolderOpen,
                        contentDescription = stringResource(R.string.install_from_file),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )

        if (manager.extensions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 36.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Extension,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(Modifier.height(18.dp))
                    Text(
                        text = stringResource(R.string.no_extensions),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.no_extensions_summary),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(
                    count = manager.extensions.size,
                    key = { index -> manager.extensions[index].id }
                ) { index ->
                    val extension = manager.extensions[index]
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val label = extension.name.ifBlank {
                                stringResource(R.string.unnamed_extension)
                            }
                            SiteAvatar(host = label, label = label, size = 40.dp, corner = 13.dp)
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "v" + extension.version + "  ·  MV" +
                                        extension.manifest.manifestVersion + "  ·  " +
                                        stringResource(
                                            R.string.extension_scripts,
                                            extension.manifest.contentScripts.size
                                        ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (extension.description.isNotBlank()) {
                                    Text(
                                        text = extension.description,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Switch(
                                checked = extension.enabled,
                                onCheckedChange = { manager.setEnabled(extension.id, it) }
                            )
                            IconButton(
                                onClick = { manager.remove(extension.id) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = stringResource(R.string.remove),
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showStoreDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!installing) {
                    showStoreDialog = false
                }
            },
            title = { Text(stringResource(R.string.store_install_title)) },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.store_install_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = storeUrl,
                        onValueChange = { storeUrl = it },
                        singleLine = false,
                        maxLines = 3,
                        enabled = !installing,
                        label = { Text(stringResource(R.string.store_link_hint)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (installing) {
                        Spacer(Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = stringResource(R.string.downloading),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !installing && storeUrl.isNotBlank(),
                    onClick = {
                        installing = true
                        scope.launch {
                            val result = manager.installFromStoreUrl(storeUrl)
                            installing = false
                            Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                            if (result.success) {
                                storeUrl = ""
                                showStoreDialog = false
                            }
                        }
                    }
                ) {
                    Text(stringResource(R.string.install))
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !installing,
                    onClick = { showStoreDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
