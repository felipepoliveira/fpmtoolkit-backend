package io.felipepoliveira.fpmtoolkit.features.projectMembers

import com.fasterxml.jackson.annotation.JsonIgnore
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberModel
import io.felipepoliveira.fpmtoolkit.features.projects.ProjectModel
import io.felipepoliveira.fpmtoolkit.features.users.UserModel
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull

@Entity
@Table(name = "project_member", indexes = [
    Index(name = "UI_uuid_AT_project_member", columnList = "uuid", unique = true),
    Index(name = "UI_project_id_AND_user_id_AT_project_member", columnList = "project_id, user_id", unique = true),
])
class ProjectMemberModel(
    /**
     * The id of the project member
     */
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    @field:JsonIgnore
    val id: Long?,

    /**
     * The UUID of the project member
     */
    @field:Column(name = "uuid", nullable = false, length = 40)
    @field:NotNull
    val uuid: String,

    /**
     * The project associated with the member
     */
    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(name = "project_id", nullable = false)
    @field:JsonIgnore
    val project: ProjectModel,

    /**
     * The user associated in the project membership
     */
    @field:ManyToOne
    @field:JoinColumn(name = "user_id", nullable = false)
    val user: UserModel,
)

enum class ProjectMemberRoles {
    
}