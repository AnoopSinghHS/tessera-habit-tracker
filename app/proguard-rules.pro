# Tessera ProGuard Rules
-keepattributes *Annotation*,InnerClasses,Signature
-keep class app.tessera.habit.data.model.** { *; }
-keep class app.tessera.habit.data.local.** { *; }
-keep class app.tessera.habit.api.** { *; }
-keep class app.tessera.habit.widget.** { *; }

# Kotlinx Serialization
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}
