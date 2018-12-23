
plugins {
    id("org.jetbrains.kotlin.jvm").version("1.3.10")

    application
}

repositories {
    jcenter()
}

val ktorVersion = "1.0.1"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.kodein.di:kodein-di-generic-jvm:6.0.1")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-websockets:$ktorVersion")
    implementation("io.ktor:ktor-gson:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:1.2.3")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

application {
    // Define the main class for the application
    mainClassName = "chess.AppKt"
}
