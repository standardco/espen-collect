import dependencies.Dependencies
import dependencies.Versions

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}

apply(from = "../config/quality.gradle")

android {
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

    buildFeatures {
        viewBinding = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    namespace = "org.odk.collect.projects"
}

dependencies {
    coreLibraryDesugaring(Dependencies.desugar)

    implementation(project(":shared"))
    implementation(project(":androidshared"))
    implementation(project(":material"))
    implementation(Dependencies.kotlin_stdlib)
    implementation(Dependencies.androidx_appcompat)
    implementation(Dependencies.androidx_core_ktx)
    implementation(Dependencies.androidx_fragment_ktx)
    implementation(Dependencies.gson)
    implementation(Dependencies.dagger)
    kapt(Dependencies.dagger_compiler)

    testImplementation(Dependencies.junit)
    testImplementation(Dependencies.hamcrest)
    testImplementation(Dependencies.androidx_test_ext_junit)
    testImplementation(project(":test-shared"))
    testImplementation(Dependencies.androidx_test_espresso_core)
    testImplementation(Dependencies.mockito_kotlin)

    debugImplementation(Dependencies.androidx_fragment_testing) {
        exclude(group = "androidx.test", module = "monitor") // fixes issue https://github.com/android/android-test/issues/731
    }
}
