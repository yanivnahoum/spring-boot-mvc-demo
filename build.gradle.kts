plugins {
    java
    id("org.springframework.boot") version "2.3.0.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("io.freefair.lombok") version "5.1.0"
}

repositories {
    mavenCentral()
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

extra["rest-assured.version"] = "4.3.0"
extra["groovy.version"] = "3.0.2" // upgrading from 2.x for rest-assured
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    val swaggerVersion = "2.9.2"
    implementation("io.springfox:springfox-swagger2:$swaggerVersion")
    implementation("io.springfox:springfox-swagger-ui:$swaggerVersion")

    implementation("com.google.guava:guava:29.0-jre")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    runtimeOnly("io.micrometer:micrometer-registry-graphite")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("io.rest-assured:rest-assured")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
}
