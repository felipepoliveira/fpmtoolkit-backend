package io.felipepoliveira.fpmtoolkit.tests.unit.features

import io.felipepoliveira.fpmtoolkit.BusinessRuleException
import io.felipepoliveira.fpmtoolkit.BusinessRulesError
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationService
import io.felipepoliveira.fpmtoolkit.features.organizations.dto.CreateOrganizationDTO
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

@SpringBootTest
@ContextConfiguration(classes = [UnitTestsConfiguration::class])
class OrganizationFeaturesTests @Autowired constructor(
    val mockedUserDAO: MockedUserDAO,
    val organizationService: OrganizationService,
) : FunSpec({

    test("Test if organization is created successfully") {
        // Arrange
        val requester = mockedUserDAO.user1()
        val dto = CreateOrganizationDTO(
            profileName = "neworg-a1b2c3d4e",
            presentationName = "New Organization"
        )

        // Act
        val createdOrganization = organizationService.createOrganization(
            requester.uuid,
            dto
        )

        // Assert
        val createdOrganizationOwner = createdOrganization.members.first { o ->
            o.isOrganizationOwner && o.user.id == requester.id
        }
        createdOrganization.profileName shouldBe dto.profileName.lowercase()
        createdOrganization.presentationName shouldBe dto.presentationName
        createdOrganizationOwner.user.id shouldBe requester.id
    }

    test("Test if fails when profile name is not valid") {
        // Arrange
        val requester = mockedUserDAO.user1()
        val dto = CreateOrganizationDTO(
            profileName = "invalid profile name", // profile name should have a specific format of letters and digits
            presentationName = ""  // presentation name can not be blank
        )

        // Act
        val exception = shouldThrow<BusinessRuleException> {
            organizationService.createOrganization(
                requester.uuid,
                dto
            )
        }

        // Assert
        exception.error shouldBe BusinessRulesError.VALIDATION
        val errorDetails = exception.details.shouldNotBeNull()
        errorDetails.shouldContainKeys("profileName")
        errorDetails.shouldContainKeys("presentationName")
    }

    test("Test if fail when profile name does not end with free prefix format") {
        // Arrange
        val requester = mockedUserDAO.user1()
        val dto = CreateOrganizationDTO(
            profileName = "invalid profile name", // profile name should have a specific format of letters and digits
            presentationName = "invalidprofile"  // presentation name can not be blank
        )

        // Act
        val exception = shouldThrow<BusinessRuleException> {
            organizationService.createOrganization(
                requester.uuid,
                dto
            )
        }

        // Assert
        exception.error shouldBe BusinessRulesError.VALIDATION
        val errorDetails = exception.details.shouldNotBeNull()
        errorDetails.shouldContainKeys("profileName")
    }

    test("Test if fails when requester has reached the maximum amount of organizations") {
        // Arrange
        val requester = mockedUserDAO.userWithALotOfOrganizations()
        val dto = CreateOrganizationDTO(
            profileName = "neworg-a1b2c3d4e",
            presentationName = "New Organization"
        )

        // Act
        val exception = shouldThrow<BusinessRuleException> {
            organizationService.createOrganization(
                requester.uuid,
                dto
            )
        }

        // Assert
        exception.error shouldBe BusinessRulesError.FORBIDDEN
    }

})