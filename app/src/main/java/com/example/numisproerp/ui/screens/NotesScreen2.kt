package com.numisproerp.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.numisproerp.data.entities.Note
import com.numisproerp.data.settings.SettingsManager
import com.numisproerp.di.SettingsManagerEntryPoint
import com.numisproerp.ui.components.NotificationSettingsDialog
import com.numisproerp.ui.i18n.tr
import com.numisproerp.ui.theme.AccentBlue
import com.numisproerp.ui.theme.AccentGreen
import com.numisproerp.ui.theme.AccentOrange
import com.numisproerp.ui.theme.AccentRed
import com.numisproerp.ui.theme.IOSDesign
import com.numisproerp.ui.viewmodel.NotesViewModel
import dagger.hilt.android.EntryPointAccessors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun MyNotesScreen(
    navController: NavHostController,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var deleteCandidate by remember { mutableStateOf<Note?>(null) }
    var showNotificationSettings by remember { mutableStateOf(false) }

    val settings: SettingsManager = remember {
        EntryPointAccessors
            .fromApplication(context.applicationContext, SettingsManagerEntryPoint::class.java)
            .settings()
    }
    val soundUri by settings.noteAlarmSoundUriState
    val soundLabel by settings.noteAlarmSoundLabelState

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = tr("Назад", "Back"),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        IconButton(
            onClick = { showNotificationSettings = true },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd)
        ) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = tr("Налаштування сповіщень", "Notification settings"),
                tint = AccentBlue
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Text(
                text = tr("Мої замітки", "My Notes"),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp),
                textAlign = TextAlign.Center
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.notes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            tr("Замітки порожні", "No notes yet"),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                        Text(
                            tr("Натисніть «+» щоб створити замітку", "Tap «+» to create a note"),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.notes) { note ->
                        NoteCard(
                            note = note,
                            onToggleCompleted = { viewModel.toggleCompleted(note) },
                            onEdit = { viewModel.openEditDialog(note) },
                            onDelete = { deleteCandidate = note }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { viewModel.openAddDialog() },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = tr("Додати замітку", "Add note"))
        }
    }

    if (uiState.showAddDialog) {
        AddOrEditNoteDialog(
            initial = uiState.editingNote,
            errorMessage = uiState.errorMessage,
            onDismiss = { viewModel.closeDialog() },
            onSave = { title, text, reminderDate ->
                viewModel.saveNote(title, text, reminderDate)
            }
        )
    }

    if (showNotificationSettings) {
        NotificationSettingsDialog(
            currentUri = soundUri,
            currentLabel = soundLabel,
            onDismiss = { showNotificationSettings = false },
            onSoundSelected = { uri, label ->
                settings.noteAlarmSoundUri = uri
                settings.noteAlarmSoundLabel = label
            }
        )
    }

    val toDelete = deleteCandidate
    if (toDelete != null) {
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text(tr("Видалити замітку?", "Delete note?")) },
            text = { Text(tr("Замітку «${toDelete.title}» буде видалено. Цю дію не можна скасувати.", "Note «${toDelete.title}» will be deleted. This cannot be undone.")) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteNote(toDelete)
                    deleteCandidate = null
                }) { Text(tr("Видалити", "Delete"), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deleteCandidate = null }) { Text(tr("Скасувати", "Cancel")) }
            }
        )
    }
}

@Composable
private fun NoteCard(
    note: Note,
    onToggleCompleted: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    val isOverdue = note.reminderDate != null && note.reminderDate < System.currentTimeMillis() && !note.isCompleted

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (note.isCompleted) 0.6f else 1f),
        shape = RoundedCornerShape(IOSDesign.CardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = IOSDesign.CardElevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = note.isCompleted,
                    onCheckedChange = { onToggleCompleted() }
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = note.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (note.isCompleted) TextDecoration.LineThrough else null
                    )
                    if (note.text.isNotEmpty()) {
                        Text(
                            text = note.text,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            maxLines = 3,
                            textDecoration = if (note.isCompleted) TextDecoration.LineThrough else null
                        )
                    }
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = tr("Редагувати", "Edit"), modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = tr("Видалити", "Delete"), modifier = Modifier.size(18.dp), tint = AccentRed)
                }
            }

            if (note.reminderDate != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Icon(
                        Icons.Default.Alarm,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (isOverdue) AccentRed else AccentOrange
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = dateFormat.format(Date(note.reminderDate)),
                        fontSize = 11.sp,
                        color = if (isOverdue) AccentRed else AccentOrange,
                        fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                    )
                    if (isOverdue) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tr("Прострочено!", "Overdue!"),
                            fontSize = 10.sp,
                            color = AccentRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(note.createdAt)),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun AddOrEditNoteDialog(
    initial: Note?,
    errorMessage: String,
    onDismiss: () -> Unit,
    onSave: (String, String, Long?) -> Unit
) {
    val context = LocalContext.current
    var title by remember(initial) { mutableStateOf(initial?.title ?: "") }
    var text by remember(initial) { mutableStateOf(initial?.text ?: "") }
    var reminderDate by remember(initial) { mutableStateOf(initial?.reminderDate) }

    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (initial == null) tr("Нова замітка", "New note")
                else tr("Редагувати замітку", "Edit note")
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(tr("Заголовок", "Title")) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
                )

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(tr("Текст замітки", "Note text")) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
                )

                // Reminder date picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Alarm,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = AccentOrange
                    )
                    if (reminderDate != null) {
                        Text(
                            text = dateFormat.format(Date(reminderDate!!)),
                            fontSize = 13.sp,
                            color = AccentOrange,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { reminderDate = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = tr("Видалити нагадування", "Remove reminder"), modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Text(
                            text = tr("Без нагадування", "No reminder"),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                OutlinedButton(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        if (reminderDate != null) {
                            calendar.timeInMillis = reminderDate!!
                        }
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                val timeCal = Calendar.getInstance()
                                if (reminderDate != null) timeCal.timeInMillis = reminderDate!!
                                TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        val cal = Calendar.getInstance()
                                        cal.set(year, month, day, hour, minute, 0)
                                        cal.set(Calendar.MILLISECOND, 0)
                                        reminderDate = cal.timeInMillis
                                    },
                                    timeCal.get(Calendar.HOUR_OF_DAY),
                                    timeCal.get(Calendar.MINUTE),
                                    true
                                ).show()
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
                ) {
                    Icon(Icons.Outlined.Notifications, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(tr("Встановити нагадування", "Set reminder"))
                }

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, text, reminderDate) },
                shape = RoundedCornerShape(IOSDesign.ButtonCornerRadius)
            ) {
                Text(tr("Зберегти", "Save"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(tr("Скасувати", "Cancel"))
            }
        }
    )
}
