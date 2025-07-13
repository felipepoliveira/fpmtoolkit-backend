package io.felipepoliveira.fpmtoolkit.features.users

import io.felipepoliveira.fpmtoolkit.beans.context.ContextualBeans
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
        cacheHandler.put(key, ttl.toSeconds().toString(), ttl)
    }

    /**
     * Return a flag indicating if the password recovery mail is on timeout
     */
    fun executeOnPasswordMailTimeout(primaryEmail: String, callback: () -> Unit) {
        executeOnTimeout("PWD_RCV_$primaryEmail", callback, Duration.ofMinutes(5))
    }

    fun executeOnPrimaryMailConfirmationTimeout(requester: UserModel, callback: () -> Unit) {
        executeOnTimeout("PRIMARY_EMAIL_CONFIRMATION_${requester.uuid}", callback, Duration.ofMinutes(3))
    }

    fun onPrimaryMailChangeTimeout(requester: UserModel): Boolean {
        return cacheHandler.keyExists("PRIMARY_EMAIL_CHANGE_${requester.uuid}")
    }

    fun putPrimaryMailChangeOnTimeout(requester: UserModel) {
        val key = "PRIMARY_EMAIL_CHANGE_${requester.uuid}"
        val timeout = Duration.ofMinutes(1)
        cacheHandler.put(key, timeout.toSeconds().toString(), timeout)
    }

}