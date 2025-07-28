package io.felipepoliveira.fpmtoolkit.tests.unit.features

import io.felipepoliveira.fpmtoolkit.BusinessRuleException
import io.felipepoliveira.fpmtoolkit.BusinessRulesError
import io.felipepoliveira.fpmtoolkit.commons.i18n.I18nRegion
import io.felipepoliveira.fpmtoolkit.features.users.UserService
import io.felipepoliveira.fpmtoolkit.features.users.dto.*
import io.felipepoliveira.fpmtoolkit.security.comparePassword
import io.felipepoliveira.fpmtoolkit.security.tokens.PasswordRecoveryTokenProvider
import io.felipepoliveira.fpmtoolkit.security.tokens.PrimaryEmailConfirmationTokenProvider
import io.felipepoliveira.fpmtoolkit.tests.UnitTestsConfiguration
import io.felipepoliveira.fpmtoolkit.tests.mocks.dao.MockedUserDAO
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldContainKeys
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import java.time.Instant
import java.time.temporal.ChronoUnit

@SpringBootTest
@ContextConfiguration(classes = [UnitTestsConfiguration::class])
class ConfirmPrimaryEmailWithTokenTests @Autowired constructor(
    private val primaryEmailConfirmationTokenProvider: PrimaryEmailConfirmationTokenProvider,
    private val mockedUserDAO: MockedUserDAO,
    private val userService: UserService,
) : FunSpec({

    test("Test success") {
        // Arrange
        val requester = mockedUserDAO.userWithUnconfirmedPrimaryEmail()
        val confirmationToken = primaryEmailConfirmationTokenProvider.issue(
            requester,
            Instant.now().plus(1, ChronoUnit.DAYS)
        ).token
        val dto = ConfirmPrimaryEmailWithTokenDTO(
            confirmationToken
        )

        // Act
        val updatedUser = userService.confirmPrimaryEmailWithToken(dto)

        // Assert
        requester.primaryEmailConfirmedAt shouldBe null
        updatedUser.id shouldBe requester.id
        updatedUser.primaryEmailConfirmedAt.shouldNotBeNull()
    }

    test("Test if fails when using a DTO with invalid data") {
        // Arrange
        val requester = mockedUserDAO.userWithUnconfirmedPrimaryEmail()
        val dto = ConfirmPrimaryEmailWithTokenDTO(
            "" // confirmation token can not be blank
        )

        // Act
        val exception = shouldThrow<BusinessRuleException> { userService.confirmPrimaryEmailWithToken(dto) }

        // Assert
        exception.error shouldBe BusinessRulesError.VALIDATION
        val details = exception.details.shouldNotBeNull()
        details.shouldContainKeys("confirmationToken")
    }

    test("Test if fails when using a invalid token") {
        // Arrange
        val dto = ConfirmPrimaryEmailWithTokenDTO(
            "invalid_token"
        )

        // Act
        val exception = shouldThrow<BusinessRuleException> { userService.confirmPrimaryEmailWithToken(dto) }

        // Assert
        exception.error shouldBe BusinessRulesError.INVALID_CREDENTIALS
    }
})


/**
 * Tests for UserService.createUser
 */
