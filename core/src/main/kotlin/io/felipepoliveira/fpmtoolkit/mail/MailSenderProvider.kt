package io.felipepoliveira.fpmtoolkit.mail

/**
 * Default abstraction for mail sender provider module
 */
interface MailSenderProvider {

    /**
     * Send a mail to the given mail recipients.
     */
    fun sendMail(title: String, body: ByteArray, contentType: MailContentType, vararg mailRecipients: MailRecipient)

    fun sendMailAsync(title: String, body: ByteArray, contentType: MailContentType, vararg mailRecipients: MailRecipient) : Thread {
        val t =  Thread {
            sendMail(title, body, contentType, *mailRecipients)
        }
        t.start()
        return t
    }
}

data class MailRecipient(
    val emailAddress: String,
    val type: MailRecipientType
)

enum class MailContentType {
    HTML
}

enum class MailRecipientType {
    TO
}