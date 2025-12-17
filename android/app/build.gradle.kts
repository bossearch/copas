import java.util.Properties

val localProperties = Properties().apply {
  val file = rootProject.file("local.properties")
  if (file.exists()) {
    file.inputStream().use { load(it) }
  }
}

val copasServerUrl: String =
  localProperties.getProperty("COPAS_SERVER_URL") ?: ""

val copasAuthToken: String =
  localProperties.getProperty("COPAS_AUTH_TOKEN") ?: ""

plugins {
  id("com.android.application") version "8.5.0"
  id("org.jetbrains.kotlin.android") version "1.9.22"
}

android {
  namespace = "copas.app"
  compileSdk = 34

  defaultConfig {
    applicationId = "copas.app"
    minSdk = 26
    targetSdk = 34
    versionCode = 1
    versionName = "1.0"

    buildConfigField(
      "String",
      "DEFAULT_SERVER_URL",
      "\"$copasServerUrl\""
    )

    buildConfigField(
      "String",
      "DEFAULT_AUTH_TOKEN",
      "\"$copasAuthToken\""
    )
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  kotlinOptions { jvmTarget = "17" }

  buildFeatures {
    buildConfig = true
    compose = true
  }

  composeOptions { kotlinCompilerExtensionVersion = "1.5.8" }
}

dependencies {
  implementation("androidx.core:core-ktx:1.12.0")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
  implementation("androidx.activity:activity-compose:1.8.2")
  implementation("androidx.compose.ui:ui:1.6.0")
  implementation("androidx.compose.material3:material3:1.2.0")

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

  implementation("io.ktor:ktor-client-core:2.3.10")
  implementation("io.ktor:ktor-client-android:2.3.10")
  implementation("io.ktor:ktor-client-content-negotiation:2.3.10")
  implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.10")
  implementation("com.google.android.material:material:1.12.0")

}
