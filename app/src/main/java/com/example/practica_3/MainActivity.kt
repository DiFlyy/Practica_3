package com.example.practica_3

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // ---- Selector de tema persistente ----
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val theme = prefs.getString("theme", "guinda")
        if (theme == "azul") {
            setTheme(R.style.Theme_Practica3_Azul)
        } else {
            setTheme(R.style.Theme_Practica3_Guinda)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ---- Permisos de almacenamiento según versión ----
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
            }
        } else {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        }

        // Carga fragmento principal si es la primera vez
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, FileExplorerFragment())
                .commitNow()
        }

        // ---- Cambio de tema (long press) ----
        findViewById<View>(R.id.fragment_container).setOnLongClickListener {
            val options = arrayOf("Tema Guinda", "Tema Azul")
            AlertDialog.Builder(this)
                .setTitle("Cambiar tema")
                .setItems(options) { _, which ->
                    val editor = prefs.edit()
                    if (which == 0) editor.putString("theme", "guinda") else editor.putString("theme", "azul")
                    editor.apply()
                    recreate()
                }.show()
            true
        }
    }
}
