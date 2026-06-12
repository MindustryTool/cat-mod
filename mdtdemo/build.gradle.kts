plugins {
    `java-library`
}

val mindustryVersion = rootProject.property("mindustryVersion") as String

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
    compileOnly("org.jetbrains:annotations:26.0.1")

    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("org.slf4j:slf4j-simple:2.0.9")

    implementation("org.codejargon.feather:feather:1.0")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}