@SpringBootTest
@ContextConfiguration(classes = [UnitTestsConfiguration::class])
class CreateUserTests @Autowired constructor(
    private val mockedUserDAO: MockedUserDAO,
    private val userService: UserService,
) : FunSpec({

    test("Success when using valid DTO") {
        // Arrange
        val dto = CreateUserDTO(
            preferredRegion = I18nRegion.PT_BR,
            primaryEmail = "newuser@email.com",
            presentationName = "New User",
            password = "NewSafePassword1!"
        )

        // Act
        val createdUser = userService.createUser(dto)

        // Assert
        createdUser.primaryEmail shouldBe dto.primaryEmail
        createdUser.presentationName shouldBe dto.presentationName
        comparePassword(dto.password, createdUser.hashedPassword) shouldBe true
    }

    test("Test if fails when using a email already tooked by another user") {
        // Arrange
        val dto = CreateUserDTO(
            preferredRegion = I18nRegion.PT_BR,
            primaryEmail = mockedUserDAO.user1().primaryEmail,
            presentationName = "New User",
            password = "NewSafePassword1!"
        )

        // Act
        val exception = shouldThrow<BusinessRuleException> { userService.createUser(dto) }

        // Assert
        exception.error shouldBe BusinessRulesError.INVALID_EMAIL
    }

    test("Test if fails when using a unsafe password") {
        // Arrange
        val dto = CreateUserDTO(
            preferredRegion = I18nRegion.PT_BR,
            primaryEmail = "newuser@email.com",
            presentationName = "New User",
            password = "thisshouldbeunsafe"
        )

        // Act
        val exception = shouldThrow<BusinessRuleException> { userService.createUser(dto) }

        // Assert
        exception.error shouldBe BusinessRulesError.INVALID_PASSWORD
    }

    test("Test if fails when using invalid values on DTO") {
        // Arrange
        val dto = CreateUserDTO(
            preferredRegion = I18nRegion.PT_BR,
            primaryEmail = "not a email",
            presentationName = "", // presentation name should not be empty
            password = "" // this can not be blank
        )

        // Act
        val exception = shouldThrow<BusinessRuleException> { userService.createUser(dto) }

        // Assert
        exception.error shouldBe BusinessRulesError.VALIDATION
        val details = exception.details.shouldNotBeNull()
        details.shouldContainKeys("primaryEmail")
        details.shouldContainKeys("presentationName")
        details.shouldContainKeys("password")

    }
})

@SpringBootTest
@ContextConfiguration(classes = [UnitTestsConfiguration::class])
class FindByPrimaryEmailAndPasswordTests @Autowired constructor(
    private val mockedUserDAO: MockedUserDAO,
    private val userService: UserService,
) : FunSpec({

    test("Test success when using valid email and password") {
        // Arrange
        val dto = FindByPrimaryEmailAndPasswordDTO(
            password = mockedUserDAO.defaultPassword(),
            primaryEmail = mockedUserDAO.user1().primaryEmail
        )

        // Act
        val user = userService.findByPrimaryEmailAndPassword(dto)

        // Assert
        val user1 = mockedUserDAO.user1()
        user.primaryEmail shouldBe user1.primaryEmail
        user.id shouldBe user1.id
        user.uuid shouldBe user1.uuid
    }

    test("Test if fails when using a invalid email") {
        // Arrange
        val dto = FindByPrimaryEmailAndPasswordDTO(
            password = mockedUserDAO.defaultPassword(),
            primaryEmail = "invalid@email.com"
        )

        // Act
        val exception = shouldThrow<BusinessRuleException> { userService.findByPrimaryEmailAndPassword(dto) }

        // Assert
        exception.error shouldBe BusinessRulesError.NOT_FOUND
    }

    test("Test if fails when using a invalid password") {
        // Arrange
        val dto = FindByPrimaryEmailAndPasswordDTO(
            password = "InvalidPassword",
            primaryEmail = mockedUserDAO.user1().primaryEmail
        )

        // Act
        val exception = shouldThrow<BusinessRuleException> { userService.findByPrimaryEmailAndPassword(dto) }

        // Assert
        exception.error shouldBe BusinessRulesError.NOT_FOUND
    }

    test("Test if fails when data is invalid") {
        // Arrange
        val dto = FindByPrimaryEmailAndPasswordDTO(
            password = "", // Should not be blank
            primaryEmail = "InvalidEmail" // Should be a valid email
        )

        // Act
        val exception = shouldThrow<BusinessRuleException> { userService.findByPrimaryEmailAndPassword(dto) }

        // Assert
        exception.error shouldBe BusinessRulesError.VALIDATION
        val errorDetails = exception.details.shouldNotBeNull()
        errorDetails.shouldContainKeys("primaryEmail", "password")
    }
})

@SpringBootTest
@ContextConfiguration(classes = [UnitTestsConfiguration::class])
class SendPasswordRecoveryMailTests @Autowired constructor(
    private val mockedUserDAO: MockedUserDAO,
    private val userService: UserService,
) : FunSpec({

    test("Test success") {
        // Arrange
        val userMail = mockedUserDAO.user1().primaryEmail

        // Act
        userService.sendPasswordRecoveryMail(userMail)

        // Assert - Do not need. Just checking for exceptions to throw
    }
})

