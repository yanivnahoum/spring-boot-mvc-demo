plugins {
    java
    id("org.springframework.boot") version "3.5.8"
    id("io.spring.dependency-management") version "1.1.7"
    id("io.freefair.lombok") version "9.1.0"
}

group = "com.att.training.spring.boot"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

// Overriding vulnerable dependency 3.17.0
extra["commons-lang3.version"] = "3.20.0"
val mockitoAgent = configurations.create("mockitoAgent")
dependencies {
    val guavaVersion = "33.5.0-jre"
    val springdocVersion = "2.8.13"
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocVersion")
    implementation("com.google.guava:guava:$guavaVersion")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    runtimeOnly("io.micrometer:micrometer-registry-graphite")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("com.fasterxml.jackson.module:jackson-module-mrbean")
    mockitoAgent("org.mockito:mockito-core") { isTransitive = false }
}

tasks {
    withType<JavaCompile>().configureEach {
        with(options) {
            compilerArgs.add("-Xlint:all,-processing,-auxiliaryclass")
        }
    }

    test {
        useJUnitPlatform()
        jvmArgs("-javaagent:${mockitoAgent.asPath}")
        testLogging {
            events("passed", "skipped", "failed")
            showStandardStreams = true
        }
    }
}
