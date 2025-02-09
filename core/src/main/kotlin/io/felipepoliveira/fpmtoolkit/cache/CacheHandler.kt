package io.felipepoliveira.fpmtoolkit.cache

import java.time.Duration

/**
 * Main abstraction for cache handling functionalities
 */
interface CacheHandler {

    /**
     * Return the content stored in the cache identified by the given key
     */
    fun get(key: String): String?

    /**
     * Return a flag indicating if the key exists in the cache database
     */
    fun keyExists(key: String): Boolean

    /**
     * Include a value in the cache database
     */
    fun put(key: String, content: String, ttl: Duration? = null)

}