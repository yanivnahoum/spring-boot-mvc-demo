plugins {
    java
    id("org.springframework.boot") version "2.3.4.RELEASE"
    id("io.spring.dependency-management") version "1.0.10.RELEASE"
    id("io.freefair.lombok") version "5.2.1"
}

group = "com.att.training.spring.boot"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

extra["rest-assured.version"] = "4.3.1"
extra["groovy.version"] = "3.0.5" // upgrading from 2.x for rest-assured
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("io.springfox:springfox-boot-starter:3.0.0")

    implementation("com.google.guava:guava:29.0-jre")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    runtimeOnly("io.micrometer:micrometer-registry-graphite")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("io.rest-assured:rest-assured")
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
            events("passed", "skipped", "failed")
            showStandardStreams = true
        }
    }
}
