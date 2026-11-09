-keep class kotlinx.serialization.** { *; }
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class app.kitsune.stream.** {
    *** Companion;
}
-keepclasseswithmembers class app.kitsune.stream.** {
    kotlinx.serialization.KSerializer serializer(...);
}
