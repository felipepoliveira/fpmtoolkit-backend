package io.felipepoliveira.fpmtoolkit.tests.mocks.dao

import io.felipepoliveira.fpmtoolkit.features.users.UserDAO
import io.felipepoliveira.fpmtoolkit.features.users.UserModel
import io.felipepoliveira.fpmtoolkit.commons.i18n.I18nRegion
import io.felipepoliveira.fpmtoolkit.security.hashPassword
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
class MockedUserDAO : BaseMockedDAO<Long, UserModel>(), UserDAO {

    private val mockedDatabase = mutableListOf<MockedObject<UserModel>>()

    private val primaryEmailConfirmedAt = LocalDateTime.now()

    init {
        mockedDatabase.add(MockedObject { user1() })
        mockedDatabase.add(MockedObject { user2() })
        mockedDatabase.add(MockedObject { userWithUnconfirmedPrimaryEmail() })
    }

    /**
     * The default password used for all mocked users
     */
    fun defaultPassword() = "Usr1Pwd!"

    /**
     * Create a mocked instance of the user 1. This entity is included in the mocked database
     */
    fun user1() = UserModel(
        uuid = "4c89e3d7-1b72-4b25-b6a1-91be8d127a5b",
        preferredRegion = I18nRegion.PT_BR,
        primaryEmail = "user1@mock.com",
        id = 1,
        hashedPassword = hashPassword(defaultPassword()),
        presentationName = "User 1",
        primaryEmailConfirmedAt = primaryEmailConfirmedAt
    )

    /**
     * Create a mocked instance of the user 2. This entity is included in the mocked database
     */
    fun user2() = UserModel(
        uuid = "3f1b6d9e-1d74-4d5e-90cd-6f3f1b6a9eab",
        preferredRegion = I18nRegion.PT_BR,
        primaryEmail = "user2@mock.com",
        id = 2,
        hashedPassword = hashPassword(defaultPassword()),
        presentationName = "User 2",
        primaryEmailConfirmedAt = primaryEmailConfirmedAt
    )

    /**
     * A user account that never had its primary email confirmed
     */
    fun userWithUnconfirmedPrimaryEmail() = UserModel(
        uuid = "794e0e24-ce57-4d38-9c05-b09478c1d013",
        preferredRegion = I18nRegion.PT_BR,
        primaryEmail = "user_emailnotconfirmed@mock.com",
        id = 4000,
        hashedPassword = hashPassword(defaultPassword()),
        presentationName = "User With Unconfirmed Primary Email",
        primaryEmailConfirmedAt = null
    )

    override fun findByPrimaryEmail(primaryEmail: String): UserModel? {
        return mock(
            mockedDatabase.find { m -> m.reference.primaryEmail == primaryEmail }
        )
    }

    override fun findByUuid(uuid: String): UserModel? {
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