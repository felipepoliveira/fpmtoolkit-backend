package io.felipepoliveira.fpmtoolkit.features.users

import io.felipepoliveira.fpmtoolkit.beans.context.ContextualBeans
import io.felipepoliveira.fpmtoolkit.cache.CacheHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.Duration

/**
 * Interface used to abstract user related operation on a cache database
 */
@Component
class UserCache @Autowired constructor(
    contextualBeans: ContextualBeans
) {

    // Store the cacheHandler from the contextual beans
    private val cacheHandler = contextualBeans.cacheHandler()

    private fun executeOnTimeout(key: String, callback: () -> Unit, ttl: Duration) {

        // do not execute anything if the operation is on timeout
        if (cacheHandler.keyExists(key)) {
            return
        }

        // execute the callback operation
        callback()

        // include the key on the cache to set the timeout
        cacheHandler.put(key, ttl.toMillis().toString(), ttl)
    }

    /**
     * Return a flag indicating if the password recovery mail is on timeout
     */
    fun executeOnPasswordMailTimeout(primaryEmail: String, callback: () -> Unit) {
        executeOnTimeout("PWD_RCV_$primaryEmail", callback, Duration.ofMinutes(5))
    }

    fun executeOnPrimaryMailChangeTimeout(requester: UserModel, callback: () -> Unit) {
        executeOnTimeout("PRIMARY_EMAIL_CHANGE_${requester.uuid}", callback, Duration.ofMinutes(3))
    }

    fun executeOnPrimaryMailConfirmationTimeout(requester: UserModel, callback: () -> Unit) {
        executeOnTimeout("PRIMARY_EMAIL_CONFIRMATION_${requester.uuid}", callback, Duration.ofMinutes(3))
    }

}