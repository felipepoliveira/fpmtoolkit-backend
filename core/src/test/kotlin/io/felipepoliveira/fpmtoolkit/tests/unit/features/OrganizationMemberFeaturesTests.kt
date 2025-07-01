package io.felipepoliveira.fpmtoolkit.tests.unit.features

import io.felipepoliveira.fpmtoolkit.BusinessRuleException
import io.felipepoliveira.fpmtoolkit.BusinessRulesError
import io.felipepoliveira.fpmtoolkit.features.organizationMemberInvite.OrganizationMemberInviteModel
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberModel
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberRoles
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberService
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.UpdateOrganizationMemberDTO
import io.felipepoliveira.fpmtoolkit.security.tokens.OrganizationMemberInviteTokenProvider
import io.felipepoliveira.fpmtoolkit.tests.UnitTestsConfiguration
import io.felipepoliveira.fpmtoolkit.tests.mocks.dao.MockedOrganizationDAO
import io.felipepoliveira.fpmtoolkit.tests.mocks.dao.MockedOrganizationMemberDAO
import io.felipepoliveira.fpmtoolkit.tests.mocks.dao.MockedOrganizationMemberInviteDAO
import io.felipepoliveira.fpmtoolkit.tests.mocks.dao.MockedUserDAO
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import java.time.Instant
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@SpringBootTest
@ContextConfiguration(classes = [UnitTestsConfiguration::class])
class AddOrganizationMemberTests @Autowired constructor(
    private val mockedOrganizationDAO: MockedOrganizationDAO,
    private val mockedUserDAO: MockedUserDAO,
    private val organizationMemberService: OrganizationMemberService,
) : FunSpec({

    test("Test success") {
        // Arrange
        val organization = mockedOrganizationDAO.organization1OwnedByUser1()
        val newMember = OrganizationMemberModel(
            organization = organization,
            isOrganizationOwner = false,
            roles = listOf(),
            user = mockedUserDAO.userWithNoOrganization(),
            id = null,
            uuid = "new-uuid"
        )

        // Act
        val addedMember = organizationMemberService.addOrganizationMember(organization, newMember)

        // Assert
        addedMember.organization.id shouldBe organization.id
        addedMember.isOrganizationOwner shouldBe newMember.isOrganizationOwner
        addedMember.roles shouldBe newMember.roles
        addedMember.user.id shouldBe newMember.user.id
    }
})

@SpringBootTest
@ContextConfiguration(classes = [UnitTestsConfiguration::class])
class ChangeOrganizationOwnerTests @Autowired constructor(
    private val mockedOrganizationDAO: MockedOrganizationDAO,
    private val mockedOrganizationMemberModel: MockedOrganizationMemberDAO,
    private val mockedUserDAO: MockedUserDAO,
    private val organizationMemberService: OrganizationMemberService,
    private val organizationMemberInviteTokenProvider: OrganizationMemberInviteTokenProvider,
) : FunSpec({

    test("Asset that the owner can change the organization owner") {
        // Arrange
        val requester = mockedOrganizationMemberModel.organization1OwnerMember(
            mockedOrganizationDAO.organization1OwnedByUser1()
        )
        val targetMember = mockedOrganizationMemberModel.organization1MemberWithNoRoles(requester.organization)

        // Act
        val updatedMember = organizationMemberService.changeOrganizationOwner(
            requester.user.uuid,
            targetMember.uuid
        )

        // Assert
        updatedMember.uuid shouldBe targetMember.uuid
        updatedMember.isOrganizationOwner shouldBe true
    }

    test("Assert that only organization owners can make this operation ") {
        // Arrange
        val requester = mockedOrganizationMemberModel.organization1MemberWithAllRoles(
            mockedOrganizationDAO.organization1OwnedByUser1()
        )
        val targetMember = mockedOrganizationMemberModel.organization1MemberWithNoRoles(requester.organization)

        // Act
        val exception = shouldThrow<BusinessRuleException> {
            organizationMemberService.changeOrganizationOwner(
                requester.user.uuid,
                targetMember.uuid
            )
        }

        // Assert
        exception.error shouldBe BusinessRulesError.FORBIDDEN
    }

    test("Assert that the requester is from the same organization as the target user") {
        // Arrange
        val requester = mockedUserDAO.user2()
        val targetMember = mockedOrganizationMemberModel.organization1MemberWithNoRoles(
            mockedOrganizationDAO.organization1OwnedByUser1()
        )

        // Act
        val exception = shouldThrow<BusinessRuleException> {
            organizationMemberService.changeOrganizationOwner(
                requester.uuid,
                targetMember.uuid
            )
        }

        // Assert
        exception.error shouldBe BusinessRulesError.FORBIDDEN
    }

    test("Assert that requester can not set itself as the owner of the organization") {
        // Arrange
        val requester = mockedOrganizationMemberModel.organization1OwnerMember(
            mockedOrganizationDAO.organization1OwnedByUser1()
        )
        val targetMember = mockedOrganizationMemberModel.organization1OwnerMember(
            mockedOrganizationDAO.organization1OwnedByUser1()
        )

        // Act
        val exception = shouldThrow<BusinessRuleException> {
            organizationMemberService.changeOrganizationOwner(
                requester.user.uuid,
                targetMember.uuid
            )
        }

        // Assert
        exception.error shouldBe BusinessRulesError.FORBIDDEN
    }

})

