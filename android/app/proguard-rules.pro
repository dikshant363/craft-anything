# Keep JNI entry points for NativeBridge
-keepclassmembers class dev.craftanything.app.core.NativeBridge {
    public static native <methods>;
}
