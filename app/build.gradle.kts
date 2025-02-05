plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.itsmarts.SmartRouteTruckApp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.itsmarts.SmartRouteTruckApp"
        minSdk = 24
        targetSdk = 34
        versionCode = 85
        versionName = "1.2.02"
        /*versionName = "2.0.0"*/

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        resConfigs ("zz")
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "BASE_URL", "\"http://72.167.220.178:3002/\"")
            buildConfigField("boolean", "PRODUCCION", "true")
        }
        create("desarrollo") {
            isDebuggable = true
            isJniDebuggable = true
            isRenderscriptDebuggable = true
            signingConfig = signingConfigs.getByName("debug") // 👈 Agregando configuración de firma
            buildConfigField("String", "BASE_URL", "\"http://ec2-user@ec2-18-205-239-47.compute-1.amazonaws.com:3002/\"")
            buildConfigField("boolean", "PRODUCCION", "false")
        }
        create("produccion") {
            isDebuggable = true
            isJniDebuggable = true
            isRenderscriptDebuggable = true
            signingConfig = signingConfigs.getByName("debug") // 👈 Agregando configuración de firma
            buildConfigField("String", "BASE_URL", "\"http://72.167.220.178:3002/\"")
            buildConfigField("boolean", "PRODUCCION", "true")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(files("/libs/hereSDK.aar"))
    implementation("com.airbnb.android:lottie:3.5.0")
    implementation ("com.squareup.retrofit2:retrofit:2.11.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.10.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
}