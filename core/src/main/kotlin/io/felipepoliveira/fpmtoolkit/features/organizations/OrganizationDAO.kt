package io.felipepoliveira.fpmtoolkit.features.organizations

import io.felipepoliveira.fpmtoolkit.dao.DAO
import io.felipepoliveira.fpmtoolkit.dao.Pagination
import io.felipepoliveira.fpmtoolkit.features.users.UserModel

interface OrganizationDAO : DAO<Long, OrganizationModel> {

    /**
     * Return all organizations owned by the given user using pagination
     */
    fun findByOwner(owner: UserModel, itemsPerPage: Int, currentPage: Int): Collection<OrganizationModel>

    /**
     * Return pagination metadata of all organizations owned by the given user
     */
    fun paginationByOwner(owner: UserModel, itemsPerPage: Int): Pagination

    /**
     * Find an organization identified by the given profile name. If any organization is registered with the given
     * profile name it returns null
     */
    fun findByProfileName(profileName: String): OrganizationModel?

    /**
     * Return all organizations where th given user is a member using pagination
     */
    fun findByMember(member: UserModel, itemsPerPage: Int, currentPage: Int): Collection<OrganizationModel>

    /**
     * Return pagination metadata of all organizations where the given user is a member
     */
    fun paginationByMember(member: UserModel, itemsPerPage: Int): Pagination

    /**
     * Find a OrganizationModel identified by its UUID
     */
    fun findByUuid(uuid: String): OrganizationModel?

}