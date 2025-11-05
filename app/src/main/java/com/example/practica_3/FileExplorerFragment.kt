package com.example.practica_3

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.practica_3.databinding.FragmentFileExplorerBinding
import com.example.practica_3.models.FileItem
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.Stack

class FileExplorerFragment : Fragment() {

    private var _binding: FragmentFileExplorerBinding? = null
    private val binding get() = _binding!!

    private lateinit var recycler: RecyclerView
    private lateinit var pathText: TextView
    private lateinit var adapter: FileAdapter
    private lateinit var fabMenu: FloatingActionButton
    private lateinit var fabMore: FloatingActionButton
    private lateinit var fabCancelSelection: FloatingActionButton

    private lateinit var currentPath: File
    private val pathHistory = Stack<File>()

    private var copiedFile: File? = null
    private var isMoveOperation = false
    private var isSelectionMode = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFileExplorerBinding.inflate(inflater, container, false)

        recycler = binding.recyclerFiles
        pathText = binding.currentPath
        fabMenu = binding.fabMenu
        fabMore = binding.fabMoreOptions
        fabCancelSelection = binding.fabCancelSelection

        recycler.layoutManager = LinearLayoutManager(requireContext())

        adapter = FileAdapter(
            onOpen = { openFile(it) },
            onSelectionChanged = { selected ->
                if (selected.isNotEmpty()) {
                    isSelectionMode = true
                    showCancelButtonAnimated()
                } else {
                    isSelectionMode = false
                    hideCancelButtonAnimated()
                }
            }
        )
        recycler.adapter = adapter

        // 📁 Directorio inicial
        currentPath = getSafeRootDirectory()
        loadFiles(currentPath)

        // 🎨 Colores según tema
        aplicarColorPorTema()

        // 🧭 Botones principales
        fabMenu.setOnClickListener { createFolderDialog() }
        fabMore.setOnClickListener { showAdvancedMenu() }
        binding.btnBack.setOnClickListener { goBack() }

        // 💾 Cambiar almacenamiento (interno / externo)
        binding.btnSwitchStorage.setOnClickListener {
            if (currentPath == getSafeRootDirectory()) {
                // Cambiar a almacenamiento interno
                currentPath = requireContext().filesDir
                loadFiles(currentPath)
                binding.btnSwitchStorage.text = "Cambiar a Externo"
                Toast.makeText(requireContext(), "Cambiado a almacenamiento interno", Toast.LENGTH_SHORT).show()
            } else {
                // Cambiar a almacenamiento externo
                currentPath = getSafeRootDirectory()
                loadFiles(currentPath)
                binding.btnSwitchStorage.text = "Cambiar a Interno"
                Toast.makeText(requireContext(), "Cambiado a almacenamiento externo", Toast.LENGTH_SHORT).show()
            }
        }

        fabCancelSelection.setOnClickListener {
            adapter.clearSelection()
            isSelectionMode = false
            hideCancelButtonAnimated()
            Toast.makeText(requireContext(), "Selección cancelada", Toast.LENGTH_SHORT).show()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { goBack() }

        return binding.root
    }

