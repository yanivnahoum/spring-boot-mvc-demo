plugins {
    java
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
    id("io.freefair.lombok") version "8.10"
}

group = "com.att.training.spring.boot"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

dependencies {
    val guavaVersion = "33.3.0-jre"
    val springdocVersion = "2.6.0"
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocVersion")
    implementation("com.google.guava:guava:$guavaVersion")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    runtimeOnly("io.micrometer:micrometer-registry-graphite")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("com.fasterxml.jackson.module:jackson-module-mrbean")
}

tasks {
    withType<JavaCompile>().configureEach {
        with(options) {
            release = 21
            compilerArgs.add("-Xlint:all,-processing,-auxiliaryclass")
        }
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
            showStandardStreams = true
        }
    }
}
