package com.example.practica_3

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView

class ImageViewerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Mantén el mismo tema seleccionado que en MainActivity
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val theme = prefs.getString("theme", "guinda")
        if (theme == "azul") setTheme(R.style.Theme_Practica3_Azul) else setTheme(R.style.Theme_Practica3_Guinda)

        super.onCreate(savedInstanceState)

        val photoView = PhotoView(this)
        setContentView(photoView)

        val imagePath = intent.getStringExtra("imagePath")
        if (imagePath != null) {
            Glide.with(this).load(imagePath).into(photoView)
            photoView.scaleType = ImageView.ScaleType.FIT_CENTER
        }
    }
}
