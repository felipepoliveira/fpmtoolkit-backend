package io.felipepoliveira.fpmtoolkit.features.users

import io.felipepoliveira.fpmtoolkit.dao.DAO

interface UserDAO : DAO<Long, UserModel> {

    /**
     * Find a user identified by its primary email
     */
    fun findByPrimaryEmail(primaryEmail: String): UserModel?

    /**
     * Find a UserModel identified by its UUID. If the user is not found return null instead
     */
    fun findByUuid(uuid: String): UserModel?

}