package io.felipepoliveira.fpmtoolkit.beans.context

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.felipepoliveira.fpmtoolkit.features.users.UserMail
import io.felipepoliveira.fpmtoolkit.io.felipepoliveira.fpmtoolkit.mail.FileSystemMockedMailSender
import io.felipepoliveira.fpmtoolkit.io.felipepoliveira.fpmtoolkit.mail.MailSenderProvider
import java.io.File
import java.util.*
import javax.sql.DataSource

class DevelopmentBeans : ContextualBeans {
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

    override fun userMailSenderProvider(): MailSenderProvider = FileSystemMockedMailSender.fromTempDir("fpmtoolkit")

    override fun webappUrl() = "http://localhost:4000"
}