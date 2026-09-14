import java.io.File
import java.util.Base64
import java.util.Properties
import javax.imageio.ImageIO

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

val generatedArtworkResDir =
    layout.buildDirectory.dir("generated/artworkRes").get().asFile

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.isFile) {
        keystorePropertiesFile.inputStream().use(::load)
    }
}
val hasReleaseKeystore = keystorePropertiesFile.isFile

val generateArtwork by tasks.registering {
    val artworkSourceDir = file("src/main/assets/artwork")
    inputs.dir(artworkSourceDir)
    outputs.dir(generatedArtworkResDir)

    doLast {
        fun rebuildPng(
            partsDir: File,
            outputName: String,
            expectedWidth: Int,
            expectedHeight: Int
        ) {
            val parts = partsDir.listFiles { file ->
                file.isFile && file.extension == "b64"
            }?.sortedBy { it.name }.orEmpty()

            check(parts.isNotEmpty()) {
                "No artwork chunks found in ${partsDir.path}"
            }

            val encoded = buildString {
                parts.forEach { append(it.readText().trim()) }
            }

            val bytes = Base64.getDecoder().decode(encoded)
            val output = File(
                generatedArtworkResDir,
                "drawable-nodpi/$outputName"
            )
            output.parentFile.mkdirs()
            output.writeBytes(bytes)

            val image = ImageIO.read(output)
                ?: error("Generated artwork is not a readable PNG: ${output.path}")

            check(image.width == expectedWidth && image.height == expectedHeight) {
                "Unexpected artwork dimensions for $outputName: " +
                    "${image.width}x${image.height}, expected " +
                    "$expectedWidth x $expectedHeight"
            }

            check(output.length() > 5_000L) {
                "Generated artwork is unexpectedly small: ${output.path}"
            }
        }

        rebuildPng(
            partsDir = File(artworkSourceDir, "icon"),
            outputName = "app_icon_generated.png",
            expectedWidth = 160,
            expectedHeight = 160
        )

        rebuildPng(
            partsDir = File(artworkSourceDir, "vial"),
            outputName = "peptide_vial_generated.png",
            expectedWidth = 192,
            expectedHeight = 288
        )
    }
}

android {
    namespace = "gr.peptidetracker.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "gr.peptidetracker.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 27
        versionName = "4.4.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets["main"].res.srcDir(generatedArtworkResDir)

    signingConfigs {
        if (hasReleaseKeystore) {
            create("release") {
                storeFile = file(requireNotNull(keystoreProperties.getProperty("storeFile")))
                storePassword = requireNotNull(keystoreProperties.getProperty("storePassword"))
                keyAlias = requireNotNull(keystoreProperties.getProperty("keyAlias"))
                keyPassword = requireNotNull(keystoreProperties.getProperty("keyPassword"))
            }
        }
    }

    buildTypes {
        release {
            if (hasReleaseKeystore) {
                signingConfig = signingConfigs.getByName("release")
            }
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

    kotlinOptions { jvmTarget = "17" }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

tasks.named("preBuild").configure {
    dependsOn(generateArtwork)
}

ksp {
    arg("room.schemaLocation", file("schemas").absolutePath)
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.05.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
    implementation("androidx.navigation:navigation-compose:2.9.0")
    implementation("androidx.work:work-runtime:2.11.2")
    implementation("androidx.biometric:biometric:1.1.0")
    implementation("androidx.fragment:fragment-ktx:1.9.0")

    implementation("androidx.room:room-runtime:2.8.5")
    implementation("androidx.room:room-ktx:2.8.5")
    ksp("androidx.room:room-compiler:2.8.5")


    testImplementation("junit:junit:4.13.2")
}
