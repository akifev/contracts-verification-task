package org.jetbrains.dummy.lang

import org.jetbrains.dummy.lang.tree.*

class FunctionSignatureChecker(private val reporter: DiagnosticReporter) : AbstractChecker() {

    private inner class FunctionSignatureVisitor : DummyLangVisitor<Unit, Any?>() {
        override fun visitElement(element: Element, data: Any?) {
            element.acceptChildren(this, data)
        }

        override fun visitFunctionDeclaration(functionDeclaration: FunctionDeclaration, data: Any?) {
            val setOfNonConflicting = mutableSetOf<String>()
            functionDeclaration.parameters.forEach {
                if (setOfNonConflicting.contains(it)) {
                    setOfNonConflicting.remove(it)
                } else {
                    setOfNonConflicting.add(it)
                }
            }

            functionDeclaration.parameters.forEach {
                if (!setOfNonConflicting.contains(it)) {
                    reportParameterNameConflict(functionDeclaration, it)
                }
            }
        }
    }

    override fun inspect(file: File) {
        file.accept(FunctionSignatureVisitor(), null)
    }

    private fun reportParameterNameConflict(functionDeclaration: FunctionDeclaration, parameter: String) {
        reporter.report(
            functionDeclaration,
            "Parameter name conflict in function '${functionDeclaration.name}' declaration: parameter '${parameter}' occurs several times"
        )
    }
}