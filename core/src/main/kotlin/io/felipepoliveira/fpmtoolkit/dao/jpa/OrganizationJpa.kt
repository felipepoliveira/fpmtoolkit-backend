package io.felipepoliveira.fpmtoolkit.dao.jpa

import io.felipepoliveira.fpmtoolkit.dao.Pagination
import io.felipepoliveira.fpmtoolkit.ext.fetchAllPaginated
import io.felipepoliveira.fpmtoolkit.ext.fetchFirst
import io.felipepoliveira.fpmtoolkit.ext.fetchPagination
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationDAO
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationModel
import io.felipepoliveira.fpmtoolkit.features.users.UserModel
import jakarta.persistence.Query
import org.springframework.stereotype.Repository

@Repository
class OrganizationJpa : OrganizationDAO, BaseJpa<Long, OrganizationModel>() {

    override fun findByProfileName(profileName: String): OrganizationModel? {
        return query("org")
            .where("org.profileName = :profileName")
            .prepare()
            .setParameter("profileName", profileName)
            .fetchFirst()
    }

    private fun queryByMember(hqlSmartQuery: HqlSmartQuery<OrganizationModel>, member: UserModel): Query {
        return hqlSmartQuery
            .join("o.members", "m")
            .where("m.user.id = :memberId")
            .prepare()
            .setParameter("memberId", member.id)
    }

    override fun findByMember(member: UserModel, itemsPerPage: Int, currentPage: Int): Collection<OrganizationModel> {
        return queryByMember(query("o"), member).fetchAllPaginated(itemsPerPage, currentPage)
    }

    override fun paginationByMember(member: UserModel, itemsPerPage: Int): Pagination {
        return queryByMember(query("o").asPagination(), member).fetchPagination(itemsPerPage)
    }

    override fun findByUuid(uuid: String): OrganizationModel? {
        return query("org")
            .where("org.uuid = :uuid")
            .prepare()
            .setParameter("uuid", uuid)
            .fetchFirst()
    }

    private fun queryByOwner(owner: UserModel): HqlSmartQuery<OrganizationModel> {
        return query("org")
            .join("org.members", "m")
            .where("m.isOrganizationOwner = TRUE") // should be the owner of the org
            .and("m.user.id = :ownerId") // should match the given user id
            .build()
    }

    override fun findByOwner(owner: UserModel, itemsPerPage: Int, currentPage: Int): Collection<OrganizationModel> {
        return queryByOwner(owner)
            .prepare()
            .setParameter("ownerId", owner.id)
            .fetchAllPaginated(itemsPerPage, currentPage)
    }

    override fun paginationByOwner(owner: UserModel, itemsPerPage: Int): Pagination {
        return queryByOwner(owner).asPagination()
            .prepare()
            .setParameter("ownerId", owner.id)
            .fetchPagination(itemsPerPage)
    }

    override fun getModelType() = OrganizationModel::class.java
}