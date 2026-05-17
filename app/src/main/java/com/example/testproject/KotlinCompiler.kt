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

    /**
     * Находит конец всего if/else блока (включая else если есть)
     */
    private fun findFullIfElseEnd(lines: List<String>, startIndex: Int): Int {
        var depth = 0
        var foundElse = false

        for (i in startIndex until lines.size) {
            val line = lines[i]

            for (j in line.indices) {
                when (line[j]) {
                    '{' -> depth++
                    '}' -> {
                        depth--
                        if (depth == 0) {
                            // Проверяем, есть ли else после этой }
                            val after = line.substring(j + 1).trim()
                            if (after.startsWith("else") && after.contains('{')) {
                                // Есть else блок — продолжаем
                                depth = 1
                                foundElse = true
                            } else {
                                // Нет else — это конец
                                return i
                            }
                        }
                    }
                }
            }
        }

        return lines.size - 1
    }

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
                return index + 1
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
                return index + 1
            }

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

                        val endIndex = findBlockEnd(lines, index)
                        val innerLines = getBlockContent(lines, index, endIndex)

                        for (value in start..end) {
                            variables[varName] = value
                            executeLines(innerLines)
                        }

                        return endIndex + 1
                    }
                }
                return index + 1
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

                val blockEnd = findFullIfElseEnd(lines, index)
                val ifInner = mutableListOf<String>()
                val elseInner = mutableListOf<String>()
                var inElse = false

                for (i in index..blockEnd) {
                    val l = lines[i]

                    if (i == index) {
                        val braceIdx = l.indexOf('{')
                        if (braceIdx >= 0) {
                            val after = l.substring(braceIdx + 1).trim()
                            if (after.isNotEmpty()) {
                                ifInner.add(after)
                            }
                        }
                    } else if (l.trim().startsWith("} else {") || l.trim() == "} else {" || l.trim().startsWith("else {")) {
                        inElse = true
                        val afterElse = l.trim().removePrefix("} else {").removePrefix("else {").trim()
                        if (afterElse.isNotEmpty() && afterElse != "}") {
                            elseInner.add(afterElse)
                        }
                    } else if (l.trim() == "}") {
                        continue
                    } else if (inElse) {
                        val clean = l.trim()
                        if (clean != "}" && clean.isNotEmpty()) {
                            elseInner.add(clean)
                        }
                    } else {
                        val clean = l.trim()
                        if (clean != "}" && clean.isNotEmpty()) {
                            ifInner.add(clean)
                        }
                    }
                }

                if (condBool) {
                    executeLines(ifInner)
                } else if (elseInner.isNotEmpty()) {
                    executeLines(elseInner)
                }

                return blockEnd + 1
            }
        }

        return index + 1
    }

    private fun findBlockEnd(lines: List<String>, startIndex: Int): Int {
        var depth = 0

        for (i in startIndex until lines.size) {
            val currentLine = lines[i]

            for (j in currentLine.indices) {
                when (currentLine[j]) {
                    '{' -> depth++
                    '}' -> {
                        depth--
                        if (depth == 0) {
                            // Проверяем, что после } нет { (как в "} else {")
                            val afterBrace = currentLine.substring(j + 1).trim()
                            if (afterBrace.startsWith("else") && afterBrace.contains('{')) {
                                // Это "} else {" — блок НЕ закончился
                                depth = 1
                            } else {
                                return i
                            }
                        }
                    }
                }
            }
        }

        return lines.size - 1
    }

    private fun getBlockContent(lines: List<String>, startIndex: Int, endIndex: Int): List<String> {
        val content = mutableListOf<String>()

        for (i in startIndex..endIndex) {
            val line = lines[i]

            if (i == startIndex) {
                val braceIdx = line.indexOf('{')
                if (braceIdx >= 0) {
                    var after = line.substring(braceIdx + 1).trim()
                    // Убираем } если она есть в той же строке
                    val closeIdx = after.indexOf('}')
                    if (closeIdx >= 0) {
                        after = after.substring(0, closeIdx).trim()
                    }
                    if (after.isNotEmpty()) {
                        content.add(after)
                    }
                }
            } else if (i == endIndex) {
                val braceIdx = line.indexOf('}')
                if (braceIdx > 0) {
                    var before = line.substring(0, braceIdx).trim()
                    // Убираем else если есть
                    if (before.startsWith("else {")) {
                        before = before.removePrefix("else {").trim()
                    }
                    if (before.isNotEmpty()) {
                        content.add(before)
                    }
                }
            } else {
                var cleanLine = line
                // Пропускаем строки, которые являются частью else
                if (cleanLine.trimStart().startsWith("else {")) {
                    cleanLine = cleanLine.trimStart().removePrefix("else {").trim()
                }
                if (cleanLine == "}") continue
                if (cleanLine.startsWith("} else")) continue
                if (cleanLine.isNotEmpty()) {
                    content.add(cleanLine)
                }
            }
        }

        return content
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