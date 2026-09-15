package com.soukmar.app.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.soukmar.app.data.remote.ApiService
import com.soukmar.app.data.remote.dto.ApiErrorDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: ApiService,
    private val json: Json
) {
    private fun <T> parseError(response: Response<T>): ApiResult.Error {
        val body = response.errorBody()?.string()
        val parsed = try {
            body?.let { json.decodeFromString(ApiErrorDto.serializer(), it) }
        } catch (e: Exception) { null }
        return ApiResult.Error(parsed?.error ?: "Une erreur est survenue.")
    }

    /** Downscales+re-encodes each picked gallery Uri into a cache JPEG file
     * before upload — mirrors the web's `compressListingPhoto()`/
     * `compressAvatar()` (browser-image-compression), needed because a raw
     * phone-camera photo (8-12MB) can exceed the backend's 10MB multer
     * limit and is otherwise sent unnecessarily large over mobile data.
     * [type] also selects the backend's Cloudinary preset (listing: 1200px
     * limit; avatar: 400x400 face-crop) — sending "listing" for an avatar
     * upload would silently apply the wrong crop. */
    suspend fun uploadImages(uris: List<Uri>, type: String = "listing"): ApiResult<List<String>> = withContext(Dispatchers.IO) {
        val maxDimension = if (type == "avatar") 640 else 1600
        val tempFiles = mutableListOf<File>()
        try {
            val parts = uris.mapIndexed { index, uri ->
                val tempFile = File.createTempFile("upload_${index}_", ".jpg", context.cacheDir)
                tempFiles += tempFile
                compressToJpeg(uri, tempFile, maxDimension)
                MultipartBody.Part.createFormData("images", tempFile.name, tempFile.asRequestBody("image/jpeg".toMediaType()))
            }
            val typeBody = type.toRequestBody("text/plain".toMediaTypeOrNull())
            val res = api.uploadImages(parts, typeBody)
            if (res.isSuccessful && res.body() != null) ApiResult.Success(res.body()!!.urls) else parseError(res)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Erreur réseau.")
        } finally {
            tempFiles.forEach { it.delete() }
        }
    }

    /** Decodes [uri] downsampled to roughly [maxDimension] on its longer
     * side (avoids loading a full-resolution bitmap into memory just to
     * shrink it), then writes it back out as an 82%-quality JPEG. Falls
     * back to a raw byte copy if decoding fails (e.g. an already-tiny or
     * unusual format image) rather than failing the whole upload. */
    private fun compressToJpeg(uri: Uri, outFile: File, maxDimension: Int) {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        val (width, height) = bounds.outWidth to bounds.outHeight
        if (width <= 0 || height <= 0) {
            context.contentResolver.openInputStream(uri)?.use { input ->
                outFile.outputStream().use { output -> input.copyTo(output) }
            }
            return
        }

        var sampleSize = 1
        while (width / (sampleSize * 2) >= maxDimension || height / (sampleSize * 2) >= maxDimension) sampleSize *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val sampled = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
            ?: run {
                context.contentResolver.openInputStream(uri)?.use { input -> outFile.outputStream().use { output -> input.copyTo(output) } }
                return
            }

        val longerSide = maxOf(sampled.width, sampled.height)
        val scaled = if (longerSide > maxDimension) {
            val scale = maxDimension.toFloat() / longerSide
            Bitmap.createScaledBitmap(sampled, (sampled.width * scale).toInt(), (sampled.height * scale).toInt(), true)
        } else sampled

        FileOutputStream(outFile).use { out -> scaled.compress(Bitmap.CompressFormat.JPEG, 82, out) }
        if (scaled !== sampled) sampled.recycle()
        scaled.recycle()
    }
}
