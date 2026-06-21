import org.gradle.api.JavaVersion

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.sqldelight)
}

kotlin {
    applyDefaultHierarchyTemplate()

    androidTarget()
    
    iosArm64()
    iosSimulatorArm64()
    iosX64()

    jvm()

    js {
        browser()
    }

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.datetime)
            implementation(libs.multiplatform.settings)
            api(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serializationKotlinxJson)
        }
        
        val wasmJsMain by getting {
            dependencies {
                implementation(libs.ktor.client.js)
            }
        }

        jsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
        
        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.appcompat)
            implementation(libs.ktor.client.okhttp)
            implementation("androidx.work:work-runtime-ktx:2.9.0")
        }
        
        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
            implementation(libs.ktor.client.darwin)
        }
        
        jvmMain.dependencies {
            implementation("app.cash.sqldelight:sqlite-driver:2.0.2")
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.multiplatform.settings.test)
        }
    }
}

android {
    namespace = "com.gantlab.satori.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

sqldelight {
    databases {
        create("SatoriDatabase") {
            packageName.set("com.gantlab.satori.db")
            // Powoduje, że kod nie jest generowany dla commonMain, 
            // a tylko dla konkretnych platform źródłowych
            srcDirs.setFrom("src/androidMain/sqldelight")
        }
    }
}
