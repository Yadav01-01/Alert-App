plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id ("kotlin-kapt")
    id ("com.google.dagger.hilt.android")
    id ("com.google.firebase.firebase-perf")
}

android {
    namespace = "com.alert.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.alert.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            val BASE_URL = project.property("BASE_URL")
            buildConfigField("String", "BASE_URL", "${BASE_URL}")

            val OPENAI_API_KEY = project.property("OPENAI_API_KEY")
            buildConfigField("String", "OPENAI_API_KEY", "${OPENAI_API_KEY}")

            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            val BASE_URL = project.property("BASE_URL")
            buildConfigField("String", "BASE_URL", "${BASE_URL}")

            val OPENAI_API_KEY = project.property("OPENAI_API_KEY")
            buildConfigField("String", "OPENAI_API_KEY", "${OPENAI_API_KEY}")
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        jvmToolchain(11)
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.3")
    implementation("androidx.activity:activity:1.9.3")
    implementation("com.google.firebase:protolite-well-known-types:18.0.0")
    implementation("androidx.camera:camera-core:1.4.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    //chat_emogi
    implementation ("com.vanniktech:emoji-google:0.6.0")
    implementation ("org.apache.commons:commons-lang3:3.4")

    //Dimen
    implementation("com.intuit.ssp:ssp-android:1.0.5")
    implementation("com.intuit.sdp:sdp-android:1.0.6")

    implementation ("de.hdodenhof:circleimageview:3.1.0")

    //OtpView
    implementation("com.github.aabhasr1:OtpView:v1.1.2-ktx")
    implementation("io.github.chaosleung:pinview:1.4.4")
    implementation("com.github.Dhaval2404:ImagePicker:v2.1")

    //Glide
    implementation("com.github.bumptech.glide:glide:4.11.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.11.0")

    // multi select calendar
    implementation ("com.github.prolificinteractive:material-calendarview:2.0.1")

    implementation ("com.github.chthai64:SwipeRevealLayout:1.4.0")

    implementation ("joda-time:joda-time:2.11.1")

    //firebase
   /* implementation ("com.google.firebase:firebase-analytics:21.2.2")
    implementation ("com.google.firebase:firebase-messaging:23.1.2")
    // Add the dependency for the Firebase SDK for Google Analytics
    implementation ("com.google.firebase:firebase-analytics")
    //Cloud Messaging
    implementation ("com.google.firebase:firebase-messaging")
    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))
    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation ("com.google.firebase:firebase-crashlytics:<latest-version>")
    implementation ("com.google.firebase:firebase-perf")

    implementation ("com.google.firebase:firebase-auth-ktx:22.1.1")*/

    /*    // Firebase SDKs with explicit versions
    implementation("com.google.firebase:firebase-auth-ktx:23.2.1")
//    implementation("com.google.firebase:firebase-firestore-ktx:25.2.1")
    implementation("com.google.firebase:firebase-analytics-ktx:22.5.0")
    implementation("com.google.firebase:firebase-crashlytics-ktx:19.4.4")
    implementation("com.google.firebase:firebase-messaging-ktx:24.1.2")
    implementation("com.google.firebase:firebase-perf-ktx:21.0.5")

    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:34.7.0"))

    // Declare the dependency for the Cloud Firestore library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-firestore")*/

    // Import the Firebase BoM
//    implementation(platform("com.google.firebase:firebase-bom:34.7.0"))
    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))

    // When using the BoM, you don't specify versions in Firebase library dependencies

    // Add the dependency for the Firebase SDK for Google Analytics
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-perf")


    //dagger hilt
    implementation("com.google.dagger:hilt-android:2.52")
    kapt ("com.google.dagger:hilt-android-compiler:2.52")
    kapt ("androidx.hilt:hilt-compiler:1.0.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0-alpha03")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
    kapt("androidx.lifecycle:lifecycle-compiler:2.3.1")
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.4.0")
    implementation("androidx.activity:activity-ktx:1.3.1")
    // Coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.10")

    // loader latest
    implementation ("com.airbnb.android:lottie:3.4.0")

    implementation ("com.google.android.gms:play-services-auth:20.7.0")

    ////agora voice calling
    implementation ("io.agora.rtc:full-sdk:3.5.0")

    implementation ("com.google.android.gms:play-services-maps:18.2.0")
    implementation ("com.google.android.gms:play-services-location:21.2.0")
    implementation ("com.google.android.gms:play-services-auth:21.1.1")
    implementation("com.google.android.libraries.places:places:2.4.0")
    implementation ("com.android.volley:volley:1.2.1")
    implementation ("com.google.maps.android:android-maps-utils:2.2.5")

    //exoplayer
    implementation ("com.google.android.exoplayer:exoplayer:2.14.0")

    implementation ("de.hdodenhof:circleimageview:3.1.0")

    implementation ("com.google.android.gms:play-services-location:21.3.0")

    implementation ("com.hbb20:ccp:2.7.0")

    implementation ("com.prolificinteractive:material-calendarview:1.6.1")





    //PayU
    //implementation("in.payu:payu-checkout-pro:2.8.0")



}