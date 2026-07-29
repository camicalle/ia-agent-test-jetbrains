package com.camilocalle

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

/**
 * Acción que inicializa el agente AI en el proyecto por primera vez.
 * El usuario la ejecuta desde Tools → Initialize Agent Test.
 *
 * Delega la lógica de copia a AgentCopier para evitar duplicación de código.
 */
class InitializeAgentAction: AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: run {
            Messages.showErrorDialog("Abre un proyecto primero", "Error")
            return
        }

        val projectPath = project.basePath ?: return
        val copied = AgentCopier.copyToProject(projectPath, javaClass.classLoader)

        if (copied) {
            Messages.showInfoMessage("✅ Agente AI inicializado en tu proyecto!", "IA Agent Test")
        } else {
            Messages.showErrorDialog("No se encontró la carpeta templates en el plugin", "Error")
        }
    }
}