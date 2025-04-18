group = "io.felipepoliveira.fpmtoolkit"
version = "1.0-SNAPSHOT"

plugins {
    kotlin("jvm") version "2.0.0"

    // The `kotlin-allopen` plugin is used to open non-final classes, functions, and properties in Kotlin classes.
    // This is particularly useful in the context of frameworks like Spring and Hibernate, which require certain
    // methods and properties to be open for proxying and AOP (Aspect-Oriented Programming) to work correctly.
    // By applying this plugin and specifying the `@Entity` annotation for classes you want to open,
    // you allow these classes to be extended and overridden by proxy classes, ensuring proper functionality
    // with Spring and Hibernate.
    id("org.jetbrains.kotlin.plugin.allopen") version "1.9.20"

    //    The no-arg compiler plugin generates an additional zero-argument constructor for classes with a specific annotation.
    //    The generated constructor is synthetic, so it can't be directly called from Java or Kotlin, but it can be called using reflection.
    //    This allows the Java Persistence API (JPA) to instantiate a class although it doesn't have the zero-parameter constructor
    //    from Kotlin or Java point of view (see the description of kotlin-jpa plugin below).
    kotlin("plugin.noarg") version "2.0.20"
}

allOpen {
    // Marks all below to be all open
    annotation("org.springframework.context.annotation.Configuration")
    annotation("org.springframework.stereotype.Repository")
    annotation("org.springframework.stereotype.Service")
}

noArg {
    annotation("jakarta.persistence.Entity")
}

repositories {
    mavenCentral()
}

dependencies {

    // https://mvnrepository.com/artifact/com.auth0/java-jwt
    implementation("com.auth0:java-jwt:4.4.0")

    // https://mvnrepository.com/artifact/com.fasterxml.jackson.module/jackson-module-kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")

    // Hibernate Core Relocation
    // Hibernate's core ORM functionality
    // https://mvnrepository.com/artifact/org.hibernate/hibernate-core
    implementation("org.hibernate:hibernate-core:6.6.5.Final")

    // HikariCP
    // HikariCP is a "zero-overhead" production ready JDBC connection pool.
    // https://mvnrepository.com/artifact/com.zaxxer/HikariCP
    implementation("com.zaxxer:HikariCP:6.2.1")

    // Jakarta Persistence API
    // Jakarta Persistence API
    // https://mvnrepository.com/artifact/jakarta.persistence/jakarta.persistence-api
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")

    // Jakarta Validation API
    // https://mvnrepository.com/artifact/jakarta.validation/jakarta.validation-api
    implementation("jakarta.validation:jakarta.validation-api:3.1.0")

    // JBoss Logging 3
    // The JBoss Logging Framework
    // https://mvnrepository.com/artifact/org.jboss.logging/jboss-logging
    implementation("org.jboss.logging:jboss-logging:3.6.1.Final")

    // Jedis
    // Jedis is a blazingly small and sane Redis java client.
    // https://mvnrepository.com/artifact/redis.clients/jedis
    implementation("redis.clients:jedis:5.2.0")

    // MySQL Connector Java
    // MySQL Connector/J is a JDBC Type 4 driver, which means that it is pure Java implementation of the MySQL protocol
    // and does not rely on the MySQL client libraries. This driver supports auto-registration with the Driver Manager,
    // standardized validity checks, categorized SQLExceptions, support for large update counts, support for local and
    // offset date-time variants from the java.time package, support for JDBC-4.x XML processing, support for per
    // connection client information and support for the NCHAR, NVARCHAR
    // https://mvnrepository.com/artifact/mysql/mysql-connector-java
    implementation("mysql:mysql-connector-java:8.0.33")

    // Spring Boot Starter Web
    // Starter for building web, including RESTful, applications using Spring MVC. Uses Tomcat as the default embedded container
    // https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-web
    implementation("org.springframework.boot:spring-boot-starter-validation:3.4.2")

    // Sprint Data JPA
    // Spring Data module for JPA repositories
    // https://mvnrepository.com/artifact/org.springframework.data/spring-data-jpa
    implementation("org.springframework.data:spring-data-jpa:3.4.2")

    // Spring Security Crypto
    // Spring Security
    // https://mvnrepository.com/artifact/org.springframework.security/spring-security-crypto
    implementation("org.springframework.security:spring-security-crypto:6.4.2")

    // Spring Transaction
    // Support for programmatic and declarative transaction management for classes that implement special interfaces or any POJO
    // https://mvnrepository.com/artifact/org.springframework/spring-tx
    implementation("org.springframework:spring-tx:6.2.2")

    // --- TEST DEPENDENCIES
    // Kotest
    // https://mvnrepository.com/artifact/io.kotest/kotest-assertions-core-jvm
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.9.1")
    // https://mvnrepository.com/artifact/io.kotest/kotest-extensions-spring
    implementation("io.kotest:kotest-extensions-spring:4.4.3")
    // https://mvnrepository.com/artifact/io.kotest/kotest-runner-junit5-jvm
    testImplementation("io.kotest:kotest-runner-junit5-jvm:5.9.1")

    // Spring Boot Test
    // https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-test
    testImplementation("org.springframework.boot:spring-boot-test:3.4.2")

    //Spring TestContext Framework
    // Spring Test supports the unit testing and integration testing of Spring components with JUnit or TestNG.
    // It provides consistent loading and caching of Spring ApplicationContexts and provides mock objects that you
    // can use to test your code in isolation
    // https://mvnrepository.com/artifact/org.springframework/spring-test
    testImplementation("org.springframework:spring-test:6.2.2")

}