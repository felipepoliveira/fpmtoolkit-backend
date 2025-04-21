package io.felipepoliveira.fpmtoolkit.features.organizationMemberInvite

import io.felipepoliveira.fpmtoolkit.beans.context.ContextualBeans
import io.felipepoliveira.fpmtoolkit.commons.i18n.I18nRegion
import io.felipepoliveira.fpmtoolkit.commons.io.WildcardString
import io.felipepoliveira.fpmtoolkit.commons.io.getLocalizedResourceAsInputStream
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationModel
import io.felipepoliveira.fpmtoolkit.features.users.UserModel
import io.felipepoliveira.fpmtoolkit.mail.MailContentType
import io.felipepoliveira.fpmtoolkit.mail.MailRecipient
import io.felipepoliveira.fpmtoolkit.mail.MailRecipientType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Contains the business logic for mail delivery related to organization member invite features
 */
@Component
class OrganizationMemberInviteMail @Autowired constructor(
    private val contextualBeans: ContextualBeans
) {
    /**
     * Store the mail sender provider used in this component
     */
    private val mailSenderProvider = contextualBeans.userMailSenderProvider()

    fun sendInviteMail(mailLanguage: I18nRegion, sender: OrganizationModel, recipientEmail: String, invitationUrl: String) {
        val mailContentInputStream = getLocalizedResourceAsInputStream(
            "/mails/features/organizationInvites/invite/invite.{{region}}.html",
            mailLanguage
        )

        // Replace the wildcards from the mail
        val mailContent = WildcardString(mailContentInputStream)
        mailContent.add("organization.name", sender.presentationName)
        mailContent.add("invitationUrl", invitationUrl)

        // Send the mail
        mailSenderProvider.sendMailAsync(
            "FPM Toolkit - Convite de Organização - ${sender.presentationName}",
            mailContent.toString().toByteArray(),
            MailContentType.HTML,
            MailRecipient(recipientEmail, MailRecipientType.TO)
        )
    }
}