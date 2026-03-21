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

    sourceSets {
        commonMain.dependencies {
            implementation(libs.sqldelight.runtime)
            implementation(libs.kotlinx.datetime)
            implementation(libs.multiplatform.settings)
        }
        
        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
        }
        
        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }
        
        jvmMain.dependencies {
            implementation("app.cash.sqldelight:sqlite-driver:2.0.2")
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        val androidUnitTest by getting {
            dependencies {
                implementation("app.cash.sqldelight:sqlite-driver:2.0.2")
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation("app.cash.sqldelight:sqlite-driver:2.0.2")
            }
        }
    }
}

android {
    namespace = "com.gantlab.satori.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

sqldelight {
    databases {
        create("SatoriDatabase") {
            packageName.set("com.gantlab.satori.db")
        }
    }
}
