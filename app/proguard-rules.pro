# Add project specific ProGuard rules here.

# WorkManager + Hilt workeri instanciraju se preko generirane tvornice (ne izravno),
# pa R8 ne smije misliti da su neiskorišteni.
-keep class * extends androidx.work.ListenableWorker { <init>(...); }

# Zadrži imena naših domenskih klasa čitljivima u eventualnim stack traceovima.
-keepnames class com.navijacisazabranom.app.data.** { *; }
