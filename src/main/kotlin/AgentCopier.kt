package com.camilocalle

import java.io.File
import java.net.JarURLConnection

/**
 * Clase utilitaria encargada de copiar los archivos del agente AI
 * desde los recursos del plugin al proyecto del usuario.
 *
 * Centraliza la lógica de copia para evitar duplicación entre
 * InitializeAgentAction y AgentStartupActivity.
 *
 * Maneja dos escenarios:
 * - Modo desarrollo: los recursos son archivos normales en disco
 * - Modo producción: los recursos están empaquetados dentro de un jar
 */
object AgentCopier {
    private const val RESOURCE_PREFIX = "templates/.ai-agent-test"

    /**
     * Copia los archivos del agente al proyecto especificado.
     * Detecta automáticamente si está en modo desarrollo o producción
     * y usa la estrategia correcta en cada caso.
     */
    fun copyToProject(projectPath: String, classLoader: ClassLoader): Boolean {
        val destination = File(projectPath, ".ai-agent-test")
        val resourceUrl = classLoader.getResource(RESOURCE_PREFIX) ?: return false

        when (resourceUrl.protocol) {
            // Modo desarrollo — los recursos son archivos normales en disco
            "file" -> {
                val sourceFolder = File(resourceUrl.toURI())
                copyFolder(sourceFolder, destination)
            }
            // Modo producción — los recursos están empaquetados en un jar
            "jar" -> {
                val jarConnection = resourceUrl.openConnection() as JarURLConnection
                val jarFile = jarConnection.jarFile

                jarFile.entries().asSequence()
                    .filter { it.name.startsWith(RESOURCE_PREFIX) && !it.isDirectory }
                    .forEach { entry ->
                        val relativePath = entry.name.removePrefix("$RESOURCE_PREFIX/")
                        val destFile = File(destination, relativePath)
                        destFile.parentFile.mkdirs()

                        classLoader.getResourceAsStream(entry.name)?.use { input ->
                            destFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
            }
        }
        return true
    }

    /**
     * Copia recursivamente una carpeta completa de origen a destino.
     * Solo se usa en modo desarrollo cuando los recursos son archivos normales.
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