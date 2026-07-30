package de.mathiiis.notes

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.LaunchedEffect
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import de.mathiiis.notes.data.NotesDatabase
import de.mathiiis.notes.data.NotesRepository
import de.mathiiis.notes.ui.NEW_NOTE_ID
import de.mathiiis.notes.ui.NoteEditScreen
import de.mathiiis.notes.ui.NotesListScreen
import de.mathiiis.notes.ui.NotesViewModel
import de.mathiiis.notes.ui.theme.NotesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repo = NotesRepository(NotesDatabase.get(applicationContext).noteDao())
        val sharedText = if (savedInstanceState == null) intent?.sharedText() else null

        setContent {
            NotesTheme {
                val vm: NotesViewModel =
                    viewModel(
                        factory = NotesViewModel.Factory(application, repo),
                    )
                val navController = rememberNavController()

                // ==== shared text arrives as a new note ====
                LaunchedEffect(sharedText) {
                    val text = sharedText ?: return@LaunchedEffect
                    if (text.isBlank()) return@LaunchedEffect
                    val id = vm.createNow(text)
                    navController.navigate("$ROUTE_EDIT/$id")
                }

                NavHost(
                    navController = navController,
                    startDestination = ROUTE_LIST,
                    enterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Start,
                            spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow,
                            ),
                        ) + fadeIn()
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Start,
                            spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow,
                            ),
                        ) + fadeOut()
                    },
                    popEnterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.End,
                            spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow,
                            ),
                        ) + fadeIn()
                    },
                    popExitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.End,
                            spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow,
                            ),
                        ) + fadeOut()
                    },
                ) {
                    // ==== list ====
                    composable(ROUTE_LIST) {
                        NotesListScreen(
                            viewModel = vm,
                            onOpen = { id -> navController.navigate("$ROUTE_EDIT/$id") },
                        )
                    }

                    // ==== editor ====
                    composable(
                        route = "$ROUTE_EDIT/{$ARG_ID}",
                        arguments = listOf(navArgument(ARG_ID) { type = NavType.LongType }),
                    ) { entry ->
                        val id = entry.arguments?.getLong(ARG_ID) ?: NEW_NOTE_ID
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

    private fun Intent.sharedText(): String? =
        if (action == Intent.ACTION_SEND && type == "text/plain") {
            getStringExtra(Intent.EXTRA_TEXT)
        } else {
            null
        }

    private companion object {
        const val ROUTE_LIST = "list"
        const val ROUTE_EDIT = "edit"
        const val ARG_ID = "id"
    }
}
