package com.camilocalle

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import java.io.File
import java.net.JarURLConnection

/**
 * Acción que inicializa el agente AI en el proyecto por primera vez.
 * El usuario la ejecuta desde Tools → Initialize Agent Test.
 *
 * Responsabilidades:
 * - Verificar que haya un proyecto abierto
 * - Copiar toda la carpeta templates del plugin al proyecto del usuario
 * - Funciona tanto en desarrollo como empaquetado en jar
 * - Notificar al usuario cuando el proceso termina
 */
class InitializeAgentAction: AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: run {
            Messages.showErrorDialog("Abre un proyecto primero", "Error")
            return
        }

        copyAgentToProject(project)
        Messages.showInfoMessage("✅ Agente AI inicializado en tu proyecto!", "IA Agent Test")
    }

    private fun copyAgentToProject(project: Project) {
        val projectPath = project.basePath ?: return
        val destination = File(projectPath, ".ai-agent-test")
        val resourcePrefix = "templates/.ai-agent-test"

        val resourceUrl = javaClass.classLoader.getResource(resourcePrefix) ?: run {
            Messages.showErrorDialog("No se encontró la carpeta templates en el plugin", "Error")
            return
        }

        when (resourceUrl.protocol) {
            // Modo desarrollo — los recursos son archivos normales
            "file" -> {
                val sourceFolder = File(resourceUrl.toURI())
                copyFolder(sourceFolder, destination)
            }
            // Modo empaquetado — los recursos están dentro de un jar
            "jar" -> {
                val jarConnection = resourceUrl.openConnection() as JarURLConnection
                val jarFile = jarConnection.jarFile

                jarFile.entries().asSequence()
                    .filter { it.name.startsWith(resourcePrefix) && !it.isDirectory }
                    .forEach { entry ->
                        val relativePath = entry.name.removePrefix("$resourcePrefix/")
                        val destFile = File(destination, relativePath)
                        destFile.parentFile.mkdirs()

                        javaClass.classLoader.getResourceAsStream(entry.name)?.use { input ->
                            destFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
            }
        }
    }

    /**
     * Copia recursivamente una carpeta completa de origen a destino.
     * Solo se usa en modo desarrollo cuando los recursos son archivos normales.
     * En modo jar se usa el recorrido de entradas del jar directamente.
     */
    private fun copyFolder(source: File, destination: File) {
        destination.mkdirs()
        source.listFiles()?.forEach { file ->
            val dest = File(destination, file.name)
            if (file.isDirectory) {
                copyFolder(file, dest)
            } else {
                file.copyTo(dest, overwrite = true)
            }
        }
    }
}