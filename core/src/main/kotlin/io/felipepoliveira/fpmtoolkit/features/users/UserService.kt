package io.felipepoliveira.fpmtoolkit.features.users

import io.felipepoliveira.fpmtoolkit.BaseService
import io.felipepoliveira.fpmtoolkit.BusinessRuleException
import io.felipepoliveira.fpmtoolkit.BusinessRulesError
import io.felipepoliveira.fpmtoolkit.beans.context.ContextualBeans
import io.felipepoliveira.fpmtoolkit.commons.io.RandomString
import io.felipepoliveira.fpmtoolkit.ext.addError
import io.felipepoliveira.fpmtoolkit.features.users.dto.*
import io.felipepoliveira.fpmtoolkitio.felipepoliveira.fpmtoolkit.features.users.dto.IsEmailAvailableDTO
import io.felipepoliveira.fpmtoolkit.security.PasswordRank
import io.felipepoliveira.fpmtoolkit.security.calculatePasswordRank
import io.felipepoliveira.fpmtoolkit.security.comparePassword
import io.felipepoliveira.fpmtoolkit.security.hashPassword
import io.felipepoliveira.fpmtoolkit.security.tokens.PasswordRecoveryTokenProvider
import io.felipepoliveira.fpmtoolkit.security.tokens.PrimaryEmailChangeTokenAndPayload
import io.felipepoliveira.fpmtoolkit.security.tokens.PrimaryEmailChangeTokenProvider
import io.felipepoliveira.fpmtoolkit.security.tokens.PrimaryEmailConfirmationTokenProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.SmartValidator
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class UserService @Autowired constructor(
    private val contextualBeans: ContextualBeans,
    private val passwordRecoveryTokenProvider: PasswordRecoveryTokenProvider,
    private val primaryEmailChangeTokenProvider: PrimaryEmailChangeTokenProvider,
    private val primaryEmailConfirmationTokenProvider: PrimaryEmailConfirmationTokenProvider,
    private val userCache: UserCache,
    private val userDAO: UserDAO,
    private val userMail: UserMail,
    validator: SmartValidator,
) : BaseService(validator) {

    @Throws(Exception::class)
    fun assertFindByUuid(uuid: String): UserModel {
        return userDAO.findByUuid(uuid) ?:
            throw Exception("An unexpected error was thrown. Could not find user identified by UUID: $uuid")
    }

    /**
     * Confirm the user primary email using the token provided by PrimaryEmailConfirmationTokenProvider
     * @see PrimaryEmailConfirmationTokenProvider
     */
    @Transactional
    fun confirmPrimaryEmailWithToken(dto: ConfirmPrimaryEmailWithTokenDTO): UserModel {

        // validate the DTO
        val validationResult = validate(dto)
        if (validationResult.hasErrors()) {
            throw BusinessRuleException(validationResult)
        }

        // Decode and validate the token
        val tokenPayload = primaryEmailConfirmationTokenProvider.validateAndDecode(dto.confirmationToken) ?:
            throw BusinessRuleException(
                BusinessRulesError.INVALID_CREDENTIALS,
                "The provided token is invalid or has expired"
            )

        // fetch the requester from the token payload
        val requester = assertFindByUuid(tokenPayload.userIdentifier)

        // update the primary email confirmation date
        requester.primaryEmailConfirmedAt = LocalDateTime.now()
        userDAO.update(requester)

        return requester
    }

    /**
     * Create a new UserModel in the database
     */
    @Throws(BusinessRuleException::class)
    @Transactional
    fun createUser(dto: CreateUserDTO): UserModel {
        // check for validation results
        val validationResult = validate(dto)
        if (validationResult.hasErrors()) {
            throw BusinessRuleException(validationResult)
        }

        // check for unsafe password
        if (!calculatePasswordRank(dto.password).isAtLeast(PasswordRank.Acceptable)) {
            throw BusinessRuleException(
                BusinessRulesError.INVALID_PASSWORD,
                reason = "The given password is not acceptable by the security standards"
            )
        }

        // check for duplicated email
        if (userDAO.findByPrimaryEmail(dto.primaryEmail) != null) {
            throw BusinessRuleException(
                BusinessRulesError.INVALID_EMAIL,
                "The given email is already in use"
            )
        }

        // Persist the user in the database
        val user = UserModel(
            id = null,
            uuid = UUID.randomUUID().toString(),
            preferredRegion = dto.preferredRegion,
            primaryEmail = dto.primaryEmail,
            hashedPassword = hashPassword(dto.password),
            presentationName = dto.presentationName,
            primaryEmailConfirmedAt = null
        )

        // Persist the user in the database
        userDAO.persist(user)

        // Send the welcome mail to the user
        userMail.sendWelcomeMail(user)

        // send the primary email confirmation to the user
        sendPrimaryEmailConfirmationMail(user)

        return user
    }

    /**
     * Find a UserModel identified by its primary email and password. If the user is not found this method
     * will throw a BusinessRuleException with BusinessRulesErrors.NotFound
     */
    @Throws(BusinessRuleException::class)
    fun findByPrimaryEmailAndPassword(dto: FindByPrimaryEmailAndPasswordDTO): UserModel {

        // validate the dto
        val validationResult = validate(dto)
        if (validationResult.hasErrors()) {
            throw BusinessRuleException(validationResult)
        }

        val userNotFoundException = BusinessRuleException(
            error = BusinessRulesError.NOT_FOUND,
            reason = "User not found"
        )

        // find the user by its email and password match
        val user = userDAO.findByPrimaryEmail(dto.primaryEmail) ?: throw userNotFoundException
        if (!comparePassword(dto.password, user.hashedPassword)) {
            throw userNotFoundException
        }

        return user
    }

    /**
     * Try to find a UserModel identified by its uuid. Thrown an exception if not found
     */
    fun findByUuid(uuid: String): UserModel {
        return userDAO.findByUuid(uuid) ?: throw BusinessRuleException(
            BusinessRulesError.NOT_FOUND,
            "User identified by UUID $uuid not found"
        )
    }

    /**
     * Return an object data indicating if the given email is available to use in the platform
     */
    fun isEmailAvailableToUseAsAccessCredential(email: String): IsEmailAvailableDTO {
        return IsEmailAvailableDTO(
            isAvailable = userDAO.findByPrimaryEmail(email) == null
        )
    }

    fun sendPrimaryEmailChangeMail(requesterUUID: String, dto: SendPrimaryEmailChangeMailDTO): PrimaryEmailChangeTokenAndPayload {
        // generate the secret confirmation code
        val confirmationCode = RandomString.generate(
            RandomString.SEED_UPPER_CASE_LETTERS + RandomString.SEED_DIGITS,
            8
        )

        return sendPrimaryEmailChangeMail(requesterUUID, dto, confirmationCode)
    }

    /**
     * Send a password recovery mail to the account related with the given primary mail. If the given email
     * is not from a registered account this method will not sent any mail
     */
    fun sendPasswordRecoveryMail(primaryMail: String) {

        // First, check if the user exists on the database, otherwise, do nothing
        val user = userDAO.findByPrimaryEmail(primaryMail) ?: return

        // issue the token
        val recoveryTokenAndPayload = passwordRecoveryTokenProvider.issue(
            user,
            expiresAt = Instant.now().plus(1, ChronoUnit.HOURS)
        )

        val passwordRecoveryUrl = "${contextualBeans.webappUrl()}/password-recovery?recoveryToken=${recoveryTokenAndPayload.token}"

        // Everything is executed on the mail timeout as this service can be used in a public context
        userCache.executeOnPasswordMailTimeout(primaryMail) {
            userMail.sendPasswordRecoveryMail(user, passwordRecoveryUrl)
        }
    }

    /**
     * Send a primary email change mail
     */
    internal fun sendPrimaryEmailChangeMail(requesterUUID: String, dto: SendPrimaryEmailChangeMailDTO, confirmationCode: String): PrimaryEmailChangeTokenAndPayload {
        // validate dto
        val validationResult = validate(dto)
        if (validationResult.hasErrors()) throw BusinessRuleException(validationResult)

        // get the requester account
        val requester = assertFindByUuid(requesterUUID)

        // check for timeout
        if (userCache.onPrimaryMailChangeTimeout(requester)) {
            throw BusinessRuleException(
                BusinessRulesError.TOO_MANY_REQUESTS,
                "Too many requests for this service"
            )
        }

        // if the user did not have confirmed its primary email the operation can not be completed
        if (requester.primaryEmailConfirmedAt == null ||
            Duration.between(requester.primaryEmailConfirmedAt, LocalDateTime.now()).toDays() > 7) {
            throw BusinessRuleException(
                BusinessRulesError.EMAIL_NOT_CONFIRMED,
                "You have to confirm the possession of you current primary email '${requester.primaryEmail}' before using this service"
            )
        }

        // check if the new primary email is available
        if (userDAO.findByPrimaryEmail(dto.newPrimaryEmail) != null) {
            throw BusinessRuleException(
                BusinessRulesError.INVALID_EMAIL,
                "Email ${dto.newPrimaryEmail} is not available"
            )
        }

        // generate the token
        val tokenAndPayload = primaryEmailChangeTokenProvider.issue(
            user = requester,
            expiresAt = Instant.now().plus(1, ChronoUnit.HOURS),
            newPrimaryEmail = dto.newPrimaryEmail,
            confirmationCode = confirmationCode
        )

        // send the confirmation code to the new e-mail to confirm its ownership
        userMail.sendPrimaryEmailChangeMail(requester, dto.newPrimaryEmail, confirmationCode)

        // put the requester on timeout
        userCache.putPrimaryMailChangeOnTimeout(requester)

        // return the token and payload
        return tokenAndPayload
    }

    /**
     * Send the primary email confirmation mail to the given user identified by the uuid
     */
    fun sendPrimaryEmailConfirmationMail(requesterUuid: String) {
        sendPrimaryEmailConfirmationMail(assertFindByUuid(requesterUuid))
    }

    /**
     * Send the primary email confirmation mail to the given user
     */
    internal fun sendPrimaryEmailConfirmationMail(requester: UserModel) {
        userCache.executeOnPrimaryMailConfirmationTimeout(requester) {
            val tokenExpiresAt = Instant.now().plus(7, ChronoUnit.DAYS)
            val emailConfirmationToken: String = primaryEmailConfirmationTokenProvider.issue(requester, tokenExpiresAt).token
            val emailConfirmationUrl = "${contextualBeans.webappUrl()}/confirm-email?confirmationToken=${emailConfirmationToken}"
            userMail.sendPrimaryEmailConfirmationMail(requester, emailConfirmationUrl)
        }
    }

    /**
     * Update the password of the user identified by 'requesterUUID' using its current password as authentication method
     */
    @Transactional
    fun updatePassword(requesterUUID: String, dto: UpdatePasswordDTO): UserModel {

        // validate DTO
        val validationResult = validate(dto)

        // check if both password is the same
        if (dto.newPassword == dto.currentPassword) {
            validationResult.addError(
                "newPassword",
                "The new password can not be the same as the current password"
            )
        }

        if (validationResult.hasErrors()) {
            throw BusinessRuleException(validationResult)
        }
        // fetch the requester account
        val userAccount = assertFindByUuid(requesterUUID)

        // check the current password
        if (!comparePassword(dto.currentPassword, userAccount.hashedPassword)) {
            throw BusinessRuleException(
                BusinessRulesError.FORBIDDEN,
                "Invalid credentials"
            )
        }

        // check if the new password is safe
        if (!calculatePasswordRank(dto.newPassword).isAtLeast(PasswordRank.Acceptable)) {
            throw BusinessRuleException(
                error = BusinessRulesError.INVALID_PASSWORD,
                reason = "The given new password was considered unsafe by the server standards"
            )
        }

        // update the user password
        userAccount.hashedPassword = hashPassword(dto.newPassword)
        userDAO.update(userAccount)

        return userAccount
    }

    /**
     * Change the user password using a password recovery token
     */
    @Transactional
    fun updatePasswordWithRecoveryToken(dto: UpdatePasswordWithRecoveryTokenDTO): UserModel {
        // check for validation errors
        val validationResult = validate(dto)
        if (validationResult.hasErrors()) {
            throw BusinessRuleException(validationResult)
        }

        // check if the password is acceptable
        if (!calculatePasswordRank(dto.newPassword).isAtLeast(PasswordRank.Acceptable)) {
            throw BusinessRuleException(
                BusinessRulesError.INVALID_PASSWORD,
                "The given password is not considered safe by the platform standards"
            )
        }

        // decode the recovery password
        val tokenPayload = passwordRecoveryTokenProvider.validateAndDecode(dto.recoveryToken) ?:
            throw BusinessRuleException(
                BusinessRulesError.INVALID_CREDENTIALS,
                "The given recovery token is invalid or has expired"
            )

        // change the user password
        val user = assertFindByUuid(tokenPayload.userIdentifier)
        user.hashedPassword = hashPassword(dto.newPassword)

        // update the user in the database
        userDAO.update(user)

        return user
    }

    /**
     * Update the user account identified by the 'requesterUserUuid' parameter
     */
    @Transactional
    fun updateUser(requesterUserUuid: String, dto: UpdateUserDTO): UserModel {

        val targetUser = assertFindByUuid(requesterUserUuid)

        // validate DTO
        val validationResult = validate(dto)
        if (validationResult.hasErrors()) {
            throw BusinessRuleException(validationResult)
        }

        // update the user in the database
        targetUser.presentationName = dto.presentationName
        targetUser.preferredRegion = dto.preferredRegion
        userDAO.update(targetUser)

        return targetUser

    }

    /**
     * Update the primary email of the requester using the primary email change token strategy.
     * In this service the user most provide the token that can be obtained from `sendPrimaryEmailChangeMail` and
     * the code that is sent to the target primary email.
     */
    @Transactional
    fun updatePrimaryEmailWithPrimaryEmailChangeToken(
        requesterUUID: String,
        dto: UpdatePrimaryEmailWithPrimaryEmailChangeTokenDTO
    ) : UserModel {
        // validate the given DTO
        val validationResult = validate(dto)
        if (validationResult.hasErrors()) {
            throw BusinessRuleException(validationResult)
        }

        // fetch the user account
        val requester = assertFindByUuid(requesterUUID)

        // if the user did not have confirmed its primary email the operation can not be completed
        if (requester.primaryEmailConfirmedAt == null) {
            throw BusinessRuleException(
                BusinessRulesError.EMAIL_NOT_CONFIRMED,
                "You have to confirm the possession of you current primary email '${requester.primaryEmail}' before using this service"
            )
        }

        // Fetch the token payload
        val tokenPayload = primaryEmailChangeTokenProvider.validateAndDecode(dto.token, dto.confirmationCode) ?:
            throw BusinessRuleException(
                BusinessRulesError.INVALID_CREDENTIALS,
                "Invalid token or confirmation code provided"
            )

        // validate if the token provider is different from the requester
        if (tokenPayload.userIdentifier != requester.uuid) {
            throw BusinessRuleException(
                BusinessRulesError.FORBIDDEN,
                "The requester is different from the user that requested the operation"
            )
        }

        // update the requester primary email with the primary email provided in the token
        // also, update the confirmation of the primary email ownership
        requester.primaryEmail = tokenPayload.newPrimaryEmail
        requester.primaryEmailConfirmedAt = LocalDateTime.now()

        // Update the user in the database
        userDAO.update(requester)

        return requester
    }
}