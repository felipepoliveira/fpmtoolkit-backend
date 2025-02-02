package io.felipepoliveira.fpmtoolkit.tests.unit.features

import io.felipepoliveira.fpmtoolkit.BusinessRuleException
import io.felipepoliveira.fpmtoolkit.BusinessRulesError
import io.felipepoliveira.fpmtoolkit.features.users.UserService
import io.felipepoliveira.fpmtoolkit.features.users.dto.CreateUserDTO
import io.felipepoliveira.fpmtoolkit.io.felipepoliveira.fpmtoolkit.commons.i18n.I18nRegion
import io.felipepoliveira.fpmtoolkit.io.felipepoliveira.fpmtoolkit.features.users.dto.FindByPrimaryEmailAndPasswordDTO
import io.felipepoliveira.fpmtoolkit.security.comparePassword
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
        exception.error shouldBe BusinessRulesError.InvalidEmail
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
        exception.error shouldBe BusinessRulesError.InvalidPassword
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
        exception.error shouldBe BusinessRulesError.Validation
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
        exception.error shouldBe BusinessRulesError.NotFound
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
        exception.error shouldBe BusinessRulesError.NotFound
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
        exception.error shouldBe BusinessRulesError.Validation
        val errorDetails = exception.details.shouldNotBeNull()
        errorDetails.shouldContainKeys("primaryEmail", "password")
    }
})

