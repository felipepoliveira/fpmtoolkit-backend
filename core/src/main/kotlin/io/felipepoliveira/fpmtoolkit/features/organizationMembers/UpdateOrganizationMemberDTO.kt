package io.felipepoliveira.fpmtoolkit.features.organizationMembers

data class UpdateOrganizationMemberDTO(
    /**
     * The roles of the organization member
     */
    val roles: Collection<OrganizationMemberRoles>
)