@SpringBootTest
@ContextConfiguration(classes = [UnitTestsConfiguration::class])
class IngressByInviteTests @Autowired constructor(
    private val mockedOrganizationMemberInviteDAO: MockedOrganizationMemberInviteDAO,
    private val mockedUserDAO: MockedUserDAO,
    private val organizationMemberService: OrganizationMemberService,
    private val organizationMemberInviteTokenProvider: OrganizationMemberInviteTokenProvider,
) : FunSpec({

    test("Test success when invite is sent by a user with an account in the platform") {

        // arrange
        val invite = mockedOrganizationMemberInviteDAO.inviteForUserWithNoOrganizationButHasAnAccount()
        val inviteToken = organizationMemberInviteTokenProvider.issue(
            expiresAt = Instant.now().plus(1, ChronoUnit.DAYS),
            recipientEmail = mockedUserDAO.userWithNoOrganization().primaryEmail,
            invite = invite
        )

        // act
        val newMember = organizationMemberService.ingressByInvite(inviteToken.token)

        // assert
        newMember.organization.id shouldBe invite.organization.id
        newMember.roles.shouldBeEmpty()
        newMember.isOrganizationOwner shouldBe false
    }

    test("Test if fails when token is invalid") {
        // arrange - do not need

        // act
        val exception = shouldThrow<BusinessRuleException> { organizationMemberService.ingressByInvite("invalid_token") }

        // assert
        exception.error shouldBe BusinessRulesError.INVALID_CREDENTIALS
    }

    test("Test if fails when token has a invite with invalid ID") {
        // arrange
        val token = organizationMemberInviteTokenProvider.issue(
            expiresAt = Instant.now().plus(1, ChronoUnit.DAYS),
            recipientEmail = mockedUserDAO.userWithNoOrganization().primaryEmail,
            invite = OrganizationMemberInviteModel(
                organization = mockedOrganizationMemberInviteDAO.invite1OwnerByOrganization1().organization,
                memberEmail = "new_member@email.com",
                id = null,
                uuid = "invalid_invite",
                createdAt = LocalDateTime.now()
            )
        )

        // act
        val exception = shouldThrow<BusinessRuleException> {
            organizationMemberService.ingressByInvite(token.token)
        }

        // assert
        exception.error shouldBe BusinessRulesError.FORBIDDEN
    }
})

@SpringBootTest
@ContextConfiguration(classes = [UnitTestsConfiguration::class])
class RemoveOrganizationMemberTests @Autowired constructor(
    private val mockedOrganizationDAO: MockedOrganizationDAO,
    private val mockedOrganizationMemberModel: MockedOrganizationMemberDAO,
    private val mockedUserDAO: MockedUserDAO,
    private val organizationMemberService: OrganizationMemberService,
    private val organizationMemberInviteTokenProvider: OrganizationMemberInviteTokenProvider,
) : FunSpec({
    test("Assert that the organization owner can remove the member of its own organization") {

        // Arrange
        val requester = mockedUserDAO.user1()
        val targetOrganization = mockedOrganizationDAO.organization1OwnedByUser1()
        val targetMember = mockedOrganizationMemberModel.organization1MemberWithNoRoles(targetOrganization)

        // Act
        val removedOrganization = organizationMemberService.removeOrganizationMember(
            requester.uuid,
            targetOrganization.uuid,
            targetMember.uuid
            )

        // Assert
        removedOrganization.user.id shouldBe targetMember.user.id
    }

    test("Assert that the organization ADMINISTRATOR can remove the member of its own organization") {

        // Arrange
        val requester = mockedUserDAO.user10OfOrg1WithAllRoles()
        val targetOrganization = mockedOrganizationDAO.organization1OwnedByUser1()
        val targetMember = mockedOrganizationMemberModel.organization1MemberWithNoRoles(targetOrganization)

        // Act
        val removedOrganization = organizationMemberService.removeOrganizationMember(
            requester.uuid,
            targetOrganization.uuid,
            targetMember.uuid
        )

        // Assert
        removedOrganization.user.id shouldBe targetMember.user.id
    }

    test("Assert that the organization that is not the ADMINISTRATOR or OWNER can not remove the organization member") {

        // Arrange
        val requester = mockedUserDAO.user11OfOrg1WithNoRoles()
        val targetOrganization = mockedOrganizationDAO.organization1OwnedByUser1()
        val targetMember = mockedOrganizationMemberModel.organization1MemberWithAllRoles(targetOrganization)

        // Act
        val exception = shouldThrow<BusinessRuleException> {
            organizationMemberService.removeOrganizationMember(
                requester.uuid,
                targetOrganization.uuid,
                targetMember.uuid
            )
        }

        // Assert
        exception.error shouldBe BusinessRulesError.FORBIDDEN
    }

    test("Assert that the organization OWNER can not remove a member of other organization") {

        // Arrange
        val requester = mockedUserDAO.user2()
        val targetOrganization = mockedOrganizationDAO.organization1OwnedByUser1()
        val targetMember = mockedOrganizationMemberModel.organization1MemberWithAllRoles(targetOrganization)

        // Act
        val exception = shouldThrow<BusinessRuleException> {
            organizationMemberService.removeOrganizationMember(
                requester.uuid,
                targetOrganization.uuid,
                targetMember.uuid
            )
        }

        // Assert
        exception.error shouldBe BusinessRulesError.FORBIDDEN
    }
})

