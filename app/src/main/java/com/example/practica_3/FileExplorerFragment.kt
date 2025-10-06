package com.example.practica_3

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.practica_3.databinding.FragmentFileExplorerBinding
import com.example.practica_3.models.FileItem
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File

class FileExplorerFragment : Fragment() {

    private var _binding: FragmentFileExplorerBinding? = null
    private val binding get() = _binding!!

    private lateinit var recycler: RecyclerView
    private lateinit var pathText: TextView
    private lateinit var adapter: FileAdapter
    private lateinit var fabMenu: FloatingActionButton

    private var currentPath: String = Environment.getExternalStorageDirectory().path

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFileExplorerBinding.inflate(inflater, container, false)
        val view = binding.root

        recycler = binding.recyclerFiles
        pathText = binding.currentPath
        fabMenu = binding.fabMenu

        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = FileAdapter(
            onOpen = { openFile(it) },
            onSelected = { /* visual feedback handled in adapter */ }
        )
        recycler.adapter = adapter

        fabMenu.setOnClickListener { showFileMenu() }

        loadFiles(currentPath)
        return view
    }

    private fun loadFiles(path: String) {
        try {
            val files = FileUtils.listFiles(path)
            pathText.text = path
            adapter.submitList(files)
            currentPath = path
        } catch (e: SecurityException) {
            Toast.makeText(requireContext(), "Acceso denegado a: $path", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al listar archivos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openFile(file: FileItem) {
        if (file.isDirectory) {
            loadFiles(file.path)
        } else {
            val mime = getMimeType(file.path)
            if (mime.startsWith("image/")) {
                val intent = Intent(requireContext(), ImageViewerActivity::class.java)
                intent.putExtra("imagePath", file.path)
                startActivity(intent)
            } else {
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().packageName + ".provider",
                    File(file.path)
                )
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, mime)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "No hay app para abrir este tipo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getMimeType(path: String): String {
        val extension = MimeTypeMap.getFileExtensionFromUrl(path) ?: ""
        val guess = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
        return guess ?: "*/*"
    }

    // --------- Gestión básica: Crear / Renombrar / Eliminar ---------

    private fun showFileMenu() {
        val options = arrayOf("Crear carpeta", "Renombrar (selección)", "Eliminar (selección)")
        AlertDialog.Builder(requireContext())
            .setTitle("Acciones")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> createFolderDialog()
                    1 -> renameDialog()
                    2 -> deleteDialog()
                }
            }.show()
    }

    private fun createFolderDialog() {
        val input = EditText(requireContext()).apply { hint = "Nombre de carpeta" }
        AlertDialog.Builder(requireContext())
            .setTitle("Nueva carpeta")
            .setView(input)
            .setPositiveButton("Crear") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isEmpty()) return@setPositiveButton
                val folder = File(currentPath, name)
                if (!folder.exists()) {
                    val ok = folder.mkdir()
                    Toast.makeText(requireContext(), if (ok) "Carpeta creada" else "No se pudo crear", Toast.LENGTH_SHORT).show()
                    loadFiles(currentPath)
                } else {
                    Toast.makeText(requireContext(), "Ya existe una carpeta con ese nombre", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun renameDialog() {
        val selected = adapter.getSelectedItem()
        if (selected == null) {
            Toast.makeText(requireContext(), "Selecciona un archivo/carpeta primero (tap)", Toast.LENGTH_SHORT).show()
            return
        }
        val input = EditText(requireContext()).apply { hint = "Nuevo nombre" }
        AlertDialog.Builder(requireContext())
            .setTitle("Renombrar: ${selected.name}")
            .setView(input)
            .setPositiveButton("Renombrar") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isEmpty()) return@setPositiveButton
                val oldFile = File(selected.path)
                val newFile = File(oldFile.parent, newName)
                val ok = try { oldFile.renameTo(newFile) } catch (_: Exception) { false }
                Toast.makeText(requireContext(), if (ok) "Renombrado" else "No se pudo renombrar", Toast.LENGTH_SHORT).show()
                loadFiles(currentPath)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteDialog() {
        val selected = adapter.getSelectedItem()
        if (selected == null) {
            Toast.makeText(requireContext(), "Selecciona un archivo/carpeta primero (tap)", Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar")
            .setMessage("¿Deseas eliminar '${selected.name}'?")
            .setPositiveButton("Sí") { _, _ ->
                val file = File(selected.path)
                val ok = try { file.deleteRecursively() } catch (_: Exception) { false }
                Toast.makeText(requireContext(), if (ok) "Eliminado" else "No se pudo eliminar", Toast.LENGTH_SHORT).show()
                loadFiles(currentPath)
            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
