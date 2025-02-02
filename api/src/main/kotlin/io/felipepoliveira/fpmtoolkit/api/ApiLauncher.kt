package io.felipepoliveira.fpmtoolkit.api

import io.felipepoliveira.fpmtoolkit.api.config.ApiConfiguration
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation.Import

@SpringBootApplication(
    exclude = [DataSourceAutoConfiguration::class]
)
@Import(ApiConfiguration::class)
class ApiLauncher

fun main(args: Array<String>) {
    SpringApplication.run(ApiLauncher::class.java, *args)
}