package io.felipepoliveira.fpmtoolkit.features.projectDeliverables

import io.felipepoliveira.fpmtoolkit.features.projects.ProjectModel
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Entity
@Table(name = "project_deliverable", indexes = [
    Index(name = "UI_uuid_AT_project_deliverable", columnList = "uuid", unique = true)
])
class ProjectDeliverableModel(
    /**
     * Deliverable ID
     */
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    /**
     * Deliverable UUID (used publicly)
     */
    @field:Column(name = "uuid", length = 40, nullable = false)
    @field:NotBlank
    val uuid: String,

    /**
     * The project associated with the deliverable
     */
    @field:ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @field:JoinColumn(name = "project_id", nullable = false)
    val project: ProjectModel,

    /**
     * The name of the deliverable
     */
    @field:Column(name = "name", nullable = false, length = 200)
    @field:NotBlank
    @field:Size(min = 1, max = 200)
    val name: String,
)