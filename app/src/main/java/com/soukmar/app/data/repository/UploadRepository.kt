package com.soukmar.app.data.repository

import com.soukmar.app.data.i18n.genericErrorText
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.soukmar.app.data.remote.ApiService
import com.soukmar.app.data.remote.dto.ApiErrorDto
import com.soukmar.app.data.remote.dto.ListingDto
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


/** Outcome of the AI listing assistant: [Failed.message] is the server's text in the app language, if any. */
sealed class AiDraftResult {
    data class Success(val draft: com.soukmar.app.data.remote.dto.AiListingDraftDto) : AiDraftResult()
    data class Failed(val message: String?, val unavailable: Boolean) : AiDraftResult()
}

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
        return ApiResult.Error(parsed?.error ?: genericErrorText())
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
            ApiResult.Error(genericErrorText())
        } finally {
            tempFiles.forEach { it.delete() }
        }
    }

    /** One photo for a chat message: downscaled like a listing photo, the server strips EXIF/GPS and returns a private signed URL. */
    suspend fun uploadChatImage(uri: Uri): ApiResult<String> = withContext(Dispatchers.IO) {
        val tempFile = File.createTempFile("chat_", ".jpg", context.cacheDir)
        try {
            compressToJpeg(uri, tempFile, 1600)
            val part = MultipartBody.Part.createFormData("image", tempFile.name, tempFile.asRequestBody("image/jpeg".toMediaType()))
            val res = api.uploadChatImage(part)
            if (res.isSuccessful && res.body() != null) ApiResult.Success(res.body()!!.url) else parseError(res)
        } catch (e: Exception) {
            ApiResult.Error(genericErrorText())
        } finally {
            tempFile.delete()
        }
    }

    /** AI listing assistant: null when the status cannot be read (treated as "not configured"). */
    suspend fun aiStatus(): com.soukmar.app.data.remote.dto.AiStatusDto? = try {
        val res = api.getAiStatus()
        if (res.isSuccessful) res.body() else null
    } catch (e: Exception) { null }

    /** AI listing assistant: sends downscaled copies of the photos (same compression as a normal upload). */
    suspend fun aiListingDraft(uris: List<Uri>, force: Boolean): AiDraftResult = withContext(Dispatchers.IO) {
        val tempFiles = mutableListOf<File>()
        try {
            val parts = uris.mapIndexed { index, uri ->
                val tempFile = File.createTempFile("ai_${index}_", ".jpg", context.cacheDir)
                tempFiles += tempFile
                compressToJpeg(uri, tempFile, 1600)
                MultipartBody.Part.createFormData("images", tempFile.name, tempFile.asRequestBody("image/jpeg".toMediaType()))
            }
            val res = api.aiListingDraft(parts, if (force) "1".toRequestBody("text/plain".toMediaTypeOrNull()) else null)
            val body = res.body()
            if (res.isSuccessful && body != null) return@withContext AiDraftResult.Success(body)
            val parsed = try {
                res.errorBody()?.string()?.let { json.decodeFromString(com.soukmar.app.data.remote.dto.AiErrorDto.serializer(), it) }
            } catch (e: Exception) { null }
            // Our API already answers in the app language (limit reached, busy, unavailable, invalid file).
            AiDraftResult.Failed(parsed?.error, unavailable = parsed?.code == "AI_UNAVAILABLE")
        } catch (e: Exception) {
            AiDraftResult.Failed(null, unavailable = e is java.io.IOException)
        } finally {
            tempFiles.forEach { it.delete() }
        }
    }

    /** "Search by photo" — mirrors the web's `ListingService.searchByImage()`:
     * a dedicated public multipart endpoint (field name `image`, singular),
     * separate from the general [uploadImages]/`/api/upload` — the backend
     * hashes the photo locally (free, no paid vision API) rather than
     * storing it, so this doesn't go through Cloudinary at all. */
    suspend fun searchByImage(uri: Uri, country: String? = null): ApiResult<List<ListingDto>> = withContext(Dispatchers.IO) {
        val tempFile = File.createTempFile("search_by_image_", ".jpg", context.cacheDir)
        try {
            compressToJpeg(uri, tempFile, maxDimension = 1200)
            val part = MultipartBody.Part.createFormData("image", tempFile.name, tempFile.asRequestBody("image/jpeg".toMediaType()))
            val res = api.searchByImage(part, country)
            if (res.isSuccessful && res.body() != null) ApiResult.Success(res.body()!!) else parseError(res)
        } catch (e: Exception) {
            ApiResult.Error(genericErrorText())
        } finally {
            tempFile.delete()
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
