package io.felipepoliveira.fpmtoolkit.features.organizationMemberInvite

import io.felipepoliveira.fpmtoolkit.beans.context.ContextualBeans
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class OrganizationMemberInviteCache @Autowired constructor(
    private val contextualBeans: ContextualBeans,
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
     * Execute an operation passed by the callback parameter using the cache timeout system. Read this method to check
     * the current used timeout
     */
    fun executeOnOrganizationMemberInviteMailTimeout(organization: OrganizationModel, memberEmail: String, callback: () -> Unit) {
        executeOnTimeout(
            "ORG_MEMBER_INVITE_org-id-${organization.uuid};memberEmail-$memberEmail",
            callback,
            Duration.ofMinutes(5)
        )
    }

}