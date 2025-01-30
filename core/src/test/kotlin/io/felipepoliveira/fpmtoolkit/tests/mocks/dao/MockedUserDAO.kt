package io.felipepoliveira.fpmtoolkit.tests.mocks.dao

import io.felipepoliveira.fpmtoolkit.features.users.UserDAO
import io.felipepoliveira.fpmtoolkit.features.users.UserModel
import io.felipepoliveira.fpmtoolkit.io.felipepoliveira.fpmtoolkit.commons.i18n.I18nRegion
import io.felipepoliveira.fpmtoolkit.security.hashPassword
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class MockedUserDAO : BaseMockedDAO<Long, UserModel>(), UserDAO {

    private val mockedDatabase = mutableListOf<MockedObject<UserModel>>()

    init {
        mockedDatabase.add(MockedObject { user1() })
    }

    /**
     * The default password used for all mocked users
     */
    fun defaultPassword() = "Usr1Pwd!"

    /**
     * Create a mocked instance of the user 1. This entity is included in the mocked database
     */
    fun user1() = UserModel(
        uuid = UUID.fromString("4c89e3d7-1b72-4b25-b6a1-91be8d127a5b"),
        preferredRegion = I18nRegion.PT_BR,
        primaryEmail = "user1@mock.com",
        id = 1,
        hashedPassword = hashPassword(defaultPassword()),
        presentationName = "User 1"
    )

    override fun findByPrimaryEmail(primaryEmail: String): UserModel? {
        return mock(
            mockedDatabase.find { m -> m.reference.primaryEmail == primaryEmail }
        )
    }

    override fun findByUuid(uuid: UUID): UserModel? {
        return mock(
            mockedDatabase.find { m -> m.reference.uuid == uuid }
        )
    }

    override fun findById(id: Long): UserModel? {
        return mock(
            mockedDatabase.find { m -> m.reference.id == id }
        )
    }

}