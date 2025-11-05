package com.example.practica_3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.practica_3.models.FileItem
import java.io.File

class FileAdapter(
    private val onOpen: (FileItem) -> Unit,
    private val onSelectionChanged: (List<FileItem>) -> Unit
) : RecyclerView.Adapter<FileAdapter.ViewHolder>() {

    private val files = mutableListOf<FileItem>()

    fun submitList(newList: List<FileItem>) {
        files.clear()
        files.addAll(newList)
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<FileItem> = files.filter { it.isSelected }

    fun clearSelection() {
        files.forEach { it.isSelected = false }
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.fileIcon)
        val name: TextView = view.findViewById(R.id.fileName)
        val info: TextView = view.findViewById(R.id.fileInfo)

        fun bind(file: FileItem) {
            name.text = file.name
            info.text = if (file.isDirectory) "Carpeta" else formatSize(file.size)

            itemView.setBackgroundColor(
                if (file.isSelected)
                    itemView.context.getColor(R.color.purple_200)
                else
                    itemView.context.getColor(android.R.color.transparent)
            )

            // Iconos o miniaturas
            if (file.isDirectory) {
                icon.setImageResource(R.drawable.ic_folder)
            } else {
                val ext = MimeTypeMap.getFileExtensionFromUrl(file.path) ?: ""
                when (ext.lowercase()) {
                    "jpg", "jpeg", "png", "gif", "webp" -> {
                        Glide.with(itemView.context)
                            .load(File(file.path))
                            .placeholder(R.drawable.ic_image)
                            .into(icon)
                    }
                    "pdf" -> icon.setImageResource(R.drawable.ic_pdf)
                    "mp3" -> icon.setImageResource(R.drawable.ic_audio)
                    "txt", "md", "log", "json", "xml" -> icon.setImageResource(R.drawable.ic_text)
                    else -> icon.setImageResource(R.drawable.ic_file)
                }
            }

            // Click simple → abrir o seleccionar
            itemView.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    if (files.any { it.isSelected }) {
                        file.isSelected = !file.isSelected
                        notifyItemChanged(pos)
                        onSelectionChanged(getSelectedItems())
                    } else {
                        onOpen(file)
                    }
                }
            }

            // Long click → activar selección múltiple
            itemView.setOnLongClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    file.isSelected = !file.isSelected
                    notifyItemChanged(pos)
                    onSelectionChanged(getSelectedItems())
                }
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(files[position])
    }

    override fun getItemCount(): Int = files.size

    private fun formatSize(size: Long): String {
        if (size <= 0) return ""
        val kb = size / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1 -> String.format("%.1f MB", mb)
            kb >= 1 -> String.format("%.1f KB", kb)
            else -> "$size B"
        }
    }
}
