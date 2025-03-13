package io.felipepoliveira.fpmtoolkit.tests.unit.features

import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberModel
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberService
import io.felipepoliveira.fpmtoolkit.tests.UnitTestsConfiguration
import io.felipepoliveira.fpmtoolkit.tests.mocks.dao.MockedOrganizationDAO
import io.felipepoliveira.fpmtoolkit.tests.mocks.dao.MockedOrganizationMemberDAO
import io.felipepoliveira.fpmtoolkit.tests.mocks.dao.MockedUserDAO
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration

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
