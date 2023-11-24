plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.facebook.testing.screenshot")
}

android {
    namespace = "com.raycoarana.codeinputview"

    compileSdk = BuildConfig.compileSdkVerion

    defaultConfig {
        testApplicationId = "com.raycoarana.codeinputview"
        minSdk = BuildConfig.minSdkVersion

        testInstrumentationRunner = "com.raycoarana.codeinputview.core.ScreenshotTestRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")

    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")

    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
