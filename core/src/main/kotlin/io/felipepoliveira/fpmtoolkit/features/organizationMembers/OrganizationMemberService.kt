package io.felipepoliveira.fpmtoolkit.features.organizationMembers

import io.felipepoliveira.fpmtoolkit.BusinessRuleException
import io.felipepoliveira.fpmtoolkit.BusinessRulesError
import io.felipepoliveira.fpmtoolkit.features.organizations.OrganizationModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrganizationMemberService @Autowired constructor(
    private val organizationMemberDAO: OrganizationMemberDAO,
) {

    companion object {
        const val MAXIMUM_AMOUNT_OF_FREE_MEMBERS: Int = 20
    }

    /**
     * Add a new organization member in the given organization
     */
    @Transactional
    fun addOrganizationMember(
        organization: OrganizationModel,
        newMember: OrganizationMemberModel): OrganizationMemberModel {

        // if the user is already a member of the organization throw an error
        if (organizationMemberDAO.findByOrganizationAndUser(organization, newMember.user) != null) {
            throw BusinessRuleException(
                BusinessRulesError.FORBIDDEN,
                "This user is already a member of the organization"
            )
        }

        // check if the maximum amount of members per organization has being reached
        val paginationOfOrgMembers = organizationMemberDAO.paginationByOrganization(organization, 1)
        if (paginationOfOrgMembers.totalRecords >= MAXIMUM_AMOUNT_OF_FREE_MEMBERS) {
            throw BusinessRuleException(
                BusinessRulesError.PAYMENT_REQUIRED,
                "The maximum amount of members in this organization has being reach. " +
                        "Current amount: ${paginationOfOrgMembers.totalRecords}; Limit: $MAXIMUM_AMOUNT_OF_FREE_MEMBERS"
            )
        }

        // if the new member is marked to be the owner of the organization and organization already have a owner
        // throw an error
        val currentOrgOwner = organizationMemberDAO.findOwnerByOrganization(organization)
        if (newMember.isOrganizationOwner && currentOrgOwner != null) {
            throw BusinessRuleException(
                BusinessRulesError.FORBIDDEN,
                "The new member is marked to be the organization owner but the organization already have a owner"
            )
        }

        // persist the new member into the database
        organization.members.add(newMember)
        organizationMemberDAO.persist(newMember)

        return newMember
    }

}