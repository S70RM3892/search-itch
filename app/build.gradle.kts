plugins {
    id("com.android.application")
}

android {
    namespace = "com.zat.quicklookup"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.zat.quicklookup"
        minSdk = 23          // 選択メニュー連携(PROCESS_TEXT)は Android 6.0 以降
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
