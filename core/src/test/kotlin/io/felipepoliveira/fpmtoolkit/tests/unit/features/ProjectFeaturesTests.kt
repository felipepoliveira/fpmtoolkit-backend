package io.felipepoliveira.fpmtoolkit.tests.unit.features

import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationService
import io.felipepoliveira.fpmtoolkit.features.projects.ProjectService
import io.felipepoliveira.fpmtoolkit.features.projects.dto.CreateOrUpdateProjectDTO
import io.felipepoliveira.fpmtoolkit.tests.UnitTestsConfiguration
import io.felipepoliveira.fpmtoolkit.tests.mocks.dao.MockedOrganizationDAO
import io.felipepoliveira.fpmtoolkit.tests.mocks.dao.MockedUserDAO
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration

@SpringBootTest
@ContextConfiguration(classes = [UnitTestsConfiguration::class])
class CreateProjectTests @Autowired constructor(
    val mockedOrganizationDAO: MockedOrganizationDAO,
    val mockedUserDAO: MockedUserDAO,
    val projectService: ProjectService,
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

})