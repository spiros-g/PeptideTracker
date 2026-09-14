import java.io.File
import java.util.Base64
import javax.imageio.ImageIO

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val generatedArtworkResDir =
    layout.buildDirectory.dir("generated/artworkRes").get().asFile

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
                    "${expectedWidth}x${expectedHeight}"
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
        versionCode = 22
        versionName = "3.2.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets["main"].res.srcDir(generatedArtworkResDir)

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

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.05.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.0")
    implementation("androidx.navigation:navigation-compose:2.9.0")

    implementation("com.google.android.gms:play-services-ads:24.3.0")
    implementation("com.google.android.ump:user-messaging-platform:3.2.0")

    testImplementation("junit:junit:4.13.2")
}
