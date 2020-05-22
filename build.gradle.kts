plugins {
    java
    id("org.springframework.boot") version "2.3.0.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("io.freefair.lombok") version "5.1.0"
}

group = "com.att.training.spring.boot"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.generateLombokConfig {
    isEnabled = false
}

repositories {
    mavenCentral()
}

// Lock down mysql version
extra["mysql.version"] = "8.0.20"
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.google.guava:guava:29.0-jre")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    runtimeOnly("mysql:mysql-connector-java")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("org.springframework.cloud:spring-cloud-starter-contract-stub-runner:2.2.2.RELEASE")

    val testContainersVersion = "1.14.2"
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
