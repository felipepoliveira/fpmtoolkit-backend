package io.felipepoliveira.fpmtoolkit.dao.jpa

import io.felipepoliveira.fpmtoolkit.dao.Pagination
import io.felipepoliveira.fpmtoolkit.dao.jpa.BaseJpa
import io.felipepoliveira.fpmtoolkit.dao.jpa.HqlSmartQuery
import io.felipepoliveira.fpmtoolkit.ext.fetchAllPaginated
import io.felipepoliveira.fpmtoolkit.ext.fetchFirst
import io.felipepoliveira.fpmtoolkit.ext.fetchPagination
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberDAO
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberModel
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationModel
import io.felipepoliveira.fpmtoolkit.features.users.UserModel
import jakarta.persistence.Query
import org.springframework.stereotype.Repository

@Repository
class OrganizationMemberJpa : OrganizationMemberDAO, BaseJpa<Long, OrganizationMemberModel>() {
    override fun findByOrganizationAndUser(organization: OrganizationModel, user: UserModel): OrganizationMemberModel? {
        return query("om")
            .where("om.organization.id = :organizationId")
            .and("om.user.id = :userId")
            .prepare()
            .setParameter("organizationId", organization.id)
            .setParameter("userId", user.id)
            .fetchFirst()
    }

    private fun queryByOrganizationOwner(organization: OrganizationModel): HqlSmartQuery<OrganizationMemberModel> {
        return query("om")
            .where("om.isOrganizationOwner = TRUE")
            .and("om.organization.id = :organizationId")
            .build()
    }

    override fun findOwnerByOrganization(organization: OrganizationModel): OrganizationMemberModel? {
        return queryByOrganizationOwner(organization)
            .prepare()
            .setParameter("organizationId", organization.id)
            .fetchFirst()
    }

    private fun queryByOrganization(
        hsq: HqlSmartQuery<OrganizationMemberModel>,
        queryField: String?,
        organization: OrganizationModel
    ): Query {

        // only join if query parameters is not null
        if(!queryField.isNullOrBlank()) {
            hsq.join("om.user", "memberUser")
        }

        // add default filters
        val whereFilters = hsq.where("om.organization.id = :organizationId")
            .setParameter("organizationId", organization.id)

        // add optional filters
        if (!queryField.isNullOrBlank()) {
            whereFilters.andStartQueryGroup("memberUser.presentationName LIKE :queryField")
                .or("memberUser.primaryEmail LIKE :queryField")
                .closeQueryGroup()
                .setParameter("queryField", "$queryField%")
        }

        // return the query
        return whereFilters.prepare()

    }

    override fun findByOrganization(
        organization: OrganizationModel,
        queryField: String?,
        itemsPerPage: Int,
        page: Int
    ): Collection<OrganizationMemberModel> {
        return queryByOrganization(query("om"), queryField, organization)
            .fetchAllPaginated(itemsPerPage, page)
    }

    override fun findByOrganizationAndUserPrimaryEmail(
        organization: OrganizationModel,
        userPrimaryEmail: String
    ): OrganizationMemberModel? {
        return query("member")
            .where("member.organization.id = :organizationId")
            .and("member.user.primaryEmail = :userPrimaryEmail")
            .prepare()
            .setParameter("organizationId", organization.id)
            .setParameter("userPrimaryEmail", userPrimaryEmail)
            .fetchFirst()
    }

    override fun paginationByOrganization(
        organization: OrganizationModel,
        queryField: String?,
        itemsPerPage: Int
    ): Pagination {
        return queryByOrganization(query("om").asPagination(), queryField, organization)
            .fetchPagination(itemsPerPage)
    }

    override fun findByUuid(uuid: String): OrganizationMemberModel? {
        return query("member")
            .where("member.uuid = :uuid")
            .prepare()
            .setParameter("uuid", uuid)
            .fetchFirst()
    }

    override fun getModelType() = OrganizationMemberModel::class.java
}