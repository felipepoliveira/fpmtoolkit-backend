package io.felipepoliveira.fpmtoolkit.features.organizationMembers

import io.felipepoliveira.fpmtoolkit.dao.DAO
import io.felipepoliveira.fpmtoolkit.dao.Pagination
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationModel
import io.felipepoliveira.fpmtoolkit.features.users.UserModel

interface OrganizationMemberDAO : DAO<Long, OrganizationMemberModel> {

    /**
     * Find the OrganizationMemberModel of the combination of the given organization and user. If the user
     * is not a member of the given organization this method will return null
     */
    fun findByOrganizationAndUser(organization: OrganizationModel, user: UserModel): OrganizationMemberModel?

    /**
     * Return the OrganizationMemberModel from a organization using pagination
     */
    fun findByOrganization(organization: OrganizationModel, queryField: String?, itemsPerPage: Int, page: Int): Collection<OrganizationMemberModel>

    /**
     * Find a member of the given organization identified by the given primary email
     */
    fun findByOrganizationAndUserPrimaryEmail(organization: OrganizationModel, userPrimaryEmail: String): OrganizationMemberModel?

    /**
     * Return the OrganizationMemberModel marked as owner of the given organization
     */
    fun findOwnerByOrganization(organization: OrganizationModel): OrganizationMemberModel?

    /**
     * Return Pagination metadata of the members of the given organization
     */
    fun paginationByOrganization(organization: OrganizationModel, queryField: String?, itemsPerPage: Int): Pagination

    /**
     * Find a OrganizationMemberModel identified by the given UUID. If the member is not found this method will
     * return null
     */
    fun findByUuid(uuid: String): OrganizationMemberModel?

}