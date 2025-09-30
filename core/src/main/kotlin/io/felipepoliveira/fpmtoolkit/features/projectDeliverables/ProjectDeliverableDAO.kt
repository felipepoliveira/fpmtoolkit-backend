package io.felipepoliveira.fpmtoolkit.features.projectDeliverables

import io.felipepoliveira.fpmtoolkit.dao.DAO
import io.felipepoliveira.fpmtoolkit.dao.Pagination
import io.felipepoliveira.fpmtoolkit.features.projects.ProjectModel

interface ProjectDeliverableDAO : DAO<Long, ProjectDeliverableModel> {

    /**
     * Find a project deliverable identified by its name and project
     */
    fun findByNameAndProject(name: String, project: ProjectModel): ProjectDeliverableModel?

    /**
     * Find all ProjectDeliverableModel owned by the given project using pagination
     */
    fun findByProject(project: ProjectModel, limit: Int, page: Int, query: String?): Collection<ProjectDeliverableModel>

    /**
     * Return pagination metadata of all ProjectDeliverableModel owned by the given project
     */
    fun paginationByProject(project: ProjectModel, limit: Int, query: String?): Pagination

    /**
     * Return all deliverables of a project identified by its UUIDs. If the UUID is not associated with the given
     * project the deliverable will not be returned
     */
    fun findByProjectAndUuid(project: ProjectModel, uuids: Collection<String>): Collection<ProjectDeliverableModel>

    /**
     * Find a ProjectDeliverableModel identified by its UUID
     */
    fun findByUuid(uuid: String): ProjectDeliverableModel?

    /**
     * Return all successors of the given deliverable
     */
    fun findSuccessors(deliverable: ProjectDeliverableModel): Collection<ProjectDeliverableModel>

}