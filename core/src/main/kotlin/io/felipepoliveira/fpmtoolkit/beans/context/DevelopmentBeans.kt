package io.felipepoliveira.fpmtoolkit.beans.context

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.felipepoliveira.fpmtoolkit.cache.CacheHandler
import io.felipepoliveira.fpmtoolkit.cache.RedisCacheHandler
import io.felipepoliveira.fpmtoolkit.mail.FileSystemMockedMailSender
import io.felipepoliveira.fpmtoolkit.mail.MailSenderProvider
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import java.util.*
import javax.sql.DataSource

class DevelopmentBeans : ContextualBeans {
    override fun authenticationTokenSecretKey(): ByteArray = "authenticationTokenSecretKey".toByteArray()

    override fun cacheHandler(): CacheHandler {
        val poolConfig = JedisPoolConfig().apply {
            maxTotal = 10  // Max connections in the pool
            maxIdle = 5    // Max idle connections
            minIdle = 2    // Min idle connections
            blockWhenExhausted = true
            testOnBorrow = true
        }

        // Initialize the JedisPool with Redis server details
        return RedisCacheHandler(JedisPool(poolConfig, "localhost", 6379))
    }

    override fun organizationInviteTokenSecretKey(): ByteArray = "organizationInviteTokenSecretKey".toByteArray()


    override fun jpaDataSource(): DataSource {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:mysql://localhost:3306/fpmtoolkit?createDatabaseIfNotExist=true&autoReconnect=true&useSSL=false&useUnicode=yes&characterEncoding=UTF-8&allowPublicKeyRetrieval=true"
        config.username = "root"
        config.password = "root132"
        config.driverClassName = com.mysql.cj.jdbc.Driver::class.java.name
        config.maximumPoolSize = 10
        config.minimumIdle = 5
        config.connectionTimeout = 30_000
        config.idleTimeout = 600_000
        config.maxLifetime = 1_800_000

        return HikariDataSource(config)
    }

    override fun jpaProperties(): Properties {
        val properties = Properties()
        properties.setProperty("hibernate.dialect", org.hibernate.dialect.MySQLDialect::class.java.name)
        properties.setProperty("hibernate.hbm2ddl.auto", "update");
        properties.setProperty("hibernate.show_sql", "true");

        return properties
    }

    override fun passwordRecoveryTokenSecretKey(): ByteArray = "passwordRecoveryTokenSecretKey".toByteArray()

    override fun primaryEmailChangeTokenSecretKey(): ByteArray = "primaryEmailChangeTokenSecretKey".toByteArray()

    override fun primaryEmailConfirmationTokenSecretKey(): ByteArray = "primaryEmailConfirmationTokenSecretKey".toByteArray()

    override fun userMailSenderProvider(): MailSenderProvider = FileSystemMockedMailSender.fromTempDir("fpmtoolkit")

    override fun webappUrl() = "http://localhost:4000"
}