package de.mathiiis.notes.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Visibility
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.m3.Markdown
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * The single note editor.
 *
 * > edit mode is one full screen markdown source field, preview mode renders it
 * > autosaves a short moment after typing stops
 * > leaving by back or the toolbar arrow persists first, a note left blank is dropped
 * > the field sits above the keyboard via ime padding so the caret stays visible
 * > the image button picks a photo, copies it into app storage and inserts a markdown link
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
    var preview by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    // ==== persist current state ====
    val saveAndExit = {
        val text = field.text
        if (text.isBlank()) {
            viewModel.delete(noteId)
        } else if (text != lastSaved) {
            viewModel.save(noteId, text)
            lastSaved = text
        }
        focusManager.clearFocus()
        onBack()
    }

    BackHandler { saveAndExit() }

    // ==== image picker ====
    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val ref = ImageStore.persist(context, uri) ?: return@launch
                field = insertAtCursor(field, "\n![](" + ref + ")\n")
            }
        }
    }

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

    // ==== focus field when editing ====
    LaunchedEffect(loaded, preview) {
        if (loaded && !preview) focusRequester.requestFocus()
    }

    // ==== backstop flush for non back disposal ====
    val currentText by rememberUpdatedState(field.text)
    DisposableEffect(Unit) {
        onDispose {
            val text = currentText
            if (text.isNotBlank() && text != lastSaved) {
                viewModel.save(noteId, text)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { saveAndExit() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!preview) {
                        IconButton(onClick = {
                            pickImage.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                            )
                        }) {
                            Icon(Icons.Rounded.Image, contentDescription = "Insert image")
                        }
                    }
                    IconButton(onClick = { preview = !preview }) {
                        if (preview) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Edit")
                        } else {
                            Icon(Icons.Rounded.Visibility, contentDescription = "Preview")
                        }
                    }
                    IconButton(onClick = {
                        viewModel.delete(noteId)
                        focusManager.clearFocus()
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
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            if (preview) {
                Markdown(
                    content = field.text,
                    imageTransformer = Coil3ImageTransformerImpl,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                )
            } else {
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
                                text = "Start writing. Markdown supported.",
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
}

/**
 * Inserts text at current cursor and places caret after it
 *
 * > used when dropping an image link into note body
 */
private fun insertAtCursor(value: TextFieldValue, insert: String): TextFieldValue {
    val start = value.selection.start.coerceIn(0, value.text.length)
    val newText = value.text.substring(0, start) + insert + value.text.substring(start)
    return TextFieldValue(newText, selection = TextRange(start + insert.length))
}
