package io.felipepoliveira.fpmtoolkit.dao.jpa

import io.felipepoliveira.fpmtoolkit.dao.Pagination
import io.felipepoliveira.fpmtoolkit.ext.fetchAllPaginated
import io.felipepoliveira.fpmtoolkit.ext.fetchFirst
import io.felipepoliveira.fpmtoolkit.ext.fetchPagination
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationModel
import io.felipepoliveira.fpmtoolkit.features.organizationMemberInvite.OrganizationMemberInviteDAO
import io.felipepoliveira.fpmtoolkit.features.organizationMemberInvite.OrganizationMemberInviteModel
import jakarta.persistence.Query
import org.springframework.stereotype.Repository

@Repository
class OrganizationMemberInviteJpa : OrganizationMemberInviteDAO, BaseJpa<Long, OrganizationMemberInviteModel>() {

    override fun findByOrganizationAndMemberEmail(
        organization: OrganizationModel,
        memberEmail: String
    ): OrganizationMemberInviteModel? {
        return query("invite")
            .where("invite.organization.id = :organizationId")
            .and("invite.memberEmail = :memberEmail")
            .prepare()
            .setParameter("organizationId", organization.id)
            .setParameter("memberEmail", memberEmail)
            .fetchFirst()
    }

    private fun queryByOrganization(
        hql: HqlSmartQuery<OrganizationMemberInviteModel>,
        organization: OrganizationModel,
        queryField: String?,
        ): Query {

        val whereFilters =  hql
            .where("invite.organization.id = :organizationId")

        if (!queryField.isNullOrBlank()) {
            whereFilters
                .andStartQueryGroup("invite.memberEmail LIKE :queryField")
                .closeQueryGroup()
                .setParameter("queryField", "$queryField%")
        }

        return whereFilters.prepare()
            .setParameter("organizationId", organization.id)
    }

    override fun findByOrganization(
        organization: OrganizationModel,
        limit: Int,
        page: Int,
        queryField: String?,
    ): Collection<OrganizationMemberInviteModel> {
        return queryByOrganization(
            query("invite"), organization, queryField
        ).fetchAllPaginated(limit, page)
    }

    override fun paginationByOrganization(organization: OrganizationModel, limit: Int, queryField: String?,): Pagination {
        return queryByOrganization(
            paginatedQuery("invite"), organization, queryField
        ).fetchPagination(limit)
    }

    override fun findByUuid(uuid: String): OrganizationMemberInviteModel? {
        return query("invite")
            .where("invite.uuid = :uuid")
            .prepare()
            .setParameter("uuid", uuid)
            .fetchFirst()
    }

    override fun getModelType() = OrganizationMemberInviteModel::class.java
}