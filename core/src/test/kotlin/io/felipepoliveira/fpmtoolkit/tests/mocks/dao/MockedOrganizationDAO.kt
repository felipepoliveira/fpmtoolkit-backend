package io.felipepoliveira.fpmtoolkit.tests.mocks.dao

import io.felipepoliveira.fpmtoolkit.dao.Pagination
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberModel
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberRoles
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationDAO
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationModel
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationService
import io.felipepoliveira.fpmtoolkit.features.users.UserModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class MockedOrganizationDAO @Autowired constructor(
    private val mockedUserDAO: MockedUserDAO,
    private val mockedOrganizationMemberDAO: MockedOrganizationMemberDAO,
) : OrganizationDAO, BaseMockedDAO<Long, OrganizationModel>() {

    private val mockedDatabase = mutableListOf<MockedObject<OrganizationModel>>()

    init {
        mockedDatabase.add(MockedObject { organization1OwnedByUser1() })
        mockedDatabase.add(MockedObject { notOrganization1() })
        mockedDatabase.addAll(organizationsOfUserWithALotOfOrganizations().map { o -> MockedObject { o } })
    }

    fun organization1OwnedByUser1() : OrganizationModel {

        val org = OrganizationModel(
            profileName = "user1-org1",
            uuid = "user1-org1",
            presentationName = "Organization 1 Owned by User 1",
            id = 1,
            createdAt = LocalDateTime.now(),
            members = mutableListOf()
        )

        // add the owner member
        org.members.add(mockedOrganizationMemberDAO.organization1OwnerMember(org))
        // add a member with all roles
        org.members.add(mockedOrganizationMemberDAO.organization1MemberWithAllRoles(org))
        // add a member with no roles
        org.members.add(mockedOrganizationMemberDAO.organization1MemberWithNoRoles(org))

        mockedOrganizationMemberDAO.addOrganizationMembersInTheMockedDatabase(org,
            { mockedOrganizationMemberDAO.organization1OwnerMember(org) },
            { mockedOrganizationMemberDAO.organization1MemberWithAllRoles(org) },
            { mockedOrganizationMemberDAO.organization1MemberWithNoRoles(org) },
        )

        return org
    }

    fun notOrganization1() = OrganizationModel(
        profileName = "not-org1",
        uuid = "not-org1",
        presentationName = "Not Organization",
        id = 999,
        createdAt = LocalDateTime.now(),
        members = mutableListOf()
    )

    final fun organizationsOfUserWithALotOfOrganizations(): List<OrganizationModel> {
        val orgs = mutableListOf<OrganizationModel>()
        val orgIdStart = 4000
        for (i in 1..OrganizationService.MAXIMUM_AMOUNT_OF_ORGANIZATIONS_PER_FREE_ACCOUNT) {

            // create the org object
            val orgId = (orgIdStart + i).toLong()
            val newOrg = OrganizationModel(
                profileName = "Org ${orgIdStart + i}",
                id = orgId,
                uuid = "org-uuid-${orgIdStart + i}",
                createdAt = LocalDateTime.now(),
                presentationName = "org$orgId-a1b2c3d4e",
                members = mutableListOf(),
            )

            // include the new org owner as member and owner of the org
            val newOrgOwner = mockedUserDAO.userWithALotOfOrganizations()
            val newMemberSeeder = {
                OrganizationMemberModel(
                    id = orgIdStart.toLong(),
                    organization = newOrg,
                    user = newOrgOwner,
                    isOrganizationOwner = true,
                    roles = listOf(),
                    uuid = orgIdStart.toString(),
                )
            }
            mockedOrganizationMemberDAO.mockedDatabase.add(MockedObject(newMemberSeeder))
            newOrg.members.add(newMemberSeeder())
            orgs.add(newOrg)
        }

        return orgs
    }

    override fun findByProfileName(profileName: String): OrganizationModel? {
        return mock(mockedDatabase.find { o -> o.reference.profileName == profileName })
    }

    override fun findByMember(member: UserModel, itemsPerPage: Int, currentPage: Int): Collection<OrganizationModel> {
        return mock(mockedDatabase.filter { m -> m.reference.members.any { me -> me.user.id == member.id } })
    }

    override fun paginationByMember(member: UserModel, itemsPerPage: Int): Pagination {
        return mockPagination(mockedDatabase.filter { m -> m.reference.members.any { me -> me.user.id == member.id } })
    }

    override fun findByUuid(uuid: String): OrganizationModel? {
        return mock(mockedDatabase.find { m -> m.reference.uuid == uuid })
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