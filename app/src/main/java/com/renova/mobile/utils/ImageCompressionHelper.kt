package com.renova.mobile.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import android.graphics.Matrix

object ImageCompressionHelper {
    private const val TAG = "ImageCompression"
    private const val MAX_SIZE_BYTES = 5 * 1024 * 1024 // 5MB
    private const val INITIAL_QUALITY = 85
    private const val MIN_QUALITY = 20
    private const val QUALITY_STEP = 10

    /**
     * Comprime una imagen siempre, independientemente de su tamaño original.
     * Garantiza que la imagen final sea menor a 5MB.
     */
    fun compressImage(context: Context, uri: Uri, prefix: String = "compressed"): Result<Uri> {
        return try {
            Log.d(TAG, "Iniciando compresión para: $uri")

            // 1. Leer la imagen original
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return Result.failure(Exception("No se pudo abrir el archivo"))

            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (originalBitmap == null) {
                return Result.failure(Exception("No se pudo decodificar la imagen"))
            }

            val correctedBitmap = correctImageOrientation(context, uri, originalBitmap)

            // 2. Calcular nueva resolución si es muy grande
            val maxDimension = 1920 // Full HD
            val scaledBitmap = if (correctedBitmap.width > maxDimension ||
                correctedBitmap.height > maxDimension) {
                Log.d(TAG, "Redimensionando imagen de ${correctedBitmap.width}x${correctedBitmap.height}")
                scaleBitmap(correctedBitmap, maxDimension)
            } else {
                correctedBitmap
            }

            // 3. Crear archivo temporal
            val timestamp = System.currentTimeMillis()
            val compressedFile = File(context.cacheDir, "${prefix}_${timestamp}.jpg")

            // 4. Comprimir con calidad decreciente hasta cumplir el límite
            var quality = INITIAL_QUALITY
            var attempt = 0

            do {
                attempt++
                val outputStream = compressedFile.outputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                outputStream.close()

                val fileSize = compressedFile.length()
                Log.d(TAG, "Intento $attempt: calidad=$quality, tamaño=${fileSize / 1024}KB")

                if (fileSize <= MAX_SIZE_BYTES) {
                    break
                }

                quality -= QUALITY_STEP
            } while (quality >= MIN_QUALITY && attempt < 10)

            // 5. Liberar recursos
            if (scaledBitmap != correctedBitmap) {
                scaledBitmap.recycle()
            }
            if (correctedBitmap != originalBitmap) {
                correctedBitmap.recycle()
            }
            originalBitmap.recycle()

            // 6. Verificar resultado final
            if (compressedFile.length() > MAX_SIZE_BYTES) {
                compressedFile.delete()
                return Result.failure(Exception(
                    "No se pudo comprimir la imagen por debajo de 5MB. " +
                            "Tamaño final: ${compressedFile.length() / 1024}KB"
                ))
            }

            Log.d(TAG, " Compresión exitosa: ${compressedFile.length() / 1024}KB")

            // 7. Crear URI con FileProvider
            val compressedUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                compressedFile
            )

            Result.success(compressedUri)

        } catch (e: Exception) {
            Log.e(TAG, "Error al comprimir imagen", e)
            Result.failure(e)
        }
    }

    /**
     * Escala un bitmap manteniendo la relación de aspecto
     */
    private fun scaleBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val scale = if (width > height) {
            maxDimension.toFloat() / width
        } else {
            maxDimension.toFloat() / height
        }

        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * Corrige la orientación de una imagen según sus metadatos EXIF
     */
    private fun correctImageOrientation(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val exif = ExifInterface(inputStream!!)
            inputStream.close()

            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            }

            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: Exception) {
            Log.e(TAG, "Error corrigiendo orientación", e)
            bitmap // Si falla, devolver bitmap original
        }
    }

    /**
     * Valida si un archivo cumple con el límite de tamaño
     */
    fun validateFileSize(context: Context, uri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val fileSize = inputStream.available()
                fileSize <= MAX_SIZE_BYTES
            } ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error validando tamaño", e)
            false
        }
    }
}