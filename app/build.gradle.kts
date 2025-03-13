plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.flowerapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.flowerapp"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    buildToolsVersion = "34.0.0"
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)
    implementation(libs.legacy.support.v4)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.mediarouter)
    implementation(libs.viewpager2)
    implementation(libs.recyclerview)
    implementation(libs.cardview)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(platform(libs.firebase.bom))
    // Thêm các thư viện mới
    implementation(libs.jbcrypt)  // Bắt buộc cho BCrypt
    implementation(libs.firebase.storage)  // Tùy chọn cho hình ảnh
    implementation(libs.room.runtime)  // Tùy chọn cho Room
    annotationProcessor(libs.room.compiler)  // Tùy chọn cho Room
    implementation(libs.lifecycle.livedata)  // Tùy chọn cho LiveData

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}