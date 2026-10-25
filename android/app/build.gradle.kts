plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
    namespace = "com.aaryan.rounds"
    compileSdk = 34
    defaultConfig { applicationId = "com.aaryan.rounds"; minSdk = 26; targetSdk = 34; versionCode = 1; versionName = "1.0" }
    // Fixed signing key so new builds install over the old one (keeps your data)
    signingConfigs { getByName("debug") { storeFile = file("../debug.keystore"); storePassword = "android"; keyAlias = "androiddebugkey"; keyPassword = "android" } }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
android.sourceSets.getByName("main").assets.srcDirs("../../www")
dependencies { implementation("androidx.webkit:webkit:1.11.0") }
