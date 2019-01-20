import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

buildscript {
  repositories { jcenter() }
  dependencies {
    classpath("com.github.jengelman.gradle.plugins:shadow:4.0.1")
  }
}

plugins {
  id("org.jetbrains.kotlin.jvm").version("1.3.10")
  id("com.github.johnrengelman.shadow").version("4.0.1")

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

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Jar> {
  manifest {
    attributes(mapOf(
            "Main-Class" to "chess.AppKt"
    ))
  }
}

tasks.withType<ShadowJar> {
  baseName = project.name
  classifier = ""
  version = ""
}