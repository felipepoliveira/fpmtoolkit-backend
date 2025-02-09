package io.felipepoliveira.fpmtoolkit.tests.mocks.cache

import io.felipepoliveira.fpmtoolkit.cache.CacheHandler
import java.time.Duration

/**
 * In this mocked implementation it will never store anything and always return null or false in all operations from
 * CacheHandler
 * @see CacheHandler
 */
class MockedCacheHandler : CacheHandler {

    override fun get(key: String): String? = null

    override fun keyExists(key: String): Boolean = false

    override fun put(key: String, content: String, ttl: Duration?) {}
}