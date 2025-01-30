plugins {
    kotlin("jvm") version "2.0.0"
}

group = "io.felipepoliveira.fpmtoolkit"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}