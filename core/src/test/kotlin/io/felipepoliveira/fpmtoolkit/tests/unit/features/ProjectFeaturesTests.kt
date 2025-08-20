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
class ArchiveProjectTests @Autowired constructor(
    private val mockedOrganizationDAO: MockedOrganizationDAO,
    private val mockedProjectDAO: MockedProjectDAO,
    private val mockedUserDAO: MockedUserDAO,
    private val projectService: ProjectService,
) : FunSpec({

    test("Assert that it is possible to archive") {
        // arrange
        val requester = mockedUserDAO.user1()
        val organization = mockedOrganizationDAO.organization1OwnedByUser1()
        val targetProject = mockedProjectDAO.project1OwnerByOrganization1()

        // act
        val updatedProject = projectService.archiveProject(requester.uuid, organization.uuid, targetProject.uuid)

        // assert
        updatedProject.archivedAt.shouldNotBeNull()
    }

    test("Assert than an error is triggered when the requester does not have authorization") {
        // arrange
        val requester = mockedUserDAO.user11OfOrg1WithNoRoles()
        val organization = mockedOrganizationDAO.organization1OwnedByUser1()
        val targetProject = mockedProjectDAO.project1OwnerByOrganization1()

        // act
        val exception = shouldThrow<BusinessRuleException> {
            projectService.archiveProject(requester.uuid, organization.uuid, targetProject.uuid)
        }

        // assert
        exception.error shouldBe BusinessRulesError.FORBIDDEN
    }

    test("Assert that an error is thrown an trying to archive a project of another organization") {
        // arrange
        val requester = mockedUserDAO.user2()
        val organization = mockedOrganizationDAO.organization1OwnedByUser1()
        val targetProject = mockedProjectDAO.project1OwnerByOrganization1()

        // act
        val exception = shouldThrow<BusinessRuleException> {
            projectService.archiveProject(requester.uuid, organization.uuid, targetProject.uuid)
        }

        // assert
        exception.error shouldBe BusinessRulesError.FORBIDDEN
    }

    test("Assert that an error is thrown when trying to archive a project that is already archived") {
        // arrange
        val requester = mockedUserDAO.user1()
        val organization = mockedOrganizationDAO.organization1OwnedByUser1()
        val targetProject = mockedProjectDAO.project3ArchivedOwnedByOrganization1()

        // act
        val exception = shouldThrow<BusinessRuleException> {
            projectService.archiveProject(requester.uuid, organization.uuid, targetProject.uuid)
        }

        // assert
        exception.error shouldBe BusinessRulesError.FORBIDDEN
    }

})

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
        val createdProject = projectService.createProject(requester.uuid, organization.uuid, dto)

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
            projectService.createProject(requester.uuid, organization.uuid, dto)
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
            projectService.createProject(requester.uuid, organization.uuid, dto)
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
            projectService.createProject(requester.uuid, organization.uuid, dto)
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

@SpringBootTest
@ContextConfiguration(classes = [UnitTestsConfiguration::class])
class UpdateProjectTests @Autowired constructor(
    private val mockedOrganizationDAO: MockedOrganizationDAO,
    private val mockedProjectDAO: MockedProjectDAO,
    private val mockedUserDAO: MockedUserDAO,
    private val projectService: ProjectService,
) : FunSpec({

    test("Assert that project is updated") {
        // arrange
        val requester = mockedUserDAO.user1()
        val organization = mockedOrganizationDAO.organization1OwnedByUser1()
        val targetProject = mockedProjectDAO.project1OwnerByOrganization1()
        val dto = CreateOrUpdateProjectDTO(
            name = "Updated name",
            profileName = "updated-name"
        )

        // act
        val updatedProject = projectService.updateProject(requester.uuid, organization.uuid, targetProject.uuid, dto)

        // assert
        updatedProject.name shouldBe dto.name
        updatedProject.profileName shouldBe dto.profileName
    }

    test("Assert that fails on data validation errors") {
        // arrange
        val requester = mockedUserDAO.user1()
        val organization = mockedOrganizationDAO.organization1OwnedByUser1()
        val targetProject = mockedProjectDAO.project1OwnerByOrganization1()
        val dto = CreateOrUpdateProjectDTO(
            name = "",
            profileName = ""
        )

        // act
        val exception = shouldThrow<BusinessRuleException> {
            projectService.updateProject(requester.uuid, organization.uuid, targetProject.uuid, dto)
        }

        // assert
        exception.error shouldBe BusinessRulesError.VALIDATION
        val details = exception.details.shouldNotBeNull()
        details.shouldContainKeys("name", "profileName")
    }

    test("Assert that fails when requester is from another organization") {
        // arrange
        val requester = mockedUserDAO.user2()
        val organization = mockedOrganizationDAO.organization1OwnedByUser1()
        val targetProject = mockedProjectDAO.project1OwnerByOrganization1()
        val dto = CreateOrUpdateProjectDTO(
            name = "Updated name",
            profileName = "updated-name"
        )

        // act
        val exception = shouldThrow<BusinessRuleException> {
            projectService.updateProject(requester.uuid, organization.uuid, targetProject.uuid, dto)
        }

        // assert
        exception.error shouldBe BusinessRulesError.FORBIDDEN
    }

    test("Assert that fails when requester is from same organization but with no required role") {
        // arrange
        val requester = mockedUserDAO.user11OfOrg1WithNoRoles()
        val organization = mockedOrganizationDAO.organization1OwnedByUser1()
        val targetProject = mockedProjectDAO.project1OwnerByOrganization1()
        val dto = CreateOrUpdateProjectDTO(
            name = "Updated name",
            profileName = "updated-name"
        )

        // act
        val exception = shouldThrow<BusinessRuleException> {
            projectService.updateProject(requester.uuid, organization.uuid, targetProject.uuid, dto)
        }

        // assert
        exception.error shouldBe BusinessRulesError.FORBIDDEN
    }

    test("Assert that throws an error when project uuid is not found") {
        // arrange
        val requester = mockedUserDAO.user1()
        val organization = mockedOrganizationDAO.organization1OwnedByUser1()
        val dto = CreateOrUpdateProjectDTO(
            name = "Updated name",
            profileName = "updated-name"
        )

        // act
        val exception = shouldThrow<BusinessRuleException> {
            projectService.updateProject(requester.uuid, organization.uuid, "INVALID_PROJECT_UUID", dto)
        }

        // assert
        exception.error shouldBe BusinessRulesError.NOT_FOUND
    }

    test("Assert that an error occur when the target project is not from the requester organization") {
        // arrange
        val requester = mockedUserDAO.user1()
        val organization = mockedOrganizationDAO.organization1OwnedByUser1()
        val targetProject = mockedProjectDAO.project2NotOwnedByOrganization1()
        val dto = CreateOrUpdateProjectDTO(
            name = "Updated name",
            profileName = "updated-name"
        )

        // act
        val exception = shouldThrow<BusinessRuleException> {
            projectService.updateProject(requester.uuid, organization.uuid, targetProject.uuid, dto)
        }

        // assert
        exception.error shouldBe BusinessRulesError.NOT_FOUND
    }
})