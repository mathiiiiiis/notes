package de.mathiiis.notes.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * note editor full screen text surface
 *
 * > loads note once on entry then tracks edits in local field value
 * > autosaves short moment after typing stops
 * > leaving screen flushes final save, note left blank is dropped
 * > trash icon deletes and returns to list
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    noteId: Long,
    viewModel: NotesViewModel,
    onBack: () -> Unit,
) {
    var field by remember { mutableStateOf(TextFieldValue("")) }
    var loaded by remember { mutableStateOf(false) }
    var lastSaved by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    // ==== load once ====
    LaunchedEffect(noteId) {
        val note = viewModel.load(noteId)
        val content = note?.content.orEmpty()
        field = TextFieldValue(content, selection = TextRange(content.length))
        lastSaved = content
        loaded = true
    }

    // ==== debounced autosave ====
    LaunchedEffect(field.text, loaded) {
        if (!loaded) return@LaunchedEffect
        if (field.text == lastSaved) return@LaunchedEffect
        delay(400)
        viewModel.save(noteId, field.text)
        lastSaved = field.text
    }

    // ==== focus field once content is ready ====
    LaunchedEffect(loaded) {
        if (loaded) focusRequester.requestFocus()
    }

    // ==== flush on leave ====
    val currentText by rememberUpdatedState(field.text)
    DisposableEffect(Unit) {
        onDispose {
            val text = currentText
            if (text.isBlank()) {
                viewModel.delete(noteId)
            } else if (text != lastSaved) {
                viewModel.save(noteId, text)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = {
                        focusManager.clearFocus()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.delete(noteId)
                        onBack()
                    }) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete note")
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            BasicTextField(
                value = field,
                onValueChange = { field = it },
                modifier = Modifier
                    .fillMaxSize()
                    .focusRequester(focusRequester),
                textStyle = LocalTextStyle.current.copy(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { inner ->
                    if (field.text.isEmpty()) {
                        Text(
                            text = "Start writing.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    inner()
                },
            )
        }
    }
}
