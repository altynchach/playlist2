plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")  // Добавление поддержки KAPT
}

android {
    namespace = "com.example.playlistmaker"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.playlistmaker"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Добавление блока packagingOptions в формате Kotlin Script для исключения файлов
    packagingOptions {
        resources {
            excludes.add("LICENSE_OFL")
            excludes.add("LICENSE_UNICODE")  // Исключение дублирующегося файла LICENSE_UNICODE
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.glide)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.gson)
    implementation(libs.androidx.emoji.bundled)
    implementation(libs.androidx.emoji2.bundled)

    kapt(libs.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.v121)
    androidTestImplementation(libs.androidx.espresso.core.v361)
    implementation(libs.glide)
    annotationProcessor(libs.compiler)

    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.gson)
}
