@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package de.mathiiis.notes.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FormatBold
import androidx.compose.material.icons.rounded.FormatItalic
import androidx.compose.material.icons.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.m3.Markdown
import de.mathiiis.notes.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val NEW_NOTE_ID = -1L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    noteId: Long,
    viewModel: NotesViewModel,
    onBack: () -> Unit,
) {
    var field by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var currentId by rememberSaveable { mutableStateOf(noteId) }
    var loaded by rememberSaveable { mutableStateOf(false) }
    var lastSaved by rememberSaveable { mutableStateOf("") }
    var preview by rememberSaveable { mutableStateOf(false) }
    var pinned by rememberSaveable { mutableStateOf(false) }
    var deleted by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    var toolbarInset by remember { mutableStateOf(FloatingToolbarDefaults.ContainerSize + 24.dp) }

    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    // ==== load once ====
    LaunchedEffect(currentId) {
        if (loaded) return@LaunchedEffect
        if (currentId == NEW_NOTE_ID) {
            loaded = true
            return@LaunchedEffect
        }
        val note = viewModel.load(currentId)
        val content = note?.content.orEmpty()
        field = TextFieldValue(content, selection = TextRange(content.length))
        lastSaved = content
        pinned = note?.pinned == true
        loaded = true
    }

    // ==== debounced autosave ====
    LaunchedEffect(field.text, loaded) {
        if (!loaded || deleted) return@LaunchedEffect
        val text = field.text
        if (text == lastSaved) return@LaunchedEffect
        delay(400)
        if (currentId == NEW_NOTE_ID) {
            if (text.isNotBlank()) {
                currentId = viewModel.createNow(text)
                lastSaved = text
            }
        } else {
            viewModel.save(currentId, text)
            lastSaved = text
        }
    }

    // ==== focus the field when editing ====
    LaunchedEffect(loaded, preview) {
        if (loaded && !preview) focusRequester.requestFocus()
    }

    // ==== flush on disposal ====
    val liveText by rememberUpdatedState(field.text)
    val liveId by rememberUpdatedState(currentId)
    val liveSaved by rememberUpdatedState(lastSaved)
    val liveDeleted by rememberUpdatedState(deleted)
    DisposableEffect(Unit) {
        onDispose {
            val text = liveText
            if (liveDeleted || text.isBlank() || text == liveSaved) return@onDispose
            if (liveId == NEW_NOTE_ID) {
                viewModel.createDetached(text)
            } else {
                viewModel.save(liveId, text)
            }
        }
    }

    // ==== image picker ====
    val pickImage =
        rememberLauncherForActivityResult(
            ActivityResultContracts.PickVisualMedia(),
        ) { uri ->
            if (uri != null) {
                scope.launch {
                    val ref = ImageStore.persist(context, uri) ?: return@launch
                    field = Md.insert(field, "\n![]($ref)\n")
                }
            }
        }

    val title = NoteText.titleOf(field.text)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title ?: stringResource(R.string.new_note),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                actions = {
                    if (currentId != NEW_NOTE_ID) {
                        IconButton(
                            onClick = {
                                pinned = !pinned
                                viewModel.setPinned(currentId, pinned)
                            },
                        ) {
                            Icon(
                                Icons.Rounded.PushPin,
                                contentDescription =
                                    stringResource(
                                        if (pinned) R.string.unpin else R.string.pin,
                                    ),
                                tint =
                                    if (pinned) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                            )
                        }
                    }

                    IconButton(onClick = { preview = !preview }) {
                        Icon(
                            imageVector =
                                if (preview) {
                                    Icons.Rounded.Edit
                                } else {
                                    Icons.Rounded.Visibility
                                },
                            contentDescription =
                                stringResource(
                                    if (preview) R.string.edit else R.string.preview,
                                ),
                        )
                    }

                    IconButton(onClick = { confirmDelete = true }) {
                        Icon(
                            Icons.Rounded.DeleteOutline,
                            contentDescription = stringResource(R.string.delete_note),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .imePadding(),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(
                            start = 20.dp,
                            end = 20.dp,
                            top = 8.dp,
                            bottom = if (preview) 8.dp else toolbarInset,
                        ),
            ) {
                if (preview) {
                    Markdown(
                        content = Md.forRender(context, field.text),
                        imageTransformer = Coil3ImageTransformerImpl,
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                    )
                } else {
                    BasicTextField(
                        value = field,
                        onValueChange = { field = it },
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .focusRequester(focusRequester),
                        textStyle =
                            LocalTextStyle.current.copy(
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                            ),
                        keyboardOptions =
                            KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                            ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { inner ->
                            if (field.text.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.editor_placeholder),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            inner()
                        },
                    )
                }
            }

            // ==== markdown toolbar ====
            if (!preview) {
                FormatToolbar(
                    onAction = { field = it(field) },
                    onPickImage = {
                        pickImage.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly,
                            ),
                        )
                    },
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .onSizeChanged { size ->
                                toolbarInset = with(density) { size.height.toDp() } + 8.dp
                            }
                            .padding(bottom = 16.dp),
                )
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = { Text(stringResource(R.string.delete_confirm_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        deleted = true
                        if (currentId != NEW_NOTE_ID) {
                            scope.launch { viewModel.delete(currentId) }
                        }
                        onBack()
                    },
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormatToolbar(
    onAction: ((TextFieldValue) -> TextFieldValue) -> Unit,
    onPickImage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HorizontalFloatingToolbar(
        expanded = true,
        modifier = modifier,
    ) {
        IconButton(onClick = { onAction { Md.wrap(it, "**") } }) {
            Icon(
                Icons.Rounded.FormatBold,
                contentDescription = stringResource(R.string.format_bold),
            )
        }
        IconButton(onClick = { onAction { Md.wrap(it, "*") } }) {
            Icon(
                Icons.Rounded.FormatItalic,
                contentDescription = stringResource(R.string.format_italic),
            )
        }
        IconButton(onClick = { onAction { Md.linePrefix(it, "# ") } }) {
            Icon(
                Icons.Rounded.Title,
                contentDescription = stringResource(R.string.format_heading),
            )
        }
        IconButton(onClick = { onAction { Md.linePrefix(it, "- ") } }) {
            Icon(
                Icons.Rounded.FormatListBulleted,
                contentDescription = stringResource(R.string.format_bullet),
            )
        }
        IconButton(onClick = { onAction { Md.linePrefix(it, "- [ ] ") } }) {
            Icon(
                Icons.Rounded.CheckBox,
                contentDescription = stringResource(R.string.format_checkbox),
            )
        }
        IconButton(onClick = { onAction { Md.wrap(it, "`") } }) {
            Icon(
                Icons.Rounded.Code,
                contentDescription = stringResource(R.string.format_code),
            )
        }
        IconButton(onClick = onPickImage) {
            Icon(
                Icons.Rounded.Image,
                contentDescription = stringResource(R.string.insert_image),
            )
        }
    }
}
