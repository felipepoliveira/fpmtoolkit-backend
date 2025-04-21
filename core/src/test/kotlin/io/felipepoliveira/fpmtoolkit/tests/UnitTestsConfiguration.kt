package io.felipepoliveira.fpmtoolkit.tests


import io.felipepoliveira.fpmtoolkit.beans.context.ContextualBeans
import io.felipepoliveira.fpmtoolkit.mail.MailSenderProvider
import io.felipepoliveira.fpmtoolkit.tests.mocks.cache.MockedCacheHandler
import io.felipepoliveira.fpmtoolkit.tests.mocks.mail.MockedMailSenderProvider
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
    ComponentScan("io.felipepoliveira.fpmtoolkit.security"),
    ComponentScan("io.felipepoliveira.fpmtoolkit.tests.mocks")
])
class UnitTestsConfiguration : ContextualBeans {

    /**
     * Default instance jakarta validation
     */
    @Bean
    fun localValidatorFactoryBean() = LocalValidatorFactoryBean()
    override fun authenticationTokenSecretKey(): ByteArray  = "authenticationTokenSecretKey".toByteArray()

    override fun cacheHandler() = MockedCacheHandler()
    override fun organizationInviteTokenSecretKey(): ByteArray = "organizationInviteTokenSecretKey".toByteArray()

    override fun jpaDataSource(): DataSource = throw Exception("Bean not needed on unit test context")
    override fun jpaProperties(): Properties = throw Exception("Bean not needed on unit test context")

    override fun passwordRecoveryTokenSecretKey(): ByteArray = "passwordRecoveryTokenSecretKey".toByteArray()

    override fun primaryEmailChangeTokenSecretKey(): ByteArray = "primaryEmailChangeTokenSecretKey".toByteArray()

    override fun primaryEmailConfirmationTokenSecretKey(): ByteArray = "primaryEmailConfirmationTokenSecretKey".toByteArray()

    override fun userMailSenderProvider(): MailSenderProvider = MockedMailSenderProvider()

    override fun webappUrl(): String = "mocked webappUrl"

}