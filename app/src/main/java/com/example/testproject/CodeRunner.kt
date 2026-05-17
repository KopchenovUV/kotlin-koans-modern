package com.example.testproject // ← Твой пакет!

object CodeRunner {

    fun runCode(sourceCode: String): String {
        return KotlinCompiler.executeCode(sourceCode)
    }
}