    // =====================================================
    // 🎨 Aplicar color dinámico (Guinda / Azul + modo oscuro)
    // =====================================================
    private fun aplicarColorPorTema() {
        val prefs = requireContext().getSharedPreferences("settings", AppCompatActivity.MODE_PRIVATE)
        val theme = prefs.getString("theme", "guinda")

        val isDark = (resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES

        // 🎨 Color base según tema seleccionado
        val colorPrincipal = when {
            theme == "azul" && isDark -> R.color.azul_escom_oscuro
            theme == "azul" && !isDark -> R.color.azul_escom
            theme == "guinda" && isDark -> R.color.guinda_ipn_oscuro
            else -> R.color.guinda_ipn
        }

        // 🕶️ Color de íconos (blanco en claro, gris en oscuro)
        val iconColor = if (isDark) android.R.color.darker_gray else android.R.color.white

        listOf(fabMenu, fabMore, fabCancelSelection).forEach {
            it.backgroundTintList = ContextCompat.getColorStateList(requireContext(), colorPrincipal)
            it.imageTintList = ContextCompat.getColorStateList(requireContext(), iconColor)
        }

        // ⚙️ Botón de cambio de almacenamiento
        binding.btnSwitchStorage.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), colorPrincipal)
        binding.btnSwitchStorage.setTextColor(
            ContextCompat.getColor(requireContext(), android.R.color.white)
        )
    }

    // =====================================================
    // 🎞️ Animaciones suaves (Material Motion)
    // =====================================================
    private fun showCancelButtonAnimated() {
        if (fabCancelSelection.visibility == View.VISIBLE) return
        fabCancelSelection.visibility = View.VISIBLE
        fabCancelSelection.alpha = 0f
        fabCancelSelection.translationY = 60f

        fabCancelSelection.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(250)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun hideCancelButtonAnimated() {
        if (fabCancelSelection.visibility == View.GONE) return
        fabCancelSelection.animate()
            .alpha(0f)
            .translationY(60f)
            .setDuration(250)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction { fabCancelSelection.visibility = View.GONE }
            .start()
    }

    // =====================================================
    // 📂 Cargar archivos
    // =====================================================
    private fun getSafeRootDirectory(): File {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            else
                Environment.getExternalStorageDirectory()
        } catch (e: Exception) {
            requireContext().filesDir
        }
    }

    private fun loadFiles(path: File) {
        try {
            val files = path.listFiles()?.map {
                FileItem(
                    name = it.name,
                    path = it.absolutePath,
                    isDirectory = it.isDirectory,
                    size = if (it.isDirectory) 0 else it.length(),
                    lastModified = it.lastModified()
                )
            }?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() })) ?: emptyList()

            pathText.text = path.absolutePath
            adapter.submitList(files)
            if (this::currentPath.isInitialized && currentPath != path) pathHistory.push(currentPath)
            currentPath = path

            // 🔄 Actualiza texto del botón según ubicación
            val isExternal = currentPath == getSafeRootDirectory()
            binding.btnSwitchStorage.text =
                if (isExternal) "Cambiar a Interno" else "Cambiar a Externo"

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al listar archivos", Toast.LENGTH_SHORT).show()
        }
    }

    // =====================================================
    // ⚙️ Menú avanzado (⋮)
    // =====================================================
    private fun showAdvancedMenu() {
        val options = arrayOf("Copiar", "Pegar", "Mover", "Renombrar", "Eliminar")
        AlertDialog.Builder(requireContext())
            .setTitle("Opciones avanzadas")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> copyFileDialog()
                    1 -> pasteFile()
                    2 -> moveFileDialog()
                    3 -> renameDialog()
                    4 -> deleteDialog()
                }
            }
            .show()
    }

    // =====================================================
    // 📋 Copiar / Mover / Pegar
    // =====================================================
    private fun copyFileDialog() {
        val selected = adapter.getSelectedItems().firstOrNull()
        if (selected == null) {
            Toast.makeText(requireContext(), "Selecciona un archivo o carpeta", Toast.LENGTH_SHORT).show()
            return
        }
        copiedFile = File(selected.path)
        isMoveOperation = false
        Toast.makeText(requireContext(), "Archivo copiado. Usa 'Pegar' para colocarlo.", Toast.LENGTH_SHORT).show()
    }

    private fun moveFileDialog() {
        val selected = adapter.getSelectedItems().firstOrNull()
        if (selected == null) {
            Toast.makeText(requireContext(), "Selecciona un archivo o carpeta", Toast.LENGTH_SHORT).show()
            return
        }
        copiedFile = File(selected.path)
        isMoveOperation = true
        Toast.makeText(requireContext(), "Archivo listo para mover. Usa 'Pegar' en el destino.", Toast.LENGTH_SHORT).show()
    }

    private fun pasteFile() {
        val source = copiedFile ?: run {
            Toast.makeText(requireContext(), "No hay archivo copiado o movido.", Toast.LENGTH_SHORT).show()
            return
        }
        val destination = File(currentPath, source.name)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING)
                if (isMoveOperation) source.delete()
            } else {
                source.copyTo(destination, overwrite = true)
                if (isMoveOperation) source.delete()
            }
            Toast.makeText(requireContext(), if (isMoveOperation) "Archivo movido" else "Archivo copiado", Toast.LENGTH_SHORT).show()
            copiedFile = null
            loadFiles(currentPath)
        } catch (e: IOException) {
            Toast.makeText(requireContext(), "Error al pegar archivo: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // =====================================================
    // 🖼️ Abrir archivos
    // =====================================================
    private fun openFile(file: FileItem) {
        val context = requireContext()
        val fileObject = File(file.path)

        if (!fileObject.exists() || !fileObject.canRead()) {
            Toast.makeText(context, "No se puede acceder al archivo", Toast.LENGTH_SHORT).show()
            return
        }

        if (file.isDirectory) {
            loadFiles(fileObject)
            return
        }

        val mime = getMimeType(file.path)
        try {
            when {
                mime.startsWith("image/") -> {
                    val intent = Intent(context, ImageViewerActivity::class.java)
                    intent.putExtra("imagePath", file.path)
                    startActivity(intent)
                }

                mime == "application/pdf" -> {
                    val intent = Intent(context, PdfViewerActivity::class.java)
                    intent.putExtra("pdfPath", file.path)
                    startActivity(intent)
                }

                else -> {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        fileObject
                    )
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, mime)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    if (intent.resolveActivity(context.packageManager) != null) {
                        startActivity(intent)
                    } else {
                        Toast.makeText(context, "No hay aplicación para abrir este tipo de archivo", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al abrir archivo: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getMimeType(path: String): String {
        val extension = MimeTypeMap.getFileExtensionFromUrl(path)?.lowercase()
        return when (extension) {
            "jpg", "jpeg", "png", "gif", "webp" -> "image/$extension"
            "pdf" -> "application/pdf"
            "txt" -> "text/plain"
            "mp3" -> "audio/mpeg"
            else -> MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"
        }
    }

    // =====================================================
    // ✏️ Crear / Renombrar / Eliminar
    // =====================================================
    private fun createFolderDialog() {
        val input = EditText(requireContext())
        AlertDialog.Builder(requireContext())
            .setTitle("Nueva carpeta")
            .setView(input)
            .setPositiveButton("Crear") { _, _ ->
                val folder = File(currentPath, input.text.toString())
                if (!folder.exists() && folder.mkdir()) {
                    Toast.makeText(requireContext(), "Carpeta creada", Toast.LENGTH_SHORT).show()
                    loadFiles(currentPath)
                } else {
                    Toast.makeText(requireContext(), "No se pudo crear carpeta", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun renameDialog() {
        val selected = adapter.getSelectedItems().firstOrNull()
        if (selected == null) {
            Toast.makeText(requireContext(), "Selecciona un archivo o carpeta", Toast.LENGTH_SHORT).show()
            return
        }

        val input = EditText(requireContext()).apply { hint = "Nuevo nombre" }
        AlertDialog.Builder(requireContext())
            .setTitle("Renombrar: ${selected.name}")
            .setView(input)
            .setPositiveButton("Renombrar") { _, _ ->
                val newName = input.text.toString().trim()
                val oldFile = File(selected.path)
                val newFile = File(oldFile.parent, newName)
                if (oldFile.renameTo(newFile)) {
                    Toast.makeText(requireContext(), "Renombrado correctamente", Toast.LENGTH_SHORT).show()
                    loadFiles(currentPath)
                } else {
                    Toast.makeText(requireContext(), "Error al renombrar", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteDialog() {
        val selected = adapter.getSelectedItems().firstOrNull()
        if (selected == null) {
            Toast.makeText(requireContext(), "Selecciona un archivo o carpeta", Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar")
            .setMessage("¿Deseas eliminar '${selected.name}'?")
            .setPositiveButton("Sí") { _, _ ->
                File(selected.path).deleteRecursively()
                loadFiles(currentPath)
                Toast.makeText(requireContext(), "Eliminado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun goBack() {
        if (pathHistory.isNotEmpty()) {
            loadFiles(pathHistory.pop())
        } else {
            Toast.makeText(requireContext(), "Ya estás en la raíz", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
