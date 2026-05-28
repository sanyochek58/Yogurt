plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(ktorLibs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
}

group = "com.yogurtvpn"
version = "1.0.0-SNAPSHOT"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

kotlin {
    jvmToolchain(21)
}
dependencies {
    // Зависимости ktor
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.netty)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.server.callLogging)
    implementation(ktorLibs.server.cors)
    implementation(ktorLibs.server.statusPages)
    implementation(ktorLibs.serialization.kotlinx.json)

    // Зависимости JWT
    implementation("io.ktor:ktor-server-auth:3.4.3")
    implementation("io.ktor:ktor-server-auth-jwt:3.4.3")
    implementation("com.auth0:java-jwt:4.4.0")

    // Зависимости БД (Exposed ORM и PostgreSQL JDBC)
    implementation("org.jetbrains.exposed:exposed-core:0.61.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.61.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.61.0")
    implementation("org.postgresql:postgresql:42.7.4")

    // Зависимости миграций (Flyway)
    implementation("org.flywaydb:flyway-core:9.22.3")

    // Connection pool
    implementation("com.zaxxer:HikariCP:6.2.1")

    // Хэширование паролей
    implementation("org.mindrot:jbcrypt:0.4")

    // Логирование
    implementation(libs.logback.classic)

    // Email(smtp)
    implementation("org.simplejavamail:simple-java-mail:8.12.2")

    // Тесты
    testImplementation(kotlin("test"))
    testImplementation(ktorLibs.server.testHost)
}
