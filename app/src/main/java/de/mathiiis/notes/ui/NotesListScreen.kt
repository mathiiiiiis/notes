@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package de.mathiiis.notes.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.mathiiis.notes.R
import de.mathiiis.notes.data.Note
import kotlinx.coroutines.launch

private val SwipeThreshold: Dp = 56.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    viewModel: NotesViewModel,
    onOpen: (Long) -> Unit,
) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val total by viewModel.total.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()

    var searching by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val deletedMessage = stringResource(R.string.note_deleted)
    val undoLabel = stringResource(R.string.undo)

    val fabExpanded by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset < 40
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AnimatedContent(
                targetState = searching,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "top-bar",
            ) { isSearching ->
                if (isSearching) {
                    SearchTopBar(
                        query = query,
                        onQueryChange = { value ->
                            viewModel.setQuery(value)
                            // new list, so do not open it mid scroll
                            scope.launch { listState.scrollToItem(0) }
                        },
                        onClose = {
                            viewModel.setQuery("")
                            searching = false
                            scrollBehavior.state.heightOffset = 0f
                            scrollBehavior.state.contentOffset = 0f
                        },
                    )
                } else {
                    LargeFlexibleTopAppBar(
                        title = { Text(stringResource(R.string.notes_title)) },
                        subtitle = {
                            Text(pluralStringResource(R.plurals.note_count, total, total))
                        },
                        actions = {
                            IconButton(
                                onClick = {
                                    searching = true
                                    scrollBehavior.state.heightOffset = 0f
                                    scrollBehavior.state.contentOffset = 0f
                                },
                            ) {
                                Icon(
                                    Icons.Rounded.Search,
                                    contentDescription = stringResource(R.string.search),
                                )
                            }
                        },
                        scrollBehavior = scrollBehavior,
                    )
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onOpen(NEW_NOTE_ID) },
                expanded = fabExpanded,
                icon = {
                    Icon(
                        Icons.Rounded.Add,
                        contentDescription = stringResource(R.string.new_note),
                    )
                },
                text = { Text(stringResource(R.string.new_note)) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when {
            notes.isEmpty() && query.isNotBlank() ->
                EmptyState(
                    title = stringResource(R.string.empty_search_title),
                    body = stringResource(R.string.empty_search_body, query),
                    modifier = Modifier.padding(padding),
                )

            notes.isEmpty() ->
                EmptyState(
                    title = stringResource(R.string.empty_title),
                    body = stringResource(R.string.empty_body),
                    modifier = Modifier.padding(padding),
                )

            else ->
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding =
                        PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = padding.calculateTopPadding() + 8.dp,
                            bottom = padding.calculateBottomPadding() + 104.dp,
                        ),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(notes, key = { it.id }) { note ->
                        SwipeableNoteCard(
                            note = note,
                            onClick = { onOpen(note.id) },
                            onPinToggle = { viewModel.setPinned(note.id, !note.pinned) },
                            onDelete = {
                                scope.launch {
                                    val removed = viewModel.delete(note.id) ?: return@launch
                                    val result =
                                        snackbarHostState.showSnackbar(
                                            message = deletedMessage,
                                            actionLabel = undoLabel,
                                            withDismissAction = true,
                                        )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.restore(removed)
                                    }
                                }
                            },
                        )
                    }
                }
        }
    }
}

// ==== search bar ====

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    TopAppBar(
        title = {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                placeholder = { Text(stringResource(R.string.search_hint)) },
                singleLine = true,
                colors =
                    TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.search_clear),
                            )
                        }
                    }
                },
            )
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.search_close),
                )
            }
        },
    )
}

// ==== rows ====

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableNoteCard(
    note: Note,
    onClick: () -> Unit,
    onPinToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    val state =
        remember(note.id, density) {
            SwipeToDismissBoxState(
                initialValue = SwipeToDismissBoxValue.Settled,
                positionalThreshold = { with(density) { SwipeThreshold.toPx() } },
            )
        }

    val pinned by rememberUpdatedState(note.pinned)
    val pinToggle by rememberUpdatedState(onPinToggle)
    val delete by rememberUpdatedState(onDelete)

    val settled = state.settledValue
    LaunchedEffect(settled) {
        when (settled) {
            SwipeToDismissBoxValue.StartToEnd -> {
                pinToggle()
                scope.launch { state.reset() }
            }

            SwipeToDismissBoxValue.EndToStart ->
                if (pinned) {
                    pinToggle()
                    scope.launch { state.reset() }
                } else {
                    delete()
                }

            SwipeToDismissBoxValue.Settled -> Unit
        }
    }

    SwipeToDismissBox(
        state = state,
        backgroundContent = {
            SwipeBackground(direction = state.dismissDirection, pinned = note.pinned)
        },
        content = {
            NoteCard(
                note = note,
                onClick = onClick,
                onPinToggle = onPinToggle,
                onDelete = onDelete,
            )
        },
    )
}

@Composable
private fun SwipeBackground(
    direction: SwipeToDismissBoxValue,
    pinned: Boolean,
) {
    val deleting = direction == SwipeToDismissBoxValue.EndToStart && !pinned

    val container by animateColorAsState(
        targetValue =
            when {
                direction == SwipeToDismissBoxValue.Settled -> Color.Transparent
                deleting -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.tertiaryContainer
            },
        label = "swipe-bg",
    )

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(28.dp))
                .background(container)
                .padding(horizontal = 24.dp),
        contentAlignment =
            if (direction == SwipeToDismissBoxValue.EndToStart) {
                Alignment.CenterEnd
            } else {
                Alignment.CenterStart
            },
    ) {
        Icon(
            imageVector =
                when {
                    deleting -> Icons.Rounded.Delete
                    pinned -> Icons.Outlined.PushPin
                    else -> Icons.Rounded.PushPin
                },
            contentDescription = null,
            tint =
                if (deleting) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onTertiaryContainer
                },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onPinToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    val title = NoteText.titleOf(note.content) ?: stringResource(R.string.untitled)
    val preview = NoteText.previewOf(note.content)
    val imageCount = remember(note.content) { Md.refsIn(note.content).size }

    val pinLabel = stringResource(if (note.pinned) R.string.unpin else R.string.pin)
    val deleteLabel = stringResource(R.string.delete_note)
    val cardLabel = stringResource(R.string.a11y_note_card, title)

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (note.pinned) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    },
            ),
        modifier =
            Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = cardLabel
                    customActions =
                        listOf(
                            CustomAccessibilityAction(pinLabel) {
                                onPinToggle()
                                true
                            },
                            CustomAccessibilityAction(deleteLabel) {
                                onDelete()
                                true
                            },
                        )
                },
    ) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (note.pinned) {
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.Rounded.PushPin,
                        contentDescription = stringResource(R.string.pinned),
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }

            if (preview.isNotEmpty()) {
                Text(
                    text = preview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }

            Row(
                modifier = Modifier.padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = NoteText.timestamp(note.updatedAt),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (imageCount > 0) {
                    Spacer(Modifier.width(10.dp))
                    Icon(
                        Icons.Rounded.Image,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = imageCount.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

// ==== empty states ====

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EmptyState(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier =
                Modifier
                    .size(88.dp)
                    .rotate(12f)
                    .clip(MaterialShapes.Sunny.toShape())
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        )

        Spacer(Modifier.height(28.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmallEmphasized,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
