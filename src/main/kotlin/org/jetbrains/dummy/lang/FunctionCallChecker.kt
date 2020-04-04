package org.jetbrains.dummy.lang

import org.jetbrains.dummy.lang.tree.DummyLangVisitor
import org.jetbrains.dummy.lang.tree.Element
import org.jetbrains.dummy.lang.tree.File
import org.jetbrains.dummy.lang.tree.FunctionCall

class FunctionCallChecker(private val reporter: DiagnosticReporter) : AbstractChecker() {
    private inner class FunctionCallVisitor : DummyLangVisitor<Unit, Set<Pair<String, Int>>>() {
        override fun visitElement(element: Element, data: Set<Pair<String, Int>>) {
            element.acceptChildren(this, data)
        }

        override fun visitFunctionCall(functionCall: FunctionCall, data: Set<Pair<String, Int>>) {
            super.visitFunctionCall(functionCall, data)

            val functionInfo = Pair(functionCall.function, functionCall.arguments.size)
            if (!data.contains(functionInfo)) {
                reportIncorrectFunctionCall(functionCall)
            }
        }
    }

    override fun inspect(file: File) {
        file.accept(FunctionCallVisitor(), file.functions.map { Pair(it.name, it.parameters.size) }.toSet())
    }

    private fun reportIncorrectFunctionCall(functionCall: FunctionCall) {
        reporter.report(
            functionCall,
            "Incorrect function call: there is no function named '${functionCall.function}' with ${functionCall.arguments.size} arguments"
        )
    }
}