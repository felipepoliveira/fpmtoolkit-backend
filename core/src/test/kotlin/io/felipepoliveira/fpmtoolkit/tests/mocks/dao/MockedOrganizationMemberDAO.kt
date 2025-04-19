package io.felipepoliveira.fpmtoolkit.tests.mocks.dao

import io.felipepoliveira.fpmtoolkit.dao.Pagination
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberDAO
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberModel
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationModel
import io.felipepoliveira.fpmtoolkit.features.users.UserModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class MockedOrganizationMemberDAO @Autowired constructor(
    private val mockedUserDAO: MockedUserDAO,
) : OrganizationMemberDAO, BaseMockedDAO<Long, OrganizationMemberModel>() {

    val mockedDatabase = mutableListOf<MockedObject<OrganizationMemberModel>>()

    override fun findByOrganizationAndUser(organization: OrganizationModel, user: UserModel): OrganizationMemberModel? {
        return mock(mockedDatabase.find {
            m -> m.reference.organization.id == organization.id && m.reference.user.id == user.id
        })
    }

    override fun findByOrganization(
        organization: OrganizationModel,
        itemsPerPage: Int,
        page: Int
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

    override fun paginationByOrganization(organization: OrganizationModel, itemsPerPage: Int): Pagination {
        return mockPagination(mockedDatabase.filter { m -> m.reference.organization.id == organization.id })
    }

    override fun findByUuid(uuid: String): OrganizationMemberModel? {
        return mock(mockedDatabase.find { m -> m.reference.uuid == uuid })
    }

    override fun findById(id: Long): OrganizationMemberModel? {
        return mock(mockedDatabase.find { m -> m.reference.id == id })
    }
}