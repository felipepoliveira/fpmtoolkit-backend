package io.felipepoliveira.fpmtoolkit.features.users

import io.felipepoliveira.fpmtoolkit.BaseService
import io.felipepoliveira.fpmtoolkit.BusinessRuleException
import io.felipepoliveira.fpmtoolkit.BusinessRulesErrors
import io.felipepoliveira.fpmtoolkit.features.users.dto.CreateUserDTO
import io.felipepoliveira.fpmtoolkit.io.felipepoliveira.fpmtoolkit.features.users.dto.FindByPrimaryEmailAndPasswordDTO
import io.felipepoliveira.fpmtoolkit.security.PasswordRank
import io.felipepoliveira.fpmtoolkit.security.calculatePasswordRank
import io.felipepoliveira.fpmtoolkit.security.comparePassword
import io.felipepoliveira.fpmtoolkit.security.hashPassword
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.SmartValidator
import java.util.UUID

@Service
class UserService @Autowired constructor(
    val userDAO: UserDAO,
    val userMail: UserMail,
    validator: SmartValidator,
) : BaseService(validator) {

    @Throws(Exception::class)
    fun assertFindByUuid(uuid: UUID): UserModel {
        return userDAO.findByUuid(uuid) ?:
            throw Exception("An unexpected error was thrown. Could not find user identified by UUID: $uuid")
    }

    /**
     * Create a new UserModel in the database
     */
    @Throws(BusinessRuleException::class)
    @Transactional
    fun createUser(dto: CreateUserDTO): UserModel {
        // check for validation results
        val validationResult = validate(dto)
        if (validationResult.hasErrors()) {
            throw BusinessRuleException(validationResult)
        }

        // check for unsafe password
        if (!calculatePasswordRank(dto.password).isAtLeast(PasswordRank.Acceptable)) {
            throw BusinessRuleException(
                BusinessRulesErrors.InvalidPassword,
                reason = "The given password is not acceptable by the security standards"
            )
        }

        // check for duplicated email
        if (userDAO.findByPrimaryEmail(dto.primaryEmail) != null) {
            throw BusinessRuleException(
                BusinessRulesErrors.InvalidEmail,
                "The given email is already in use"
            )
        }

        // Persist the user in the database
        val user = UserModel(
            id = null,
            uuid = UUID.randomUUID(),
            preferredRegion = dto.preferredRegion,
            primaryEmail = dto.primaryEmail,
            hashedPassword = hashPassword(dto.password),
            presentationName = dto.presentationName
        )

        // Persist the user in the database
        userDAO.persist(user)

        // Send the welcome mail to the user
        userMail.sendWelcomeMail(user)

        return user
    }

    /**
     * Find a UserModel identified by its primary email and password. If the user is not found this method
     * will throw a BusinessRuleException with BusinessRulesErrors.NotFound
     */
    @Throws(BusinessRuleException::class)
    fun findByPrimaryEmailAndPassword(dto: FindByPrimaryEmailAndPasswordDTO): UserModel {

        // validate the dto
        val validationResult = validate(dto)
        if (validationResult.hasErrors()) {
            throw BusinessRuleException(validationResult)
        }

        val userNotFoundException = BusinessRuleException(
            error = BusinessRulesErrors.NotFound,
            reason = "User not found"
        )

        // find the user by its email and password match
        val user = userDAO.findByPrimaryEmail(dto.primaryEmail) ?: throw userNotFoundException
        if (!comparePassword(dto.password, user.hashedPassword)) {
            throw userNotFoundException
        }

        return user
    }

    /**
     * Try to find a UserModel identified by its uuid. Thrown an exception if not found
     */
    fun findByUuid(uuid: UUID): UserModel {
        return userDAO.findByUuid(uuid) ?: throw BusinessRuleException(
            BusinessRulesErrors.NotFound,
            "User identified by UUID $uuid not found"
        )
    }
}