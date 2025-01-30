package io.felipepoliveira.fpmtoolkit.features.users

import io.felipepoliveira.fpmtoolkit.beans.context.ContextualBeans
import io.felipepoliveira.fpmtoolkit.io.felipepoliveira.fpmtoolkit.commons.io.WildcardString
import io.felipepoliveira.fpmtoolkit.io.felipepoliveira.fpmtoolkit.commons.io.getLocalizedResourceAsInputStream
import io.felipepoliveira.fpmtoolkit.io.felipepoliveira.fpmtoolkit.mail.MailContentType
import io.felipepoliveira.fpmtoolkit.io.felipepoliveira.fpmtoolkit.mail.MailRecipient
import io.felipepoliveira.fpmtoolkit.io.felipepoliveira.fpmtoolkit.mail.MailRecipientType
import io.felipepoliveira.fpmtoolkit.io.felipepoliveira.fpmtoolkit.mail.MailSenderProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Contains the business logic for mail delivery related to the user feature
 */
@Component
class UserMail @Autowired constructor(
    private val contextualBeans: ContextualBeans,
) {

    /**
     * Store the mail sender provided instance used
     */
    private val mailSenderProvider = contextualBeans.userMailSenderProvider()

    /**
     * Send a mail to the given user containing a welcome message so it can access the platform
     */
    fun sendWelcomeMail(user: UserModel) {
        val mailContentInputStream = getLocalizedResourceAsInputStream(
            "/mails/features/users/welcome/welcome.{{region}}.html", user.preferredRegion
        )

        // Replace the wildcards from the mail
        val mailContent = WildcardString(mailContentInputStream)
        mailContent.add("link", contextualBeans.webappUrl())
        mailContent.add("user.presentationName", user.presentationName)

        // Send the mail
        mailSenderProvider.sendMailAsync(
            "Saudações do FPM Toolkit",
            mailContent.toString().toByteArray(),
            MailContentType.HTML,
            MailRecipient(user.primaryEmail, MailRecipientType.TO)
        )
    }
}