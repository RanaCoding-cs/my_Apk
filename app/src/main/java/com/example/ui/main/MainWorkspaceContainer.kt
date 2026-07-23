package com.example.ui.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.example.ui.WorkspaceViewModel
import com.example.ui.notepad.NotepadScreen
import com.example.ui.portfolio.PortfolioFeedScreen
import com.example.ui.projects.ProjectsScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.DevWorkspaceTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.vault.VaultScreen

enum class WorkspaceTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    PORTFOLIO("Portfolio", Icons.Filled.Person, Icons.Outlined.Person),
    PROJECTS("Projects", Icons.Filled.Code, Icons.Outlined.Code),
    NOTEPAD("Notepad", Icons.Filled.DataObject, Icons.Outlined.DataObject),
    VAULT("Vault", Icons.Filled.Lock, Icons.Outlined.Lock),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainWorkspaceContainer(
    viewModel: WorkspaceViewModel
) {
    var currentTab by remember { mutableStateOf(WorkspaceTab.PORTFOLIO) }
    val themeMode by viewModel.themeMode.collectAsState()

    DevWorkspaceTheme(themeMode = themeMode) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "DEVWORKSPACE",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = NeonCyan
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    WorkspaceTab.values().forEach { tab ->
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title
                                )
                            },
                            label = { Text(tab.title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = NeonCyan,
                                indicatorColor = NeonCyan
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tab_transition"
                ) { targetTab ->
                    when (targetTab) {
                        WorkspaceTab.PORTFOLIO -> PortfolioFeedScreen(
                            viewModel = viewModel,
                            onNavigateToProjects = { currentTab = WorkspaceTab.PROJECTS }
                        )
                        WorkspaceTab.PROJECTS -> ProjectsScreen(viewModel = viewModel)
                        WorkspaceTab.NOTEPAD -> NotepadScreen(viewModel = viewModel)
                        WorkspaceTab.VAULT -> VaultScreen(viewModel = viewModel)
                        WorkspaceTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
