package com.soukmar.app.data.remote

import com.soukmar.app.data.remote.dto.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

/** Mirrors soukmar-backend's /api/auth routes. Grows as more screens land —
 * listings/chat/etc. endpoints will be added alongside their features. */
interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<MessageResponse>

    @POST("auth/resend-verification")
    suspend fun resendVerification(@Body body: ForgotPasswordRequest): Response<MessageResponse>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest): Response<MessageResponse>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): Response<MessageResponse>

    @PUT("auth/change-password")
    suspend fun changePassword(@Body body: ChangePasswordRequest): Response<MessageResponse>

    @GET("auth/me")
    suspend fun me(): Response<UserDto>

    @GET("listings")
    suspend fun getListings(@QueryMap params: Map<String, String>): Response<ListingsResponseDto>

    @GET("catalog/categories/{category}/full")
    suspend fun getCategoryFull(@Path("category") category: String): Response<CategoryFullResponse>

    @GET("listings/{id}")
    suspend fun getListing(@Path("id") id: String): Response<ListingDto>

    @POST("listings")
    suspend fun createListing(@Body body: ListingUpsertRequest): Response<ListingDto>

    @PUT("listings/{id}")
    suspend fun updateListing(@Path("id") id: String, @Body body: ListingUpsertRequest): Response<ListingDto>

    @Multipart
    @POST("upload")
    suspend fun uploadImages(@Part images: List<MultipartBody.Part>): Response<UploadResponseDto>

    @GET("favorites")
    suspend fun getFavorites(): Response<List<ListingDto>>

    @POST("favorites/{id}")
    suspend fun addFavorite(@Path("id") id: String): Response<FavoriteRecordDto>

    @DELETE("favorites/{id}")
    suspend fun removeFavorite(@Path("id") id: String): Response<SuccessDto>

    @GET("reviews/can-review/{listingId}")
    suspend fun canReview(@Path("listingId") listingId: String): Response<CanReviewResponse>

    @POST("reviews")
    suspend fun submitReview(@Body body: ReviewSubmitRequest): Response<ReviewDto>

    @POST("reports")
    suspend fun submitReport(@Body body: ReportRequest): Response<ReportRecordDto>

    @POST("chat/conversations")
    suspend fun createConversation(@Body body: CreateConversationRequest): Response<ConversationDto>
}
