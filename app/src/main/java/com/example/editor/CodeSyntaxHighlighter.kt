package com.example.editor

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import com.example.ui.theme.*

object CodeSyntaxHighlighter {

    private val KOTLIN_KEYWORDS = setOf(
        "package", "import", "fun", "val", "var", "class", "object", "interface",
        "sealed", "data", "override", "public", "private", "protected", "internal",
        "if", "else", "when", "for", "while", "return", "try", "catch", "throw",
        "null", "true", "false", "suspend", "is", "as", "by", "init", "typealias"
    )

    private val PYTHON_KEYWORDS = setOf(
        "def", "class", "import", "from", "as", "return", "if", "elif", "else",
        "while", "for", "in", "and", "or", "not", "is", "lambda", "try", "except",
        "raise", "with", "yield", "pass", "None", "True", "False", "global", "async", "await"
    )

    private val SQL_KEYWORDS = setOf(
        "SELECT", "FROM", "WHERE", "JOIN", "LEFT", "RIGHT", "INNER", "OUTER", "ON",
        "GROUP", "BY", "ORDER", "HAVING", "LIMIT", "OFFSET", "INSERT", "INTO", "VALUES",
        "UPDATE", "SET", "DELETE", "CREATE", "TABLE", "INDEX", "WITH", "AS", "AND", "OR"
    )

    fun highlight(code: String, language: String): AnnotatedString {
        return buildAnnotatedString {
            append(code)

            val keywords = when (language.lowercase()) {
                "python" -> PYTHON_KEYWORDS
                "sql" -> SQL_KEYWORDS
                else -> KOTLIN_KEYWORDS
            }

            // Highlight Comments
            val lines = code.split("\n")
            var currentOffset = 0
            for (line in lines) {
                val commentIndex = line.indexOf("//").takeIf { it != -1 } ?: line.indexOf("#").takeIf { it != -1 }
                if (commentIndex != null) {
                    addStyle(
                        style = SpanStyle(color = CodeComment),
                        start = currentOffset + commentIndex,
                        end = currentOffset + line.length
                    )
                }
                currentOffset += line.length + 1
            }

            // Highlight Keywords & Strings
            val wordsRegex = Regex("""\b[A-Za-z_][A-Za-z0-9_]*\b|"[^"]*"|'[^']*'|\b\d+\b""")
            for (match in wordsRegex.findAll(code)) {
                val value = match.value
                val start = match.range.first
                val end = match.range.last + 1

                when {
                    keywords.contains(value) || keywords.contains(value.uppercase()) -> {
                        addStyle(SpanStyle(color = CodeKeyword), start, end)
                    }
                    value.startsWith("\"") || value.startsWith("'") -> {
                        addStyle(SpanStyle(color = CodeString), start, end)
                    }
                    value.all { it.isDigit() } -> {
                        addStyle(SpanStyle(color = CodeNumber), start, end)
                    }
                    code.getOrNull(end) == '(' -> {
                        addStyle(SpanStyle(color = CodeFunction), start, end)
                    }
                }
            }
        }
    }
}
