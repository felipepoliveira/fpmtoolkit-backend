package io.felipepoliveira.fpmtoolkit.tests.mocks.dao

class MockedObject<T>(
    private val seeder: () -> T
) {

    /**
     * The reference object that should be used for comparison
     */
    val reference: T = seeder()

    fun mock(): T = seeder()

}