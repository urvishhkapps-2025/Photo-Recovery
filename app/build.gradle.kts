import org.gradle.kotlin.dsl.annotationProcessor

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.Blue.photorecovery"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.Blue.photorecovery"
        minSdk = 24
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
    kotlinOptions {
        jvmTarget = "11"
    }

    viewBinding {
        this.enable = true
    }

    sourceSets {
        getByName("main") {
            res {
                srcDirs(
                    "src\\main\\res",
                    "src\\main\\res\\layouts",
                    "src\\main\\res",
                    "src\\main\\res\\layouts\\activity",
                    "src\\main\\res",
                    "src\\main\\res\\layouts\\activity\\layout",
                    "src\\main\\res",
                    "src\\main\\res\\layouts\\fragment",
                    "src\\main\\res",
                    "src\\main\\res\\layouts\\fragment\\layout",
                    "src\\main\\res",
                    "src\\main\\res\\layouts\\dialogs",
                    "src\\main\\res",
                    "src\\main\\res\\layouts\\dialogs\\layout",
                    "src\\main\\res",
                    "src\\main\\res\\layouts\\recyclerItem",
                    "src\\main\\res",
                    "src\\main\\res\\layouts\\recyclerItem\\layout",
                )
            }
        }
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.multidex)

    /*****************************
     ********  Animation *********
     *****************************/
    implementation(libs.lottie)
    implementation("com.daimajia.easing:library:2.4@aar")
    implementation("com.daimajia.androidanimations:library:2.4@aar")


    /***********************************
     ******** Shimmer Animation *********
     ***********************************/
    implementation(libs.shimmer)

    /************************
     ******** Glide *********
     ************************/
    implementation(libs.glide)
    annotationProcessor (libs.compiler)

    /***********************
     ******** Gson *********
     ***********************/
    implementation(libs.gson)

    implementation(libs.androidx.documentfile)

}