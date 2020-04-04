package org.jetbrains.dummy.lang

import org.jetbrains.dummy.lang.tree.*

class FunctionCallChecker(private val reporter: DiagnosticReporter) : AbstractChecker() {
    private inner class FunctionCallVisitor(private val declaredFunctions: Set<Pair<String, Int>>) :
        DummyLangVisitor<Unit, Any?>() {
        override fun visitElement(element: Element, data: Any?) {
            element.acceptChildren(this, data)
        }

        override fun visitFunctionCall(functionCall: FunctionCall, data: Any?) {
            super.visitFunctionCall(functionCall, data)

            val functionInfo = Pair(functionCall.function, functionCall.arguments.size)
            if (functionInfo !in declaredFunctions) {
                reportIncorrectFunctionCall(functionCall)
            }
        }
    }

    override fun inspect(file: File) {
        val declaredFunctions = mutableSetOf<Pair<String, Int>>()
        file.functions.forEach {
            val functionInfo = Pair(it.name, it.parameters.size)
            if (functionInfo in declaredFunctions) {
                reportRedeclaration(it)
            }
            declaredFunctions.add(functionInfo)
        }

        file.accept(FunctionCallVisitor(declaredFunctions), null)
    }

    private fun reportIncorrectFunctionCall(functionCall: FunctionCall) {
        reporter.report(
            functionCall,
            "Incorrect function call: there is no function named '${functionCall.function}' with ${functionCall.arguments.size} arguments"
        )
    }

    private fun reportRedeclaration(functionDeclaration: FunctionDeclaration) {
        reporter.report(
            functionDeclaration,
            "Function redeclaration: function named '${functionDeclaration.name}' with ${functionDeclaration.parameters.size} parameters is already declared"
        )
    }
}