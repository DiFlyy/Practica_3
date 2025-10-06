package com.example.practica_3

import com.example.practica_3.models.FileItem
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object FileUtils {
    fun listFiles(path: String): List<FileItem> {
        val directory = File(path)
        val files = directory.listFiles() ?: return emptyList()
        return files.map {
            FileItem(
                name = it.name,
                path = it.path,
                isDirectory = it.isDirectory,
                size = if (it.isDirectory) 0L else it.length(),
                lastModified = it.lastModified()
            )
        }.sortedWith(compareByDescending<FileItem> { it.isDirectory }.thenBy { it.name.lowercase() })
    }

    fun formatDate(timestamp: Long): String =
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
}
