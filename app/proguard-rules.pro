# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Retrolambda
-dontwarn java.lang.invoke.*
-dontwarn **$$Lambda$*

# EventBus 3.0
-keepattributes *Annotation*
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

-dontwarn com.xuhao.android.libsocket.**
-keep class com.xuhao.android.socket.impl.abilities.** { *; }
-keep class com.xuhao.android.socket.impl.exceptions.** { *; }
-keep class com.xuhao.android.socket.impl.EnvironmentalManager { *; }
-keep class com.xuhao.android.socket.impl.BlockConnectionManager { *; }
-keep class com.xuhao.android.socket.impl.UnBlockConnectionManager { *; }
-keep class com.xuhao.android.socket.impl.SocketActionHandler { *; }
-keep class com.xuhao.android.socket.impl.PulseManager { *; }
-keep class com.xuhao.android.socket.impl.ManagerHolder { *; }
-keep class com.xuhao.android.socket.interfaces.** { *; }
-keep class com.xuhao.android.socket.sdk.** { *; }
 # 枚举类不能被混淆
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class com.xuhao.android.socket.sdk.OkSocketOptions$* {
    *;
}
-keep class com.xuhao.android.libsocket.sdk.OkSocketSSLConfig$* {
    *;
}