package io.felipepoliveira.fpmtoolkit.io.felipepoliveira.fpmtoolkit.dao.jpa

import io.felipepoliveira.fpmtoolkit.dao.Pagination
import io.felipepoliveira.fpmtoolkit.dao.jpa.BaseJpa
import io.felipepoliveira.fpmtoolkit.dao.jpa.HqlSmartQuery
import io.felipepoliveira.fpmtoolkit.ext.fetchAll
import io.felipepoliveira.fpmtoolkit.ext.fetchAllPaginated
import io.felipepoliveira.fpmtoolkit.ext.fetchFirst
import io.felipepoliveira.fpmtoolkit.ext.fetchPagination
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationDAO
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationModel
import io.felipepoliveira.fpmtoolkit.features.users.UserModel
import jakarta.persistence.Query

class OrganizationJpa : OrganizationDAO, BaseJpa<Long, OrganizationModel>() {

    override fun findByProfileName(profileName: String): OrganizationModel? {
        return query("org")
            .where("org.profileName = :profileName")
            .prepare()
            .setParameter("profileName", profileName)
            .fetchFirst()
    }

    private fun queryByOwner(owner: UserModel): HqlSmartQuery<OrganizationModel> {
        return query("org")
            .where("org.owner.id = :ownerId")
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