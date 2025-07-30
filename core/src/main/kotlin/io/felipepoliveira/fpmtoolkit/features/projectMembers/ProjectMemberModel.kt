package io.felipepoliveira.fpmtoolkit.features.projectMembers

import com.fasterxml.jackson.annotation.JsonIgnore
import io.felipepoliveira.fpmtoolkit.features.organizationMembers.OrganizationMemberModel
import io.felipepoliveira.fpmtoolkit.features.projects.ProjectModel
import io.felipepoliveira.fpmtoolkit.features.users.UserModel
import jakarta.persistence.*

@Entity
@Table(name = "project_member", indexes = [
    Index(name = "UI_uuid_AT_project_member", columnList = "uuid", unique = true)
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
    val uuid: String,

    /**
     * The project associated with the member
     */
    @field:ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @field:JoinColumn(name = "project_id", nullable = false)
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