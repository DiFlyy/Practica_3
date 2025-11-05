package com.example.practica_3

import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class PdfViewerActivity : AppCompatActivity() {

    private lateinit var pdfRenderer: PdfRenderer
    private lateinit var currentPage: PdfRenderer.Page
    private lateinit var imageView: ImageView
    private lateinit var parcelFileDescriptor: ParcelFileDescriptor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageView = ImageView(this)
        setContentView(imageView)

        val path = intent.getStringExtra("pdfPath") ?: return
        val file = File(path)

        try {
            parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = PdfRenderer(parcelFileDescriptor)
            showPage(0)
        } catch (e: Exception) {
            e.printStackTrace()
            finish()
        }
    }

    private fun showPage(index: Int) {
        if (::currentPage.isInitialized) currentPage.close()
        currentPage = pdfRenderer.openPage(index)

        val bitmap = android.graphics.Bitmap.createBitmap(
            currentPage.width,
            currentPage.height,
            android.graphics.Bitmap.Config.ARGB_8888
        )
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        imageView.setImageBitmap(bitmap)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::currentPage.isInitialized) currentPage.close()
        pdfRenderer.close()
        parcelFileDescriptor.close()
    }
}
