package com.numisproerp.ui.components

import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.numisproerp.notifications.AlarmSound
import com.numisproerp.notifications.AlarmSoundCatalog
import com.numisproerp.ui.i18n.tr

/**
 * Діалог налаштувань сповіщень нотаток. Дозволяє:
 *  - обрати один із системних/доданих звуків,
 *  - програти звук попередньо,
 *  - додати власний звук з файлу.
 */
@Composable
fun NotificationSettingsDialog(
    currentUri: String,
    currentLabel: String,
    onDismiss: () -> Unit,
    onSoundSelected: (uri: String, label: String) -> Unit
) {
    val context = LocalContext.current

    var systemSounds by remember { mutableStateOf<List<AlarmSound>>(emptyList()) }
    var customSounds by remember { mutableStateOf<List<AlarmSound>>(emptyList()) }
    var previewing by remember { mutableStateOf<Ringtone?>(null) }

    LaunchedEffect(Unit) {
        systemSounds = AlarmSoundCatalog.systemSounds(context)
        customSounds = AlarmSoundCatalog.customSounds(context)
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                previewing?.stop()
            } catch (_: Exception) {
            }
        }
    }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            // Утримуємо доступ для file copy одразу.
            val displayName = uri.lastPathSegment?.substringAfterLast('/')?.substringAfterLast(':')
                ?: "custom_sound"
            val imported = AlarmSoundCatalog.importCustomSound(context, it, displayName)
            if (imported != null) {
                customSounds = AlarmSoundCatalog.customSounds(context)
                onSoundSelected(imported.uri, imported.title)
            }
        }
    }

    fun preview(uri: String) {
        try {
            previewing?.stop()
            previewing = null
            val rt = RingtoneManager.getRingtone(context, Uri.parse(uri))
            rt?.play()
            previewing = rt
        } catch (_: Exception) {
            // ignore preview errors silently
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tr("Налаштування сповіщень", "Notification settings")) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        tr("Стандартні звуки", "Standard sounds"),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                items(systemSounds) { sound ->
                    SoundRow(
                        sound = sound,
                        selected = sound.uri == currentUri,
                        onSelect = { onSoundSelected(sound.uri, sound.title) },
                        onPreview = { preview(sound.uri) }
                    )
                }

                if (customSounds.isNotEmpty()) {
                    item {
                        Text(
                            tr("Власні звуки", "Custom sounds"),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    items(customSounds) { sound ->
                        SoundRow(
                            sound = sound,
                            selected = sound.uri == currentUri,
                            onSelect = { onSoundSelected(sound.uri, sound.title) },
                            onPreview = { preview(sound.uri) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            pickerLauncher.launch(arrayOf("audio/*"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(tr("Додати власний звук…", "Add your own sound…"))
                    }
                }

                if (currentLabel.isNotEmpty()) {
                    item {
                        Text(
                            tr("Поточний звук: ", "Current sound: ") + currentLabel,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(tr("Готово", "Done"))
            }
        }
    )
}

@Composable
private fun SoundRow(
    sound: AlarmSound,
    selected: Boolean,
    onSelect: () -> Unit,
    onPreview: () -> Unit
) {
    val accent = MaterialTheme.colorScheme.primary
    val rowBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    val border = if (selected)
        androidx.compose.foundation.BorderStroke(1.5.dp, accent.copy(alpha = 0.85f))
    else null
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = rowBg),
        border = border,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onSelect)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = sound.title,
                modifier = Modifier.weight(1f),
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = onPreview, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Прослухати",
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
            }
            RadioButton(
                selected = selected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = accent,
                    unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                )
            )
        }
    }
}


