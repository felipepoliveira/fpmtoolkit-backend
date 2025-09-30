package io.felipepoliveira.fpmtoolkit.tests.unit.features

import io.felipepoliveira.fpmtoolkit.BusinessRuleException
import io.felipepoliveira.fpmtoolkit.BusinessRulesError
import io.felipepoliveira.fpmtoolkit.features.projectDeliverables.ProjectDeliverableService
import io.felipepoliveira.fpmtoolkit.features.projectDeliverables.dao.CreateOrUpdateProjectDeliverableDTO
import io.felipepoliveira.fpmtoolkit.features.projectMembers.ProjectMemberService
import io.felipepoliveira.fpmtoolkit.tests.UnitTestsConfiguration
import io.felipepoliveira.fpmtoolkit.tests.mocks.dao.MockedProjectDAO
import io.felipepoliveira.fpmtoolkit.tests.mocks.dao.MockedProjectDeliverableDAO
import io.felipepoliveira.fpmtoolkit.tests.mocks.dao.MockedProjectMemberDAO
import io.felipepoliveira.fpmtoolkit.tests.mocks.dao.MockedUserDAO
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.maps.shouldContainKeys
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@SpringBootTest
@ContextConfiguration(classes = [UnitTestsConfiguration::class])
class CreateDeliverableTests @Autowired constructor(
    private val mockedProjectDAO: MockedProjectDAO,
    private val mockedProjectDeliverableDAO: MockedProjectDeliverableDAO,
    private val mockedProjectMemberDAO: MockedProjectMemberDAO,
    private val mockedUserDAO: MockedUserDAO,
    private val projectDeliverableService: ProjectDeliverableService,
) : FunSpec({

    test("Assert that the owner of the organization can add a deliverable into a project") {
        // Arrange
        val requester = mockedUserDAO.user1()
        val project = mockedProjectDAO.project1OwnerByOrganization1()
        val predecessors = listOf(mockedProjectDeliverableDAO.deliverable1FromProject1().uuid)
        val responsible = listOf(mockedProjectMemberDAO.user10MembershipInProject1().uuid)
        val dto = CreateOrUpdateProjectDeliverableDTO(
            name = "New Deliverable",
            predecessors = predecessors,
            responsible = responsible,
            factualEndDate = null,
            factualStartDate = null,
            expectedStartDate = LocalDate.now(),
            expectedEndDate = LocalDate.now().plus(30, ChronoUnit.DAYS)
        )

        // Act
        val deliverable = projectDeliverableService.createDeliverable(requester.uuid, project.uuid, dto)

        // Arrange
        deliverable.name shouldBe dto.name
        deliverable.predecessors.all { p -> predecessors.any { it == p.uuid }  }
        deliverable.responsible.all { r -> responsible.any { it == r.uuid }  }
        deliverable.factualEndDate.shouldBeNull()
        deliverable.factualStartDate.shouldBeNull()
        deliverable.expectedStartDate shouldBe dto.expectedStartDate
        deliverable.expectedEndDate shouldBe dto.expectedEndDate
    }

    test("Assert that the service does not accept a invalid DTO") {
        // Arrange
        val requester = mockedUserDAO.user1()
        val project = mockedProjectDAO.project1OwnerByOrganization1()
        val dto = CreateOrUpdateProjectDeliverableDTO(
            name = "",
            predecessors = listOf(),
            responsible = listOf(),
            factualEndDate = null,
            factualStartDate = null,
            expectedStartDate = LocalDate.now(),
            expectedEndDate = LocalDate.now().plus(30, ChronoUnit.DAYS)
        )

        // Act
        val exception = shouldThrow<BusinessRuleException> {
            projectDeliverableService.createDeliverable(requester.uuid, project.uuid, dto)
        }

        // Arrange
        exception.error shouldBe BusinessRulesError.VALIDATION
        val details = exception.details.shouldNotBeNull()
        details.shouldContainKeys("name")
    }

    test("Assert that an error is thrown when the requester is not from the same organization of the target project") {
        // Arrange
        val requester = mockedUserDAO.userWithNoOrganization()
        val project = mockedProjectDAO.project1OwnerByOrganization1()
        val predecessors = listOf(mockedProjectDeliverableDAO.deliverable1FromProject1().uuid)
        val responsible = listOf(mockedProjectMemberDAO.user10MembershipInProject1().uuid)
        val dto = CreateOrUpdateProjectDeliverableDTO(
            name = "New Deliverable",
            predecessors = predecessors,
            responsible = responsible,
            factualEndDate = null,
            factualStartDate = null,
            expectedStartDate = LocalDate.now(),
            expectedEndDate = LocalDate.now().plus(30, ChronoUnit.DAYS)
        )

        // Act
        val exception = shouldThrow<BusinessRuleException> {
            projectDeliverableService.createDeliverable(requester.uuid, project.uuid, dto)
        }

        // Arrange
        exception.error shouldBe BusinessRulesError.FORBIDDEN
    }

    test("Assert that that an error is thrown when using a duplicated name") {
        // Arrange
        val requester = mockedUserDAO.user1()
        val project = mockedProjectDAO.project1OwnerByOrganization1()
        val predecessors = listOf(mockedProjectDeliverableDAO.deliverable1FromProject1().uuid)
        val responsible = listOf(mockedProjectMemberDAO.user10MembershipInProject1().uuid)
        val dto = CreateOrUpdateProjectDeliverableDTO(
            name = mockedProjectDeliverableDAO.deliverable1FromProject1().name,
            predecessors = predecessors,
            responsible = responsible,
            factualEndDate = null,
            factualStartDate = null,
            expectedStartDate = LocalDate.now(),
            expectedEndDate = LocalDate.now().plus(30, ChronoUnit.DAYS)
        )

        // Act
        val exception = shouldThrow<BusinessRuleException> {
            projectDeliverableService.createDeliverable(requester.uuid, project.uuid, dto)
        }

        // Arrange
        exception.error shouldBe BusinessRulesError.VALIDATION
        val details = exception.details.shouldNotBeNull()
        details.shouldContainKeys("name")
    }

    test("Assert that an error is thrown when any deliverable predecessors is not found on the list") {
        // Arrange
        val requester = mockedUserDAO.user1()
        val project = mockedProjectDAO.project1OwnerByOrganization1()
        val predecessors = listOf(mockedProjectDeliverableDAO.deliverable1FromProject1().uuid, "NOT_FOUND_UUID")
        val responsible = listOf(mockedProjectMemberDAO.user10MembershipInProject1().uuid)
        val dto = CreateOrUpdateProjectDeliverableDTO(
            name = "New Deliverable",
            predecessors = predecessors,
            responsible = responsible,
            factualEndDate = null,
            factualStartDate = null,
            expectedStartDate = LocalDate.now(),
            expectedEndDate = LocalDate.now().plus(30, ChronoUnit.DAYS)
        )

        // Act
        val exception = shouldThrow<BusinessRuleException> {
            projectDeliverableService.createDeliverable(requester.uuid, project.uuid, dto)
        }

        // Arrange
        exception.error shouldBe BusinessRulesError.NOT_FOUND
    }
})

