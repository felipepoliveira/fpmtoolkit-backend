package io.felipepoliveira.fpmtoolkit.dao.jpa

import io.felipepoliveira.fpmtoolkit.dao.Pagination
import io.felipepoliveira.fpmtoolkit.dao.jpa.BaseJpa
import io.felipepoliveira.fpmtoolkit.dao.jpa.HqlSmartQuery
import io.felipepoliveira.fpmtoolkit.ext.fetchAll
import io.felipepoliveira.fpmtoolkit.ext.fetchAllPaginated
import io.felipepoliveira.fpmtoolkit.ext.fetchFirst
import io.felipepoliveira.fpmtoolkit.ext.fetchPagination
import io.felipepoliveira.fpmtoolkit.features.projectMembers.ProjectMemberDAO
import io.felipepoliveira.fpmtoolkit.features.projectMembers.ProjectMemberModel
import io.felipepoliveira.fpmtoolkit.features.projects.ProjectModel
import io.felipepoliveira.fpmtoolkit.features.users.UserModel
import jakarta.persistence.Query
import org.springframework.stereotype.Repository

@Repository
class ProjectMemberJpa : ProjectMemberDAO, BaseJpa<Long, ProjectMemberModel>() {

    override fun findByUuid(uuid: String): ProjectMemberModel? {
        return query("m")
            .where("m.uuid = :uuid")
            .setParameter("uuid", uuid)
            .prepare()
            .fetchFirst()
    }

    private fun queryByProject(hqlSmartQuery: HqlSmartQuery<ProjectMemberModel>, project: ProjectModel, queryField: String?): Query {
        val queryCriteria = hqlSmartQuery
            .where("m.project.id = :projectId")
            .setParameter("projectId", project.id)

        if (!queryField.isNullOrBlank()) {
            queryCriteria
                .andStartQueryGroup("m.user.email LIKE :queryField")
                .or("m.user.presentationName LIKE :queryField")
                .closeQueryGroup()
                .setParameter("queryField", "$queryField%")
        }

        return queryCriteria.prepare()
    }

    override fun findByProject(
        project: ProjectModel,
        page: Int,
        limit: Int,
        queryField: String?
    ): Collection<ProjectMemberModel> {
        return queryByProject(query("m"), project, queryField)
            .fetchAllPaginated(limit, page)
    }

    override fun paginationByProject(project: ProjectModel, limit: Int, queryField: String?): Pagination {
        return queryByProject(paginatedQuery("m"), project, queryField)
            .fetchPagination(limit)
    }

    override fun findByProjectAndUser(project: ProjectModel, user: UserModel): ProjectMemberModel? {
        return query("m")
            .where("m.project.id = :projectId")
            .and("m.user.id = :userId")
            .setParameter("projectId", project.id)
            .setParameter("userId", user.id)
            .prepare()
            .fetchFirst()
    }

    override fun findByProjectAndUuid(
        project: ProjectModel,
        uuids: Collection<String>
    ): Collection<ProjectMemberModel> {
        return query("m")
            .where("m.project.id = :projectId")
            .and("m.uuid IN :uuids")
            .setParameter("projectId", project.id)
            .setParameter("uuids", uuids)
            .prepare()
            .fetchAll()
    }

    override fun getModelType() = ProjectMemberModel::class.java
}