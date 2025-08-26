package io.felipepoliveira.fpmtoolkit.io.felipepoliveira.fpmtoolkit.dao.jpa

import io.felipepoliveira.fpmtoolkit.dao.jpa.BaseJpa
import io.felipepoliveira.fpmtoolkit.ext.fetchAll
import io.felipepoliveira.fpmtoolkit.ext.fetchFirst
import io.felipepoliveira.fpmtoolkit.features.projectDeliverables.ProjectDeliverableDAO
import io.felipepoliveira.fpmtoolkit.features.projectDeliverables.ProjectDeliverableModel
import io.felipepoliveira.fpmtoolkit.features.projects.ProjectModel
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