package com.nxteam.nxbrowser.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nxteam.nxbrowser.R
import com.nxteam.nxbrowser.data.Prefs
import com.nxteam.nxbrowser.data.SearchEngines
import com.nxteam.nxbrowser.data.SettingsStore

@Composable
fun SettingsScreen(
    prefs: Prefs,
    settings: SettingsStore,
    onOpenClearData: () -> Unit,
    onBack: () -> Unit
) {
    var showEnginePicker by remember { mutableStateOf(false) }
    var showThemePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        ScreenTopBar(title = stringResource(R.string.settings), onBack = onBack)

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            SettingsSection(stringResource(R.string.section_search))
            SettingsRow(
                title = stringResource(R.string.search_engine),
                subtitle = SearchEngines.byId(prefs.searchEngine).label,
                onClick = { showEnginePicker = true }
            )

            SettingsSection(stringResource(R.string.section_appearance))
            SettingsRow(
                title = stringResource(R.string.theme),
                subtitle = when (prefs.themeMode) {
                    "dark" -> stringResource(R.string.theme_dark)
                    "light" -> stringResource(R.string.theme_light)
                    else -> stringResource(R.string.theme_system)
                },
                onClick = { showThemePicker = true }
            )
            SettingsToggle(
                title = stringResource(R.string.desktop_default),
                subtitle = stringResource(R.string.desktop_default_summary),
                checked = prefs.desktopModeDefault,
                onChange = { settings.setDesktopModeDefault(it) }
            )
            SettingsToggle(
                title = stringResource(R.string.dark_web_content),
                subtitle = stringResource(R.string.dark_web_content_summary),
                checked = prefs.darkWebContent,
                onChange = { settings.setDarkWebContent(it) }
            )

            SettingsSection(stringResource(R.string.section_site_behaviour))
            SettingsToggle(
                title = "JavaScript",
                subtitle = stringResource(R.string.javascript_summary),
                checked = prefs.javaScriptEnabled,
                onChange = { settings.setJavaScriptEnabled(it) }
            )
            SettingsToggle(
                title = stringResource(R.string.load_images),
                subtitle = stringResource(R.string.load_images_summary),
                checked = prefs.loadImages,
                onChange = { settings.setLoadImages(it) }
            )
            SettingsToggle(
                title = stringResource(R.string.block_popups),
                subtitle = stringResource(R.string.block_popups_summary),
                checked = prefs.blockPopups,
                onChange = { settings.setBlockPopups(it) }
            )

            SettingsSection(stringResource(R.string.section_privacy))
            SettingsToggle(
                title = stringResource(R.string.do_not_track),
                subtitle = stringResource(R.string.do_not_track_summary),
                checked = prefs.doNotTrack,
                onChange = { settings.setDoNotTrack(it) }
            )
            SettingsToggle(
                title = stringResource(R.string.clear_on_exit),
                subtitle = stringResource(R.string.clear_on_exit_summary),
                checked = prefs.clearOnExit,
                onChange = { settings.setClearOnExit(it) }
            )
            SettingsRow(
                title = stringResource(R.string.clear_browsing_data),
                subtitle = stringResource(R.string.clear_browsing_data_summary),
                onClick = onOpenClearData
            )

            SettingsSection(stringResource(R.string.section_tabs))
            SettingsToggle(
                title = stringResource(R.string.restore_tabs),
                subtitle = stringResource(R.string.restore_tabs_summary),
                checked = prefs.restoreTabs,
                onChange = { settings.setRestoreTabs(it) }
            )

            SettingsSection(stringResource(R.string.section_about))
            SettingsRow(
                title = "NX Browser",
                subtitle = stringResource(
                    R.string.about_summary,
                    stringResource(R.string.version_name)
                ),
                onClick = {}
            )
            Spacer(Modifier.height(28.dp))
        }
    }

    if (showEnginePicker) {
        AlertDialog(
            onDismissRequest = { showEnginePicker = false },
            title = { Text(stringResource(R.string.search_engine)) },
            text = {
                Column {
                    SearchEngines.all.forEach { engine ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settings.setSearchEngine(engine.id)
                                    showEnginePicker = false
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = prefs.searchEngine == engine.id,
                                onClick = {
                                    settings.setSearchEngine(engine.id)
                                    showEnginePicker = false
                                }
                            )
                            Text(engine.label)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showEnginePicker = false }) { Text(stringResource(R.string.close)) }
            }
        )
    }

    if (showThemePicker) {
        val options = listOf("system" to stringResource(R.string.theme_system), "light" to stringResource(R.string.theme_light), "dark" to stringResource(R.string.theme_dark))
        AlertDialog(
            onDismissRequest = { showThemePicker = false },
            title = { Text(stringResource(R.string.theme)) },
            text = {
                Column {
                    options.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settings.setThemeMode(option.first)
                                    showThemePicker = false
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = prefs.themeMode == option.first,
                                onClick = {
                                    settings.setThemeMode(option.first)
                                    showThemePicker = false
                                }
                            )
                            Text(option.second)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemePicker = false }) { Text(stringResource(R.string.close)) }
            }
        )
    }
}

@Composable
private fun SettingsSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 6.dp)
    )
}

@Composable
private fun SettingsRow(title: String, subtitle: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChange(!checked) }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
