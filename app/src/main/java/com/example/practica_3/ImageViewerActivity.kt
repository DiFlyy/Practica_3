package com.example.practica_3

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.practica_3.databinding.ActivityImageViewerBinding

class ImageViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imagePath = intent.getStringExtra("imagePath")

        if (imagePath != null) {
            Glide.with(this)
                .load(imagePath)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_broken_image)
                .into(binding.imageView) // ✅ correcto
        }
    }
}
