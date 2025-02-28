package io.felipepoliveira.fpmtoolkit.features.organizations

import io.felipepoliveira.fpmtoolkit.dao.DAO
import io.felipepoliveira.fpmtoolkit.dao.Pagination
import io.felipepoliveira.fpmtoolkit.features.users.UserModel

interface OrganizationDAO : DAO<Long, OrganizationModel> {

    /**
     * Find an organization identified by the given profile name. If any organization is registered with the given
     * profile name it returns null
     */
    fun findByProfileName(profileName: String): OrganizationModel?

    /**
     * Return all organizations owned by the given user using pagination
     */
    fun findByOwner(owner: UserModel, itemsPerPage: Int, currentPage: Int): Collection<OrganizationModel>

    /**
     * Return pagination metadata of all organizations owned by the given user
     */
    fun paginationByOwner(owner: UserModel, itemsPerPage: Int): Pagination

}