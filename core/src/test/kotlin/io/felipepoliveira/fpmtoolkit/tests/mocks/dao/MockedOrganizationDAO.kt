package io.felipepoliveira.fpmtoolkit.tests.mocks.dao

import io.felipepoliveira.fpmtoolkit.dao.Pagination
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberModel
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationDAO
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationModel
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationService
import io.felipepoliveira.fpmtoolkit.features.users.UserModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
class MockedOrganizationDAO @Autowired constructor(
    private val mockedUserDAO: MockedUserDAO
) : OrganizationDAO, BaseMockedDAO<Long, OrganizationModel>() {

    private val mockedDatabase = mutableListOf<MockedObject<OrganizationModel>>()

    init {
        mockedDatabase.addAll(organizationsOfUserWithALotOfOrganizations().map { o -> MockedObject { o } })
    }

    private fun organizationsOfUserWithALotOfOrganizations(): Collection<OrganizationModel> {
        val orgs = mutableListOf<OrganizationModel>()
        val orgIdStart = 4000
        for (i in 1..OrganizationService.MAXIMUM_AMOUNT_OF_ORGANIZATIONS_PER_FREE_ACCOUNT) {

            // create the org object
            val orgId = (orgIdStart + i).toLong()
            val newOrg = OrganizationModel(
                profileName = "Org ${orgIdStart + i}",
                id = orgId,
                uuid = UUID.randomUUID().toString(),
                createdAt = LocalDateTime.now(),
                presentationName = "org$orgId-a1b2c3d4e",
                members = mutableListOf(),
            )

            // include the new org owner as member and owner of the org
            val newOrgOwner = mockedUserDAO.userWithALotOfOrganizations()
            newOrg.members.add(
                OrganizationMemberModel(
                id = orgIdStart.toLong(),
                organization = newOrg,
                user = newOrgOwner,
                isOrganizationOwner = true,
                roles = listOf()
            ))
            orgs.add(newOrg)
        }

        return orgs
    }

    override fun findByProfileName(profileName: String): OrganizationModel? {
        return mock(mockedDatabase.find { o -> o.reference.profileName == profileName })
    }

    override fun findByOwner(owner: UserModel, itemsPerPage: Int, currentPage: Int): Collection<OrganizationModel> {
        return mock(mockedDatabase.filter { m ->
            m.reference.members.any { me -> me.isOrganizationOwner && me.user.id == owner.id }
        })
    }

    override fun paginationByOwner(owner: UserModel, itemsPerPage: Int): Pagination {
        return mockPagination(mockedDatabase.filter { m ->
            m.reference.members.any { me -> me.isOrganizationOwner && me.user.id == owner.id }
        })
    }

    override fun findById(id: Long): OrganizationModel? {
        return mock(mockedDatabase.find { m -> m.reference.id == id })
    }

}