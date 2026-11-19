# Anti-reverse-engineering R8 obfuscation and shrinking rules
-repackageclasses 'v.o'
-allowaccessmodification
-overloadaggressively

# Strip debug/verbose logs from release binaries
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# Keep Moshi JsonClass models and generated adapters
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}
-keep @com.squareup.moshi.JsonClass class * { *; }
-keep class *JsonAdapter { *; }

# Keep Room entities & DAO
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }

# Keep MainActivity entry point
-keep class com.example.MainActivity { *; }
