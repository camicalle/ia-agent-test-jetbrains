package com.camilocalle

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import java.io.File

/**
 * Actividad que corre automáticamente cada vez que JetBrains abre un proyecto.
 *
 * Comportamiento:
 * - Si encuentra la carpeta .ai-agent-test la actualiza automáticamente
 * - Si no la encuentra no hace nada — espera que el usuario inicialice manualmente
 *
 * Delega la lógica de copia a AgentCopier para evitar duplicación de código.
 */
class AgentStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        val projectPath = project.basePath ?: return
        val agentFolder = File(projectPath, ".ai-agent-test")

        if (agentFolder.exists()) {
            AgentCopier.copyToProject(projectPath, javaClass.classLoader)
        }
    }
}