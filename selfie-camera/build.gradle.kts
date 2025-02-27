import dependencies.Dependencies
import dependencies.Versions

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}

apply(from = "../config/quality.gradle")

android {
    namespace = "org.odk.collect.selfiecamera"

    compileSdk = Versions.android_compile_sdk

    defaultConfig {
        minSdk = Versions.android_min_sdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    coreLibraryDesugaring(Dependencies.desugar)

    implementation(project(":androidshared"))
    implementation(project(":strings"))
    implementation(project(":permissions"))
    implementation(project(":external-app"))
    implementation(project(":analytics"))

    implementation(Dependencies.camerax_core)
    implementation(Dependencies.camerax_view)
    implementation(Dependencies.camerax_lifecycle)
    implementation(Dependencies.camerax_video)
    implementation(Dependencies.camerax_camera2)
    implementation("com.google.guava:guava:33.0.0-android") // Guava is a dependency required by CameraX. It shouldn't be used in any other context and should be removed when no longer necessary.
    implementation(Dependencies.dagger)
    kapt(Dependencies.dagger_compiler)

    testImplementation(project(":androidtest"))

    testImplementation(Dependencies.androidx_test_ext_junit)
    testImplementation(Dependencies.robolectric)
    testImplementation(Dependencies.hamcrest)
    testImplementation(Dependencies.androidx_test_espresso_core)
}
