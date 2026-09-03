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
