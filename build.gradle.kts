plugins {
    id("java")
    application
}

application {
    mainClass = "org.example.telegrambot.TelegramBotApplication"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // Telegram Bot API (long polling)
    implementation("org.telegram:telegrambots:6.9.7.1")

    // PostgreSQL JDBC driver
    implementation("org.postgresql:postgresql:42.7.4")

    // HikariCP connection pool
    implementation("com.zaxxer:HikariCP:5.1.0")

    // Hibernate 6 standalone (no Spring Boot)
    implementation("org.hibernate.orm:hibernate-core:6.4.4.Final")

    // Hibernate HikariCP integration
    implementation("org.hibernate.orm:hibernate-hikaricp:6.4.4.Final")

    // JPA API
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")

    // SLF4J simple implementation for logging
    implementation("org.slf4j:slf4j-simple:2.0.12")

    // JAXB API (required by Hibernate Jackson integration)
    implementation("javax.xml.bind:jaxb-api:2.3.1")

    // Liquibase
    implementation("org.liquibase:liquibase-core:4.24.0")

    // Spring Data JPA (only for JpaRepository interface)
    implementation("org.springframework.data:spring-data-jpa:3.2.4")
    implementation("org.springframework:spring-context:6.1.5")
    implementation("org.springframework:spring-orm:6.1.5")
    implementation("org.springframework:spring-tx:6.1.5")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register("runBot", JavaExec::class) {
    group = "application"
    description = "Run the Telegram bot"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "org.example.telegrambot.TelegramBotApplication"
}