-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

-keepclassmembers enum com.mehmetbozkurt.questlog.** {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
    public java.lang.String name();
}

-keep class com.mehmetbozkurt.questlog.core.navigation.*RouteKey { *; }
-keep class com.mehmetbozkurt.questlog.core.navigation.*RouteKey$* { *; }

-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
    static ** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

-keep class * extends androidx.work.ListenableWorker {
    public <init>(...);
}

-dontwarn org.slf4j.**
-dontwarn javax.naming.**
