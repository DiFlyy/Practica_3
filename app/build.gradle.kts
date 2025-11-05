plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.20"
}



android {
    namespace = "com.example.practica_3"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.practica_3"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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

    buildFeatures {
        compose = true     // 👈 Activa soporte para Jetpack Compose
        viewBinding = true // 👈 Mantiene ViewBinding para XML
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3" // Usa versión estable de Compose
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
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Jetpack Compose
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation("androidx.compose.ui:ui:1.5.3")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.3")
    implementation("androidx.compose.material3:material3:1.2.0")

    debugImplementation("androidx.compose.ui:ui-tooling:1.5.3")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.3")

    // Glide (miniaturas y carga de imágenes)
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // PhotoView (zoom para visor de imágenes)
    implementation("io.github.chrisbanes:PhotoView:2.3.0")

    // Tests
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.3")
}
