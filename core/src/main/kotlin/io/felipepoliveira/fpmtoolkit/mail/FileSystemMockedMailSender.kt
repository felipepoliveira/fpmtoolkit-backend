package io.felipepoliveira.fpmtoolkit.io.felipepoliveira.fpmtoolkit.mail

import java.io.File

class FileSystemMockedMailSender(
    /**
     * Where the e-mail files will be stored
     */
    val directory: File,

    /**
     * Flag that checks if the directory will be created if not exists
     */
    val createDirectoryIfNotExists: Boolean = true,
) : MailSenderProvider {

    companion object {
        /**
         * Create a FileSystemMockedMailSender instance where the directory wil be on the temp directory of the system.
         * The user can define a subDirectory by given a specific name in subDirectory parameter like 'my_mails' so
         * a <b><code>"${System.getProperty("java.io.tmpdir")}${File.separator}$subDirectory"</code></b> will be created
         * @see FileSystemMockedMailSender
         */
        fun fromTempDir(
            subDirectory: String? = null,
            createDirectoryIfNotExists: Boolean = true
        ): FileSystemMockedMailSender {
            var path = System.getProperty("java.io.tmpdir")
            if (subDirectory != null) {
                path += "${File.separator}$subDirectory"
            }

            return FileSystemMockedMailSender(File(path), createDirectoryIfNotExists)
        }
    }


    override fun sendMail(
        title: String,
        body: ByteArray,
        contentType: MailContentType,
        vararg mailRecipients: MailRecipient
    ) {
        TODO("Not yet implemented")
    }
}