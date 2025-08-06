package io.felipepoliveira.fpmtoolkit.features.projects

import com.fasterxml.jackson.annotation.JsonIgnore
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberModel
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationModel
import io.felipepoliveira.fpmtoolkit.features.projectMembers.ProjectMemberModel
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "project", indexes = [
    Index(name = "UI_uuid_AT_project", columnList = "uuid", unique = true),
    Index(name = "UI_organization_id_AND_profile_name_AT_project", columnList = "organization_id, profile_name", unique = true),
])
class ProjectModel(
    /**
     * The project relational id
     */
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    @field:JsonIgnore
    val id: Long?,

    /**
     * The project UUID
     */
    @field:Column(name = "uuid", nullable = false, length = 40)
    val uuid: String,

    /**
     * Mark the owner of the organization
     */
    @field:ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @field:JoinColumn(name = "organization_id", nullable = false)
    @field:JsonIgnore
    val owner: OrganizationModel,

    /**
     * The name of the project
     */
    @field:Column(name = "name", nullable = false, length = 140)
    @field:NotBlank
    @field:Size(min = 1, max = 140)
    var name: String,

    /**
     * The profile name of the project
     */
    @field:Column(name = "profile_name", nullable = false, length = 60)
    @field:NotBlank
    @field:Pattern(regexp = "^[\\w-]+\$")
    @field:Size(min = 1, max = 60)
    var profileName: String,

    /**
     * When the project was created
     */
    @field:Column(name = "created_at", nullable = false)
    @field:Temporal(TemporalType.TIMESTAMP)
    val createdAt: LocalDateTime,

    /**
     * Store a short description of the project.
     */
    @field:Column(name = "short_description", nullable = false, length = 500)
    val shortDescription: String,

    /**
     * Bidirectional reference to the members that is originally mapped on ProjectMemberModel.project field
     */
    @field:JsonIgnore
    @field:OneToMany(mappedBy = "project", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    val members: MutableList<ProjectMemberModel>,

    /**
     * If the project was archived store the date when the action happened
     */
    @field:Column(name = "archived_at", nullable = true)
    @field:Temporal(TemporalType.TIMESTAMP)
    var archivedAt: LocalDateTime?
)