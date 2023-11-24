plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
}

android {
  namespace = "com.raycoarana.codeinputview.sample"
  compileSdk = BuildConfig.compileSdkVerion

  defaultConfig {
    applicationId = "com.raycoarana.codeinputview.sample"
    minSdk = BuildConfig.minSdkVersion
    targetSdk = BuildConfig.targetSdkVersion
    versionCode = 1 // TODO Should get from property?
    versionName = "1.0" // TODO Should get from property?

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables {
      useSupportLibrary = true
    }
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
  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
}

dependencies {
  implementation("androidx.core:core-ktx:1.12.0")
  implementation("androidx.appcompat:appcompat:1.6.1")

  implementation(project(":codeinputview"))
}
