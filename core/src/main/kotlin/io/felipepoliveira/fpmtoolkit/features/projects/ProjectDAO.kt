package io.felipepoliveira.fpmtoolkit.features.projects

import io.felipepoliveira.fpmtoolkit.dao.DAO
import io.felipepoliveira.fpmtoolkit.dao.Pagination
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationModel

interface ProjectDAO : DAO<Long, ProjectModel> {

    /**
     * Find all ProjectModel owned by the given 'owner' parameter. This method uses pagination
     */
    fun findByOwner(owner: OrganizationModel, itemsPerPage: Int, page: Int, queryField: String?): Collection<ProjectModel>

    /**
     * Return Pagination metadata of all ProjectModel owned by the given 'owner' parameter. This method uses pagination
     */
    fun paginationByOwner(owner: OrganizationModel, itemsPerPage: Int, queryField: String?): Pagination

    /**
     * Return an ProjectModel identified by 'owner' and 'profileName' parameter
     */
    fun findByOwnerAndProfileName(owner: OrganizationModel, profileName: String): ProjectModel?

    /**
     * Find a ProjectModel by its 'uuid'
     */
    fun findByUuid(uuid: String): ProjectModel?

}