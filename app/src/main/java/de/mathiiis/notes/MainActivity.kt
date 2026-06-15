package de.mathiiis.notes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import de.mathiiis.notes.data.NotesDatabase
import de.mathiiis.notes.data.NotesRepository
import de.mathiiis.notes.ui.NoteEditScreen
import de.mathiiis.notes.ui.NotesListScreen
import de.mathiiis.notes.ui.NotesViewModel
import de.mathiiis.notes.ui.theme.NotesTheme

/**
 * Single activity host
 *
 * > wires database, repository and viewmodel factory, no di framework
 * > two destinations, the list and editor keyed by note id
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repo = NotesRepository(NotesDatabase.get(applicationContext).noteDao())

        setContent {
            NotesTheme {
                val vm: NotesViewModel = viewModel(factory = NotesViewModel.Factory(repo))
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = ROUTE_LIST) {

                    // ==== list ====
                    composable(ROUTE_LIST) {
                        val notes = vm.notes.collectAsState()
                        NotesListScreen(
                            notesState = notes,
                            onOpen = { id -> navController.navigate("$ROUTE_EDIT/$id") },
                            onCreate = {
                                vm.create { id -> navController.navigate("$ROUTE_EDIT/$id") }
                            },
                        )
                    }

                    // ==== editor ====
                    composable(
                        route = "$ROUTE_EDIT/{$ARG_ID}",
                        arguments = listOf(navArgument(ARG_ID) { type = NavType.LongType }),
                    ) { entry ->
                        val id = entry.arguments?.getLong(ARG_ID) ?: return@composable
                        NoteEditScreen(
                            noteId = id,
                            viewModel = vm,
                            onBack = { navController.popBackStack() },
                        )
                    }
                }
            }
        }
    }

    private companion object {
        const val ROUTE_LIST = "list"
        const val ROUTE_EDIT = "edit"
        const val ARG_ID = "id"
    }
}
