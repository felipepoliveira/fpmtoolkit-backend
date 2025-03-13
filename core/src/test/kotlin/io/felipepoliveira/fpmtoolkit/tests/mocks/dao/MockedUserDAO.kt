package io.felipepoliveira.fpmtoolkit.tests.mocks.dao

import io.felipepoliveira.fpmtoolkit.commons.i18n.I18nRegion
import io.felipepoliveira.fpmtoolkit.features.users.UserDAO
import io.felipepoliveira.fpmtoolkit.features.users.UserModel
import io.felipepoliveira.fpmtoolkit.security.hashPassword
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class MockedUserDAO : BaseMockedDAO<Long, UserModel>(), UserDAO {

    private val mockedDatabase = mutableListOf<MockedObject<UserModel>>()

    private val primaryEmailConfirmedAt = LocalDateTime.now()

    init {
        mockedDatabase.add(MockedObject { user1() })
        mockedDatabase.add(MockedObject { user2() })
        mockedDatabase.add(MockedObject { user10OfOrg1WithAllRoles() })
        mockedDatabase.add(MockedObject { user11OfOrg1WithNoRoles() })
        mockedDatabase.add(MockedObject { userWithUnconfirmedPrimaryEmail() })
        mockedDatabase.add(MockedObject { userWithALotOfOrganizations() })
        mockedDatabase.add(MockedObject { userWithNoOrganization() })
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

    fun user10OfOrg1WithAllRoles() = UserModel(
        uuid = "4c89e3d7-1b72-4b25-b6a1-91be8d127a5c",
        preferredRegion = I18nRegion.PT_BR,
        primaryEmail = "user10@mock.com",
        id = 10,
        hashedPassword = hashPassword(defaultPassword()),
        presentationName = "User 10",
        primaryEmailConfirmedAt = primaryEmailConfirmedAt
    )

    fun user11OfOrg1WithNoRoles() = UserModel(
        uuid = "4c89e3d7-1b72-4b25-b6a1-91be8d127a5d",
        preferredRegion = I18nRegion.PT_BR,
        primaryEmail = "user11@mock.com",
        id = 11,
        hashedPassword = hashPassword(defaultPassword()),
        presentationName = "User 11",
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

    fun userWithNoOrganization() = UserModel(
        uuid = "user-with-no-org",
        id = 9123,
        presentationName = "User with no organization",
        primaryEmailConfirmedAt = primaryEmailConfirmedAt,
        hashedPassword = hashPassword(defaultPassword()),
        preferredRegion = I18nRegion.PT_BR,
        primaryEmail = "userwithnoorg@email.com"
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

    fun userWithALotOfOrganizations() = UserModel(
        uuid = "550e8400-e29b-41d4-a716-446655440000",
        preferredRegion = I18nRegion.PT_BR,
        primaryEmail = "userwithalotoforgs@email.com",
        id = 9000,
        hashedPassword = hashPassword(defaultPassword()),
        presentationName = "User with a lot of organizations",
        primaryEmailConfirmedAt = primaryEmailConfirmedAt
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