@SpringBootTest
@ContextConfiguration(classes = [UnitTestsConfiguration::class])
class SendPrimaryEmailChangeMailTests @Autowired constructor(
    private val mockedUserDAO: MockedUserDAO,
    private val userService: UserService,
) : FunSpec({

    test("Send primary email change successfully") {
        // Arrange
        val requester = mockedUserDAO.user1()
        val dto = SendPrimaryEmailChangeMailDTO(
            newPrimaryEmail = "new@email.com",
        )

        // Act
        val tokenAndPayload = userService.sendPrimaryEmailChangeMail(requester.uuid, dto)

        // Assert - Not needed
    }

    test("Test if fails when requester did not have confirmed its primary email") {
        // Arrange
        val requester = mockedUserDAO.userWithUnconfirmedPrimaryEmail()
        val dto = SendPrimaryEmailChangeMailDTO(
            newPrimaryEmail = "new@email.com",
        )

        // Act
        val exception = shouldThrow<BusinessRuleException> { userService.sendPrimaryEmailChangeMail(requester.uuid, dto) }

        // Assert
        exception.error shouldBe BusinessRulesError.EMAIL_NOT_CONFIRMED
    }

    test("Test if fails while using a invalid DTO data") {
        // Arrange
        val requester = mockedUserDAO.user1()
        val dto = SendPrimaryEmailChangeMailDTO(
            newPrimaryEmail = "not a email",
        )

        // Act
        val exception = shouldThrow<BusinessRuleException> {
            userService.sendPrimaryEmailChangeMail(requester.uuid, dto)
        }

        // Assert
        exception.error shouldBe BusinessRulesError.VALIDATION
    }

    test("Test if fails while an e-mail that is already in use") {
        // Arrange
        val requester = mockedUserDAO.user1()
        val dto = SendPrimaryEmailChangeMailDTO(
            newPrimaryEmail = mockedUserDAO.user2().primaryEmail,
        )

        // Act
        val exception = shouldThrow<BusinessRuleException> {
            userService.sendPrimaryEmailChangeMail(requester.uuid, dto)
        }

        // Assert
        exception.error shouldBe BusinessRulesError.INVALID_EMAIL
    }
})

@SpringBootTest
@ContextConfiguration(classes = [UnitTestsConfiguration::class])
class SendPrimaryEmailConfirmationMailTests @Autowired constructor(
    private val mockedUserDAO: MockedUserDAO,
    private val userService: UserService,
) : FunSpec({
    test("Test success") {
        // Arrange
        val requester = mockedUserDAO.user1()

        // Act
        userService.sendPrimaryEmailConfirmationMail(requester)

        // Assert - Do not need. Just checking from exceptions
    }
})

@SpringBootTest
@ContextConfiguration(classes = [UnitTestsConfiguration::class])
class UpdatePasswordWithRecoveryTokenTests @Autowired constructor(
    private val mockedUserDAO: MockedUserDAO,
    private val passwordRecoveryTokenProvider: PasswordRecoveryTokenProvider,
    private val userService: UserService,
) : FunSpec({

    test("Successfully update a user password using a password recovery token") {
        // Arrange
        val dto = UpdatePasswordWithRecoveryTokenDTO(
            newPassword = "NewPassword123!",
            recoveryToken = passwordRecoveryTokenProvider
                .issue(mockedUserDAO.user1(), Instant.now().plus(1, ChronoUnit.HOURS))
                .token
        )

        // Act
        val updatedUser = userService.updatePasswordWithRecoveryToken(dto)

        // Assert
        comparePassword(dto.newPassword, updatedUser.hashedPassword) shouldBe true
    }

    test("Test if fails when dto has invalid data") {
        // Arrange
        val dto = UpdatePasswordWithRecoveryTokenDTO(
            newPassword = "",
            recoveryToken = ""
        )

        // Act
        val exception = shouldThrow<BusinessRuleException> { userService.updatePasswordWithRecoveryToken(dto) }

        // Assert
        exception.error shouldBe BusinessRulesError.VALIDATION
    }

    test("Test if fails when used password is considered unsafe") {
        // Arrange
        val dto = UpdatePasswordWithRecoveryTokenDTO(
            newPassword = "unsafePassword",
            recoveryToken = passwordRecoveryTokenProvider
                .issue(mockedUserDAO.user1(), Instant.now().plus(1, ChronoUnit.HOURS))
                .token
        )

        // Act
        val exception = shouldThrow<BusinessRuleException> { userService.updatePasswordWithRecoveryToken(dto) }

        // Assert
        exception.error shouldBe BusinessRulesError.INVALID_PASSWORD
    }

    test("Test if fails when using a invalid recovery token") {
        // Arrange
        val dto = UpdatePasswordWithRecoveryTokenDTO(
            newPassword = "NewSafePassword123!",
            recoveryToken = "invalidRecoveryToken"
        )

        // Act
        val exception = shouldThrow<BusinessRuleException> { userService.updatePasswordWithRecoveryToken(dto) }

        // Assert
        exception.error shouldBe BusinessRulesError.INVALID_CREDENTIALS
    }
})

