package com.example.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProfileEntity
import com.example.ui.WorkspaceViewModel
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.ThemeMode

@Composable
fun SettingsScreen(
    viewModel: WorkspaceViewModel
) {
    val profile by viewModel.userProfile.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    var showPinResetDialog by remember { mutableStateOf(false) }
    var showSocialEditDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Quick Connect & Settings",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Manage social links, appearance themes, and security settings.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }

        // Section 1: Appearance & Theme
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Palette, contentDescription = null, tint = NeonCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Appearance Theme",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeOptionChip(
                            label = "Cyber Dark",
                            isSelected = themeMode == ThemeMode.CYBER_DARK,
                            onClick = { viewModel.setThemeMode(ThemeMode.CYBER_DARK) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionChip(
                            label = "Light",
                            isSelected = themeMode == ThemeMode.LIGHT,
                            onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionChip(
                            label = "AMOLED",
                            isSelected = themeMode == ThemeMode.AMOLED_DARK,
                            onClick = { viewModel.setThemeMode(ThemeMode.AMOLED_DARK) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Section 2: Quick Connect Social Links
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Link, contentDescription = null, tint = NeonPurple)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Quick Connect Links",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        IconButton(onClick = { showSocialEditDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Links", tint = NeonCyan)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    SocialLinkRow(title = "GitHub", url = profile?.githubUrl ?: "https://github.com/alexmercer-dev")
                    SocialLinkRow(title = "LinkedIn", url = profile?.linkedinUrl ?: "https://linkedin.com/in/alexmercer-dev")
                    SocialLinkRow(title = "Twitter / X", url = profile?.twitterUrl ?: "https://x.com/alexmercer_code")
                    SocialLinkRow(title = "Portfolio Web", url = profile?.websiteUrl ?: "https://alexmercer.dev")
                }
            }
        }

        // Section 3: Vault Security Settings
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.LockReset, contentDescription = null, tint = NeonEmerald)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Security Settings",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showPinResetDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Change Master 4-Digit PIN")
                    }
                }
            }
        }

        // Section 4: Data Backup & Export
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Share, contentDescription = null, tint = NeonCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Workspace Data Export",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Export project files, snippets, and bio metadata to JSON.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            val backupJson = """
                                {
                                  "workspace": "DevWorkspace",
                                  "version": "2.4",
                                  "user": "${profile?.name}",
                                  "title": "${profile?.title}",
                                  "exported_at": "${System.currentTimeMillis()}"
                                }
                            """.trimIndent()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Workspace Backup", backupJson)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Workspace JSON exported to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Workspace JSON")
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showPinResetDialog) {
        ResetPinModal(
            onDismiss = { showPinResetDialog = false },
            onReset = { oldPin, newPin ->
                val success = viewModel.resetPin(oldPin, newPin)
                if (success) {
                    Toast.makeText(context, "Master PIN changed successfully!", Toast.LENGTH_SHORT).show()
                    showPinResetDialog = false
                } else {
                    Toast.makeText(context, "Incorrect current PIN", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    if (showSocialEditDialog && profile != null) {
        EditSocialLinksModal(
            profile = profile!!,
            onDismiss = { showSocialEditDialog = false },
            onSave = { updated ->
                viewModel.updateProfile(updated)
                showSocialEditDialog = false
            }
        )
    }
}

@Composable
fun ThemeOptionChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label, fontSize = 12.sp) },
        modifier = modifier
    )
}

@Composable
fun SocialLinkRow(
    title: String,
    url: String
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            Text(url, style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = NeonCyan)
        }

        IconButton(
            onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(title, url)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "$title URL copied!", Toast.LENGTH_SHORT).show()
            }
        ) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun ResetPinModal(
    onDismiss: () -> Unit,
    onReset: (oldPin: String, newPin: String) -> Unit
) {
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change 4-Digit Master PIN") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = oldPin,
                    onValueChange = { if (it.length <= 4) oldPin = it },
                    label = { Text("Current 4-Digit PIN") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { if (it.length <= 4) newPin = it },
                    label = { Text("New 4-Digit PIN") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (oldPin.length == 4 && newPin.length == 4) {
                        onReset(oldPin, newPin)
                    }
                },
                enabled = oldPin.length == 4 && newPin.length == 4
            ) {
                Text("Change PIN")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun EditSocialLinksModal(
    profile: UserProfileEntity,
    onDismiss: () -> Unit,
    onSave: (UserProfileEntity) -> Unit
) {
    var github by remember { mutableStateOf(profile.githubUrl) }
    var linkedin by remember { mutableStateOf(profile.linkedinUrl) }
    var twitter by remember { mutableStateOf(profile.twitterUrl) }
    var website by remember { mutableStateOf(profile.websiteUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Quick Connect Links") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = github,
                    onValueChange = { github = it },
                    label = { Text("GitHub URL") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = linkedin,
                    onValueChange = { linkedin = it },
                    label = { Text("LinkedIn URL") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = twitter,
                    onValueChange = { twitter = it },
                    label = { Text("Twitter / X URL") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = website,
                    onValueChange = { website = it },
                    label = { Text("Portfolio Website URL") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        profile.copy(
                            githubUrl = github,
                            linkedinUrl = linkedin,
                            twitterUrl = twitter,
                            websiteUrl = website
                        )
                    )
                }
            ) {
                Text("Save Links")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
