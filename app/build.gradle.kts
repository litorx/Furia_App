// carrega chave OpenAI do local.properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.serialization)
    id("com.google.gms.google-services")
    kotlin("kapt")
}

import org.gradle.internal.os.OperatingSystem
import java.util.Properties

// Carrega OPENAI_API_KEY de local.properties e remove possíveis aspas
val localProperties = Properties().apply {
    val propsFile = rootProject.file("local.properties")
    if (propsFile.exists()) load(propsFile.inputStream())
}
val rawOpenAiApiKey: String = localProperties.getProperty("OPENAI_API_KEY", "")
val openAiApiKey: String = rawOpenAiApiKey.removeSurrounding("\"", "\"")

android {
    namespace = "com.furia.furiafanapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.furia.furiafanapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        
        // Carrega a chave API do Gemini do local.properties
        val properties = com.android.build.gradle.internal.cxx.configure.gradleLocalProperties(rootDir)
        val geminiApiKey = properties.getProperty("GEMINI_API_KEY") ?: ""
        
        // Configura a chave API como BuildConfig field
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
        
        // disponibiliza chave no BuildConfig
        buildConfigField(
            "String", 
            "OPENAI_API_KEY", 
            "\"${openAiApiKey}\""
        )
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        // Habilita uso de BuildConfigField
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Firebase BoM para gerenciar versões de bibliotecas Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.1.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.dataconnect)
    // Google AI SDK para Gemini - versão mais recente
    implementation("com.google.ai.client.generativeai:generativeai:0.3.0")
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    // Compose UI text input & foundation
    implementation("androidx.compose.ui:ui-text")
    implementation("androidx.compose.foundation:foundation")
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    kapt(libs.hilt.compiler)
    implementation(libs.datastore.preferences)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    // Ktor Logging plugin para HttpClient
    implementation("io.ktor:ktor-client-logging:2.3.4")
    implementation("io.ktor:ktor-client-logging-jvm:2.3.4")
    implementation(libs.lottie.compose)
    implementation(libs.work.runtime.ktx)
    implementation(libs.firebase.messaging.ktx) {
        exclude(group = "com.google.android.gms", module = "play-services-measurement-api")
        exclude(group = "com.google.android.gms", module = "play-services-measurement-impl")
    }
    implementation(libs.accompanist.systemuicontroller)
    // Accompanist Navigation Animation para transições customizadas
    implementation("com.google.accompanist:accompanist-navigation-animation:0.30.1")
    implementation("com.google.accompanist:accompanist-swiperefresh:0.30.1")
    implementation("io.coil-kt:coil-compose:2.2.2")
    implementation(libs.pager)
    implementation(libs.pager.indicators)
    implementation("com.google.protobuf:protobuf-javalite:3.25.5")
    implementation("com.google.protobuf:protobuf-kotlin-lite:3.25.5")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

// Manual Proto generation setup
val os = OperatingSystem.current()
val protocClassifier = when {
    os.isWindows -> "windows-x86_64"
    os.isMacOsX -> "osx-x86_64"
    else -> "linux-x86_64"
}
val protoc by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

dependencies {
    add("protoc", "com.google.protobuf:protoc:3.25.5:$protocClassifier@exe")
}

val generateProto by tasks.register("generateProto") {
    group = "build"
    description = "Generate Java code from .proto files"
    val protoSrcDir = projectDir.resolve("src/main/proto")
    val outputDir = buildDir.resolve("generated/source/proto/main/java")
    inputs.dir(protoSrcDir)
    outputs.dir(outputDir)
    doLast {
        outputDir.mkdirs()
        val protocArtifact = configurations["protoc"].singleFile
        val protoFiles = fileTree(protoSrcDir).matching {
            include("**/*.proto")
        }.files.map { it.absolutePath }.toTypedArray()
        project.exec {
            commandLine(
                protocArtifact.absolutePath,
                "--java_out=lite:${outputDir.absolutePath}",
                "-I=${protoSrcDir.absolutePath}",
                *protoFiles
            )
        }
    }
}

android.sourceSets["main"].java.srcDir("$buildDir/generated/source/proto/main/java")

tasks.named("preBuild") {
    dependsOn(generateProto)
}
