plugins {
    java
    id("org.springframework.boot") version "2.2.5.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("io.freefair.lombok") version "4.1.6"
}

group = "com.att.training.spring.boot"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("com.google.guava:guava:28.2-jre")
    implementation("org.jetbrains:annotations:19.0.0")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    runtimeOnly("mysql:mysql-connector-java")

    testImplementation("org.springframework.boot:spring-boot-starter-test")

    val testContainersVersion = "1.13.0"
    testImplementation("org.testcontainers:mysql:$testContainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")
}

tasks.test {
    useJUnitPlatform {
        if (project.hasProperty("onlyITs")) {
            includeTags("slow")
        } else if (!project.hasProperty("withITs")) {
            excludeTags("slow")
        }
    }
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
}
