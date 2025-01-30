package io.felipepoliveira.fpmtoolkit.tests.mocks.dao.mail

import io.felipepoliveira.fpmtoolkit.io.felipepoliveira.fpmtoolkit.mail.MailContentType
import io.felipepoliveira.fpmtoolkit.io.felipepoliveira.fpmtoolkit.mail.MailRecipient
import io.felipepoliveira.fpmtoolkit.io.felipepoliveira.fpmtoolkit.mail.MailSenderProvider

class MockedMailSenderProvider : MailSenderProvider {
    override fun sendMail(
        title: String,
        body: ByteArray,
        contentType: MailContentType,
        vararg mailRecipients: MailRecipient
    ) {
        println("Sending mocked mail: $title")
    }
}