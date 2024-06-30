plugins {
    java
    id("org.springframework.boot") version "3.3.1"
    id("io.spring.dependency-management") version "1.1.5"
    id("io.freefair.lombok") version "8.6"
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
    val guavaVersion = "33.2.1-jre"
    val springCloudVersion = "2023.0.2"
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.google.guava:guava:$guavaVersion")
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion"))

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    runtimeOnly("com.mysql:mysql-connector-j")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.cloud:spring-cloud-starter-contract-stub-runner")
    testImplementation("org.testcontainers:mysql")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("com.squareup.okhttp3:mockwebserver")
    testImplementation("io.projectreactor:reactor-test")
}

tasks {
    withType<JavaCompile>().configureEach {
        with(options) {
            release = 21
            compilerArgs.add("-Xlint:all,-processing,-auxiliaryclass")
        }
    }

    test {
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
}
