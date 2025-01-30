package io.felipepoliveira.fpmtoolkit.tests


import io.felipepoliveira.fpmtoolkit.beans.context.ContextualBeans
import io.felipepoliveira.fpmtoolkit.features.users.UserMail
import io.felipepoliveira.fpmtoolkit.io.felipepoliveira.fpmtoolkit.mail.MailSenderProvider
import io.felipepoliveira.fpmtoolkit.tests.mocks.dao.mail.MockedMailSenderProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.ComponentScans
import org.springframework.context.annotation.Configuration
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import java.util.*
import javax.sql.DataSource

@Configuration
@ComponentScans(value = [
    ComponentScan("io.felipepoliveira.fpmtoolkit.features"),
    ComponentScan("io.felipepoliveira.fpmtoolkit.tests.mocks")
])
class UnitTestsConfiguration : ContextualBeans {

    /**
     * Default instance jakarta validation
     */
    @Bean
    fun localValidatorFactoryBean() = LocalValidatorFactoryBean()

    override fun jpaDataSource(): DataSource = throw Exception("Bean not needed on unit test context")
    override fun jpaProperties(): Properties = throw Exception("Bean not needed on unit test context")
    override fun userMailSenderProvider(): MailSenderProvider = MockedMailSenderProvider()

    override fun webappUrl(): String = "mocked webappUrl"

}