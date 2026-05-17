package com.example.testproject // ← Замени на свой пакет!

import java.io.ByteArrayOutputStream
import java.io.PrintStream

object KotlinCompiler {

    fun executeCode(code: String): String {
        val originalOut = System.out
        val originalErr = System.err

        val outputStream = ByteArrayOutputStream()
        val printStream = PrintStream(outputStream, true, "UTF-8")

        System.setOut(printStream)
        System.setErr(printStream)

        try {
            val interpreter = SimpleInterpreter()
            interpreter.execute(code)
        } catch (e: Exception) {
            System.err.println("Ошибка: ${e.message}")
        } finally {
            System.out.flush()
            System.err.flush()
            System.setOut(originalOut)
            System.setErr(originalErr)
        }

        return outputStream.toString("UTF-8").trim()
    }
}

class SimpleInterpreter {
    private val variables = mutableMapOf<String, Any?>()

    fun execute(code: String) {
        val lines = code.trim().lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("//") }

        executeLines(lines)
    }

    private fun executeLines(lines: List<String>) {
        var i = 0
        while (i < lines.size) {
            i = processLine(lines, i)
        }
    }

    private fun processLine(lines: List<String>, index: Int): Int {
        val line = lines[index]

        when {
            // ========== println ==========
            line.startsWith("println(") -> {
                val content = extractParens(line, "println(")
                val value = eval(content)
                println(value)
            }

            // ========== val / var ==========
            line.startsWith("val ") || line.startsWith("var ") -> {
                val prefix = if (line.startsWith("val ")) "val " else "var "
                val rest = line.removePrefix(prefix)
                val eqIndex = rest.indexOf('=')
                if (eqIndex > 0) {
                    val name = rest.substring(0, eqIndex).trim()
                    val valueStr = rest.substring(eqIndex + 1).trim()
                    variables[name] = eval(valueStr)
                }
            }

            // ========== for ==========
            // ========== for ==========
            line.startsWith("for (") -> {
                val header = extractParens(line, "for (")
                val parts = header.split(" in ")
                if (parts.size == 2) {
                    val varName = parts[0].trim()
                    val rangeStr = parts[1].trim()
                    val rangeParts = rangeStr.split("..")
                    if (rangeParts.size == 2) {
                        val start = eval(rangeParts[0])?.toString()?.toIntOrNull() ?: 0
                        val end = eval(rangeParts[1])?.toString()?.toIntOrNull() ?: 0

                        val bodyLines = collectBlock(lines, index)
                        val linesUsed = bodyLines.size
                        val innerLines = extractInnerLines(bodyLines)

                        for (value in start..end) {
                            variables[varName] = value
                            executeLines(innerLines)
                        }

                        return index + linesUsed
                    }
                }
            }

            // ========== if / else ==========
            line.startsWith("if (") -> {
                val condition = extractParens(line, "if (")
                val condResult = eval(condition)
                val condBool = when (condResult) {
                    is Boolean -> condResult
                    is Number -> condResult.toInt() != 0
                    is String -> condResult.isNotEmpty()
                    else -> true
                }

                // Собираем блок if
                val ifBlock = collectBlock(lines, index)
                val ifInner = extractInnerLines(ifBlock)
                var totalLines = ifBlock.size

                // Проверяем else
                val elseStart = index + totalLines
                var elseInner = emptyList<String>()

                if (elseStart < lines.size) {
                    val elseLine = lines[elseStart]
                    if (elseLine.startsWith("else {")) {
                        val elseBlock = collectBlock(lines, elseStart)
                        elseInner = extractInnerLines(elseBlock)
                        totalLines += elseBlock.size
                    } else if (elseLine.startsWith("else ")) {
                        elseInner = listOf(elseLine.removePrefix("else ").trim())
                        totalLines += 1
                    }
                }

                // Выполняем только одну ветку
                if (condBool) {
                    executeLines(ifInner)
                } else if (elseInner.isNotEmpty()) {
                    executeLines(elseInner)
                }

                return index + totalLines
            }
        }

        return index + 1
    }

    /**
     * Собирает все строки блока (включая открывающую строку и закрывающую скобку)
     * Например, для if (...) { ... } вернёт [if (...) {, ..., }]
     */

    private fun collectBlock(lines: List<String>, startIndex: Int): List<String> {
        val result = mutableListOf<String>()
        var depth = 0
        var started = false

        for (i in startIndex until lines.size) {
            val line = lines[i]
            result.add(line)

            for (ch in line) {
                when (ch) {
                    '{' -> {
                        depth++
                        started = true
                    }
                    '}' -> depth--
                }
            }

            if (started && depth == 0) {
                break
            }
        }

        return result
    }

    /**
     * Извлекает внутренние строки блока (без скобок)
     */
    private fun extractInnerLines(block: List<String>): List<String> {
        if (block.isEmpty()) return emptyList()

        val inner = mutableListOf<String>()

        // Обрабатываем первую строку
        val firstLine = block.first()
        val firstBraceIndex = firstLine.indexOf('{')
        if (firstBraceIndex >= 0) {
            val afterFirstBrace = firstLine.substring(firstBraceIndex + 1).trim()
            if (afterFirstBrace.isNotEmpty() && afterFirstBrace != "}") {
                inner.add(afterFirstBrace)
            }
        }

        // Если блок состоит из одной строки: if (x) { println("yes") }
        if (block.size == 1) {
            val closeBraceIndex = firstLine.lastIndexOf('}')
            if (closeBraceIndex > firstBraceIndex) {
                val content = firstLine.substring(firstBraceIndex + 1, closeBraceIndex).trim()
                if (content.isNotEmpty()) {
                    return listOf(content)
                }
            }
            return inner
        }

        // Средние строки
        for (i in 1 until block.size - 1) {
            val line = block[i].trim()
            if (line.isNotEmpty() && line != "{" && line != "}") {
                inner.add(line)
            }
        }

        // Последняя строка
        if (block.size >= 2) {
            val lastLine = block.last()
            val closeBraceIndex = lastLine.indexOf('}')
            if (closeBraceIndex > 0) {
                val beforeClose = lastLine.substring(0, closeBraceIndex).trim()
                if (beforeClose.isNotEmpty()) {
                    inner.add(beforeClose)
                }
            }
        }

        return inner
    }

    private fun extractParens(line: String, prefix: String): String {
        val start = line.indexOf(prefix) + prefix.length
        var depth = 0
        for (i in start until line.length) {
            when (line[i]) {
                '(' -> depth++
                ')' -> {
                    if (depth == 0) return line.substring(start, i)
                    depth--
                }
            }
        }
        return line.substring(start).removeSuffix(")")
    }

    private fun eval(expr: String): Any? {
        val trimmed = expr.trim()

        // Строка
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            var str = trimmed.substring(1, trimmed.length - 1)
            val regex = Regex("\\$\\{([^}]+)\\}")
            str = regex.replace(str) { match ->
                eval(match.groupValues[1])?.toString() ?: ""
            }
            for ((name, value) in variables) {
                str = str.replace("\$$name", value.toString())
            }
            return str
        }

        // Числа
        trimmed.toIntOrNull()?.let { return it }
        trimmed.toDoubleOrNull()?.let { return it }

        // Переменная
        variables[trimmed]?.let { return it }

        // Операции
        val ops = listOf("==", "+", "-", "*", "/", ">", "<")
        for (op in ops) {
            val idx = findOpIndex(trimmed, op)
            if (idx > 0) {
                val left = eval(trimmed.substring(0, idx).trim())
                val right = eval(trimmed.substring(idx + op.length).trim())
                return calc(left, right, op)
            }
        }

        return null
    }

    private fun findOpIndex(expr: String, op: String): Int {
        var depth = 0
        var inString = false
        for (i in expr.indices) {
            when (expr[i]) {
                '"' -> inString = !inString
                '(' -> if (!inString) depth++
                ')' -> if (!inString) depth--
            }
            if (depth == 0 && !inString && i > 0) {
                if (i + op.length <= expr.length && expr.substring(i, i + op.length) == op) {
                    return i
                }
            }
        }
        return -1
    }

    private fun calc(left: Any?, right: Any?, op: String): Any? {
        return when (op) {
            "+" -> {
                if (left is Number && right is Number) toNum(left.toDouble() + right.toDouble())
                else "${left}${right}"
            }
            "-" -> {
                if (left is Number && right is Number) toNum(left.toDouble() - right.toDouble())
                else null
            }
            "*" -> {
                if (left is Number && right is Number) toNum(left.toDouble() * right.toDouble())
                else null
            }
            "/" -> {
                if (left is Number && right is Number && right.toDouble() != 0.0)
                    toNum(left.toDouble() / right.toDouble())
                else null
            }
            ">" -> {
                if (left is Number && right is Number) left.toDouble() > right.toDouble()
                else false
            }
            "<" -> {
                if (left is Number && right is Number) left.toDouble() < right.toDouble()
                else false
            }
            "==" -> left == right || left?.toString() == right?.toString()
            else -> null
        }
    }

    private fun toNum(value: Double): Number {
        return if (value == value.toLong().toDouble()) value.toLong() else value
    }
}