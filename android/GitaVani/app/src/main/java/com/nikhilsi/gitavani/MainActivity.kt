package com.nikhilsi.gitavani

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.nikhilsi.gitavani.theme.GitaVaniTheme
import com.nikhilsi.gitavani.ui.navigation.GitaNavGraph
import com.nikhilsi.gitavani.ui.onboarding.OnboardingScreen
import com.nikhilsi.gitavani.viewmodel.GitaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: GitaViewModel = viewModel()
            val theme by viewModel.currentTheme.collectAsState()

            // Register audio lifecycle observer (pauses audio when app backgrounds)
            LaunchedEffect(Unit) {
                ProcessLifecycleOwner.get().lifecycle.addObserver(viewModel.audioService)
            }

            // Update system bar styles when theme changes
            LaunchedEffect(theme) {
                if (theme.isDark) {
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
                        navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    )
                } else {
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        ),
                        navigationBarStyle = SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        )
                    )
                }
            }

            GitaVaniTheme(appTheme = theme) {
                val isLoading by viewModel.isLoading.collectAsState()
                val loadError by viewModel.loadError.collectAsState()
                val hasSeenOnboarding by viewModel.settings.hasSeenOnboarding.collectAsState()

                when {
                    isLoading -> LoadingScreen()
                    loadError != null -> ErrorScreen(loadError!!)
                    !hasSeenOnboarding -> OnboardingScreen(
                        onComplete = { viewModel.settings.setHasSeenOnboarding(true) }
                    )
                    else -> {
                        val navController = rememberNavController()
                        GitaNavGraph(
                            navController = navController,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Loading Bhagavad Gita...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun ErrorScreen(error: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Error: $error",
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(32.dp)
        )
    }
}
