import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktor_version by project
val junit_jupiter_version by project
val logback_version by project

plugins {
    kotlin("jvm") version "1.2.30"
}

repositories {
    jcenter()
    mavenCentral()
    maven {
        url = uri("https://dl.bintray.com/kotlin/ktor")
    }
    maven {
        url = uri("https://dl.bintray.com/kotlin/kotlinx")
    }
}

dependencies {
    compile(kotlin("stdlib"))
    compile("io.ktor:ktor-server-core:$ktor_version")
    compile("io.ktor:ktor-websockets:$ktor_version")
    compile("io.ktor:ktor-server-netty:$ktor_version")
    compile("ch.qos.logback:logback-classic:$logback_version") {
        because("otherwise SLF4J emits warnings for a missing implementation")
    }
    testCompile("org.junit.jupiter:junit-jupiter-api:$junit_jupiter_version")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:$junit_jupiter_version")
}

kotlin {
    experimental.coroutines = Coroutines.ENABLE
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
    withType<Test> {
        useJUnitPlatform()
    }
}