@SpringBootTest
@ContextConfiguration(classes = [UnitTestsConfiguration::class])
class UpdatePrimaryEmailWithPrimaryEmailChangeTokenTests @Autowired constructor(
    private val mockedUserDAO: MockedUserDAO,
    private val userService: UserService,
) : FunSpec({

    test("Test success") {
        // Arrange
        val confirmationCode = "TEST_CODE"
        val requester = mockedUserDAO.user1()
        val changeEmailDTO = SendPrimaryEmailChangeMailDTO(
            newPrimaryEmail = "another@email.com"
        )
        val returnedTokenAndPayloadFromEmailService = userService.sendPrimaryEmailChangeMail(
            requester.uuid, changeEmailDTO, confirmationCode
        )
        val dto = UpdatePrimaryEmailWithPrimaryEmailChangeTokenDTO(
            token = returnedTokenAndPayloadFromEmailService.token,
            confirmationCode
        )

        // Act
        val updatedUser = userService.updatePrimaryEmailWithPrimaryEmailChangeToken(requester.uuid, dto)

        // Assert
        updatedUser.primaryEmail shouldBe changeEmailDTO.newPrimaryEmail
    }

    test("Test is fails if DTO has validation errors") {
        // Arrange
        val confirmationCode = "" // confirmationCode can not be blank
        val requester = mockedUserDAO.user1()
        val changeEmailDTO = SendPrimaryEmailChangeMailDTO(
            newPrimaryEmail = "another@email.com"
        )
        val returnedTokenAndPayloadFromEmailService = userService.sendPrimaryEmailChangeMail(
            requester.uuid, changeEmailDTO, confirmationCode
        )
        val dto = UpdatePrimaryEmailWithPrimaryEmailChangeTokenDTO(
            token = "", // token can not be blank
            confirmationCode
        )

        // Act
        val exception = shouldThrow<BusinessRuleException> {
            userService.updatePrimaryEmailWithPrimaryEmailChangeToken(requester.uuid, dto)
        }

        // Assert
        exception.error shouldBe BusinessRulesError.VALIDATION
        val details = exception.details.shouldNotBeNull()
        details.shouldContainKeys("confirmationCode")
        details.shouldContainKeys("token")
    }

    test("Test is fails when provided a invalid token") {
        // Arrange
        val confirmationCode = "TEST_CODE"
        val requester = mockedUserDAO.user1()
        val changeEmailDTO = SendPrimaryEmailChangeMailDTO(
            newPrimaryEmail = "another@email.com"
        )
        val returnedTokenAndPayloadFromEmailService = userService.sendPrimaryEmailChangeMail(
            requester.uuid, changeEmailDTO, confirmationCode
        )
        val dto = UpdatePrimaryEmailWithPrimaryEmailChangeTokenDTO(
            token = "INVALID_TOKEN", // When a invalid token is provided should throw an exception
            confirmationCode
        )

        // Act
        val exception = shouldThrow<BusinessRuleException> {
            userService.updatePrimaryEmailWithPrimaryEmailChangeToken(requester.uuid, dto)
        }

        // Assert
        exception.error shouldBe BusinessRulesError.INVALID_CREDENTIALS
    }

    test("Test is fails when requester is different from the user that requested the email") {
        // Arrange
        val confirmationCode = "TEST_CODE"
        val anotherRequester = mockedUserDAO.user1()
        val requester = mockedUserDAO.user2()
        val changeEmailDTO = SendPrimaryEmailChangeMailDTO(
            newPrimaryEmail = "another@email.com"
        )
        val returnedTokenAndPayloadFromEmailService = userService.sendPrimaryEmailChangeMail(
            anotherRequester.uuid, changeEmailDTO, confirmationCode
        )
        val dto = UpdatePrimaryEmailWithPrimaryEmailChangeTokenDTO(
            token = returnedTokenAndPayloadFromEmailService.token,
            confirmationCode
        )

        // Act
        val exception = shouldThrow<BusinessRuleException> {
            userService.updatePrimaryEmailWithPrimaryEmailChangeToken(requester.uuid, dto)
        }

        // Assert
        exception.error shouldBe BusinessRulesError.FORBIDDEN
    }
})

