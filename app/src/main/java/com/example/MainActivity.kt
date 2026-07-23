package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ui.WorkspaceViewModel
import com.example.ui.main.MainWorkspaceContainer
import com.example.ui.splash.SplashScreen

class MainActivity : ComponentActivity() {

    private val viewModel: WorkspaceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var isSplashFinished by remember { mutableStateOf(false) }

            AnimatedContent(
                targetState = isSplashFinished,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "splash_to_main"
            ) { finished ->
                if (!finished) {
                    SplashScreen(
                        onSplashFinished = {
                            isSplashFinished = true
                        }
                    )
                } else {
                    MainWorkspaceContainer(viewModel = viewModel)
                }
            }
        }
    }
}
