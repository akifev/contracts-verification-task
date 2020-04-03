package org.jetbrains.dummy.lang

import org.jetbrains.dummy.lang.VariableInitializationChecker.VariableStatus.*
import org.jetbrains.dummy.lang.tree.*

class VariableInitializationChecker(private val reporter: DiagnosticReporter) : AbstractChecker() {
    private enum class VariableStatus { DEFINED, INITIALIZED }

    private class VariableInfo(var status: VariableStatus, val isInOneBlock: Boolean = true)

    private inner class VariableInitializationVisitor : DummyLangVisitor<Unit, MutableMap<String, VariableInfo>>() {
        override fun visitElement(element: Element, data: MutableMap<String, VariableInfo>) {
            element.acceptChildren(this, data)
        }

        override fun visitFunctionDeclaration(
            functionDeclaration: FunctionDeclaration,
            data: MutableMap<String, VariableInfo>
        ) {
            data.putAll(functionDeclaration.parameters.map { Pair(it, VariableInfo(INITIALIZED)) })
            super.visitFunctionDeclaration(functionDeclaration, data)
            data.clear()
        }

        override fun visitBlock(block: Block, data: MutableMap<String, VariableInfo>) {
            val innerData = data.mapValues { VariableInfo(it.value.status, false) }.toMutableMap()
            super.visitBlock(block, innerData)
        }

        override fun visitAssignment(assignment: Assignment, data: MutableMap<String, VariableInfo>) {
            super.visitAssignment(assignment, data)

            if (data[assignment.variable] == null) {
                reportAccessToUndefined(assignment)
            } else {
                data[assignment.variable]!!.status = INITIALIZED
            }
        }

        override fun visitVariableDeclaration(
            variableDeclaration: VariableDeclaration,
            data: MutableMap<String, VariableInfo>
        ) {
            super.visitVariableDeclaration(variableDeclaration, data)

            if (data[variableDeclaration.name]?.isInOneBlock == true) {
                reportRedefinition(variableDeclaration)
            } else {
                data[variableDeclaration.name] =
                    VariableInfo(if (variableDeclaration.initializer != null) INITIALIZED else DEFINED)
            }
        }

        override fun visitVariableAccess(variableAccess: VariableAccess, data: MutableMap<String, VariableInfo>) {
            val variableInfo = data[variableAccess.name]

            if (variableInfo == null) {
                reportAccessToUndefined(variableAccess)
            } else if (variableInfo.status != INITIALIZED) {
                reportAccessBeforeInitialization(variableAccess)
            }
        }
    }

    override fun inspect(file: File) {
        val visitor = VariableInitializationVisitor()
        visitor.visitFile(file, mutableMapOf())
    }

    private fun reportAccessBeforeInitialization(access: VariableAccess) {
        reporter.report(access, "Variable '${access.name}' is accessed before initialization")
    }

    private fun reportAccessToUndefined(assignment: Assignment) {
        reporter.report(assignment, "Variable '${assignment.variable}' is undefined")
    }

    private fun reportAccessToUndefined(variableAccess: VariableAccess) {
        reporter.report(variableAccess, "Variable '${variableAccess.name}' is undefined")
    }

    private fun reportRedefinition(variableDeclaration: VariableDeclaration) {
        reporter.report(variableDeclaration, "Variable '${variableDeclaration.name}' is already defined")
    }
}