package com.didiglobal.booster.command

import java.io.File
import java.util.ServiceLoader

/**
 * Represents a command service
 */
class CommandService {

    companion object {

        private val commands = ServiceLoader.load(CommandProvider::class.java, Command::class.java.classLoader).map {
            it.get()
        }.flatten().map {
            it.name to it
        }.toMap()

        @Suppress("UNCHECKED_CAST")
        fun get(name: String): Command = commands[name]
                ?: fromPath(name)
                ?: throw RuntimeException("command `$name` not found")

        fun fromPath(name: String): Command? = System.getenv("PATH").split(File.pathSeparatorChar).map {
            File(it)
        }.map { path ->
            when {
                path.isDirectory -> path.listFiles { file ->
                    file.name == name && !file.isDirectory
                }?.firstOrNull()
                else -> if (path.name == name) path else null
            }
        }.find {
            it != null && it.exists()
        }?.toURI()?.toURL()?.let {
            Command(name, it)
        }
    }

}