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

    @PUT("auth/profile")
    suspend fun updateProfile(@Body body: ProfileUpdateRequest): Response<UserDto>

    @PUT("auth/profile")
    suspend fun updateProfileImage(@Body body: ProfileImageUpdateRequest): Response<UserDto>

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

    @PUT("listings/{id}")
    suspend fun updateListingStatus(@Path("id") id: String, @Body body: ListingStatusUpdateRequest): Response<ListingDto>

    @GET("listings/user/mine")
    suspend fun getMyListings(): Response<List<ListingDto>>

    @GET("listings/{id}/view-stats")
    suspend fun getViewStats(@Path("id") id: String): Response<ViewStatsDto>

    @POST("listings/{id}/bump")
    suspend fun bumpListing(@Path("id") id: String): Response<ListingDto>

    @DELETE("listings/{id}")
    suspend fun deleteListing(@Path("id") id: String): Response<SuccessDto>

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

    @GET("reviews/user/{userId}")
    suspend fun getReviewsForUser(@Path("userId") userId: String): Response<ReviewsForUserResponse>

    @GET("users/{id}/profile")
    suspend fun getSellerProfile(@Path("id") id: String): Response<SellerProfileDto>

    @GET("users/{id}/listings")
    suspend fun getSellerListings(@Path("id") id: String): Response<List<ListingDto>>

    @POST("reports")
    suspend fun submitReport(@Body body: ReportRequest): Response<ReportRecordDto>

    @GET("reports/admin")
    suspend fun getAdminReports(): Response<List<AdminReportDto>>

    @PATCH("reports/admin/{id}")
    suspend fun updateAdminReport(@Path("id") id: String, @Body body: AdminReportUpdateRequest): Response<AdminReportDto>

    @POST("chat/conversations")
    suspend fun createConversation(@Body body: CreateConversationRequest): Response<ConversationDto>

    @GET("chat/conversations")
    suspend fun getConversations(): Response<List<ConversationDto>>

    @GET("chat/conversations/{id}/messages")
    suspend fun getMessages(@Path("id") id: String): Response<List<MessageDto>>

    @GET("notifications")
    suspend fun getNotifications(): Response<List<NotificationDto>>

    @GET("notifications/unread-count")
    suspend fun getUnreadNotificationCount(): Response<UnreadCountResponse>

    @PATCH("notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: String): Response<NotificationDto>

    @PATCH("notifications/read-all")
    suspend fun markAllNotificationsRead(): Response<SuccessDto>

    @POST("push/fcm-register")
    suspend fun registerFcmToken(@Body body: FcmTokenRequest): Response<OkDto>

    @POST("push/fcm-unregister")
    suspend fun unregisterFcmToken(@Body body: FcmTokenRequest): Response<OkDto>

    @GET("saved-searches")
    suspend fun getSavedSearches(): Response<List<SavedSearchDto>>

    @POST("saved-searches")
    suspend fun createSavedSearch(@Body body: SavedSearchCreateRequest): Response<SavedSearchDto>

    @DELETE("saved-searches/{id}")
    suspend fun deleteSavedSearch(@Path("id") id: String): Response<SuccessDto>
}
