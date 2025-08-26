package io.felipepoliveira.fpmtoolkit.tests.mocks.dao

import io.felipepoliveira.fpmtoolkit.dao.Pagination
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationModel
import io.felipepoliveira.fpmtoolkit.features.projectMembers.ProjectMemberModel
import io.felipepoliveira.fpmtoolkit.features.projects.ProjectDAO
import io.felipepoliveira.fpmtoolkit.features.projects.ProjectModel
import io.felipepoliveira.fpmtoolkit.features.users.UserModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class MockedProjectDAO @Autowired constructor(
    private val mockedOrganizationDAO: MockedOrganizationDAO,
    private val mockedUserDAO: MockedUserDAO,
) : ProjectDAO, BaseMockedDAO<Long, ProjectModel>() {

    private val mockedDatabase = mutableListOf<MockedObject<ProjectModel>>()

    init {
        mockedDatabase.add(MockedObject { project1OwnerByOrganization1() })
        mockedDatabase.add(MockedObject { project2NotOwnedByOrganization1() })
        mockedDatabase.add(MockedObject { project3ArchivedOwnedByOrganization1() })
    }

    fun project1OwnerByOrganization1(): ProjectModel {
        val project = ProjectModel(
            owner = mockedOrganizationDAO.organization1OwnedByUser1(),
            uuid = "1",
            name = "Project 1 | Organization 1",
            shortDescription = "",
            id = 1,
            profileName = "project-1",
            createdAt = LocalDateTime.now(),
            members = arrayListOf(),
            archivedAt = null,
        )

        return project
    }

    fun project2NotOwnedByOrganization1() = ProjectModel(
        owner = mockedOrganizationDAO.notOrganization1(),
        uuid = "2",
        name = "Project 1 | Organization 1",
        shortDescription = "",
        id = 1,
        profileName = "project-1",
        createdAt = LocalDateTime.now(),
        members = arrayListOf(),
        archivedAt = null,
    )

    fun project3ArchivedOwnedByOrganization1() = ProjectModel(
        archivedAt = LocalDateTime.now(),
        name = "Project 3",
        profileName = "project-3",
        shortDescription = "Project 3",
        members = arrayListOf(),
        id = 3,
        uuid = "3",
        owner = mockedOrganizationDAO.organization1OwnedByUser1(),
        createdAt = LocalDateTime.now()
    )

    override fun findByOrganizationAndUserWithMembershipIgnoreArchived(
        owner: OrganizationModel,
        user: UserModel,
        itemsPerPage: Int,
        page: Int,
        queryField: String?
    ): Collection<ProjectModel> {
        return mock(mockedDatabase.filter { m ->
            m.reference.owner.id == owner.id && m.reference.members.any { member -> member.user.id == user.id }
        })
    }

    override fun paginationByOrganizationAndUserWithMembershipIgnoreArchived(
        owner: OrganizationModel,
        user: UserModel,
        itemsPerPage: Int,
        queryField: String?
    ): Pagination {
        return mockPagination(mockedDatabase.filter { m ->
            m.reference.owner.id == owner.id &&  m.reference.members.any { member -> member.user.id == user.id }
        })
    }

    override fun findByOwnerIgnoreArchived(
        owner: OrganizationModel,
        itemsPerPage: Int,
        page: Int,
        queryField: String?
    ): Collection<ProjectModel> {
        return mock(mockedDatabase.filter { m -> m.reference.owner.id == owner.id })
    }

    override fun paginationByOwnerIgnoreArchived(owner: OrganizationModel, itemsPerPage: Int, queryField: String?): Pagination {
        return mockPagination(mockedDatabase.filter { m -> m.reference.owner.id == owner.id })
    }

    override fun findByOwnerAndProfileName(owner: OrganizationModel, profileName: String): ProjectModel? {
        return mock(mockedDatabase.find { m -> m.reference.owner.id == owner.id && m.reference.profileName == profileName })
    }

    override fun findByOwnerAndUuid(owner: OrganizationModel, uuid: String): ProjectModel? {
        return mock(mockedDatabase.find { m -> m.reference.owner.id == owner.id && m.reference.uuid == uuid })
    }

    override fun findByUuid(uuid: String): ProjectModel? {
        return mock(mockedDatabase.find { m -> m.reference.uuid == uuid })
    }

    override fun findById(id: Long): ProjectModel? {
        return mock(mockedDatabase.find { m -> m.reference.owner.id == id })
    }
}