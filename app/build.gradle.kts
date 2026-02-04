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

    signingConfigs {
        create("release"){
            storeFile = file("E:\\ProjectData\\Android\\key\\app.jks")
            storePassword = System.getenv("STORE_PASSWORD") ?: "123456"
            keyAlias = System.getenv("KEY_ALIAS") ?: "key"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "123456"

        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
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
	implementation(project(":utils"))
	implementation("com.google.code.gson:gson:2.13.2")
	implementation("com.tencent:mmkv:2.3.0")
	implementation("org.xutils:xutils:3.9.0")
	// 颜色选择控件
	api("net.margaritov.preference.colorpicker.ColorPickerPreference:ColorPickerPreference:1.0.0")

    // 设备兼容框架：https://github.com/getActivity/DeviceCompat
    implementation("com.github.getActivity:DeviceCompat:2.3")
// 权限请求框架：https://github.com/getActivity/XXPermissions
    implementation("com.github.getActivity:XXPermissions:28.0")


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