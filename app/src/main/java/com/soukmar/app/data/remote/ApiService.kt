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

    @POST("auth/phone/send-code")
    suspend fun sendPhoneCode(): Response<MessageResponse>

    @POST("auth/phone/verify")
    suspend fun verifyPhoneCode(@Body body: PhoneVerifyRequest): Response<MessageResponse>

    @HTTP(method = "DELETE", path = "auth/account", hasBody = true)
    suspend fun deleteAccount(@Body body: DeleteAccountRequest): Response<MessageResponse>

    @GET("auth/id-verification")
    suspend fun getIdVerificationStatus(): Response<IdVerificationStatusDto?>

    @POST("auth/id-verification")
    suspend fun submitIdVerification(@Body body: IdVerificationSubmitRequest): Response<IdVerificationStatusDto>

    @GET("listings")
    suspend fun getListings(@QueryMap params: Map<String, String>): Response<ListingsResponseDto>

    @GET("catalog/categories/{category}/full")
    suspend fun getCategoryFull(@Path("category") category: String): Response<CategoryFullResponse>

    @GET("listings/{id}")
    suspend fun getListing(@Path("id") id: String): Response<ListingDto>

    @GET("listings/{id}/similar")
    suspend fun getSimilarListings(@Path("id") id: String): Response<List<ListingDto>>

    /** Live listing count per category in a country — shown on the homepage's category tiles. */
    @GET("stats/categories")
    suspend fun getCategoryCounts(@Query("country") country: String): Response<Map<String, Int>>

    /** Towns and villages of a country matching [q] (GeoNames data) — city field suggestions. */
    @GET("places")
    suspend fun searchPlaces(@Query("country") country: String, @Query("q") q: String, @Query("limit") limit: Int = 30): Response<List<PlaceHitDto>>

    @GET("listings/interests")
    suspend fun getInterests(@Query("country") country: String): Response<List<InterestDto>>

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

    @GET("listings/{id}/funnel")
    suspend fun getFunnel(@Path("id") id: String): Response<ListingFunnelDto>

    @POST("listings/{id}/bump")
    suspend fun bumpListing(@Path("id") id: String): Response<ListingDto>

    @GET("listings/{id}/boost-status")
    suspend fun getBoostStatus(@Path("id") id: String): Response<BoostStatusDto>

    @POST("listings/{id}/boost-request")
    suspend fun requestBoost(@Path("id") id: String, @Body body: BoostRequestBody): Response<BoostRequestDto>

    @POST("listings/{id}/extend")
    suspend fun extendListing(@Path("id") id: String): Response<ListingDto>

    @DELETE("listings/{id}")
    suspend fun deleteListing(@Path("id") id: String): Response<SuccessDto>

    @Multipart
    @POST("upload")
    suspend fun uploadImages(
        @Part images: List<MultipartBody.Part>,
        @Part("type") type: okhttp3.RequestBody
    ): Response<UploadResponseDto>

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

    @GET("auth/email-preferences")
    suspend fun getEmailPreferences(): Response<EmailPreferencesDto>

    @PUT("auth/email-preferences")
    suspend fun setEmailPreferences(@Body body: EmailPreferencesDto): Response<EmailPreferencesDto>

    @GET("users/me/blocked")
    suspend fun getBlockedUsers(): Response<List<BlockedUserDto>>

    /** Everything stored about the account (GDPR Art. 15/20) as raw JSON, saved to a file by the screen. */
    @GET("auth/export")
    suspend fun exportMyData(): Response<okhttp3.ResponseBody>

    @POST("users/{id}/block")
    suspend fun blockUser(@Path("id") id: String): Response<BlockStatusDto>

    @DELETE("users/{id}/block")
    suspend fun unblockUser(@Path("id") id: String): Response<BlockStatusDto>

    @POST("users/{id}/follow")
    suspend fun followUser(@Path("id") id: String): Response<FollowStatusDto>

    @DELETE("users/{id}/follow")
    suspend fun unfollowUser(@Path("id") id: String): Response<FollowStatusDto>

    @GET("users/me/following")
    suspend fun getFollowing(): Response<List<FollowedUserDto>>

    @POST("reports")
    suspend fun submitReport(@Body body: ReportRequest): Response<ReportRecordDto>

    @GET("reports/admin")
    suspend fun getAdminReports(): Response<List<AdminReportDto>>

    @PATCH("reports/admin/{id}")
    suspend fun updateAdminReport(@Path("id") id: String, @Body body: AdminReportUpdateRequest): Response<AdminReportDto>

    @GET("admin/users")
    suspend fun getAdminUsers(@QueryMap params: Map<String, String>): Response<List<AdminUserDto>>

    @PATCH("admin/users/{id}")
    suspend fun updateAdminUserRole(@Path("id") id: String, @Body body: AdminUserRoleRequest): Response<okhttp3.ResponseBody>

    @GET("admin/activity")
    suspend fun getAdminActivity(@QueryMap params: Map<String, String>): Response<List<AdminActivityDto>>

    @GET("admin/analytics")
    suspend fun getAdminAnalytics(@QueryMap params: Map<String, String>): Response<AdminAnalyticsDto>

    @GET("admin/security-events")
    suspend fun getAdminSecurityEvents(): Response<List<SecurityEventDto>>

    @PUT("listings/{id}")
    suspend fun updateListingPremium(@Path("id") id: String, @Body body: ListingPremiumUpdateRequest): Response<ListingDto>

    @GET("admin/id-verifications")
    suspend fun getAdminIdVerifications(): Response<List<AdminIdVerificationDto>>

    @PATCH("admin/id-verifications/{id}")
    suspend fun updateAdminIdVerification(@Path("id") id: String, @Body body: AdminIdVerificationUpdateRequest): Response<AdminIdVerificationDto>

    @GET("admin/boost-requests")
    suspend fun getAdminBoostRequests(): Response<List<BoostRequestDto>>

    @PATCH("admin/boost-requests/{id}")
    suspend fun updateAdminBoostRequest(@Path("id") id: String, @Body body: BoostRequestReviewRequest): Response<BoostRequestDto>

    // Listing review workflow (staff only): admin/moderation/*
    @GET("admin/moderation/summary")
    suspend fun getModerationSummary(): Response<ModerationSummaryDto>

    @GET("admin/moderation/listings")
    suspend fun getModerationQueue(
        @Query("status") status: String,
        @Query("country") country: String? = null,
        @Query("category") category: String? = null,
        @Query("q") q: String? = null,
        @Query("sort") sort: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ModerationQueueDto>

    @GET("admin/moderation/listings/{id}")
    suspend fun getModerationDetail(@Path("id") id: String): Response<ModerationDetailDto>

    /** action: approve | reject | request-changes | suspend | requeue */
    @POST("admin/moderation/listings/{id}/{action}")
    suspend fun decideListing(@Path("id") id: String, @Path("action") action: String, @Body body: ModerationDecisionRequest): Response<ModerationDecisionResponseDto>

    @POST("admin/moderation/events/{id}/resend-email")
    suspend fun resendModerationMail(@Path("id") id: String): Response<ModerationResendResponseDto>

    @Multipart
    @POST("listings/search-by-image")
    suspend fun searchByImage(
        @Part image: MultipartBody.Part,
        @Query("country") country: String? = null
    ): Response<List<ListingDto>>

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

    @PATCH("saved-searches/{id}")
    suspend fun updateSavedSearch(@Path("id") id: String, @Body body: SavedSearchCreateRequest): Response<SavedSearchDto>

    @DELETE("saved-searches/{id}")
    suspend fun deleteSavedSearch(@Path("id") id: String): Response<SuccessDto>
}
