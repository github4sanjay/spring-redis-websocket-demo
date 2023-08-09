plugins {
    id("org.springframework.boot") version "3.0.4"
    id("io.spring.dependency-management") version "1.1.0"
    id("com.diffplug.spotless") version "6.11.0"
    id("io.freefair.lombok") version "6.4.3"
    id("java")
}

java.sourceCompatibility = JavaVersion.VERSION_17
val artifactoryURL: String by project

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:2022.0.1"))
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.0.2")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")

    implementation("com.github.ben-manes.caffeine:caffeine:2.8.5")

    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.signal:embedded-redis:0.8.2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

spotless {
    java {
        googleJavaFormat()
    }
    kotlinGradle {
        ktlint()
    }
}
