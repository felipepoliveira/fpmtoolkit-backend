package io.felipepoliveira.fpmtoolkit.tests.unit.features

import io.felipepoliveira.fpmtoolkit.BusinessRuleException
import io.felipepoliveira.fpmtoolkit.BusinessRulesError
import io.felipepoliveira.fpmtoolkit.commons.i18n.I18nRegion
import io.felipepoliveira.fpmtoolkit.features.organizationMemberInvite.OrganizationMemberInviteService
import io.felipepoliveira.fpmtoolkit.features.organizationMemberInvite.dto.CreateOrganizationMemberInviteDTO
import io.felipepoliveira.fpmtoolkit.tests.UnitTestsConfiguration
import io.felipepoliveira.fpmtoolkit.tests.mocks.dao.MockedOrganizationDAO
import io.felipepoliveira.fpmtoolkit.tests.mocks.dao.MockedOrganizationMemberDAO
import io.felipepoliveira.fpmtoolkit.tests.mocks.dao.MockedOrganizationMemberInviteDAO
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
class CreateOrganizationMemberInviteTests @Autowired constructor(
    private val mockedOrganizationDAO: MockedOrganizationDAO,
    private val mockedOrganizationMemberDAO: MockedOrganizationMemberDAO,
    private val mockedOrganizationMemberInviteDAO: MockedOrganizationMemberInviteDAO,
    private val mockedUserDAO: MockedUserDAO,
    private val organizationMemberInviteService: OrganizationMemberInviteService,
): FunSpec({

    test("Test success when created by owner") {
        // arrange
        val organization = mockedOrganizationDAO.organization1OwnedByUser1()
        val requester = mockedUserDAO.user1()
        val dto = CreateOrganizationMemberInviteDTO(
            memberEmail = "new_member@email.com",
            inviteMailLanguage = I18nRegion.PT_BR
        )

        // act
        val invite = organizationMemberInviteService.create(
            requester.uuid,
            organization.uuid,
            dto
        )

        // assert
        invite.organization.id shouldBe organization.id
        invite.createdAt.shouldNotBeNull()
        invite.memberEmail shouldBe dto.memberEmail
    }

    test("Test success when created by user member with required role") {
        // arrange
        val organization = mockedOrganizationDAO.organization1OwnedByUser1()
        val requester = mockedUserDAO.user10OfOrg1WithAllRoles()
        val dto = CreateOrganizationMemberInviteDTO(
            memberEmail = "new_member@email.com",
            inviteMailLanguage = I18nRegion.PT_BR
        )

        // act
        val invite = organizationMemberInviteService.create(
            requester.uuid,
            organization.uuid,
            dto
        )

        // assert
        invite.organization.id shouldBe organization.id
        invite.createdAt.shouldNotBeNull()
        invite.memberEmail shouldBe dto.memberEmail
    }

    test("Test if fails on invalid DTO data") {
        // arrange
        val organization = mockedOrganizationDAO.organization1OwnedByUser1()
        val requester = mockedUserDAO.user1()
        val dto = CreateOrganizationMemberInviteDTO(
            memberEmail = "invalid_email",
            inviteMailLanguage = I18nRegion.PT_BR
        )

        // act
        val exception = shouldThrow<BusinessRuleException> {
            organizationMemberInviteService.create(
                requester.uuid,
                organization.uuid,
                dto
            )
        }

        // assert
        exception.error shouldBe BusinessRulesError.VALIDATION
        val details = exception.details.shouldNotBeNull()
        details.shouldContainKeys("memberEmail")
    }

    test("Test if fails when used by user with no required role") {
        // arrange
        val organization = mockedOrganizationDAO.organization1OwnedByUser1()
        val requester = mockedUserDAO.user11OfOrg1WithNoRoles()
        val dto = CreateOrganizationMemberInviteDTO(
            memberEmail = "new_member@email.com",
            inviteMailLanguage = I18nRegion.PT_BR
        )

        // act
        val exception = shouldThrow<BusinessRuleException> {
            organizationMemberInviteService.create(
                requester.uuid,
                organization.uuid,
                dto
            )
        }

        // assert
        exception.error shouldBe BusinessRulesError.FORBIDDEN
    }

    test("Test if fails when new member is already a member of the organization") {
        // arrange
        val organization = mockedOrganizationDAO.organization1OwnedByUser1()
        val requester = mockedUserDAO.user1()
        val dto = CreateOrganizationMemberInviteDTO(
            memberEmail = mockedUserDAO.user11OfOrg1WithNoRoles().primaryEmail, // this user is already a member of the user 1 org
            inviteMailLanguage = I18nRegion.PT_BR
        )

        // act
        val exception = shouldThrow<BusinessRuleException> {
            organizationMemberInviteService.create(
                requester.uuid,
                organization.uuid,
                dto
            )
        }

        // assert
        exception.error shouldBe BusinessRulesError.INVALID_EMAIL
    }

    test("Test if fails when new member has already a pending email invite") {
        // arrange
        val organization = mockedOrganizationDAO.organization1OwnedByUser1()
        val requester = mockedUserDAO.user1()
        val dto = CreateOrganizationMemberInviteDTO(
            memberEmail = mockedOrganizationMemberInviteDAO.invite1OwnerByOrganization1().memberEmail,
            inviteMailLanguage = I18nRegion.PT_BR
        )

        // act
        val exception = shouldThrow<BusinessRuleException> {
            organizationMemberInviteService.create(
                requester.uuid,
                organization.uuid,
                dto
            )
        }

        // assert
        exception.error shouldBe BusinessRulesError.INVALID_EMAIL
    }
})