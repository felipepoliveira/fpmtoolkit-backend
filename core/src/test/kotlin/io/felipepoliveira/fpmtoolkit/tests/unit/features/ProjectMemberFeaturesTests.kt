package io.felipepoliveira.fpmtoolkit.tests.unit.features

import io.felipepoliveira.fpmtoolkit.BusinessRuleException
import io.felipepoliveira.fpmtoolkit.BusinessRulesError
import io.felipepoliveira.fpmtoolkit.features.projectMembers.ProjectMemberService
import io.felipepoliveira.fpmtoolkit.features.projectMembers.dto.AddProjectMemberByUserDTO
import io.felipepoliveira.fpmtoolkit.tests.UnitTestsConfiguration
import io.felipepoliveira.fpmtoolkit.tests.mocks.dao.MockedProjectDAO
import io.felipepoliveira.fpmtoolkit.tests.mocks.dao.MockedProjectMemberDAO
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
class AddUserByOrganizationMembershipTests @Autowired constructor(
    private val mockedProjectDAO: MockedProjectDAO,
    private val mockedProjectMemberDAO: MockedProjectMemberDAO,
    private val mockedUserDAO: MockedUserDAO,
    private val projectMemberService: ProjectMemberService,
) : FunSpec({

    test("Assert that the owner of the organization can add the member") {

        // arrange
        val requester = mockedUserDAO.user1()
        val project = mockedProjectDAO.project1OwnerByOrganization1()
        val dto = AddProjectMemberByUserDTO(
            userUuid = mockedProjectMemberDAO.user11NotAMemberOfProject1().uuid
        )

        // act
        val addedMember = projectMemberService.addUserByOrganizationMembership(requester, project, dto)

        // assert
        addedMember.user.uuid shouldBe dto.userUuid
        addedMember.project.id shouldBe project.id
    }

    test("Assert that a user member it required role can add a new member") {

        // arrange
        val requester = mockedUserDAO.user10OfOrg1WithAllRoles()
        val project = mockedProjectDAO.project1OwnerByOrganization1()
        val dto = AddProjectMemberByUserDTO(
            userUuid = mockedProjectMemberDAO.user11NotAMemberOfProject1().uuid
        )

        // act
        val addedMember = projectMemberService.addUserByOrganizationMembership(requester, project, dto)

        // assert
        addedMember.user.uuid shouldBe dto.userUuid
        addedMember.project.id shouldBe project.id
    }

    test("Assert that a user with no roles can not add a member") {

        // arrange
        val requester = mockedUserDAO.user11OfOrg1WithNoRoles()
        val project = mockedProjectDAO.project1OwnerByOrganization1()
        val dto = AddProjectMemberByUserDTO(
            userUuid = mockedProjectMemberDAO.user11NotAMemberOfProject1().uuid
        )

        // act
        val exception = shouldThrow<BusinessRuleException> {
            projectMemberService.addUserByOrganizationMembership(requester, project, dto)
        }

        // assert
        exception.error shouldBe BusinessRulesError.FORBIDDEN
    }

    test("Assert that a user that is not a member of the organization can not") {

        // arrange
        val requester = mockedUserDAO.user1()
        val project = mockedProjectDAO.project2NotOwnedByOrganization1()
        val dto = AddProjectMemberByUserDTO(
            userUuid = mockedProjectMemberDAO.user11NotAMemberOfProject1().uuid
        )

        // act
        val exception = shouldThrow<BusinessRuleException> {
            projectMemberService.addUserByOrganizationMembership(requester, project, dto)
        }

        // assert
        exception.error shouldBe BusinessRulesError.FORBIDDEN
    }

    test("Assert that the server does not accept DTO validation errors") {

        // arrange
        val requester = mockedUserDAO.user1()
        val project = mockedProjectDAO.project1OwnerByOrganization1()
        val dto = AddProjectMemberByUserDTO(
            userUuid = "" // invalid UUID
        )

        // act
        val exception = shouldThrow<BusinessRuleException> {
            projectMemberService.addUserByOrganizationMembership(requester, project, dto)
        }

        // assert
        exception.error shouldBe BusinessRulesError.VALIDATION
        val details = exception.details.shouldNotBeNull()
        details.shouldContainKeys("userUuid")
    }
})