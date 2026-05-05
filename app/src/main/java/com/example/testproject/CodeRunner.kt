package com.example.testproject // ← Твой пакет!

/**
 * Простой "исполнитель" кода для демонстрации.
 * В реальном приложении здесь был бы компилятор Kotlin.
 * Пока что мы просто показываем, что код "выполнен успешно".
 */
object CodeRunner {

    /**
     * Принимает код пользователя и возвращает результат "выполнения".
     * В демо-режиме просто выводит сам код и сообщение об успехе.
     */
    fun runCode(sourceCode: String): String {
        // Эмулируем выполнение: просто показываем, что код принят
        val lines = sourceCode.lines()
        val printlnLines = lines.filter { it.contains("println(") }

        if (printlnLines.isEmpty()) {
            return "Код принят. Добавьте println() для вывода результата."
        }

        // Извлекаем то, что должно быть выведено
        val output = printlnLines.joinToString("\n") { line ->
            val start = line.indexOf("println(\"") + 10
            val end = line.indexOf("\")", start)
            if (start >= 10 && end > start) {
                line.substring(start, end)
            } else {
                // Для случаев с переменными просто показываем строку
                line.substringAfter("println(").substringBefore(")")
            }
        }

        return "Выполнено!\nВывод:\n$output"
    }
}