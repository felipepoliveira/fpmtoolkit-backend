package io.felipepoliveira.fpmtoolkit.tests.mocks.dao

import io.felipepoliveira.fpmtoolkit.dao.Pagination
import io.felipepoliveira.fpmtoolkit.features.projectMembers.ProjectMemberDAO
import io.felipepoliveira.fpmtoolkit.features.projectMembers.ProjectMemberModel
import io.felipepoliveira.fpmtoolkit.features.projects.ProjectModel
import io.felipepoliveira.fpmtoolkit.features.users.UserModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class MockedProjectMemberDAO @Autowired constructor(
    private val mockedProjectDAO: MockedProjectDAO,
    private val mockedUserDAO: MockedUserDAO,
): ProjectMemberDAO, BaseMockedDAO<Long, ProjectMemberModel>() {

    private val mockedDatabase: MutableList<MockedObject<ProjectMemberModel>> = mutableListOf()

    init {
        mockedDatabase.add(MockedObject { user1MembershipInProject1() })
        mockedDatabase.add(MockedObject { user10MembershipInProject1() })
    }

    fun user1MembershipInProject1() = ProjectMemberModel(
        user = mockedUserDAO.user1(),
        uuid = "user1",
        id = 1,
        project = mockedProjectDAO.project1OwnerByOrganization1()
    )

    fun user10MembershipInProject1() = ProjectMemberModel(
        user = mockedUserDAO.user10OfOrg1WithAllRoles(),
        uuid = "user10",
        id = 10,
        project = mockedProjectDAO.project1OwnerByOrganization1()
    )

    fun user11NotAMemberOfProject1() = mockedUserDAO.user11OfOrg1WithNoRoles()

    override fun findByUuid(uuid: String): ProjectMemberModel? {
        return mock(mockedDatabase.find { m -> m.reference.uuid == uuid })
    }

    override fun findByProject(
        project: ProjectModel,
        page: Int,
        limit: Int,
        queryField: String?
    ): Collection<ProjectMemberModel> {
        return mock(mockedDatabase.filter { m -> m.reference.project.id == project.id })
    }

    override fun paginationByProject(project: ProjectModel, limit: Int, queryField: String?): Pagination {
        return mockPagination(mockedDatabase.filter { m -> m.reference.project.id == project.id })
    }


    override fun findByProjectAndUser(project: ProjectModel, user: UserModel): ProjectMemberModel? {
        return mock(mockedDatabase.find { m -> m.reference.project.id == project.id && m.reference.user.id == user.id })
    }

    override fun findByProjectAndUuid(
        project: ProjectModel,
        uuids: Collection<String>
    ): Collection<ProjectMemberModel> {
        return mock(mockedDatabase.filter { m ->
            m.reference.project.id == project.id &&
            uuids.contains(m.reference.uuid)
        })
    }

    override fun findById(id: Long): ProjectMemberModel? {
        return mock(mockedDatabase.find { m -> m.reference.id == id })
    }
}