package io.felipepoliveira.fpmtoolkit.features.organizationMemberInvite

import io.felipepoliveira.fpmtoolkit.dao.DAO
import io.felipepoliveira.fpmtoolkit.dao.Pagination
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationModel

interface OrganizationMemberInviteDAO : DAO<Long, OrganizationMemberInviteModel> {

    /**
     * Find a member invite identified by the given organization and member email
     */
    fun findByOrganizationAndMemberEmail(organization: OrganizationModel, memberEmail: String): OrganizationMemberInviteModel?

    /**
     * Find all OrganizationMemberInviteModel of the given organization using pagination
     */
    fun findByOrganization(organization: OrganizationModel, limit: Int, page: Int, queryField: String?): Collection<OrganizationMemberInviteModel>

    /**
     * Fetch pagination metadata of all OrganizationMemberInviteModel of the given organization
     */
    fun paginationByOrganization(organization: OrganizationModel, limit: Int, queryField: String?): Pagination

    /**
     * Find a OrganizationMemberInviteModel by its uuid
     */
    fun findByUuid(uuid: String): OrganizationMemberInviteModel?

}