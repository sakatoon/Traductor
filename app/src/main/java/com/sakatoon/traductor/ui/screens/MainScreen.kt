package com.sakatoon.traductor.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.mlkit.nl.translate.TranslateLanguage
import com.sakatoon.traductor.data.speech.SpeechState
import com.sakatoon.traductor.data.translation.TranslationState
import com.sakatoon.traductor.viewmodel.TranslationViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: TranslationViewModel, onNavigateToSettings: () -> Unit) {
    val sourceText by viewModel.sourceText.collectAsState()
    val sourceLang by viewModel.sourceLang.collectAsState()
    val targetLang by viewModel.targetLang.collectAsState()
    val translationState by viewModel.translationState.collectAsState()
    val speechState by viewModel.speechState.collectAsState()
    val showWelcome by viewModel.showFirstLaunchDialog.collectAsState()
    val context = LocalContext.current
    var picker by remember { mutableStateOf<LanguagePicker?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) viewModel.startListening() }

    if (showWelcome) WelcomeDialog(viewModel, onNavigateToSettings)
    picker?.let { selection ->
        LanguagePickerDialog(
            title = if (selection == LanguagePicker.Source) "Idioma de origen" else "Idioma de destino",
            selectedCode = if (selection == LanguagePicker.Source) sourceLang else targetLang,
            onSelect = {
                if (selection == LanguagePicker.Source) viewModel.setSourceLang(it) else viewModel.setTargetLang(it)
                picker = null
            },
            onDismiss = { picker = null }
        )
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Column { Text("Traductor", fontWeight = FontWeight.Bold); Text("Rápido, privado y sin conexión", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } },
            actions = {
                IconButton(onClick = onNavigateToSettings) { Icon(Icons.Default.Settings, "Abrir configuración") }
            }
        )
    }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.primary) {
                    Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        LanguageButton(sourceLang, Modifier.weight(1f)) { picker = LanguagePicker.Source }
                        FilledIconButton(
                            onClick = viewModel::swapLanguages,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f),
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) { Icon(Icons.Default.SwapHoriz, "Intercambiar idiomas") }
                        LanguageButton(targetLang, Modifier.weight(1f)) { picker = LanguagePicker.Target }
                    }
                }
            }
            item {
                ElevatedCard(shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Texto original", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            if (sourceText.isNotEmpty()) IconButton(onClick = { viewModel.onSourceTextChange("") }) { Icon(Icons.Default.Close, "Borrar texto") }
                        }
                        OutlinedTextField(
                            value = sourceText,
                            onValueChange = viewModel::onSourceTextChange,
                            placeholder = { Text("Escribe o usa el micrófono…") },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp),
                            shape = RoundedCornerShape(16.dp),
                            trailingIcon = { IconButton(onClick = {
                                val permission = Manifest.permission.RECORD_AUDIO
                                if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) viewModel.startListening() else permissionLauncher.launch(permission)
                            }) { Icon(Icons.Default.Mic, if (speechState is SpeechState.Listening) "Escuchando" else "Dictar texto", tint = if (speechState is SpeechState.Listening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary) } }
                        )
                    }
                }
            }
            item {
                Button(onClick = viewModel::translate, enabled = sourceText.isNotBlank() && translationState !is TranslationState.Translating, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
                    if (translationState is TranslationState.Translating) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                    else { Icon(Icons.Default.Translate, null); Spacer(Modifier.width(8.dp)); Text("Traducir", fontWeight = FontWeight.Bold) }
                }
            }
            item { TranslationResultCard(translationState, targetLang, viewModel, context) }
        }
    }
}

private enum class LanguagePicker { Source, Target }

@Composable
private fun LanguageButton(code: String, modifier: Modifier, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(languageName(code), maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
            Text(code.uppercase(), style = MaterialTheme.typography.labelSmall)
        }
        Icon(Icons.Default.ArrowDropDown, null)
    }
}

@Composable
private fun LanguagePickerDialog(title: String, selectedCode: String, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    val languages = remember { TranslateLanguage.getAllLanguages().sortedBy(::languageName) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = {
        LazyColumn(Modifier.heightIn(max = 420.dp)) {
            items(languages) { code ->
                ListItem(
                    headlineContent = { Text(languageName(code)) }, supportingContent = { Text(code.uppercase()) },
                    trailingContent = { if (code == selectedCode) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.fillMaxWidth()
                )
                HorizontalDivider()
            }
        }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } })
}

@Composable
private fun TranslationResultCard(state: TranslationState, targetLang: String, viewModel: TranslationViewModel, context: Context) {
    ElevatedCard(modifier = Modifier.fillMaxWidth().heightIn(min = 190.dp), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(20.dp)) {
            Text("Traducción", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            when (state) {
                is TranslationState.Success -> {
                    Text(state.translatedText, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        IconButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Traducción", state.translatedText))
                            Toast.makeText(context, "Traducción copiada", Toast.LENGTH_SHORT).show()
                        }) { Icon(Icons.Default.ContentCopy, "Copiar traducción") }
                        IconButton(onClick = { viewModel.speak(state.translatedText, targetLang) }) { Icon(Icons.AutoMirrored.Filled.VolumeUp, "Escuchar traducción") }
                    }
                }
                is TranslationState.Error -> StatusMessage(Icons.Default.ErrorOutline, "No se pudo traducir", state.message, MaterialTheme.colorScheme.error)
                is TranslationState.ModelNotDownloaded -> StatusMessage(Icons.Default.Download, "Falta descargar el idioma", "Abre Configuración y descarga el modelo necesario.", MaterialTheme.colorScheme.error)
                is TranslationState.Translating -> Box(Modifier.fillMaxWidth().height(110.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                else -> StatusMessage(Icons.Default.AutoAwesome, "Tu traducción aparecerá aquí", "Elige los idiomas, escribe el texto y pulsa Traducir.", MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun StatusMessage(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, detail: String, tint: Color) {
    Row(verticalAlignment = Alignment.Top) { Icon(icon, null, tint = tint); Spacer(Modifier.width(12.dp)); Column { Text(title, fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(4.dp)); Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
}

@Composable
private fun WelcomeDialog(viewModel: TranslationViewModel, onNavigateToSettings: () -> Unit) = AlertDialog(
    onDismissRequest = viewModel::dismissFirstLaunchDialog, icon = { Icon(Icons.Default.Translate, null) },
    title = { Text("Todo listo para traducir") }, text = { Text("Descarga los idiomas que necesites para traducir incluso sin conexión.") },
    confirmButton = { TextButton(onClick = { viewModel.dismissFirstLaunchDialog(); onNavigateToSettings() }) { Text("Descargar idiomas") } },
    dismissButton = { TextButton(onClick = viewModel::dismissFirstLaunchDialog) { Text("Más tarde") } }
)

internal fun languageName(code: String): String = Locale.forLanguageTag(code).getDisplayName(Locale.forLanguageTag("es")).replaceFirstChar { it.uppercase() }
