package com.hompimpa.comfylearn.ui.games.drawing

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.hompimpa.comfylearn.R
import com.hompimpa.comfylearn.databinding.ActivityDrawingBinding
import com.hompimpa.comfylearn.helper.BaseActivity
import com.hompimpa.comfylearn.views.Board
import java.io.OutputStream

class DrawingActivity : BaseActivity() {

    private lateinit var binding: ActivityDrawingBinding

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                saveDrawing()
            } else {
                Toast.makeText(this, "Permission denied. Cannot save drawing.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDrawingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.board.setPenColor(Color.BLACK)
        binding.board.setBrushSize(20f)

        setupActionListeners()
        setupToolListeners()
    }

    /**
     * Sets up listeners for primary actions like saving, clearing, and loading pages.
     */
    private fun setupActionListeners() {
        binding.btnClear.setOnClickListener { binding.board.clear() }
        binding.btnSave.setOnClickListener { checkPermissionAndSave() }
        binding.btnColoringPage.setOnClickListener { showColoringPageSelectionDialog() }
    }

    /**
     * Sets up listeners for the various drawing tools.
     */
    private fun setupToolListeners() {
        binding.btnPickColor.setOnClickListener { showColorPickerDialog() }
        binding.btnEraser.setOnClickListener { binding.board.setEraserMode(true) }

        binding.btnBrushSmall.setOnClickListener { binding.board.setBrushSize(10f) }
        binding.btnBrushMedium.setOnClickListener { binding.board.setBrushSize(20f) }
        binding.btnBrushLarge.setOnClickListener { binding.board.setBrushSize(40f) }

        binding.btnDrawShape.setOnClickListener {
            binding.board.setDrawingMode(Board.Mode.SHAPE_RECTANGLE)
            Toast.makeText(this, "Rectangle mode activated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showColoringPageSelectionDialog() {
        val coloringPages = mapOf(
            "Animal" to R.drawable.animal_outline,
            "Car" to R.drawable.car_outline,
            "House" to R.drawable.house_outline
        )
        val pageNames = coloringPages.keys.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Choose a Coloring Page")
            .setItems(pageNames) { dialog, which ->
                val selectedPageName = pageNames[which]
                val selectedDrawableId = coloringPages[selectedPageName]

                selectedDrawableId?.let {
                    try {
                        val outlineBitmap = BitmapFactory.decodeResource(resources, it)
                        binding.board.setBackgroundImage(outlineBitmap)
                        Toast.makeText(this, "$selectedPageName page loaded!", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this, "Coloring page image not found.", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showColorPickerDialog() {
        ColorPickerDialog
            .Builder(this)
            .setTitle("Pick Color")
            .setColorShape(ColorShape.SQAURE)
            .setDefaultColor(binding.board.getPenColor())
            .setColorListener { color, _ ->
                binding.board.setEraserMode(false)
                binding.board.setPenColor(color)
            }
            .show()
    }

    private fun checkPermissionAndSave() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveDrawing()
        } else {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    saveDrawing()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
        }
    }

    private fun saveDrawing() {
        val bitmap = binding.board.getDrawingBitmap()
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "drawing_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ComfyLearn")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            try {
                val outputStream: OutputStream? = resolver.openOutputStream(it)
                outputStream?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(it, contentValues, null, null)
                }
                Toast.makeText(this, "Drawing saved to Gallery!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to save drawing.", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
}
