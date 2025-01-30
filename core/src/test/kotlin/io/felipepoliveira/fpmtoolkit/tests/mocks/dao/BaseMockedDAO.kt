package io.felipepoliveira.fpmtoolkit.tests.mocks.dao

import io.felipepoliveira.fpmtoolkit.dao.DAO

abstract class BaseMockedDAO<IDType, ModelType> : DAO<IDType, ModelType> {

    /**
     * Return a mocked instance based on the given Mocked Object from input. If the mocked object is null it returns
     * null otherwise it will return the mock seeder in the MockedObject. This method is useful when making
     * queries from Collection<MockedObject<T>> mocked databases
     */
    fun mock(mockedObject: MockedObject<ModelType>?): ModelType? {
        return if (mockedObject == null) return null
        else mockedObject.mock()
    }

    override fun delete(m: ModelType) {}

    override fun persist(m: ModelType): ModelType = m

    override fun update(m: ModelType): ModelType = m
}