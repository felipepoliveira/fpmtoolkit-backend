package io.felipepoliveira.fpmtoolkit.beans.context

import io.felipepoliveira.fpmtoolkit.cache.CacheHandler
import io.felipepoliveira.fpmtoolkit.mail.MailSenderProvider
import java.util.*
import javax.sql.DataSource

/**
 * The application contains some beans that its instance depends on the context that the application is being running.
 * That beans are abstracted by this interface. Everytime a contextual bean is needed it should be encapsulated in this
 * interface and this interface should be injected on the other components of the application.
 */
interface ContextualBeans {
    /**
     * Return the authentication token private signature key
     */
    fun authenticationTokenSecretKey(): ByteArray

    /**
     * The cache handler implementation used in this context
     */
    fun cacheHandler(): CacheHandler

    /**
     * Return the secret key used to sign te organization invite token secret
     */
    fun organizationInviteTokenSecretKey(): ByteArray

    /**
     * Return the primary email change token private signature key
     */
    fun primaryEmailChangeTokenSecretKey(): ByteArray

    /**
     * Return the primary email confirmation token private signature key
     */
    fun primaryEmailConfirmationTokenSecretKey(): ByteArray

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
     * Return the password recovery token private signature key
     */
    fun passwordRecoveryTokenSecretKey(): ByteArray

    /**
     * Return the instance used to handle user mail delivery for user mail
     */
    fun userMailSenderProvider(): MailSenderProvider

    /**
     * Return the URL to the platform WebApp
     */
    fun webappUrl(): String
}