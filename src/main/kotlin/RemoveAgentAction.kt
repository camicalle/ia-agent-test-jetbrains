package com.camilocalle

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import java.io.File

/**
 * Acción que elimina el agente AI del proyecto actual.
 * El usuario la ejecuta desde Tools → Remove Agent Test.
 *
 * Responsabilidades:
 * - Verificar que haya un proyecto abierto
 * - Verificar que exista la carpeta .ai-agent-test
 * - Pedir confirmación antes de eliminar
 * - Eliminar la carpeta con todo su contenido
 */
class RemoveAgentAction: AnAction()  {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: run {
            Messages.showErrorDialog("Abre un proyecto primero", "Error")
            return
        }

        val projectPath = project.basePath ?: return
        val agentFolder = File(projectPath, ".ai-agent-test")

        if (!agentFolder.exists()) {
            Messages.showWarningDialog(
                "No se encontró .ai-agent-test en este proyecto",
                "IA Agent Test"
            )
            return
        }

        val confirm = Messages.showYesNoDialog(
            "¿Estás seguro de que quieres eliminar .ai-agent-test de este proyecto?",
            "Eliminar Agente AI",
            Messages.getWarningIcon()
        )

        if (confirm == Messages.YES) {
            agentFolder.deleteRecursively()
            Messages.showInfoMessage("🗑️ Agente AI removido del proyecto", "IA Agent Test")
        }
    }
}