package com.example.practica_3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.practica_3.models.FileItem

class FileAdapter(
    private val onOpen: (FileItem) -> Unit,
    private val onSelected: (FileItem) -> Unit
) : ListAdapter<FileItem, FileAdapter.ViewHolder>(DiffCallback()) {

    private var selectedPosition = RecyclerView.NO_POSITION

    fun getSelectedItem(): FileItem? =
        if (selectedPosition != RecyclerView.NO_POSITION) getItem(selectedPosition) else null

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val name = view.findViewById<TextView>(R.id.nameFile)
        private val details = view.findViewById<TextView>(R.id.detailsFile)
        private val icon = view.findViewById<ImageView>(R.id.iconFile)

        fun bind(item: FileItem, position: Int) {
            name.text = item.name
            val type = if (item.isDirectory) "carpeta" else "archivo"
            details.text = "${FileUtils.formatDate(item.lastModified)} • ${item.size} bytes • $type"
            icon.setImageResource(if (item.isDirectory) R.drawable.ic_folder else R.drawable.ic_file)

            itemView.isSelected = position == selectedPosition
            itemView.alpha = if (position == selectedPosition) 0.8f else 1f

            itemView.setOnClickListener {
                // Selección
                val old = selectedPosition
                selectedPosition = bindingAdapterPosition
                if (old != RecyclerView.NO_POSITION) notifyItemChanged(old)
                notifyItemChanged(selectedPosition)
                onSelected(item)

                // Abrir (si no es solo selección; aquí abrimos también)
                onOpen(item)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<FileItem>() {
        override fun areItemsTheSame(oldItem: FileItem, newItem: FileItem) = oldItem.path == newItem.path
        override fun areContentsTheSame(oldItem: FileItem, newItem: FileItem) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position), position)
}
