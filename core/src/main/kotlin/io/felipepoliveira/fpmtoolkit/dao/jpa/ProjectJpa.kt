package io.felipepoliveira.fpmtoolkit.dao.jpa

import io.felipepoliveira.fpmtoolkit.dao.Pagination
import io.felipepoliveira.fpmtoolkit.ext.fetchAllPaginated
import io.felipepoliveira.fpmtoolkit.ext.fetchFirst
import io.felipepoliveira.fpmtoolkit.ext.fetchPagination
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationModel
import io.felipepoliveira.fpmtoolkit.features.projects.ProjectDAO
import io.felipepoliveira.fpmtoolkit.features.projects.ProjectModel
import jakarta.persistence.Query
import org.springframework.stereotype.Repository

@Repository
class ProjectJpa : ProjectDAO, BaseJpa<Long, ProjectModel>() {
    override fun findByOwner(
        owner: OrganizationModel,
        itemsPerPage: Int,
        page: Int,
        queryField: String?
    ): Collection<ProjectModel> {
        return queryByOwner(owner, queryField).fetchAllPaginated(itemsPerPage, page)
    }

    override fun paginationByOwner(owner: OrganizationModel, itemsPerPage: Int, queryField: String?): Pagination {
        return queryByOwner(owner, queryField).fetchPagination(itemsPerPage)
    }

    private fun queryByOwner(owner: OrganizationModel, queryField: String?): Query {
        val queryCriteria = query("p")
            .where("p.owner.id = :ownerId")

        // add query fields
        if (!queryField.isNullOrBlank()) {
            queryCriteria
                .andStartQueryGroup("name LIKE :queryField")
                .closeQueryGroup()
                .setParameter("queryField", "$queryField%")
        }

        return queryCriteria
            .setParameter("ownerId", owner.id)
            .prepare()
    }

    override fun findByOwnerAndProfileName(owner: OrganizationModel, profileName: String): ProjectModel? {
        return query("p")
            .where("p.owner.id = :ownerId")
            .and("p.profileName = :profileName")
            .setParameter("ownerId", owner.id)
            .setParameter("profileName", profileName)
            .prepare()
            .fetchFirst()
    }

    override fun findByUuid(uuid: String): ProjectModel? {
        return query("p")
            .where("p.uuid = :uuid")
            .setParameter("uuid", uuid)
            .prepare()
            .fetchFirst()
    }

    override fun getModelType(): Class<ProjectModel>  = ProjectModel::class.java
}