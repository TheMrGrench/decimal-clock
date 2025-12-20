# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep ViewBinding classes
-keep class * implements androidx.viewbinding.ViewBinding {
    <init>(...);
    public static *** bind(android.view.View);
    public static *** inflate(android.view.LayoutInflater);
}
