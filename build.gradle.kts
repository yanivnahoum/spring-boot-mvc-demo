plugins {
    java
    id("org.springframework.boot") version "2.4.2"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("io.freefair.lombok") version "5.3.0"
}

group = "com.att.training.spring.boot"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    runtimeOnly("mysql:mysql-connector-java")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("net.ttddyy:datasource-proxy:1.7")

    implementation(platform("org.testcontainers:testcontainers-bom:1.15.1"))
    testImplementation("org.testcontainers:mysql")
    testImplementation("org.testcontainers:junit-jupiter")

}

tasks {
    withType<JavaCompile>().configureEach {
        options.release.set(11)
    }

    generateLombokConfig {
        isEnabled = false
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("skipped", "failed")
            showStandardStreams = true
        }
    }
}
