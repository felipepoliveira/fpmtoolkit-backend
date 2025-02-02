package io.felipepoliveira.fpmtoolkit.beans.context

import io.felipepoliveira.fpmtoolkit.features.users.UserMail
import io.felipepoliveira.fpmtoolkit.io.felipepoliveira.fpmtoolkit.mail.MailSenderProvider
import java.util.Properties
import javax.sql.DataSource

/**
 * The application contains some beans that its instance depends on the context that the application is being running.
 * That beans are abstracted by this interface. Everytime a contextual bean is needed it should be encapsulated in this
 * interface and this interface should be injected on the other components of the application.
 */
interface ContextualBeans {
    /**
     * Return the authentication token used in the token signature
     */
    fun authenticationTokenSecretKey(): ByteArray

    /**
     * Return the DataSource used in the JPA implementation for the DAO interface
     * @see DataSource
     */
    fun jpaDataSource(): DataSource

    /**
     * Return the properties used in the JPA implementation for the DAO interface
     */
    fun jpaProperties(): Properties

    /**
     * Return the instance used to handle user mail delivery for user mail
     */
    fun userMailSenderProvider(): MailSenderProvider

    /**
     * Return the URL to the platform WebApp
     */
    fun webappUrl(): String
}