package io.felipepoliveira.fpmtoolkit.tests.mocks.dao

import io.felipepoliveira.fpmtoolkit.dao.Pagination
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationModel
import io.felipepoliveira.fpmtoolkit.features.organizationMemberInvite.OrganizationMemberInviteDAO
import io.felipepoliveira.fpmtoolkit.features.organizationMemberInvite.OrganizationMemberInviteModel
import io.felipepoliveira.fpmtoolkit.features.organizationMemberInvite.OrganizationMemberInviteService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class MockedOrganizationMemberInviteDAO @Autowired constructor(
    private val mockedOrganizationDAO: MockedOrganizationDAO,
    private val mockedUserDAO: MockedUserDAO,
) : OrganizationMemberInviteDAO, BaseMockedDAO<Long, OrganizationMemberInviteModel>() {

    private val mockedDatabase = mutableListOf<MockedObject<OrganizationMemberInviteModel>>()

    init {
        mockedDatabase.add(MockedObject { invite1OwnerByOrganization1() })
        mockedDatabase.add(MockedObject { inviteForUserWithNoOrganizationButHasAnAccount() })
    }

    fun invite1OwnerByOrganization1() = OrganizationMemberInviteModel(
        organization = mockedOrganizationDAO.organization1OwnedByUser1(),
        memberEmail = "pending_invite_1_of_org_1@email.com",
        id = 1,
        uuid = "1",
        createdAt = LocalDateTime.now()
    )

    fun inviteForUserWithNoOrganizationButHasAnAccount() = OrganizationMemberInviteModel(
        organization = mockedOrganizationDAO.organization1OwnedByUser1(),
        memberEmail = mockedUserDAO.userWithNoOrganization().primaryEmail,
        id =  1000,
        uuid = "1000",
        createdAt = LocalDateTime.now(),
    )

    override fun findByOrganizationAndMemberEmail(
        organization: OrganizationModel,
        memberEmail: String
    ): OrganizationMemberInviteModel? {
        return mock(mockedDatabase.find { m ->
            m.reference.organization.id == organization.id &&
            m.reference.memberEmail == memberEmail
        })
    }

    override fun findByOrganization(
        organization: OrganizationModel,
        limit: Int,
        page: Int,
        queryField: String?,
    ): Collection<OrganizationMemberInviteModel> {
        return mock(mockedDatabase.filter { m -> m.reference.organization.id == organization.id })
    }

    override fun paginationByOrganization(organization: OrganizationModel, limit: Int, queryField: String?,): Pagination {
        return mockPagination(mockedDatabase.filter { m -> m.reference.organization.id == organization.id })
    }

    override fun findByUuid(uuid: String): OrganizationMemberInviteModel? {
        return mock(mockedDatabase.find { m -> m.reference.uuid == uuid })
    }

    override fun findById(id: Long): OrganizationMemberInviteModel? {
        return mock(mockedDatabase.find { m -> m.reference.id == id })
    }
}