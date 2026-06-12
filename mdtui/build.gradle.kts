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
    compileOnly("Anuken:Mindustry:$mindustryVersion:dependencies")
    compileOnly("org.jetbrains:annotations:26.0.1")
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("org.slf4j:slf4j-simple:2.0.9")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    testImplementation("Anuken:Mindustry:$mindustryVersion:dependencies")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.1")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}
