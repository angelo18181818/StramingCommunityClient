plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.angel.stramingcommunityclient"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.angel.stramingcommunityclient"
        minSdk = 29
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    flavorDimensions += "deviceAbi"
    productFlavors {
        create("emulatorX86") {
            dimension = "deviceAbi"
            ndk {
                abiFilters += "x86"
            }
        }
        create("emulatorX86_64") {
            dimension = "deviceAbi"
            ndk {
                abiFilters += "x86_64"
            }
        }
        create("hboxArmv7") {
            dimension = "deviceAbi"
            ndk {
                abiFilters += "armeabi-v7a"
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    "emulatorX86Implementation"(libs.geckoview.x86)
    "emulatorX86_64Implementation"(libs.geckoview.x64)
    "hboxArmv7Implementation"(libs.geckoview.armeabi.v7a)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
