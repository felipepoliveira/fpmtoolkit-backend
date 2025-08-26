package io.felipepoliveira.fpmtoolkit.features.projectMembers

import io.felipepoliveira.fpmtoolkit.dao.DAO
import io.felipepoliveira.fpmtoolkit.dao.Pagination
import io.felipepoliveira.fpmtoolkit.features.projectMembers.ProjectMemberModel
import io.felipepoliveira.fpmtoolkit.features.projects.ProjectModel
import io.felipepoliveira.fpmtoolkit.features.users.UserModel

interface ProjectMemberDAO : DAO<Long, ProjectMemberModel> {

    /**
     * Find a ProjectMemberModel identified by 'uuid'
     */
    fun findByUuid(uuid: String): ProjectMemberModel?

    /**
     * Find all ProjectMemberModel of a given 'project'. This method uses pagination
     */
    fun findByProject(project: ProjectModel, page: Int, limit: Int, queryField: String?): Collection<ProjectMemberModel>

    /**
     * Find pagination metadata of all ProjectMemberModel of a given 'project'
     */
    fun paginationByProject(project: ProjectModel, limit: Int, queryField: String?): Pagination

    /**
     * Find the membership of the given user on the given project. If the user is not a member return null
     */
    fun findByProjectAndUser(project: ProjectModel, user: UserModel): ProjectMemberModel?

    /**
     * Find all members identified by the project and the given UUIDs. If the uuid is not present on the project
     * it will not be returned
     */
    fun findByProjectAndUuid(project: ProjectModel, uuids: Collection<String>): Collection<ProjectMemberModel>

}