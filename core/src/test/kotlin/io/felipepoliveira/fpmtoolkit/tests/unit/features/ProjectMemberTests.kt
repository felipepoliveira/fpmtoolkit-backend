package io.felipepoliveira.fpmtoolkit.tests.unit.features

import io.felipepoliveira.fpmtoolkit.features.projectMembers.ProjectMemberService
import io.felipepoliveira.fpmtoolkit.features.projectMembers.dto.AddProjectMemberByUserDTO
import io.felipepoliveira.fpmtoolkit.tests.UnitTestsConfiguration
import io.felipepoliveira.fpmtoolkit.tests.mocks.dao.MockedOrganizationDAO
import io.felipepoliveira.fpmtoolkit.tests.mocks.dao.MockedProjectDAO
import io.felipepoliveira.fpmtoolkit.tests.mocks.dao.MockedProjectMemberDAO
import io.felipepoliveira.fpmtoolkit.tests.mocks.dao.MockedUserDAO
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration


@SpringBootTest
@ContextConfiguration(classes = [UnitTestsConfiguration::class])
class AddUserByOrganizationMembershipTests @Autowired constructor(
    private val mockedOrganizationDAO: MockedOrganizationDAO,
    private val mockedProjectDAO: MockedProjectDAO,
    private val mockedProjectMemberDAO: MockedProjectMemberDAO,
    private val mockedUserDAO: MockedUserDAO,
    private val projectMemberService: ProjectMemberService,
) : FunSpec({

    test("Assert that the owner of the organization can add the member") {

        // arrange
        val requester = mockedUserDAO.user1()
        val organization = mockedOrganizationDAO.organization1OwnedByUser1()
        val project = mockedProjectDAO.project1OwnerByOrganization1()
        val dto = AddProjectMemberByUserDTO(
            userUuid = mockedProjectMemberDAO.user11NotAMemberOfProject1().uuid
        )

        // act
        val addedMember = projectMemberService.addUserByOrganizationMembership(requester,  organization, project, dto)

        // assert
        addedMember.user.uuid shouldBe dto.userUuid
        addedMember.project.id shouldBe project.id
    }
})