@SpringBootTest
@ContextConfiguration(classes = [UnitTestsConfiguration::class])
class UpdateTests @Autowired constructor(
    private val mockedOrganizationDAO: MockedOrganizationDAO,
    private val mockedOrganizationMemberModel: MockedOrganizationMemberDAO,
    private val mockedUserDAO: MockedUserDAO,
    private val organizationMemberService: OrganizationMemberService,
    private val organizationMemberInviteTokenProvider: OrganizationMemberInviteTokenProvider,
) : FunSpec({

    test("Assert that the owner of the organization can update the user") {
        // Arrange
        val requester = mockedUserDAO.user1()
        val targetMemberOrg = mockedOrganizationDAO.organization1OwnedByUser1()
        val targetMember = mockedOrganizationMemberModel.organization1MemberWithNoRoles(targetMemberOrg)
        val dto = UpdateOrganizationMemberDTO(
            roles = listOf(OrganizationMemberRoles.ORG_ADMINISTRATOR)
        )

        // Act
        val updatedMember = organizationMemberService.update(requester.uuid, targetMember.uuid, dto)

        // Assert
        updatedMember.roles shouldContain OrganizationMemberRoles.ORG_ADMINISTRATOR
    }

    test("Assert that that a member of the same organization as the target with required roles can update") {
        // Arrange
        val requester = mockedUserDAO.user10OfOrg1WithAllRoles()
        val targetMemberOrg = mockedOrganizationDAO.organization1OwnedByUser1()
        val targetMember = mockedOrganizationMemberModel.organization1MemberWithNoRoles(targetMemberOrg)
        val dto = UpdateOrganizationMemberDTO(
            roles = listOf(OrganizationMemberRoles.ORG_ADMINISTRATOR)
        )

        // Act
        val updatedMember = organizationMemberService.update(requester.uuid, targetMember.uuid, dto)

        // Assert
        updatedMember.roles shouldContain OrganizationMemberRoles.ORG_ADMINISTRATOR
    }

    test("Assert that a user with no roles in the same organization can not update the target member") {
        // Arrange
        val requester = mockedUserDAO.user11OfOrg1WithNoRoles()
        val targetMemberOrg = mockedOrganizationDAO.organization1OwnedByUser1()
        val targetMember = mockedOrganizationMemberModel.organization1MemberWithNoRoles(targetMemberOrg)
        val dto = UpdateOrganizationMemberDTO(
            roles = listOf(OrganizationMemberRoles.ORG_ADMINISTRATOR)
        )

        // Act
        val exception = shouldThrow<BusinessRuleException> {
            organizationMemberService.update(requester.uuid, targetMember.uuid, dto)
        }

        // Assert
        exception.error shouldBe BusinessRulesError.FORBIDDEN
    }

    test("Assert that a user of X organization can not update the user from Y organization") {
        // Arrange
        val requester = mockedUserDAO.user2()
        val targetMemberOrg = mockedOrganizationDAO.organization1OwnedByUser1()
        val targetMember = mockedOrganizationMemberModel.organization1MemberWithNoRoles(targetMemberOrg)
        val dto = UpdateOrganizationMemberDTO(
            roles = listOf(OrganizationMemberRoles.ORG_ADMINISTRATOR)
        )

        // Act
        val exception = shouldThrow<BusinessRuleException> {
            organizationMemberService.update(requester.uuid, targetMember.uuid, dto)
        }

        // Assert
        exception.error shouldBe BusinessRulesError.FORBIDDEN
    }
})
