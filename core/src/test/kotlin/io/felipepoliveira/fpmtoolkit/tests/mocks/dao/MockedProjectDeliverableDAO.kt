package io.felipepoliveira.fpmtoolkit.tests.mocks.dao

import io.felipepoliveira.fpmtoolkit.dao.Pagination
import io.felipepoliveira.fpmtoolkit.features.projectDeliverables.ProjectDeliverableDAO
import io.felipepoliveira.fpmtoolkit.features.projectDeliverables.ProjectDeliverableModel
import io.felipepoliveira.fpmtoolkit.features.projects.ProjectModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class MockedProjectDeliverableDAO @Autowired constructor(
    private val mockedProjectDAO: MockedProjectDAO,
) : ProjectDeliverableDAO, BaseMockedDAO<Long, ProjectDeliverableModel>() {

    private val mockedDatabase = mutableListOf<MockedObject<ProjectDeliverableModel>>()

    init {
        mockedDatabase.add(MockedObject { deliverable1FromProject1() })
    }

    fun deliverable1FromProject1() = ProjectDeliverableModel(
        name = "Deliverable 1",
        predecessors = listOf(),
        project = mockedProjectDAO.project1OwnerByOrganization1(),
        expectedEndDate = null,
        responsible = listOf(),
        factualEndDate = null,
        id = null,
        factualStartDate = null,
        uuid = "1",
        expectedStartDate = null,
    )

    override fun findByNameAndProject(name: String, project: ProjectModel): ProjectDeliverableModel? {
        return mock(mockedDatabase.find { m -> m.reference.name == name && m.reference.project.id == project.id })
    }

    override fun findByProject(
        project: ProjectModel,
        limit: Int,
        page: Int,
        query: String?
    ): Collection<ProjectDeliverableModel> {
        return mock(mockedDatabase.filter { m -> m.reference.project.id == project.id })
    }

    override fun paginationByProject(project: ProjectModel, limit: Int, query: String?): Pagination {
        return mockPagination(mockedDatabase.filter { m -> m.reference.project.id == project.id })
    }

    override fun findByProjectAndUuid(
        project: ProjectModel,
        uuids: Collection<String>
    ): Collection<ProjectDeliverableModel> {
        return mock(mockedDatabase.filter { m ->
            m.reference.project.id == project.id && uuids.contains(m.reference.uuid)
        })
    }

    override fun findByUuid(uuid: String): ProjectDeliverableModel? {
        return mock(mockedDatabase.find { m -> m.reference.uuid == uuid })
    }

    override fun findSuccessors(deliverable: ProjectDeliverableModel): Collection<ProjectDeliverableModel> {
        return mock(mockedDatabase.filter { it.reference.predecessors.any { p -> p.id == deliverable.id }})
    }

    override fun findById(id: Long): ProjectDeliverableModel? {
        return mock(mockedDatabase.first { m -> m.reference.id == id })
    }
}