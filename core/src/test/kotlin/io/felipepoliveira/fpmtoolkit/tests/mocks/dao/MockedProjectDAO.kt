package io.felipepoliveira.fpmtoolkit.tests.mocks.dao

import io.felipepoliveira.fpmtoolkit.dao.Pagination
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationModel
import io.felipepoliveira.fpmtoolkit.features.projects.ProjectDAO
import io.felipepoliveira.fpmtoolkit.features.projects.ProjectModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class MockedProjectDAO @Autowired constructor(
    private val mockedOrganizationDAO: MockedOrganizationDAO,
) : ProjectDAO, BaseMockedDAO<Long, ProjectModel>() {

    private val mockedDatabase = mutableListOf<MockedObject<ProjectModel>>()

    init {
        mockedDatabase.add(MockedObject { project1OwnerByOrganization1() })
    }

    fun project1OwnerByOrganization1() = ProjectModel(
        owner = mockedOrganizationDAO.organization1OwnedByUser1(),
        uuid = "1",
        name = "Project 1 | Organization 1",
        shortDescription = "",
        id = 1,
        profileName = "project-1",
        createdAt = LocalDateTime.now(),
        members = arrayListOf()
        )

    override fun findByOwner(
        owner: OrganizationModel,
        itemsPerPage: Int,
        page: Int,
        queryField: String?
    ): Collection<ProjectModel> {
        return mock(mockedDatabase.filter { m -> m.reference.owner.id == owner.id })
    }

    override fun paginationByOwner(owner: OrganizationModel, itemsPerPage: Int, queryField: String?): Pagination {
        return mockPagination(mockedDatabase.filter { m -> m.reference.owner.id == owner.id })
    }

    override fun findByOwnerAndProfileName(owner: OrganizationModel, profileName: String): ProjectModel? {
        return mock(mockedDatabase.find { m -> m.reference.owner.id == owner.id && m.reference.profileName == profileName })
    }

    override fun findByUuid(uuid: String): ProjectModel? {
        return mock(mockedDatabase.find { m -> m.reference.owner.uuid == uuid })
    }

    override fun findById(id: Long): ProjectModel? {
        return mock(mockedDatabase.find { m -> m.reference.owner.id == id })
    }
}