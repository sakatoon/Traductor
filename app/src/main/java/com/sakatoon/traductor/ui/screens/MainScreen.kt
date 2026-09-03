package com.sakatoon.traductor.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.mlkit.nl.translate.TranslateLanguage
import com.sakatoon.traductor.R
import com.sakatoon.traductor.data.speech.SpeechState
import com.sakatoon.traductor.data.translation.TranslationState
import com.sakatoon.traductor.viewmodel.TranslationViewModel
import java.util.Locale

// Dynamic Colors adapted to active MaterialTheme (Light or Dark)
@Composable
private fun appBackgroundColor() = MaterialTheme.colorScheme.background

@Composable
private fun topBarBackgroundColor() = MaterialTheme.colorScheme.surface

@Composable
private fun languageBarBackgroundColor() = if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFF2C2C2E) else Color.Black

@Composable
private fun languageBarTextColor() = Color.White

@Composable
private fun cardBackgroundColor() = MaterialTheme.colorScheme.surface

@Composable
private fun textPrimaryColor() = MaterialTheme.colorScheme.onSurface

@Composable
private fun textMutedColor() = MaterialTheme.colorScheme.onSurfaceVariant

private val AccentBlue = Color(0xFF3880EC)

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
    var showMenu by remember { mutableStateOf(false) }
    
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { 
        if (it) viewModel.startListening() 
    }

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

    Scaffold(
        containerColor = appBackgroundColor(),
        topBar = {
            Surface(color = topBarBackgroundColor()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(60.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Traductor",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimaryColor()
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Opciones",
                                    tint = textPrimaryColor()
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier.background(cardBackgroundColor())
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Configuración", color = textPrimaryColor()) },
                                    onClick = {
                                        showMenu = false
                                        onNavigateToSettings()
                                    },
                                    leadingIcon = { Icon(Icons.Default.Settings, null, tint = textPrimaryColor()) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // User profile avatar like in the image
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable { onNavigateToSettings() }
                        ) {
                            Image(
                                painter = painterResource(R.drawable.dev_logo),
                                contentDescription = "Perfil Creador",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Language Selection Selector Bar (matching the design bar in the image)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                color = languageBarBackgroundColor()
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { picker = LanguagePicker.Source },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = languageName(sourceLang),
                            color = languageBarTextColor(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = viewModel::swapLanguages,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Intercambiar",
                            tint = languageBarTextColor().copy(alpha = 0.7f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { picker = LanguagePicker.Target },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = languageName(targetLang),
                            color = languageBarTextColor(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Source Input Card (Top Card in image)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(20.dp),
                color = cardBackgroundColor()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header row: "Detectar idioma", mic button, close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Detectar idioma",
                            color = textMutedColor(),
                            fontSize = 14.sp
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    val permission = Manifest.permission.RECORD_AUDIO
                                    if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                                        viewModel.startListening()
                                    } else {
                                        permissionLauncher.launch(permission)
                                    }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Micrófono",
                                    tint = if (speechState is SpeechState.Listening) Color.Red else textMutedColor()
                                )
                            }

                            if (sourceText.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.onSourceTextChange("") },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Borrar",
                                        tint = textMutedColor()
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Text Input Field
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        if (sourceText.isEmpty()) {
                            Text(
                                text = "Escribe o usa el micrófono…",
                                color = textMutedColor(),
                                fontSize = 22.sp
                            )
                        }

                        BasicTextField(
                            value = sourceText,
                            onValueChange = {
                                viewModel.onSourceTextChange(it)
                                viewModel.translate()
                            },
                            textStyle = TextStyle(
                                color = textPrimaryColor(),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Normal,
                                lineHeight = 28.sp
                            ),
                            cursorBrush = SolidColor(AccentBlue),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Translated Result Card (Bottom Card in image)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(20.dp),
                color = cardBackgroundColor()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        when (translationState) {
                            is TranslationState.Success -> {
                                Text(
                                    text = (translationState as TranslationState.Success).translatedText,
                                    color = textPrimaryColor(),
                                    fontSize = 22.sp,
                                    lineHeight = 28.sp
                                )
                            }
                            is TranslationState.Translating -> {
                                CircularProgressIndicator(
                                    color = AccentBlue,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .align(Alignment.Center)
                                )
                            }
                            is TranslationState.Error -> {
                                Text(
                                    text = (translationState as TranslationState.Error).message,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 16.sp
                                )
                            }
                            is TranslationState.ModelNotDownloaded -> {
                                Text(
                                    text = "Descarga el paquete de idioma en Configuración.",
                                    color = textMutedColor(),
                                    fontSize = 16.sp
                                )
                            }
                            else -> {
                                Text(
                                    text = "La traducción aparecerá aquí",
                                    color = textMutedColor().copy(alpha = 0.6f),
                                    fontSize = 22.sp
                                )
                            }
                        }
                    }

                    // Action buttons at the bottom matching image design (Volume / Speaker and Copy in rounded blue/gray squares)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Speaker for source or target text
                        IconButton(
                            onClick = {
                                if (sourceText.isNotEmpty()) {
                                    viewModel.speak(sourceText, sourceLang)
                                }
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Escuchar original",
                                tint = textMutedColor()
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Speaker Blue Button
                            Surface(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        if (translationState is TranslationState.Success) {
                                            viewModel.speak((translationState as TranslationState.Success).translatedText, targetLang)
                                        }
                                    },
                                color = AccentBlue
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Escuchar traducción",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            // Copy Blue Button
                            Surface(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        if (translationState is TranslationState.Success) {
                                            val textToCopy = (translationState as TranslationState.Success).translatedText
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Traducción", textToCopy))
                                            Toast.makeText(context, "Traducción copiada", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                color = AccentBlue
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copiar traducción",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private enum class LanguagePicker { Source, Target }

@Composable
private fun LanguagePickerDialog(title: String, selectedCode: String, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    val languages = remember { TranslateLanguage.getAllLanguages().sortedBy(::languageName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = textPrimaryColor(), fontWeight = FontWeight.Bold) },
        containerColor = cardBackgroundColor(),
        text = {
            LazyColumn(Modifier.heightIn(max = 420.dp)) {
                items(languages) { code ->
                    val isSelected = code == selectedCode
                    val itemBg = if (isSelected) AccentBlue.copy(alpha = 0.15f) else Color.Transparent
                    val textColor = if (isSelected) AccentBlue else textPrimaryColor()
                    
                    Surface(
                        color = itemBg,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clickable { onSelect(code) }
                    ) {
                        ListItem(
                            headlineContent = { 
                                Text(
                                    languageName(code), 
                                    color = textColor, 
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ) 
                            },
                            supportingContent = { 
                                Text(
                                    code.uppercase(), 
                                    color = if (isSelected) AccentBlue.copy(alpha = 0.8f) else textMutedColor()
                                ) 
                            },
                            trailingContent = { 
                                if (isSelected) {
                                    Icon(Icons.Default.Check, "Seleccionado", tint = AccentBlue) 
                                }
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar", color = AccentBlue, fontWeight = FontWeight.Bold) } }
    )
}

@Composable
private fun WelcomeDialog(viewModel: TranslationViewModel, onNavigateToSettings: () -> Unit) = AlertDialog(
    onDismissRequest = viewModel::dismissFirstLaunchDialog,
    icon = { Icon(Icons.Default.Translate, null, tint = AccentBlue) },
    title = { Text("Todo listo para traducir", color = textPrimaryColor()) },
    text = { Text("Descarga los idiomas que necesites para traducir incluso sin conexión.", color = textMutedColor()) },
    containerColor = cardBackgroundColor(),
    confirmButton = { TextButton(onClick = { viewModel.dismissFirstLaunchDialog(); onNavigateToSettings() }) { Text("Descargar idiomas", color = AccentBlue) } },
    dismissButton = { TextButton(onClick = viewModel::dismissFirstLaunchDialog) { Text("Más tarde", color = textMutedColor()) } }
)

internal fun languageName(code: String): String = Locale.forLanguageTag(code).getDisplayName(Locale.forLanguageTag("es")).replaceFirstChar { it.uppercase() }
