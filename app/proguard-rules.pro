# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class com.soukmar.app.data.remote.dto.** {
    *** Companion;
}
-keep,includedescriptorclasses class com.soukmar.app.**$$serializer { *; }
-keepclassmembers class com.soukmar.app.** {
    *** Companion;
}

# ---- Release (R8) ----
# Generic signatures and annotations are read at runtime by Retrofit and kotlinx.serialization.
-keepattributes Signature, InnerClasses, EnclosingMethod, RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault
-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile

# Retrofit: the API interface is only used through a dynamic proxy; suspend functions need their Continuation type.
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keep interface com.soukmar.app.data.remote.ApiService { *; }
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# kotlinx.serialization: DTOs, their generated serializers and enums used in DTOs.
-keep,includedescriptorclasses class com.soukmar.app.**$$serializer { *; }
-keepclassmembers @kotlinx.serialization.Serializable class com.soukmar.app.** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class com.soukmar.app.** { kotlinx.serialization.KSerializer serializer(...); }

# OkHttp / Okio (optional TLS providers are not on Android).
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Socket.IO / Engine.IO client (reflection-free, but uses org.json and optional okhttp bridges).
-keep class io.socket.** { *; }
-keep class org.json.** { *; }
-dontwarn io.socket.**

# WebRTC (native code looks up Java classes and methods by name through JNI).
-keep class org.webrtc.** { *; }
-keep class io.getstream.webrtc.** { *; }
-dontwarn org.webrtc.**

# osmdroid map library.
-keep class org.osmdroid.** { *; }
-dontwarn org.osmdroid.**

# Hilt/Dagger, Coil, Firebase ship their own consumer rules; keep our entry points and the FCM service.
-keep class com.soukmar.app.fcm.SoukMarFirebaseMessagingService { *; }
-keep class com.soukmar.app.SoukMarApplication { *; }
-keep class com.soukmar.app.MainActivity { *; }
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**
