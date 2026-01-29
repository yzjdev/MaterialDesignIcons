plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "io.github.yzjdev.mdicon"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "io.github.yzjdev.mdicon"
        minSdk = 26
        targetSdk = 36
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
// Source: https://mvnrepository.com/artifact/com.caverock/androidsvg
    implementation("com.caverock:androidsvg:1.4")
    // Source: https://mvnrepository.com/artifact/net.lingala.zip4j/zip4j
    implementation("net.lingala.zip4j:zip4j:2.11.5")
    implementation("com.google.code.gson:gson:2.13.2")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}