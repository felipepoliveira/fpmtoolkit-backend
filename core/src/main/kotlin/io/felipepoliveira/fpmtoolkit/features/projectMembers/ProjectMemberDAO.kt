package io.felipepoliveira.fpmtoolkit.features.projectMembers

import io.felipepoliveira.fpmtoolkit.dao.DAO
import io.felipepoliveira.fpmtoolkit.dao.Pagination
import io.felipepoliveira.fpmtoolkit.features.projectMembers.ProjectMemberModel
import io.felipepoliveira.fpmtoolkit.features.projects.ProjectModel

interface ProjectMemberDAO : DAO<Long, ProjectMemberModel> {

    /**
     * Find a ProjectMemberModel identified by 'uuid'
     */
    fun findByUuid(uuid: String): ProjectMemberModel?

    /**
     * Find all ProjectMemberModel of a given 'project'. This method uses pagination
     */
    fun findByProject(project: ProjectModel, page: Int, limit: Int, queryField: String): Collection<ProjectMemberModel>

    /**
     * Find pagination metadata of all ProjectMemberModel of a given 'project'
     */
    fun paginationByProject(project: ProjectModel, limit: Int, queryField: String): Pagination

}