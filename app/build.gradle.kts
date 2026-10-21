import java.util.Properties
plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
val signing = Properties().apply { rootProject.file("keystore.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) } }
fun secret(key: String): String? = System.getenv(key) ?: signing.getProperty(key)
val hasReleaseKey = secret("KEYSTORE_FILE") != null
android {
    namespace = "com.deploydulupulangnanti.coachfitness"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.deploydulupulangnanti.coachfitness"
        minSdk = 26
        targetSdk = 36
        versionCode = (System.getenv("VERSION_CODE") ?: "9").toInt()
        versionName = "1.2.1"
        testInstrumentationRunner = "com.deploydulupulangnanti.coachfitness.CharacterRenderInstrumentation"
    }
    signingConfigs { if (hasReleaseKey) create("release") {
        storeFile = file(requireNotNull(secret("KEYSTORE_FILE")))
        storePassword = secret("KEYSTORE_PASSWORD")
        keyAlias = secret("KEY_ALIAS")
        keyPassword = secret("KEY_PASSWORD")
    } }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (hasReleaseKey) signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    lint { abortOnError = true; checkReleaseBuilds = true }
}
dependencies { testImplementation("junit:junit:4.13.2") }
tasks.register<JavaExec>("exportMovementFrames") {
    dependsOn("compileDebugUnitTestKotlin")
    classpath = files(layout.buildDirectory.dir("tmp/kotlin-classes/debug"),layout.buildDirectory.dir("tmp/kotlin-classes/debugUnitTest")) + configurations.getByName("debugUnitTestRuntimeClasspath")
    mainClass.set("com.deploydulupulangnanti.coachfitness.MovementFrameExporter")
    args(layout.buildDirectory.file("reports/movement-frames.json").get().asFile.absolutePath)
}
tasks.matching { it.name == "validateSigningRelease" || it.name == "packageReleaseBundle" }.configureEach {
    doFirst { check(hasReleaseKey) { "Release signing is required. Configure keystore.properties or signing environment variables." } }
}
