package io.felipepoliveira.fpmtoolkit.dao.jpa

import io.felipepoliveira.fpmtoolkit.dao.Pagination
import io.felipepoliveira.fpmtoolkit.dao.jpa.BaseJpa
import io.felipepoliveira.fpmtoolkit.ext.fetchAll
import io.felipepoliveira.fpmtoolkit.ext.fetchAllPaginated
import io.felipepoliveira.fpmtoolkit.ext.fetchFirst
import io.felipepoliveira.fpmtoolkit.ext.fetchPagination
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationModel
import io.felipepoliveira.fpmtoolkit.features.projectDeliverables.ProjectDeliverableDAO
import io.felipepoliveira.fpmtoolkit.features.projectDeliverables.ProjectDeliverableModel
import io.felipepoliveira.fpmtoolkit.features.projects.ProjectModel
import jakarta.persistence.Query
import org.springframework.stereotype.Repository

@Repository
class ProjectDeliverableJpa : ProjectDeliverableDAO, BaseJpa<Long, ProjectDeliverableModel>() {

    override fun findByNameAndProject(name: String, project: ProjectModel): ProjectDeliverableModel? {
        return query("pd")
            .where("pd.name = :name")
            .and("pd.project.id = :projectId")
            .setParameter("name", name)
            .setParameter("projectId", project.id)
            .prepare()
            .fetchFirst()
    }

    override fun findByProject(
        project: ProjectModel,
        limit: Int,
        page: Int,
        query: String?
    ): Collection<ProjectDeliverableModel> {
        return queryByProject(query("o"), project, query)
            .fetchAllPaginated(limit, page)
    }

    override fun paginationByProject(project: ProjectModel, limit: Int, query: String?): Pagination {
        return queryByProject(paginatedQuery("o"), project, query)
            .fetchPagination(limit)
    }

    private fun queryByProject(
        sourceHql: HqlSmartQuery<ProjectDeliverableModel>, project: ProjectModel, query: String?
    ): Query {
        val queryCriteria = sourceHql.where("o.project.id = :projectId")

        // add query parameters
        if (!query.isNullOrBlank()) {
            queryCriteria.andStartQueryGroup("o.name LIKE :query")
                .closeQueryGroup()
                .setParameter("query", "$query%")
        }

        queryCriteria.setParameter("projectId", project.id)

        return queryCriteria.prepare()
    }

    override fun findByProjectAndUuid(
        project: ProjectModel,
        uuids: Collection<String>
    ): Collection<ProjectDeliverableModel> {
        return query("pd")
            .where("pd.project.id = :projectId")
            .and("pd.uuid IN :uuids")
            .setParameter("projectId", project.id)
            .setParameter("uuids", uuids)
            .prepare()
            .fetchAll()
    }

    override fun findByUuid(uuid: String): ProjectDeliverableModel? {
        return query("o")
            .where("o.uuid = :uuid")
            .setParameter("uuid", uuid)
            .prepare()
            .fetchFirst()
    }

    override fun findSuccessors(deliverable: ProjectDeliverableModel): Collection<ProjectDeliverableModel> {
        return query("pd")
            .join("pd.predecessors", "predecessor")
            .where("predecessor.id = :deliverableId")
            .setParameter("deliverableId", deliverable.id)
            .prepare()
            .fetchAll()

    }

    override fun getModelType(): Class<ProjectDeliverableModel> = ProjectDeliverableModel::class.java
}