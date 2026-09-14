import java.awt.BasicStroke
import java.awt.Color
import java.awt.RenderingHints
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val generatedVialResDir = layout.buildDirectory.dir("generated/vialRes").get().asFile

val generatePeptideVialPng by tasks.registering {
    val outputFile = File(generatedVialResDir, "drawable-nodpi/peptide_vial_generated.png")
    outputs.file(outputFile)

    doLast {
        outputFile.parentFile.mkdirs()

        val width = 248
        val height = 456
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g = image.createGraphics()

        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

            fun rounded(
                x: Float,
                y: Float,
                w: Float,
                h: Float,
                arc: Float,
                fill: Color,
                stroke: Color? = null,
                strokeWidth: Float = 1f
            ) {
                val shape = RoundRectangle2D.Float(x, y, w, h, arc, arc)
                g.color = fill
                g.fill(shape)
                if (stroke != null) {
                    g.stroke = BasicStroke(strokeWidth)
                    g.color = stroke
                    g.draw(shape)
                }
            }

            // Glass body
            rounded(
                54f, 114f, 140f, 296f, 32f,
                Color(22, 31, 43, 246),
                Color(210, 231, 248, 230),
                4f
            )
            rounded(
                62f, 122f, 124f, 280f, 26f,
                Color(38, 69, 95, 78),
                Color(110, 185, 230, 55),
                2f
            )

            // Left glass highlight
            rounded(
                66f, 140f, 13f, 242f, 6f,
                Color(255, 255, 255, 38)
            )

            // Blank label
            rounded(
                70f, 198f, 108f, 118f, 10f,
                Color(248, 251, 255, 255),
                Color(214, 225, 236, 235),
                2f
            )
            rounded(
                72f, 284f, 104f, 30f, 8f,
                Color(222, 234, 245, 70)
            )

            // Lyophilized cake
            rounded(
                86f, 342f, 76f, 34f, 12f,
                Color(249, 249, 245, 255),
                Color(216, 216, 208, 255),
                2f
            )
            rounded(
                92f, 348f, 64f, 8f, 4f,
                Color(255, 255, 255, 135)
            )

            // Neck
            rounded(
                88f, 86f, 72f, 58f, 10f,
                Color(24, 36, 50, 255),
                Color(220, 235, 248, 220),
                2f
            )

            // Rubber stopper
            rounded(
                82f, 62f, 84f, 42f, 12f,
                Color(78, 86, 96, 255),
                Color(165, 180, 195, 225),
                2f
            )

            // Aluminium cap
            rounded(
                74f, 32f, 100f, 48f, 16f,
                Color(202, 210, 218, 255),
                Color(250, 252, 254, 255),
                2f
            )
            rounded(
                86f, 40f, 76f, 20f, 8f,
                Color(110, 122, 134, 255),
                Color(235, 241, 246, 220),
                2f
            )
            rounded(
                80f, 38f, 10f, 36f, 5f,
                Color(255, 255, 255, 72)
            )
        } finally {
            g.dispose()
        }

        check(ImageIO.write(image, "png", outputFile)) {
            "PNG writer unavailable"
        }
        check(outputFile.length() > 1_000L) {
            "Generated vial PNG is unexpectedly small"
        }
    }
}

android {
    namespace = "gr.peptidetracker.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "gr.peptidetracker.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 16
        versionName = "2.2.6"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets["main"].res.srcDir(generatedVialResDir)

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true; buildConfig = true }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}

tasks.named("preBuild").configure {
    dependsOn(generatePeptideVialPng)
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
