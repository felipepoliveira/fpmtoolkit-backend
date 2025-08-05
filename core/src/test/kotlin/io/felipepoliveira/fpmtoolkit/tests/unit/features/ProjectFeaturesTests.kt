package io.felipepoliveira.fpmtoolkit.tests.unit.features

import io.felipepoliveira.fpmtoolkit.BusinessRuleException
import io.felipepoliveira.fpmtoolkit.BusinessRulesError
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationService
import io.felipepoliveira.fpmtoolkit.features.projects.ProjectService
import io.felipepoliveira.fpmtoolkit.features.projects.dto.CreateOrUpdateProjectDTO
import io.felipepoliveira.fpmtoolkit.tests.UnitTestsConfiguration
import io.felipepoliveira.fpmtoolkit.tests.mocks.dao.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.maps.shouldContainKeys
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration

@SpringBootTest
@ContextConfiguration(classes = [UnitTestsConfiguration::class])
class CreateProjectTests @Autowired constructor(
    private val mockedOrganizationDAO: MockedOrganizationDAO,
    private val mockedOrganizationMemberDAO: MockedOrganizationMemberDAO,
    private val mockedProjectDAO: MockedProjectDAO,
    private val mockedUserDAO: MockedUserDAO,
    private val projectService: ProjectService,
) : FunSpec({

    test("Test if project is created") {
        // arrange
        val requester = mockedUserDAO.user1()
        val organization = mockedOrganizationDAO.organization1OwnedByUser1()
        val dto = CreateOrUpdateProjectDTO(
            name = "New Project",
            profileName = "new-project"
        )

        // act
        val createdProject = projectService.createProject(requester, organization, dto)

        // assert
        createdProject.profileName shouldBe dto.profileName
        createdProject.name shouldBe dto.name
        createdProject.createdAt.shouldNotBeNull()
        createdProject.owner.id shouldBe organization.id
        createdProject.uuid.shouldNotBeNull()
    }

    test("Test if fails on invalid data") {
        // arrange
        val requester = mockedUserDAO.user1()
        val organization = mockedOrganizationDAO.organization1OwnedByUser1()
        val dto = CreateOrUpdateProjectDTO(
            name = "",
            profileName = ""
        )

        // act
        val exception = shouldThrow<BusinessRuleException> {
            projectService.createProject(requester, organization, dto)
        }

        // assert
        exception.error shouldBe BusinessRulesError.VALIDATION
        val details = exception.details.shouldNotBeNull()
        details.shouldContainKeys("name", "profileName")
    }

    test("Test if fails when requester has no authorization to create a project") {
        // arrange
        val organization = mockedOrganizationDAO.organization1OwnedByUser1()
        val requester = mockedOrganizationMemberDAO.organization1MemberWithNoRoles(organization).user
        val dto = CreateOrUpdateProjectDTO(
            name = "New Project",
            profileName = "new-project"
        )

        // act
        val exception = shouldThrow<BusinessRuleException> {
            projectService.createProject(requester, organization, dto)
        }

        // assert
        exception.error shouldBe BusinessRulesError.FORBIDDEN
    }

    test("Test if fails if profileName is already taken") {
        // arrange
        val organization = mockedOrganizationDAO.organization1OwnedByUser1()
        val requester = mockedUserDAO.user1()
        val dto = CreateOrUpdateProjectDTO(
            name = "New Project",
            profileName = mockedProjectDAO.project1OwnerByOrganization1().profileName
        )

        // act
        val exception = shouldThrow<BusinessRuleException> {
            projectService.createProject(requester, organization, dto)
        }

        // assert
        exception.error shouldBe BusinessRulesError.VALIDATION
        val details = exception.details.shouldNotBeNull()
        details.shouldContainKeys("profileName")
    }
})

@SpringBootTest
@ContextConfiguration(classes = [UnitTestsConfiguration::class])
class FindByOwnerTests @Autowired constructor(
    private val mockedOrganizationDAO: MockedOrganizationDAO,
    private val mockedOrganizationMemberDAO: MockedOrganizationMemberDAO,
    private val mockedProjectDAO: MockedProjectDAO,
    private val mockedUserDAO: MockedUserDAO,
    private val projectService: ProjectService,
) : FunSpec({
    test("Assert that the owner or administrator or PROJECT_MANAGER of the org can fetch all projects from org") {
        // arrange
        val requester = mockedUserDAO.user1()
        val organization = mockedOrganizationDAO.organization1OwnedByUser1()

        // act
        val projects = projectService.findByOrganization(requester, organization)

        // assert
        projects.size shouldBeGreaterThan 0
    }

    test("Assert that user that does not have the required role can only see projects attached to it") {
        // arrange
        val requester = mockedUserDAO.user11OfOrg1WithNoRoles()
        val organization = mockedOrganizationDAO.organization1OwnedByUser1()

        // act
        val projects = projectService.findByOrganization(requester, organization)

        // assert
        projects.size shouldBe 0
    }
})