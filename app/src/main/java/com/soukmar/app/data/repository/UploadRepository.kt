package com.soukmar.app.data.repository

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.soukmar.app.data.remote.ApiService
import com.soukmar.app.data.remote.dto.ApiErrorDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File
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

    /** Copies each picked gallery Uri into a cache file (content:// streams
     * can't be re-read by OkHttp directly), uploads them all as one
     * multipart request, then cleans up — mirrors the web's UploadService
     * (POST /api/upload, Cloudinary-backed). */
    suspend fun uploadImages(uris: List<Uri>): ApiResult<List<String>> {
        val tempFiles = mutableListOf<File>()
        return try {
            val parts = uris.mapIndexed { index, uri ->
                val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"
                val tempFile = File.createTempFile("upload_${index}_", ".$ext", context.cacheDir)
                tempFiles += tempFile
                context.contentResolver.openInputStream(uri)?.use { input ->
                    tempFile.outputStream().use { output -> input.copyTo(output) }
                }
                MultipartBody.Part.createFormData("images", tempFile.name, tempFile.asRequestBody(mimeType.toMediaTypeOrNull()))
            }
            val res = api.uploadImages(parts)
            if (res.isSuccessful && res.body() != null) ApiResult.Success(res.body()!!.urls) else parseError(res)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Erreur réseau.")
        } finally {
            tempFiles.forEach { it.delete() }
        }
    }
}
