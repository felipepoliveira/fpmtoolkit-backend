package io.felipepoliveira.fpmtoolkit.tests.mocks.dao

import io.felipepoliveira.fpmtoolkit.dao.Pagination
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberDAO
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberModel
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberRoles
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationModel
import io.felipepoliveira.fpmtoolkit.features.users.UserModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class MockedOrganizationMemberDAO @Autowired constructor(
    private val mockedUserDAO: MockedUserDAO,
) : OrganizationMemberDAO, BaseMockedDAO<Long, OrganizationMemberModel>() {

    private val addedOrganizationsIds = mutableSetOf<Long>()
    val mockedDatabase = mutableListOf<MockedObject<OrganizationMemberModel>>()

    internal fun addOrganizationMembersInTheMockedDatabase(
        org: OrganizationModel, vararg membersSeeders: () -> OrganizationMemberModel
    ) {
        // ignore if already added
        if (addedOrganizationsIds.contains(org.id)) {
            return
        }

        membersSeeders.forEach { m -> mockedDatabase.add(MockedObject { m() }) }

        addedOrganizationsIds.add(org.id ?: throw Exception("ID can not be null"))
    }

    fun organization1OwnerMember(org: OrganizationModel) = OrganizationMemberModel(
        organization = org,
        isOrganizationOwner = true,
        id = 1,
        roles = listOf(),
        uuid = "1",
        user = mockedUserDAO.user1()
    )

    fun organization1MemberWithAllRoles(org: OrganizationModel) = OrganizationMemberModel(
        isOrganizationOwner = false,
        organization = org,
        user = mockedUserDAO.user10OfOrg1WithAllRoles(),
        roles = OrganizationMemberRoles.entries,
        id = 10,
        uuid = "10",
    )

    fun organization1MemberWithNoRoles(org: OrganizationModel) = OrganizationMemberModel(
        isOrganizationOwner = false,
        organization = org,
        user = mockedUserDAO.user11OfOrg1WithNoRoles(),
        roles = listOf(),
        id = 11,
        uuid = "11",
    )

    override fun findByOrganizationAndUser(organization: OrganizationModel, user: UserModel): OrganizationMemberModel? {
        return mock(mockedDatabase.find {
            m -> m.reference.organization.id == organization.id && m.reference.user.id == user.id
        })
    }

    override fun findByOrganization(
        organization: OrganizationModel,
        queryField: String?,
        itemsPerPage: Int,
        page: Int,
    ): Collection<OrganizationMemberModel> {
        return mock(mockedDatabase.filter { m -> m.reference.organization.id == organization.id })
    }

    override fun findByOrganizationAndUserPrimaryEmail(
        organization: OrganizationModel,
        userPrimaryEmail: String
    ): OrganizationMemberModel? {
        return mock(mockedDatabase.find { m ->
            m.reference.organization.id == organization.id &&
            m.reference.user.primaryEmail == userPrimaryEmail
        })
    }

    override fun findOwnerByOrganization(organization: OrganizationModel): OrganizationMemberModel? {
        return mock(mockedDatabase.find {
            m -> m.reference.organization.id == organization.id && m.reference.isOrganizationOwner
        })
    }

    override fun paginationByOrganization(organization: OrganizationModel, queryField: String?, itemsPerPage: Int): Pagination {
        return mockPagination(mockedDatabase.filter { m -> m.reference.organization.id == organization.id })
    }

    override fun findByUuid(uuid: String): OrganizationMemberModel? {
        return mock(mockedDatabase.find { m -> m.reference.uuid == uuid })
    }

    override fun findById(id: Long): OrganizationMemberModel? {
        return mock(mockedDatabase.find { m -> m.reference.id == id })
    }
}