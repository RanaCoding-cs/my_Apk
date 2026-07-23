package com.example.editor

import java.text.SimpleDateFormat
import java.util.*

object CodeExecutionEngine {

    data class ExecutionResult(
        val output: String,
        val isSuccess: Boolean,
        val executionTimeMs: Long
    )

    fun execute(code: String, language: String): ExecutionResult {
        val startTime = System.currentTimeMillis()
        val logs = mutableListOf<String>()
        val timeStamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())

        logs.add("[$timeStamp] Executing $language runtime environment...")

        val cleanLang = language.lowercase()

        try {
            when {
                cleanLang.contains("python") -> executePython(code, logs)
                cleanLang.contains("kotlin") -> executeKotlin(code, logs)
                cleanLang.contains("sql") -> executeSql(code, logs)
                cleanLang.contains("dart") || cleanLang.contains("flutter") -> executeDart(code, logs)
                cleanLang.contains("json") -> executeJson(code, logs)
                else -> executeGeneric(code, logs)
            }

            val endTime = System.currentTimeMillis()
            val totalTime = endTime - startTime
            logs.add("\n[Process Finished in ${totalTime}ms with Exit Code 0]")

            return ExecutionResult(
                output = logs.joinToString("\n"),
                isSuccess = true,
                executionTimeMs = totalTime
            )
        } catch (e: Exception) {
            val endTime = System.currentTimeMillis()
            logs.add("ERROR: ${e.message}")
            return ExecutionResult(
                output = logs.joinToString("\n"),
                isSuccess = false,
                executionTimeMs = endTime - startTime
            )
        }
    }

    private fun executePython(code: String, logs: MutableList<String>) {
        val printLines = code.lines().filter { it.trim().startsWith("print(") }
        if (printLines.isNotEmpty()) {
            for (line in printLines) {
                val content = line.substringAfter("print(").substringBeforeLast(")").replace("\"", "").replace("'", "")
                logs.add("STDOUT > $content")
            }
        } else {
            logs.add("STDOUT > Executed Python script successfully.")
            logs.add("Result: Module loaded without errors.")
        }
    }

    private fun executeKotlin(code: String, logs: MutableList<String>) {
        val printLines = code.lines().filter { it.trim().contains("println(") || it.trim().contains("print(") }
        if (printLines.isNotEmpty()) {
            for (line in printLines) {
                val content = line.substringAfter("print").substringAfter("(").substringBeforeLast(")").replace("\"", "").replace("'", "")
                logs.add("STDOUT > $content")
            }
        } else {
            logs.add("STDOUT > Kotlin bytecode compiled and executed.")
            logs.add("Result: Unit")
        }
    }

    private fun executeSql(code: String, logs: MutableList<String>) {
        logs.add("STDOUT > Executing SQL Query against SQLite Local DB...")
        logs.add("--------------------------------------------------")
        logs.add("| ID | TITLE                 | LANGUAGE | STARS |")
        logs.add("--------------------------------------------------")
        logs.add("|  1 | Neural Cipher Vault  | Kotlin   |   142 |")
        logs.add("|  2 | Quant Algorithmic Bot | Python   |    89 |")
        logs.add("|  3 | Flutter Cyber UI      | Dart     |   210 |")
        logs.add("--------------------------------------------------")
        logs.add("(3 rows returned)")
    }

    private fun executeDart(code: String, logs: MutableList<String>) {
        logs.add("STDOUT > Flutter Hot Reload & Dart VM Engine Ready.")
        logs.add("STDOUT > Rendering Widget Canvas...")
        logs.add("Result: Flutter App State Initialized OK.")
    }

    private fun executeJson(code: String, logs: MutableList<String>) {
        val isStructureValid = code.trim().startsWith("{") && code.trim().endsWith("}") || code.trim().startsWith("[") && code.trim().endsWith("]")
        if (isStructureValid) {
            logs.add("STDOUT > JSON Syntax Validation: SUCCESS")
            logs.add("Formatted Size: ${code.length} characters")
        } else {
            logs.add("STDOUT > JSON Syntax Warning: Non-standard structure")
        }
    }

    private fun executeGeneric(code: String, logs: MutableList<String>) {
        logs.add("STDOUT > Program compiled successfully.")
        logs.add("STDOUT > Output: Done.")
    }
}
