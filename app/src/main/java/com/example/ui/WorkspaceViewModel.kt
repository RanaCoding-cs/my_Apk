package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.editor.CodeExecutionEngine
import com.example.security.VaultCryptoManager
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WorkspaceViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = WorkspaceRepository(db)

    // Theme Mode State
    private val _themeMode = MutableStateFlow(ThemeMode.CYBER_DARK)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    // User Profile Flow
    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun updateProfile(profile: UserProfileEntity) {
        viewModelScope.launch {
            repository.updateProfile(profile)
        }
    }

    // Projects & Code Editor State
    val allProjects: StateFlow<List<ProjectEntity>> = repository.allProjects.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedProject = MutableStateFlow<ProjectEntity?>(null)
    val selectedProject: StateFlow<ProjectEntity?> = _selectedProject.asStateFlow()

    private val _currentFiles = MutableStateFlow<List<CodeFileEntity>>(emptyList())
    val currentFiles: StateFlow<List<CodeFileEntity>> = _currentFiles.asStateFlow()

    private val _selectedFile = MutableStateFlow<CodeFileEntity?>(null)
    val selectedFile: StateFlow<CodeFileEntity?> = _selectedFile.asStateFlow()

    private val _activeCodeContent = MutableStateFlow("")
    val activeCodeContent: StateFlow<String> = _activeCodeContent.asStateFlow()

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    private val _executionOutput = MutableStateFlow("")
    val executionOutput: StateFlow<String> = _executionOutput.asStateFlow()

    fun selectProject(project: ProjectEntity) {
        _selectedProject.value = project
        viewModelScope.launch {
            repository.getFilesForProject(project.id).collect { files ->
                _currentFiles.value = files
                if (files.isNotEmpty() && _selectedFile.value == null) {
                    selectFile(files.first())
                }
            }
        }
    }

    fun selectFile(file: CodeFileEntity) {
        _selectedFile.value = file
        _activeCodeContent.value = file.content
        _executionOutput.value = ""
    }

    fun updateCodeContent(newContent: String) {
        _activeCodeContent.value = newContent
    }

    fun toggleEditMode() {
        _isEditMode.value = !_isEditMode.value
    }

    fun saveActiveFileContent() {
        val file = _selectedFile.value ?: return
        val updated = file.copy(
            content = _activeCodeContent.value,
            updatedAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.updateFile(updated)
            _selectedFile.value = updated
        }
    }

    fun runActiveCode() {
        val file = _selectedFile.value ?: return
        val codeToRun = _activeCodeContent.value
        val result = CodeExecutionEngine.execute(codeToRun, file.language)
        _executionOutput.value = result.output
    }

    fun createProject(title: String, description: String, language: String, framework: String) {
        viewModelScope.launch {
            val proj = ProjectEntity(
                title = title,
                description = description,
                language = language,
                framework = framework
            )
            val projId = repository.insertProject(proj)
            // Create default file for project
            val ext = when (language.lowercase()) {
                "python" -> "py"
                "sql" -> "sql"
                "dart" -> "dart"
                "json" -> "json"
                else -> "kt"
            }
            val defaultFile = CodeFileEntity(
                projectId = projId,
                fileName = "main.$ext",
                language = language,
                content = "// Welcome to $title ($language)\n\nfun main() {\n    println(\"Hello DevWorkspace!\")\n}"
            )
            repository.insertFile(defaultFile)
        }
    }

    fun createFile(fileName: String, language: String) {
        val proj = _selectedProject.value ?: return
        viewModelScope.launch {
            val newFile = CodeFileEntity(
                projectId = proj.id,
                fileName = fileName,
                language = language,
                content = "// New file: $fileName"
            )
            repository.insertFile(newFile)
        }
    }

    fun deleteFile(file: CodeFileEntity) {
        viewModelScope.launch {
            repository.deleteFile(file)
            if (_selectedFile.value?.id == file.id) {
                _selectedFile.value = null
                _activeCodeContent.value = ""
            }
        }
    }

    // Snippets Notepad
    private val _selectedSnippetCategory = MutableStateFlow("All")
    val selectedSnippetCategory: StateFlow<String> = _selectedSnippetCategory.asStateFlow()

    val snippets: StateFlow<List<SnippetEntity>> = _selectedSnippetCategory.flatMapLatest { cat ->
        repository.getSnippetsByCategory(cat)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectSnippetCategory(category: String) {
        _selectedSnippetCategory.value = category
    }

    fun addSnippet(title: String, language: String, category: String, code: String, explanation: String, tags: String) {
        viewModelScope.launch {
            val snippet = SnippetEntity(
                title = title,
                language = language,
                category = category,
                code = code,
                explanation = explanation,
                tagsCsv = tags
            )
            repository.insertSnippet(snippet)
        }
    }

    fun deleteSnippet(snippet: SnippetEntity) {
        viewModelScope.launch {
            repository.deleteSnippet(snippet)
        }
    }

    // Vault & Security State
    private val _isVaultUnlocked = MutableStateFlow(false)
    val isVaultUnlocked: StateFlow<Boolean> = _isVaultUnlocked.asStateFlow()

    private val _masterPinHash = MutableStateFlow(VaultCryptoManager.hashPin("1234"))
    val masterPinHash: StateFlow<String> = _masterPinHash.asStateFlow()

    private val _vaultUnlockError = MutableStateFlow<String?>(null)
    val vaultUnlockError: StateFlow<String?> = _vaultUnlockError.asStateFlow()

    val vaultItems: StateFlow<List<VaultItemEntity>> = repository.allVaultItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun verifyPin(pin: String): Boolean {
        val enteredHash = VaultCryptoManager.hashPin(pin)
        return if (enteredHash == _masterPinHash.value) {
            _isVaultUnlocked.value = true
            _vaultUnlockError.value = null
            true
        } else {
            _vaultUnlockError.value = "Incorrect 4-digit PIN"
            false
        }
    }

    fun resetPin(oldPin: String, newPin: String): Boolean {
        if (verifyPin(oldPin)) {
            _masterPinHash.value = VaultCryptoManager.hashPin(newPin)
            return true
        }
        return false
    }

    fun lockVault() {
        _isVaultUnlocked.value = false
    }

    fun addVaultItem(title: String, category: String, plainSecret: String, pin: String, hint: String) {
        viewModelScope.launch {
            val encrypted = VaultCryptoManager.encrypt(plainSecret, pin)
            val item = VaultItemEntity(
                title = title,
                category = category,
                encryptedData = encrypted.cipherTextBase64,
                ivBase64 = encrypted.ivBase64,
                hint = hint
            )
            repository.insertVaultItem(item)
        }
    }

    fun deleteVaultItem(item: VaultItemEntity) {
        viewModelScope.launch {
            repository.deleteVaultItem(item)
        }
    }

    // Social Feed
    val feedPosts: StateFlow<List<FeedPostEntity>> = repository.allPosts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun createFeedPost(content: String, codeSnippet: String, language: String) {
        val profile = userProfile.value
        val authorName = profile?.name ?: "Dev Workspace"
        val authorRole = profile?.title ?: "Full Stack Engineer"

        viewModelScope.launch {
            val post = FeedPostEntity(
                authorName = authorName,
                authorRole = authorRole,
                content = content,
                codeSnippet = codeSnippet,
                language = language,
                timestamp = System.currentTimeMillis()
            )
            repository.insertPost(post)
        }
    }

    fun toggleLikePost(post: FeedPostEntity) {
        viewModelScope.launch {
            val updated = post.copy(
                isLiked = !post.isLiked,
                likesCount = if (post.isLiked) post.likesCount - 1 else post.likesCount + 1
            )
            repository.updatePost(updated)
        }
    }
}
