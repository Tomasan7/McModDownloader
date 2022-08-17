package me.tomasan7.mcmoddownloader

import java.io.File
import java.io.InputStream
import java.nio.file.Files

/**
 * Copies a [resource][InputStream] to a file system if the destination file doesn't exist already.
 * @return Whether the file was copied or not.
 */
fun copyResourceIfNotExists(resource: InputStream, destinationFile: File): Boolean
{
    if (destinationFile.exists())
        return false

    return Files.copy(resource, destinationFile.toPath()) != 0L
}

fun <T> Collection<T>.joinToString(
    separator: String = ", ",
    lastSeparator: String = " and ",
    prefix: String = "",
    postfix: String = "",
    block: (T) -> String = { it.toString() }
): String
{
    val sb = StringBuilder()

    sb.append(prefix)
    forEachIndexed { i, element ->
        sb.append(block(element))
        if (i == size - 2)
            sb.append(lastSeparator)
        else if (i + 1 != size)
            sb.append(separator)
    }
    sb.append(postfix)

    return sb.toString()
}