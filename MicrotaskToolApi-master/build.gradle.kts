import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.6.7"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    war
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
    kotlin("plugin.jpa") version "1.6.21"
}

group = "com.spatialcollective"
version = "0.0.6"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()

}

allprojects {
    repositories {
        maven ("https://jitpack.io")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.projectlombok:lombok:1.18.24")
    implementation("org.flywaydb:flyway-core:8.5.13")
    implementation("org.flywaydb:flyway-mysql:8.5.13")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("mysql:mysql-connector-java")
    providedRuntime("org.springframework.boot:spring-boot-starter-tomcat")
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.2.0")
    implementation("io.arrow-kt:arrow-core:1.1.2")
    implementation("io.github.nefilim.kjwt:kjwt-core:0.5.3")
    
    // DPW Integration Dependencies
    implementation("com.github.oshi:oshi-core:6.4.0") // System monitoring
    implementation("org.springframework.boot:spring-boot-starter-mail") // Email alerts
    implementation("org.springdoc:springdoc-openapi-ui:1.6.14") // API documentation
    implementation("org.springframework.boot:spring-boot-starter-security") // API key authentication
    implementation("org.apache.poi:poi:5.2.3") // Excel export
    implementation("org.apache.poi:poi-ooxml:5.2.3") // Excel export
    
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

//gradlew bootJar
//gradlew war