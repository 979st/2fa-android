-optimizationpasses 5
-allowaccessmodification
-dontpreverify
-repackageclasses ''

-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

-keepattributes *Annotation*
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keep class kotlin.reflect.** { *; }

-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class app.ninesevennine.twofactorauthenticator.**$$serializer { *; }
-keepclassmembers class app.ninesevennine.twofactorauthenticator.** {
    *** Companion;
}
-keepclasseswithmembers class app.ninesevennine.twofactorauthenticator.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep class org.bouncycastle.** { *; }
-keepclassmembers class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keepclassmembers class androidx.compose.** {
    *;
}

-keep @androidx.compose.runtime.Composable class * { *; }
-keep @androidx.compose.runtime.Composable interface * { *; }

-keepclassmembers class * {
    @androidx.navigation.** *;
}

-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

-keepclassmembers class app.ninesevennine.twofactorauthenticator.** {
    <init>(...);
}

-keepclassmembers class * {
    @androidx.room.** *;
}

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/allocation/variable

-keep public class * extends java.lang.Exception

-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**