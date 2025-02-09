package io.felipepoliveira.fpmtoolkit.mail

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

    init {
        // assert that the directory is always created
        if (!directory.exists()) {
            directory.mkdirs()
        }
    }

    /**
     * Return the extension that should be applied into the mail content file depending on the given
     * Content Type of the mail
     */
    private fun getMailFileExtensions(mailContentType: MailContentType): String {
        return when (mailContentType) {
            MailContentType.HTML -> ".html"
        }
    }

    /**
     * Sanitize the given input string to a always valid directory path value
     */
    private fun sanitizePathName(input: String, replacement: Char = '_'): String {
        // Define invalid characters for common file systems (Windows, Linux, macOS)
        val invalidChars = "<>:\"/\\|?*".toCharArray().toSet()

        // Replace invalid characters and trim spaces
        val sanitized = input.map { if (it in invalidChars || it.isISOControl()) replacement else it }
            .joinToString("")
            .trim()

        // Ensure the name is not empty or just dots (".", "..")
        return when {
            sanitized.isBlank() -> "default"
            sanitized.all { it == '.' } -> "default"
            else -> sanitized
        }
    }


    override fun sendMail(
        title: String,
        body: ByteArray,
        contentType: MailContentType,
        vararg mailRecipients: MailRecipient
    ) {

        // validate mail recipients
        if (mailRecipients.isEmpty()) {
            throw IllegalArgumentException("At least 1 mail recipient should be given")
        }

        // create the mocked mail directory
        val dirName = sanitizePathName(title + "_${System.currentTimeMillis()}")
        val mockedMailDir = File("${directory.path}${File.separator}${dirName}")
        if (mockedMailDir.exists()) {
            throw Exception("An unexpected error occur while creating the output mail directory: The directory ${mockedMailDir.path} already exists")
        }
        mockedMailDir.mkdirs()

        // create the recipients identification file and output the file
        var recipientsContent = StringBuffer()
        for (recipient in mailRecipients) {
            recipientsContent.append("${recipient.type}: ${recipient.emailAddress}+${System.lineSeparator()}")
        }
        val recipientsContentFile = File("${mockedMailDir.path}${File.separator}recipients.txt")
        recipientsContentFile.writeText(recipientsContent.toString())

        // output the content of the mail in the file
        val mailContentFile = File(
            "${mockedMailDir.path}${File.separator}mail.${getMailFileExtensions(contentType)}"
        )
        mailContentFile.writeBytes(body)

        // print the output (for development purposes)
        println("Mail ${title} mocked delivery can be found at: ${mockedMailDir.path}")
    }
}