package io.felipepoliveira.fpmtoolkit.dao.jpa

import io.felipepoliveira.fpmtoolkit.dao.Pagination
import io.felipepoliveira.fpmtoolkit.ext.fetchAllPaginated
import io.felipepoliveira.fpmtoolkit.ext.fetchFirst
import io.felipepoliveira.fpmtoolkit.ext.fetchPagination
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationModel
import io.felipepoliveira.fpmtoolkit.features.projects.ProjectDAO
import io.felipepoliveira.fpmtoolkit.features.projects.ProjectModel
import io.felipepoliveira.fpmtoolkit.features.users.UserModel
import jakarta.persistence.Query
import org.springframework.stereotype.Repository

@Repository
class ProjectJpa : ProjectDAO, BaseJpa<Long, ProjectModel>() {

    override fun findByOrganizationAndUserWithMembership(
        owner: OrganizationModel,
        user: UserModel,
        itemsPerPage: Int,
        page: Int,
        queryField: String?
    ): Collection<ProjectModel> {
        return queryByOwnerAndUserWithMembership(owner, user, queryField).fetchAllPaginated(itemsPerPage, page)
    }

    override fun paginationByOrganizationAndUserWithMembership(
        owner: OrganizationModel,
        user: UserModel,
        itemsPerPage: Int,
        queryField: String?
    ): Pagination {
        return queryByOwnerAndUserWithMembership(owner, user, queryField).fetchPagination(itemsPerPage)
    }

    private fun queryByOwnerAndUserWithMembership(
        owner: OrganizationModel,
        user: UserModel,
        queryField: String?
    ): Query {
        val queryCriteria = query("p")
            .join("members", "m")
            .where("p.owner.id = :ownerId")
            .and("m.user.id = :userId")
            .setParameter("ownerId", owner.id)
            .setParameter("userId", user.id)

        if (!queryField.isNullOrBlank()) {
            queryCriteria.andStartQueryGroup("p.name LIKE :queryField")
                .closeQueryGroup()
                .setParameter("queryField", "$queryField%")
        }


        return queryCriteria.prepare()
    }

    override fun findByOwner(
        owner: OrganizationModel,
        itemsPerPage: Int,
        page: Int,
        queryField: String?
    ): Collection<ProjectModel> {
        return queryByOwner(query("p"), owner, queryField).fetchAllPaginated(itemsPerPage, page)
    }

    override fun paginationByOwner(owner: OrganizationModel, itemsPerPage: Int, queryField: String?): Pagination {
        return queryByOwner(paginatedQuery("p"), owner, queryField).fetchPagination(itemsPerPage)
    }

    private fun queryByOwner(sourceQuery: HqlSmartQuery<ProjectModel>, owner: OrganizationModel, queryField: String?): Query {
        val queryCriteria = sourceQuery.where("p.owner.id = :ownerId")

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