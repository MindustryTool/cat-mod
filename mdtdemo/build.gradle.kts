import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    id("com.gradleup.shadow") version "9.2.2"
}

val mindustryVersion = rootProject.property("mindustryVersion") as String

val isWindows = System.getProperty("os.name").lowercase().contains("windows")
val sdkRoot: String? = System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
    maven { url = uri("https://www.jitpack.io") }
    ivy {
        url = uri("https://github.com/")
        metadataSources { artifact() }
        patternLayout {
            artifact("/[organisation]/[module]/releases/download/[revision]/[artifact].jar")
        }
    }
}

dependencies {
    implementation(project(":mdtui"))

    compileOnly("Anuken:Mindustry:$mindustryVersion:dependencies")
    annotationProcessor("com.github.Anuken:jabel:93fde537c7")

    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("org.slf4j:slf4j-simple:2.0.9")

    implementation("org.codejargon.feather:feather:1.0")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.named<ShadowJar>("shadowJar") {
    archiveFileName.set("mdt-demo-desktop.jar")
}

val shadowJar = tasks.named<ShadowJar>("shadowJar")

tasks.register("jarAndroid") {
    description = "Build android jar"
    dependsOn("shadowJar")

    doLast {
        if (sdkRoot.isNullOrBlank() || !File(sdkRoot).exists()) {
            throw GradleException("No valid Android SDK found. Ensure ANDROID_HOME or ANDROID_SDK_ROOT is set.")
        }

        val platformDir = File(sdkRoot, "platforms")
        val platformRoot = platformDir.listFiles()
            ?.sorted()
            ?.reversed()
            ?.firstOrNull { f -> File(f, "-android.jar").exists() }

        if (platformRoot == null) {
            throw GradleException("No -android.jar found. Ensure that you have an Android platform installed.")
        }

        val d8 = if (isWindows) "d8.bat" else "d8"

        val compileFiles = configurations.compileClasspath.get().files
        val runtimeFiles = configurations.runtimeClasspath.get().files
        val androidJar = File(platformRoot, "-android.jar")

        val classpathFiles = (compileFiles + runtimeFiles + androidJar).toList()

        val command = mutableListOf<String>().apply {
            add(d8)
            classpathFiles.forEach {
                add("--classpath")
                add(it.absolutePath)
            }
            addAll(listOf("--min-api", "26", "--output", "mdt-demo-android.jar", "mdt-demo-desktop.jar"))
        }

        val process = ProcessBuilder(command)
            .directory(layout.buildDirectory.file("libs").get().asFile)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()

        val exitValue = process.waitFor()
        if (exitValue != 0) {
            throw GradleException("d8 failed with exit code $exitValue")
        }
    }
}

tasks.register<Jar>("deploy") {
    description = "Build artifact"
    dependsOn("jarAndroid")
    dependsOn("shadowJar")

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveFileName.set("mdt-demo.jar")

    from(provider {
        listOf(
            zipTree(layout.buildDirectory.file("libs/mdt-demo-desktop.jar")),
            zipTree(layout.buildDirectory.file("libs/mdt-demo-android.jar"))
        )
    })

    doLast {
        delete(layout.buildDirectory.file("libs/mdt-demo-desktop.jar"))
        delete(layout.buildDirectory.file("libs/mdt-demo-android.jar"))
    }
}

tasks.register<JavaExec>("runGame") {
    description = "Build mod, install to Mindustry mods, and launch game"
    dependsOn(shadowJar)

    doFirst {
        val modsDir = file(System.getenv("APPDATA") + "/Mindustry/mods")
        modsDir.mkdirs()
        copy {
            from(shadowJar.get().archiveFile)
            into(modsDir)
        }
        println("Copied mod to " + modsDir.resolve("mdt-demo-desktop.jar"))
    }

    standardInput = System.`in`
    workingDir = file("test")
    classpath = files("test/Mindustry.jar")
    mainClass.set("mindustry.desktop.DesktopLauncher")
}
