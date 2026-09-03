package com.sakatoon.traductor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sakatoon.traductor.viewmodel.SettingsViewModel

import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val downloadedModels by viewModel.downloadedModels.collectAsState()
    val availableLanguages = remember(viewModel.availableLanguages) {
        viewModel.availableLanguages.sortedBy { Locale.forLanguageTag(it).getDisplayName(Locale.forLanguageTag("es")) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Idiomas sin conexión", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToAbout) {
                        Icon(Icons.Default.Info, contentDescription = "Acerca de")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                val isDarkMode by viewModel.isDarkMode.collectAsState()
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp
                ) {
                    ListItem(
                        headlineContent = {
                            Text("Modo Oscuro", fontWeight = FontWeight.Bold)
                        },
                        supportingContent = {
                            Text("Cambiar tema claro / oscuro", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                        },
                        trailingContent = {
                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = { viewModel.setDarkMode(it) }
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
                    )
                }
            }
            item {
                Column(Modifier.padding(horizontal = 4.dp, vertical = 8.dp)) {
                    Text("Descarga solo los idiomas que necesitas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("Los modelos se guardan en tu dispositivo para traducir con mayor privacidad y sin internet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    AssistChip(onClick = {}, label = { Text("${downloadedModels.size} descargados") }, leadingIcon = { Icon(Icons.Default.DownloadDone, null) })
                }
            }
            items(availableLanguages, key = { it }) { langCode ->
                val isDownloaded = downloadedModels.contains(langCode)
                val languageName = Locale.forLanguageTag(langCode).getDisplayName(Locale.forLanguageTag("es")).replaceFirstChar { it.uppercase() }
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp
                ) {
                    ListItem(
                        headlineContent = { 
                            Text(
                                languageName, 
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp
                            ) 
                        },
                        supportingContent = {
                            Text(
                                "${langCode.uppercase()} • ${if (isDownloaded) "Listo para usar" else "Disponible para descargar"}",
                                fontSize = 12.sp,
                                color = if (isDownloaded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                            )
                        },
                        trailingContent = {
                            if (isDownloaded) {
                                IconButton(
                                    onClick = { viewModel.deleteModel(langCode) },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar $languageName", tint = MaterialTheme.colorScheme.error)
                                }
                            } else {
                                IconButton(
                                    onClick = { viewModel.downloadModel(langCode) },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = "Descargar $languageName", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
                    )
                }
            }
        }
    }
}
