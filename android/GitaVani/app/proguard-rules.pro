# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.nikhilsi.gitavani.model.**$$serializer { *; }
-keepclassmembers class com.nikhilsi.gitavani.model.** {
    *** Companion;
}
-keepclasseswithmembers class com.nikhilsi.gitavani.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}
