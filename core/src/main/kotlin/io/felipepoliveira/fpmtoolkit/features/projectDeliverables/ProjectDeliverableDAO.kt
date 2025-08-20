package io.felipepoliveira.fpmtoolkit.features.projectDeliverables

import io.felipepoliveira.fpmtoolkit.dao.DAO
import io.felipepoliveira.fpmtoolkit.features.projects.ProjectModel

interface ProjectDeliverableDAO : DAO<Long, ProjectDeliverableModel> {

    /**
     * Find a project deliverable identified by its name and project
     */
    fun findByNameAndProject(name: String, project: ProjectModel): ProjectDeliverableModel?

    /**
     * Return all deliverables of a project identified by its UUIDs. If the UUID is not associated with the given
     * project the deliverable will not be returned
     */
    fun findByProjectAndUuid(project: ProjectModel, uuids: Collection<String>): Collection<ProjectDeliverableModel>

    /**
     * Return all successors of the given deliverable
     */
    fun findSuccessors(deliverable: ProjectDeliverableModel): Collection<ProjectDeliverableModel>

}