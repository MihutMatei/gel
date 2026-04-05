import org.gradle.internal.os.OperatingSystem
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.*

val javafxVersion = "21.0.2"
val javafxPlatform = when {
    OperatingSystem.current().isLinux -> "linux"
    OperatingSystem.current().isWindows -> "win"
    OperatingSystem.current().isMacOsX -> "mac"
    else -> error("Unsupported OS for JavaFX desktop map host")
}

fun loadDotEnv(rootDir: File): Map<String, String> {
    val envFile = File(rootDir, ".env")
    if (!envFile.exists()) return emptyMap()
    val props = Properties()
    envFile.inputStream().use(props::load)
    return props.stringPropertyNames().associateWith { key -> props.getProperty(key).orEmpty() }
}

val dotEnv = loadDotEnv(rootProject.projectDir)
val mapTilerApiKey = (dotEnv["MAPTILER_API_KEY"] ?: System.getenv("MAPTILER_API_KEY") ?: "").trim()

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.20"
}

configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
        force("org.jetbrains.androidx.savedstate:savedstate:1.4.0")
        force("org.jetbrains.androidx.savedstate:savedstate-compose:1.4.0")
        force("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-savedstate:2.10.0")
        force("org.jetbrains.androidx.core:core-bundle:1.0.1")
        force("org.jetbrains.androidx.navigation:navigation-compose:2.9.2")
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm()

    js {
        browser()
        binaries.executable()
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.core.bundle)
            implementation(libs.androidx.savedstate)
            implementation(libs.kotlinx.coroutinesCore)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serializationJson)
            implementation(libs.ktor.clientCore)
            implementation(libs.ktor.clientContentNegotiation)
            implementation(libs.ktor.serializationKotlinxJson)
            implementation(libs.navigation.compose)
            implementation("org.jetbrains.compose.material:material-icons-extended:1.7.1")
            implementation(projects.shared)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation("org.openjfx:javafx-base:$javafxVersion:$javafxPlatform")
            implementation("org.openjfx:javafx-graphics:$javafxVersion:$javafxPlatform")
            implementation("org.openjfx:javafx-controls:$javafxVersion:$javafxPlatform")
            implementation("org.openjfx:javafx-media:$javafxVersion:$javafxPlatform")
            implementation("org.openjfx:javafx-web:$javafxVersion:$javafxPlatform")
            implementation("org.openjfx:javafx-swing:$javafxVersion:$javafxPlatform")
        }
    }
}

android {
    namespace = "kronos.project"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "kronos.project"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        resValue("string", "maptiler_api_key", mapTilerApiKey)
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "kronos.project.MainKt"
        if (mapTilerApiKey.isNotBlank()) {
            jvmArgs("-DMAPTILER_API_KEY=$mapTilerApiKey")
        }
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "kronos.project"
            packageVersion = "1.0.0"
        }
    }
}

tasks.withType<JavaExec>().configureEach {
    val javafxJars = configurations
        .findByName("jvmRuntimeClasspath")
        ?.incoming
        ?.artifacts
        ?.artifactFiles
        ?.filter { it.name.contains("javafx") }
        ?: files()

    doFirst {
        val jars = javafxJars.files.toList()
        if (jars.isNotEmpty()) {
            jvmArgs(
                "--module-path", jars.joinToString(File.pathSeparator),
                "--add-modules", "javafx.controls,javafx.web,javafx.swing,javafx.graphics,javafx.base",
            )
        }
    }
}
