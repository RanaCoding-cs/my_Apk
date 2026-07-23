package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val language: String,
    val framework: String,
    val starsCount: Int = 0,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "code_files")
data class CodeFileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val fileName: String,
    val language: String,
    val content: String,
    val isExecutable: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "snippets")
data class SnippetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val language: String,
    val category: String, // Algorithms, UI, Database, Auth, Utils, Network
    val code: String,
    val explanation: String,
    val tagsCsv: String,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "vault_items")
data class VaultItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // API_KEY, PASSWORD, SSH_KEY, SECRET_NOTE, ENCODED_TOKEN
    val encryptedData: String,
    val ivBase64: String,
    val hint: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "feed_posts")
data class FeedPostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorName: String,
    val authorRole: String,
    val content: String,
    val codeSnippet: String = "",
    val language: String = "",
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Long = 1,
    val name: String,
    val title: String,
    val bio: String,
    val location: String,
    val skillsCsv: String,
    val githubUrl: String,
    val linkedinUrl: String,
    val twitterUrl: String,
    val websiteUrl: String,
    val email: String,
    val isAvailableForHire: Boolean = true
)
