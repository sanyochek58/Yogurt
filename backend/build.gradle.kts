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

    // Зависимости БД (Exposed ORM и PostgreSQL JDBC)
    implementation("org.jetbrains.exposed:exposed-core:0.61.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.61.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.61.0")
    implementation("org.postgresql:postgresql:42.7.4")

    // Зависимости миграций (Flyway)
    implementation("org.flywaydb:flyway-core:10.21.0")
    implementation("org.flywaydb:flyway-database-postgresql:10.21.0")

    // Connection pool
    implementation("com.zaxxer:HikariCP:6.2.1")

    // Логирование
    implementation(libs.logback.classic)

    // Тесты
    testImplementation(kotlin("test"))
    testImplementation(ktorLibs.server.testHost)
}
