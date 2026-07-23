package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProjectEntity::class,
        CodeFileEntity::class,
        SnippetEntity::class,
        VaultItemEntity::class,
        FeedPostEntity::class,
        UserProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun snippetDao(): SnippetDao
    abstract fun vaultDao(): VaultDao
    abstract fun feedDao(): FeedDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dev_workspace_db"
                )
                .addCallback(DatabaseCallback(context.applicationContext))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val appDb = getDatabase(context)
                    seedInitialData(appDb)
                }
            }
        }

        private suspend fun seedInitialData(db: AppDatabase) {
            // Seed User Profile
            db.userProfileDao().insertOrUpdateProfile(
                UserProfileEntity(
                    id = 1,
                    name = "Alex Mercer",
                    title = "Senior Full-Stack & Cyber Security Engineer",
                    bio = "Building high-performance distributed microservices, Kotlin Multiplatform desktop/mobile engines, and AES-256 encrypted vaults.",
                    location = "San Francisco, CA / Remote",
                    skillsCsv = "Kotlin, Python, Dart/Flutter, Rust, React, Docker, Cybersecurity, PostgreSQL, Room, Jetpack Compose",
                    githubUrl = "https://github.com/alexmercer-dev",
                    linkedinUrl = "https://linkedin.com/in/alexmercer-dev",
                    twitterUrl = "https://x.com/alexmercer_code",
                    websiteUrl = "https://alexmercer.dev",
                    email = "alex.mercer@devworkspace.io",
                    isAvailableForHire = true
                )
            )

            // Seed Projects & Files
            val proj1Id = db.projectDao().insertProject(
                ProjectEntity(
                    title = "Neural Cipher Vault",
                    description = "AES-256 encrypted storage engine with biometric key derivation and Kotlin Coroutines concurrency.",
                    language = "Kotlin",
                    framework = "Jetpack Compose & Room",
                    starsCount = 142,
                    isFavorite = true
                )
            )
            db.projectDao().insertFile(
                CodeFileEntity(
                    projectId = proj1Id,
                    fileName = "CipherEngine.kt",
                    language = "Kotlin",
                    content = """
                        package com.cipher.vault

                        import javax.crypto.Cipher
                        import javax.crypto.spec.SecretKeySpec
                        import javax.crypto.spec.IvParameterSpec
                        import java.security.MessageDigest
                        import android.util.Base64

                        object CipherEngine {
                            private const val ALGORITHM = "AES/CBC/PKCS5Padding"

                            fun encrypt(plainText: String, secretKey: String): Pair<String, String> {
                                val keyBytes = MessageDigest.getInstance("SHA-256").digest(secretKey.toByteArray())
                                val keySpec = SecretKeySpec(keyBytes, "AES")
                                val cipher = Cipher.getInstance(ALGORITHM)
                                cipher.init(Cipher.ENCRYPT_MODE, keySpec)
                                val encryptedBytes = cipher.doFinal(plainText.toByteArray())
                                val iv = cipher.iv
                                return Pair(
                                    Base64.encodeToString(encryptedBytes, Base64.NO_WRAP),
                                    Base64.encodeToString(iv, Base64.NO_WRAP)
                                )
                            }

                            fun main() {
                                val (encrypted, iv) = encrypt("Secret API Token 99283", "MyMasterPin1234")
                                println("Encrypted Result: " + encrypted)
                                println("IV Token: " + iv)
                            }
                        }
                    """.trimIndent()
                )
            )

            val proj2Id = db.projectDao().insertProject(
                ProjectEntity(
                    title = "Quant Algorithmic Bot",
                    description = "Python async pipeline for real-time market order evaluation & moving average signals.",
                    language = "Python",
                    framework = "AsyncIO & NumPy",
                    starsCount = 89,
                    isFavorite = true
                )
            )
            db.projectDao().insertFile(
                CodeFileEntity(
                    projectId = proj2Id,
                    fileName = "quant_strategy.py",
                    language = "Python",
                    content = """
                        import time

                        def calculate_moving_average(prices, window=3):
                            if len(prices) < window:
                                return sum(prices) / len(prices)
                            return sum(prices[-window:]) / window

                        prices = [102.5, 104.0, 103.8, 106.2, 108.5, 110.1]
                        ma = calculate_moving_average(prices)

                        print("Current Moving Average (SMA3):", round(ma, 2))
                        if prices[-1] > ma:
                            print("Signal: BULLISH BUY ORDER DETECTED")
                        else:
                            print("Signal: HOLD / NEUTRAL")
                    """.trimIndent()
                )
            )

            val proj3Id = db.projectDao().insertProject(
                ProjectEntity(
                    title = "Flutter Cyber UI System",
                    description = "Cross-platform design token system for dark cyberpunk aesthetic dashboards.",
                    language = "Dart",
                    framework = "Flutter",
                    starsCount = 210,
                    isFavorite = false
                )
            )
            db.projectDao().insertFile(
                CodeFileEntity(
                    projectId = proj3Id,
                    fileName = "cyber_theme.dart",
                    language = "Dart",
                    content = """
                        import 'package:flutter/material.dart';

                        class CyberColors {
                          static const Color background = Color(0xFF0D1117);
                          static const Color neonCyan = Color(0xFF58A6FF);
                          static const Color neonPurple = Color(0xFFA371F7);
                        }

                        void main() {
                          print("Cyber Theme Initialized for Flutter App!");
                        }
                    """.trimIndent()
                )
            )

            // Seed Snippets
            db.snippetDao().insertSnippet(
                SnippetEntity(
                    title = "Kotlin Coroutines Debounce Flow",
                    language = "Kotlin",
                    category = "UI",
                    code = """
                        fun <T> Flow<T>.debounceState(timeoutMs: Long): Flow<T> = channelFlow {
                            var lastJob: Job? = null
                            collect { value ->
                                lastJob?.cancel()
                                lastJob = launch {
                                    delay(timeoutMs)
                                    send(value)
                                }
                            }
                        }
                    """.trimIndent(),
                    explanation = "Custom debounce extension function for reactive input fields.",
                    tagsCsv = "Kotlin, Coroutines, Flow, Search",
                    isFavorite = true
                )
            )

            db.snippetDao().insertSnippet(
                SnippetEntity(
                    title = "Python Binary Search Algorithm",
                    language = "Python",
                    category = "Algorithms",
                    code = """
                        def binary_search(arr, target):
                            low, high = 0, len(arr) - 1
                            while low <= high:
                                mid = (low + high) // 2
                                if arr[mid] == target:
                                    return mid
                                elif arr[mid] < target:
                                    low = mid + 1
                                else:
                                    high = mid - 1
                            return -1

                        print("Search Index:", binary_search([10, 20, 30, 40, 50], 30))
                    """.trimIndent(),
                    explanation = "Optimal O(log n) search algorithm in sorted arrays.",
                    tagsCsv = "Python, Algorithms, Search, DataStructures",
                    isFavorite = true
                )
            )

            db.snippetDao().insertSnippet(
                SnippetEntity(
                    title = "SQL Indexed CTE Page Query",
                    language = "SQL",
                    category = "Database",
                    code = """
                        WITH PaginatedResults AS (
                            SELECT id, title, created_at,
                                   ROW_NUMBER() OVER (ORDER BY created_at DESC) AS row_num
                            FROM projects
                            WHERE is_active = 1
                        )
                        SELECT * FROM PaginatedResults
                        WHERE row_num BETWEEN 1 AND 20;
                    """.trimIndent(),
                    explanation = "Efficient pagination query using Common Table Expressions and window functions.",
                    tagsCsv = "SQL, Database, Room, CTE, Performance",
                    isFavorite = false
                )
            )

            // Seed Social Feed
            db.feedDao().insertPost(
                FeedPostEntity(
                    authorName = "Alex Mercer",
                    authorRole = "Senior Architect",
                    content = "Just shipped the new AES-256 Vault engine with hardware-backed key derivation! Here is a snippet of the encryption handshake function in Kotlin:",
                    codeSnippet = "val cipher = Cipher.getInstance(\"AES/CBC/PKCS5Padding\")\ncipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)",
                    language = "Kotlin",
                    likesCount = 42,
                    commentsCount = 7,
                    isLiked = true,
                    timestamp = System.currentTimeMillis() - 3600000
                )
            )

            db.feedDao().insertPost(
                FeedPostEntity(
                    authorName = "Elena Rostova",
                    authorRole = "Lead UI/UX Engineer",
                    content = "Loving Jetpack Compose custom canvas animations for cyber dark themes. Particle shaders give code editors such a sleek feel!",
                    codeSnippet = "Modifier.drawBehind { drawCircle(brush = Brush.radialGradient(...)) }",
                    language = "Kotlin",
                    likesCount = 28,
                    commentsCount = 3,
                    isLiked = false,
                    timestamp = System.currentTimeMillis() - 14400000
                )
            )
        }
    }
}
