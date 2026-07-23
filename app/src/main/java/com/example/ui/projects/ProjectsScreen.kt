package com.example.ui.projects

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CodeFileEntity
import com.example.data.ProjectEntity
import com.example.editor.CodeSyntaxHighlighter
import com.example.ui.WorkspaceViewModel
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    viewModel: WorkspaceViewModel
) {
    val projects by viewModel.allProjects.collectAsState()
    val selectedProject by viewModel.selectedProject.collectAsState()

    var showNewProjectDialog by remember { mutableStateOf(false) }

    if (selectedProject == null) {
        // Project Selection List
        Scaffold(
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { showNewProjectDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = "New Project") },
                    text = { Text("New Project") },
                    containerColor = NeonCyan,
                    contentColor = Color.Black
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Local Code Projects",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Manage multi-language source code with live syntax highlighting & evaluation.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(projects, key = { it.id }) { project ->
                        ProjectItemCard(
                            project = project,
                            onClick = { viewModel.selectProject(project) }
                        )
                    }
                }
            }
        }
    } else {
        // Code Editor View
        CodeEditorView(
            viewModel = viewModel,
            onBackToProjects = {
                viewModel.selectFile(CodeFileEntity(0, 0, "", "", ""))
            }
        )
    }

    if (showNewProjectDialog) {
        CreateProjectModal(
            onDismiss = { showNewProjectDialog = false },
            onCreate = { title, desc, lang, fw ->
                viewModel.createProject(title, desc, lang, fw)
                showNewProjectDialog = false
            }
        )
    }
}

@Composable
fun ProjectItemCard(
    project: ProjectEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Code,
                        contentDescription = null,
                        tint = NeonCyan
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = project.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeonPurple.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = project.language,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = NeonPurple,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = project.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Framework: ${project.framework}",
                    style = MaterialTheme.typography.labelMedium,
                    color = NeonEmerald
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFD29922),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${project.starsCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeEditorView(
    viewModel: WorkspaceViewModel,
    onBackToProjects: () -> Unit
) {
    val selectedProject by viewModel.selectedProject.collectAsState()
    val files by viewModel.currentFiles.collectAsState()
    val selectedFile by viewModel.selectedFile.collectAsState()
    val activeCode by viewModel.activeCodeContent.collectAsState()
    val isEditMode by viewModel.isEditMode.collectAsState()
    val executionOutput by viewModel.executionOutput.collectAsState()

    var showAddFileDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = selectedProject?.title ?: "Project Editor",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = selectedFile?.fileName ?: "Select file",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = NeonCyan
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackToProjects) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Mode Toggle Button (Read vs Live Edit)
                    IconButton(onClick = { viewModel.toggleEditMode() }) {
                        Icon(
                            imageVector = if (isEditMode) Icons.Outlined.Code else Icons.Outlined.Edit,
                            contentDescription = "Toggle Mode",
                            tint = if (isEditMode) NeonEmerald else NeonCyan
                        )
                    }

                    // Save Button
                    IconButton(onClick = { viewModel.saveActiveFileContent() }) {
                        Icon(Icons.Default.Save, contentDescription = "Save File", tint = NeonCyan)
                    }

                    // Run Code Button
                    IconButton(onClick = { viewModel.runActiveCode() }) {
                        Icon(Icons.Outlined.PlayArrow, contentDescription = "Run Code", tint = NeonEmerald)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // File Tabs Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(files, key = { it.id }) { file ->
                    val isSelected = selectedFile?.id == file.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectFile(file) },
                        label = {
                            Text(
                                text = file.fileName,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        },
                        trailingIcon = {
                            if (files.size > 1) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete File",
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { viewModel.deleteFile(file) }
                                )
                            }
                        }
                    )
                }

                item {
                    IconButton(
                        onClick = { showAddFileDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add File", tint = NeonCyan)
                    }
                }
            }

            // Editor Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF0D1117))
                    .padding(12.dp)
            ) {
                if (isEditMode) {
                    // Live Edit Mode Text Field
                    BasicTextField(
                        value = activeCode,
                        onValueChange = { viewModel.updateCodeContent(it) },
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = Color(0xFFE6EDF3)
                        ),
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Read Mode with Rich Syntax Highlighting
                    val highlightedText = CodeSyntaxHighlighter.highlight(
                        code = activeCode,
                        language = selectedFile?.language ?: "Kotlin"
                    )
                    Text(
                        text = highlightedText,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Code Execution Output Drawer / Console
            AnimatedVisibility(visible = executionOutput.isNotBlank()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    color = Color(0xFF030712),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF30363D))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TERMINAL EXECUTION LOGS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = NeonEmerald
                            )
                            IconButton(
                                onClick = { viewModel.runActiveCode() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Rerun", tint = Color.Gray)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = executionOutput,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color(0xFF7EE787),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }

    if (showAddFileDialog) {
        CreateFileModal(
            onDismiss = { showAddFileDialog = false },
            onCreate = { name, lang ->
                viewModel.createFile(name, lang)
                showAddFileDialog = false
            }
        )
    }
}

@Composable
fun CreateProjectModal(
    onDismiss: () -> Unit,
    onCreate: (title: String, desc: String, lang: String, fw: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var lang by remember { mutableStateOf("Kotlin") }
    var fw by remember { mutableStateOf("Jetpack Compose") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Code Project") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Project Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lang,
                    onValueChange = { lang = it },
                    label = { Text("Primary Language") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = fw,
                    onValueChange = { fw = it },
                    label = { Text("Framework / Library") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) onCreate(title, desc, lang, fw)
                },
                enabled = title.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CreateFileModal(
    onDismiss: () -> Unit,
    onCreate: (fileName: String, language: String) -> Unit
) {
    var fileName by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("Kotlin") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Source File") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("File Name (e.g. Utils.kt, app.py)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = language,
                    onValueChange = { language = it },
                    label = { Text("Language") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fileName.isNotBlank()) onCreate(fileName, language)
                },
                enabled = fileName.isNotBlank()
            ) {
                Text("Add File")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
