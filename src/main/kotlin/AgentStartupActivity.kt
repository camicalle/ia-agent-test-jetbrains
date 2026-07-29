package com.camilocalle

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import java.io.File
import java.net.JarURLConnection

/**
 * Actividad que corre automáticamente cada vez que IntelliJ abre un proyecto.
 *
 * Comportamiento:
 * - Si encuentra la carpeta .ai-agent-test en el proyecto la actualiza
 * - Si no la encuentra no hace nada — espera que el usuario inicialice manualmente
 */
class AgentStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        val projectPath = project.basePath ?: return
        val agentFolder = File(projectPath, ".ai-agent-test")

        // Solo actualiza si ya fue inicializado antes
        if (agentFolder.exists()) {
            copyAgentToProject(projectPath)
        }
    }

    private fun copyAgentToProject(projectPath: String) {
        val destination = File(projectPath, ".ai-agent-test")
        val resourcePrefix = "templates/.ai-agent-test"

        val resourceUrl = javaClass.classLoader.getResource(resourcePrefix) ?: return

        when (resourceUrl.protocol) {
            "file" -> {
                val sourceFolder = File(resourceUrl.toURI())
                copyFolder(sourceFolder, destination)
            }
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