package io.felipepoliveira.fpmtoolkit.tests.unit.features

import io.felipepoliveira.fpmtoolkit.features.projectDeliverables.ProjectDeliverableService
import io.felipepoliveira.fpmtoolkit.features.projectDeliverables.dao.CreateOrUpdateProjectDeliverableDTO
import io.felipepoliveira.fpmtoolkit.features.projectMembers.ProjectMemberService
import io.felipepoliveira.fpmtoolkit.tests.UnitTestsConfiguration
import io.felipepoliveira.fpmtoolkit.tests.mocks.dao.MockedProjectDAO
import io.felipepoliveira.fpmtoolkit.tests.mocks.dao.MockedProjectDeliverableDAO
import io.felipepoliveira.fpmtoolkit.tests.mocks.dao.MockedProjectMemberDAO
import io.felipepoliveira.fpmtoolkit.tests.mocks.dao.MockedUserDAO
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
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
})