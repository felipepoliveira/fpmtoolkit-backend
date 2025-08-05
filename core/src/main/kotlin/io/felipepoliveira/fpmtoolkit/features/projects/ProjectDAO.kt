package io.felipepoliveira.fpmtoolkit.features.projects

import io.felipepoliveira.fpmtoolkit.dao.DAO
import io.felipepoliveira.fpmtoolkit.dao.Pagination
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationModel
import io.felipepoliveira.fpmtoolkit.features.users.UserModel

interface ProjectDAO : DAO<Long, ProjectModel> {

    /**
     * Find all projects where the given 'user' is a member. This method use pagination
     */
    fun findByOrganizationAndUserWithMembership(owner: OrganizationModel, user: UserModel, itemsPerPage: Int, page: Int, queryField: String?): Collection<ProjectModel>

    /**
     * Find pagination metadata of all projects where the given 'user' is a member
     */
    fun paginationByOrganizationAndUserWithMembership(owner: OrganizationModel, user: UserModel, itemsPerPage: Int, queryField: String?): Pagination

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