package io.felipepoliveira.fpmtoolkit.tests.unit.features

import io.felipepoliveira.fpmtoolkit.BusinessRuleException
import io.felipepoliveira.fpmtoolkit.BusinessRulesError
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberModel
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberService
import io.felipepoliveira.fpmtoolkit.security.tokens.OrganizationMemberInviteTokenProvider
import io.felipepoliveira.fpmtoolkit.tests.UnitTestsConfiguration
import io.felipepoliveira.fpmtoolkit.tests.mocks.dao.MockedOrganizationDAO
import io.felipepoliveira.fpmtoolkit.tests.mocks.dao.MockedOrganizationMemberDAO
import io.felipepoliveira.fpmtoolkit.tests.mocks.dao.MockedOrganizationMemberInviteDAO
import io.felipepoliveira.fpmtoolkit.tests.mocks.dao.MockedUserDAO
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import java.time.Instant
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

    test("")
})
