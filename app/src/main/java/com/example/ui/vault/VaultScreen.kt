package com.example.ui.vault

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VaultItemEntity
import com.example.security.VaultCryptoManager
import com.example.ui.WorkspaceViewModel
import com.example.ui.theme.NeonCoral
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    viewModel: WorkspaceViewModel
) {
    val isUnlocked by viewModel.isVaultUnlocked.collectAsState()
    val vaultItems by viewModel.vaultItems.collectAsState()
    val unlockError by viewModel.vaultUnlockError.collectAsState()

    var enteredPin by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddSecretDialog by remember { mutableStateOf(false) }
    var showRecoveryDialog by remember { mutableStateOf(false) }

    val categories = listOf("All", "API_KEY", "PASSWORD", "SSH_KEY", "SECRET_NOTE")

    if (!isUnlocked) {
        // Locked State: 4-Digit PIN Lock Keypad Screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0D1117),
                            Color(0xFF030712)
                        )
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Lock Badge
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(NeonPurple.copy(alpha = 0.15f))
                        .border(2.dp, NeonPurple, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = "Vault Locked",
                        tint = NeonPurple,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "AES-256 Private Vault",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Text(
                    text = "Enter your 4-digit master PIN to decrypt credentials",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // PIN Bullets Indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { index ->
                        val isFilled = index < enteredPin.length
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(if (isFilled) NeonCyan else Color.DarkGray)
                                .border(1.dp, if (isFilled) NeonCyan else Color.Gray, CircleShape)
                        )
                    }
                }

                if (unlockError != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = unlockError!!,
                        color = NeonCoral,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 4x3 Keypad
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("BIO", "0", "DEL")
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    keys.forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            row.forEach { key ->
                                KeypadButton(
                                    key = key,
                                    onClick = {
                                        when (key) {
                                            "DEL" -> {
                                                if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
                                            }
                                            "BIO" -> {
                                                // Biometric Auth Simulation
                                                viewModel.verifyPin("1234")
                                            }
                                            else -> {
                                                if (enteredPin.length < 4) {
                                                    enteredPin += key
                                                    if (enteredPin.length == 4) {
                                                        val success = viewModel.verifyPin(enteredPin)
                                                        if (!success) enteredPin = ""
                                                    }
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                TextButton(onClick = { showRecoveryDialog = true }) {
                    Text("Forgot PIN? Security Recovery", color = NeonCyan)
                }
            }
        }
    } else {
        // Unlocked State: Vault Credentials Screen
        val filteredItems = vaultItems.filter { item ->
            selectedCategory == "All" || item.category == selectedCategory
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Encrypted Vault",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "AES-256 / SHA-256 Hardware Keys",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                color = NeonEmerald
                            )
                        }
                    },
                    actions = {
                        Button(
                            onClick = { viewModel.lockVault() },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCoral)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = "Lock Vault", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Lock Vault")
                        }
                    }
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { showAddSecretDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add Secret") },
                    text = { Text("Add Secret") },
                    containerColor = NeonCyan,
                    contentColor = Color.Black
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Category Chips Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.replace("_", " ")) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        VaultCardItem(
                            item = item,
                            enteredPin = enteredPin.ifEmpty { "1234" },
                            onDelete = { viewModel.deleteVaultItem(item) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    if (showAddSecretDialog) {
        AddVaultItemModal(
            onDismiss = { showAddSecretDialog = false },
            onSave = { title, cat, secret, hint ->
                viewModel.addVaultItem(title, cat, secret, enteredPin.ifEmpty { "1234" }, hint)
                showAddSecretDialog = false
            }
        )
    }

    if (showRecoveryDialog) {
        SecurityRecoveryModal(
            onDismiss = { showRecoveryDialog = false },
            onResetSuccess = {
                viewModel.verifyPin("1234")
                showRecoveryDialog = false
            }
        )
    }
}

@Composable
fun KeypadButton(
    key: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color(0xFF161B22),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF30363D)),
        modifier = Modifier.size(68.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            when (key) {
                "BIO" -> Icon(Icons.Outlined.Fingerprint, contentDescription = "Biometric Scan", tint = NeonCyan)
                "DEL" -> Icon(Icons.Default.Backspace, contentDescription = "Delete", tint = Color.Gray)
                else -> Text(
                    text = key,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun VaultCardItem(
    item: VaultItemEntity,
    enteredPin: String,
    onDelete: () -> Unit
) {
    var isRevealed by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val decryptedText = remember(isRevealed) {
        if (isRevealed) {
            VaultCryptoManager.decrypt(item.encryptedData, item.ivBase64, enteredPin)
        } else ""
    }

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
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = NeonCyan
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = NeonPurple.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = item.category.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = NeonPurple,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Value preview box
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF0D1117),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF30363D)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isRevealed) decryptedText else "••••••••••••••••••••••••",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = if (isRevealed) Color(0xFF7EE787) else Color.Gray,
                        modifier = Modifier.weight(1f)
                    )

                    Row {
                        IconButton(onClick = { isRevealed = !isRevealed }) {
                            Icon(
                                imageVector = if (isRevealed) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = "Toggle Visibility",
                                tint = NeonCyan
                            )
                        }

                        if (isRevealed) {
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Decrypted Credential", decryptedText)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Decrypted secret copied!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy", tint = NeonEmerald)
                            }
                        }
                    }
                }
            }

            if (item.hint.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Hint: ${item.hint}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AddVaultItemModal(
    onDismiss: () -> Unit,
    onSave: (title: String, category: String, plainSecret: String, hint: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("API_KEY") }
    var plainSecret by remember { mutableStateOf("") }
    var hint by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Encrypted Vault Secret") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title / Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (API_KEY, PASSWORD, SSH_KEY, etc.)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = plainSecret,
                    onValueChange = { plainSecret = it },
                    label = { Text("Plain Secret / Password Payload") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = hint,
                    onValueChange = { hint = it },
                    label = { Text("Recovery Hint") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && plainSecret.isNotBlank()) {
                        onSave(title, category, plainSecret, hint)
                    }
                },
                enabled = title.isNotBlank() && plainSecret.isNotBlank()
            ) {
                Text("Encrypt & Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun SecurityRecoveryModal(
    onDismiss: () -> Unit,
    onResetSuccess: () -> Unit
) {
    var answer by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Vault Security Recovery") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Security Question:")
                Text(
                    "\"What was your first programming language?\"",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = NeonCyan
                )
                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    label = { Text("Answer") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorText != null) {
                    Text(errorText!!, color = NeonCoral, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (answer.trim().lowercase() == "kotlin" || answer.trim().lowercase() == "python") {
                        onResetSuccess()
                    } else {
                        errorText = "Incorrect answer. Hint: Kotlin or Python"
                    }
                }
            ) {
                Text("Recover Vault Access")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
