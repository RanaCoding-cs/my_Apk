package com.example.data

import kotlinx.coroutines.flow.Flow

class WorkspaceRepository(private val db: AppDatabase) {

    // Projects & Files
    val allProjects: Flow<List<ProjectEntity>> = db.projectDao().getAllProjects()

    fun getFilesForProject(projectId: Long): Flow<List<CodeFileEntity>> =
        db.projectDao().getFilesForProject(projectId)

    suspend fun insertProject(project: ProjectEntity): Long = db.projectDao().insertProject(project)
    suspend fun updateProject(project: ProjectEntity) = db.projectDao().updateProject(project)
    suspend fun deleteProject(project: ProjectEntity) = db.projectDao().deleteProject(project)

    suspend fun insertFile(file: CodeFileEntity): Long = db.projectDao().insertFile(file)
    suspend fun updateFile(file: CodeFileEntity) = db.projectDao().updateFile(file)
    suspend fun deleteFile(file: CodeFileEntity) = db.projectDao().deleteFile(file)

    // Snippets
    val allSnippets: Flow<List<SnippetEntity>> = db.snippetDao().getAllSnippets()

    fun getSnippetsByCategory(category: String): Flow<List<SnippetEntity>> =
        if (category == "All") db.snippetDao().getAllSnippets()
        else db.snippetDao().getSnippetsByCategory(category)

    suspend fun insertSnippet(snippet: SnippetEntity) = db.snippetDao().insertSnippet(snippet)
    suspend fun updateSnippet(snippet: SnippetEntity) = db.snippetDao().updateSnippet(snippet)
    suspend fun deleteSnippet(snippet: SnippetEntity) = db.snippetDao().deleteSnippet(snippet)

    // Vault
    val allVaultItems: Flow<List<VaultItemEntity>> = db.vaultDao().getAllVaultItems()

    suspend fun insertVaultItem(item: VaultItemEntity) = db.vaultDao().insertVaultItem(item)
    suspend fun updateVaultItem(item: VaultItemEntity) = db.vaultDao().updateVaultItem(item)
    suspend fun deleteVaultItem(item: VaultItemEntity) = db.vaultDao().deleteVaultItem(item)

    // Feed
    val allPosts: Flow<List<FeedPostEntity>> = db.feedDao().getAllPosts()

    suspend fun insertPost(post: FeedPostEntity) = db.feedDao().insertPost(post)
    suspend fun updatePost(post: FeedPostEntity) = db.feedDao().updatePost(post)
    suspend fun deletePost(post: FeedPostEntity) = db.feedDao().deletePost(post)

    // User Profile
    val userProfile: Flow<UserProfileEntity?> = db.userProfileDao().getUserProfile()

    suspend fun updateProfile(profile: UserProfileEntity) = db.userProfileDao().insertOrUpdateProfile(profile)
}