@SpringBootTest
@ContextConfiguration(classes = [UnitTestsConfiguration::class])
class UpdateDeliverableTests @Autowired constructor(
    private val mockedProjectDAO: MockedProjectDAO,
    private val mockedProjectDeliverableDAO: MockedProjectDeliverableDAO,
    private val mockedProjectMemberDAO: MockedProjectMemberDAO,
    private val mockedUserDAO: MockedUserDAO,
    private val projectDeliverableService: ProjectDeliverableService,
) : FunSpec({

    test("Assert that deliverable are updated") {
        // arrange
        val requester = mockedUserDAO.user1()
        val targetDeliverable = mockedProjectDeliverableDAO.deliverable1FromProject1()
        val dto = CreateOrUpdateProjectDeliverableDTO(
            name = "Updated Deliverable",
            factualStartDate = LocalDate.now(),
            factualEndDate = LocalDate.now().plus(1, ChronoUnit.DAYS),
            responsible = listOf(),
            predecessors = listOf(),
            expectedStartDate = null,
            expectedEndDate = null,
        )

        // act
        val updatedDeliverable = projectDeliverableService.updateDeliverable(
            requester.uuid,
            targetDeliverable.uuid,
            dto
        )

        // assert
        updatedDeliverable.name shouldBe dto.name
        updatedDeliverable.factualStartDate shouldBe dto.factualStartDate
        updatedDeliverable.factualEndDate shouldBe dto.factualEndDate
        updatedDeliverable.responsible.map { r -> r.uuid } shouldBe dto.responsible // same uuids
        updatedDeliverable.predecessors.map { p -> p.uuid } shouldBe dto.predecessors
        updatedDeliverable.expectedStartDate shouldBe dto.expectedStartDate
        updatedDeliverable.expectedEndDate shouldBe dto.expectedEndDate

    }
})