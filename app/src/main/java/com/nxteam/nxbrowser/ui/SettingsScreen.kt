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
import androidx.compose.ui.unit.dp
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
        ScreenTopBar(title = "Ayarlar", onBack = onBack)

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            SettingsSection("Arama")
            SettingsRow(
                title = "Arama motoru",
                subtitle = SearchEngines.byId(prefs.searchEngine).label,
                onClick = { showEnginePicker = true }
            )

            SettingsSection("Görünüm")
            SettingsRow(
                title = "Tema",
                subtitle = when (prefs.themeMode) {
                    "dark" -> "Koyu"
                    "light" -> "Açık"
                    else -> "Sistem"
                },
                onClick = { showThemePicker = true }
            )
            SettingsToggle(
                title = "Varsayılan masaüstü modu",
                subtitle = "Siteleri masaüstü sürümüyle aç",
                checked = prefs.desktopModeDefault,
                onChange = { settings.setDesktopModeDefault(it) }
            )

            SettingsSection("Site davranışı")
            SettingsToggle(
                title = "JavaScript",
                subtitle = "Kapatmak bazı siteleri bozar",
                checked = prefs.javaScriptEnabled,
                onChange = { settings.setJavaScriptEnabled(it) }
            )
            SettingsToggle(
                title = "Görselleri yükle",
                subtitle = "Kapalıyken veri tasarrufu sağlar",
                checked = prefs.loadImages,
                onChange = { settings.setLoadImages(it) }
            )
            SettingsToggle(
                title = "Açılır pencereleri engelle",
                subtitle = "İstenmeyen yeni sekmeleri durdurur",
                checked = prefs.blockPopups,
                onChange = { settings.setBlockPopups(it) }
            )

            SettingsSection("Gizlilik")
            SettingsToggle(
                title = "İzlemeyi reddet",
                subtitle = "Sitelere DNT sinyali gönder",
                checked = prefs.doNotTrack,
                onChange = { settings.setDoNotTrack(it) }
            )
            SettingsToggle(
                title = "Çıkışta verileri sil",
                subtitle = "Uygulama kapanınca geçmiş, çerez ve önbelleği temizle",
                checked = prefs.clearOnExit,
                onChange = { settings.setClearOnExit(it) }
            )
            SettingsRow(
                title = "Gezinti verilerini temizle",
                subtitle = "Geçmiş, çerezler, önbellek, site verileri",
                onClick = onOpenClearData
            )

            SettingsSection("Sekmeler")
            SettingsToggle(
                title = "Sekmeleri geri yükle",
                subtitle = "Uygulama açıldığında açık sekmelere devam et",
                checked = prefs.restoreTabs,
                onChange = { settings.setRestoreTabs(it) }
            )

            SettingsSection("Hakkında")
            SettingsRow(
                title = "NX Browser",
                subtitle = "Sürüm 1.0.0 · NX Team",
                onClick = {}
            )
            Spacer(Modifier.height(28.dp))
        }
    }

    if (showEnginePicker) {
        AlertDialog(
            onDismissRequest = { showEnginePicker = false },
            title = { Text("Arama motoru") },
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
                TextButton(onClick = { showEnginePicker = false }) { Text("Kapat") }
            }
        )
    }

    if (showThemePicker) {
        val options = listOf("system" to "Sistem", "light" to "Açık", "dark" to "Koyu")
        AlertDialog(
            onDismissRequest = { showThemePicker = false },
            title = { Text("Tema") },
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
                TextButton(onClick = { showThemePicker = false }) { Text("Kapat") }
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