@SpringBootTest
@ContextConfiguration(classes = [UnitTestsConfiguration::class])
class UpdatePasswordTests @Autowired constructor(
    private val mockedUserDAO: MockedUserDAO,
    private val userService: UserService,
) : FunSpec({

    test("Test if password was changed successfully") {
        // Arrange
        val requester = mockedUserDAO.user1()
        val dto = UpdatePasswordDTO(
            currentPassword = mockedUserDAO.defaultPassword(),
            newPassword = "NewSafePassword1!"
        )

        // Act
        val updatedUser = userService.updatePassword(requester.uuid, dto)

        // Assert
        comparePassword(dto.newPassword, updatedUser.hashedPassword) shouldBe true
    }

    test("Test if fails on invalid DTO") {
        // Arrange
        val requester = mockedUserDAO.user1()
        val dto = UpdatePasswordDTO(
            currentPassword = "",
            newPassword = ""
        )

        // Act
        val exception = shouldThrow<BusinessRuleException> {
            userService.updatePassword(requester.uuid, dto)
        }

        // Assert
        exception.error shouldBe BusinessRulesError.VALIDATION
        val exceptionDetails = exception.details.shouldNotBeNull()
        exceptionDetails.shouldContainKeys("currentPassword")
        exceptionDetails.shouldContainKeys("newPassword")
    }

    test("Test if fails on invalid current password") {
        // Arrange
        val requester = mockedUserDAO.user1()
        val dto = UpdatePasswordDTO(
            currentPassword = "NotTheCurrentPassword",
            newPassword = "NewSafePassword1!"
        )

        // Act
        val exception = shouldThrow<BusinessRuleException> {
            userService.updatePassword(requester.uuid, dto)
        }

        // Assert
        exception.error shouldBe BusinessRulesError.FORBIDDEN
    }

    test("Test if fails on unsafe new password") {
        // Arrange
        val requester = mockedUserDAO.user1()
        val dto = UpdatePasswordDTO(
            currentPassword = mockedUserDAO.defaultPassword(),
            newPassword = "unsafePassword"
        )

        // Act
        val exception = shouldThrow<BusinessRuleException> {
            userService.updatePassword(requester.uuid, dto)
        }

        // Assert
        exception.error shouldBe BusinessRulesError.INVALID_PASSWORD
    }

})

@SpringBootTest
@ContextConfiguration(classes = [UnitTestsConfiguration::class])
class UpdateUserTests @Autowired constructor(
    private val mockedUserDAO: MockedUserDAO,
    private val userService: UserService,
) : FunSpec({

    test("Tests if user can be updated successfully") {
        // Arrange
        val requester = mockedUserDAO.user1()
        val dto = UpdateUserDTO(
            presentationName = "Updated name",
            preferredRegion = I18nRegion.PT_BR,
        )

        // Act
        val updatedUser = userService.updateUser(requester.uuid, dto)

        // Assert
        updatedUser.presentationName shouldBe dto.presentationName
        updatedUser.preferredRegion shouldBe dto.preferredRegion
    }

    test("Test if fails on invalid DTO") {
        // Arrange
        val requester = mockedUserDAO.user1()
        val dto = UpdateUserDTO(
            presentationName = "",
            preferredRegion = I18nRegion.PT_BR,
        )

        // Act
        val exception = shouldThrow<BusinessRuleException> {
            userService.updateUser(requester.uuid, dto)
        }

        // Assert
        exception.error shouldBe BusinessRulesError.VALIDATION
    }
})