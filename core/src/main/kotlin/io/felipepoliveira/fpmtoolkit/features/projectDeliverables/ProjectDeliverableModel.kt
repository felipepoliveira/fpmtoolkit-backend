package io.felipepoliveira.fpmtoolkit.features.projectDeliverables

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import io.felipepoliveira.fpmtoolkit.features.projectMembers.ProjectMemberModel
import io.felipepoliveira.fpmtoolkit.features.projects.ProjectModel
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Duration
import java.time.LocalDate
import java.time.Period

@Entity
@Table(name = "project_deliverable", indexes = [
    Index(name = "UI_uuid_AT_project_deliverable", columnList = "uuid", unique = true),
    Index(name = "UI_name_AND_project_id_AT_project_deliverable", columnList = "name, project_id", unique = true)
])
class ProjectDeliverableModel(
    /**
     * Deliverable ID
     */
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    @field:JsonIgnore
    val id: Long?,

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
    @field:JsonIgnore
    val project: ProjectModel,

    /**
     * The name of the deliverable
     */
    @field:Column(name = "name", nullable = false, length = 200)
    @field:NotBlank
    @field:Size(min = 1, max = 200)
    var name: String,

    /**
     * The deliverable that will precede this one. This field can be null if the deliverable has no predecessor.
     * This field is marked as JsonIgnore to prevent recursive loop. Added a flat model mapping in predecessorFlatModel
     * getter to get it
     */
    @field:ManyToMany(fetch = FetchType.EAGER)
    @field:JoinTable(
        name = "project_deliverable_predecessors",
        joinColumns = [JoinColumn(name = "deliverable_id", nullable = false)],
        inverseJoinColumns = [JoinColumn(name = "predecessor_deliverable_id", nullable = false)]
    )
    @field:JsonIgnore
    var predecessors: Collection<ProjectDeliverableModel>,

    /**
     * Store the responsible for this project deliverable
     */
    @field:ManyToMany(fetch = FetchType.EAGER)
    @field:JoinTable(
        name = "project_deliverable_responsible",
        joinColumns = [JoinColumn(name = "deliverable_id", nullable = false)],
        inverseJoinColumns = [JoinColumn(name = "responsible_id", nullable = false)]
    )
    var responsible: Collection<ProjectMemberModel>,

    /**
     * Store the expected start date of this deliverable
     */
    @field:Column(name = "expected_start_date", nullable = true)
    @field:Temporal(TemporalType.DATE)
    var expectedStartDate: LocalDate?,

    /**
     * Store the expected end date of this deliverable
     */
    @field:Column(name = "expected_end_date", nullable = true)
    @field:Temporal(TemporalType.DATE)
    var expectedEndDate: LocalDate?,

    /**
     * Store the factual start date of this deliverable
     */
    @field:Column(name = "factual_start_date", nullable = true)
    @field:Temporal(TemporalType.DATE)
    var factualStartDate: LocalDate?,

    /**
     * Store the factual end date of this deliverable
     */
    @field:Column(name = "factual_end_date", nullable = true)
    @field:Temporal(TemporalType.DATE)
    var factualEndDate: LocalDate?,
) {

    /**
     * Field used to JSON serialization of predecessors field without recursive loop
     */
    @get:JsonProperty("predecessors")
    val predecessorFlatModel: Collection<ProjectDeliverableFlatModel>
        get() = predecessors.map { p -> ProjectDeliverableFlatModel(p) }


    /**
     * Calculate how long the expected execution of this deliverable will endure
     */
    val expectedDuration: Period?
        get() {
            if (expectedStartDate == null || expectedEndDate == null) {
                return null
            }
            return Period.between(expectedStartDate, expectedEndDate)
        }

    /**
     * Calculate how long the factual duration of this deliverable will endure
     */
    val factualDuration: Period?
        get() {
            if (factualStartDate == null || factualEndDate == null) {
                return null
            }
            return Period.between(factualStartDate, factualEndDate)
        }

    /**
     * Return a flag indicating if the given deliverable is a predecessor of this deliverable or a predecessor
     * of one of this predecessors
     */
    fun isDeepPredecessor(another: ProjectDeliverableModel): Boolean {
        if (this.predecessors.any { p -> p.id == another.id }) {
            return true
        }

        for (p in predecessors) {
            if (p.isDeepPredecessor(another)) {
                return true
            }
        }

        return false
    }
}

/**
 * A non-recursive representation of ProjectDeliverableModel class
 */
data class ProjectDeliverableFlatModel(
    val id: Long?,
    val uuid: String,
    val name: String,
) {
    constructor(projectDeliverable: ProjectDeliverableModel) : this(
        id = projectDeliverable.id,
        uuid = projectDeliverable.uuid,
        name = projectDeliverable.name
    )
}