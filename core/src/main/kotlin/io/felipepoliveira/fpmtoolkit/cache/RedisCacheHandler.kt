package io.felipepoliveira.fpmtoolkit.cache

import redis.clients.jedis.JedisPool
import java.time.Duration

/**
 * Redis implementation for cache handling
 */
class RedisCacheHandler(
    private val jedisPool: JedisPool
) : CacheHandler {
    override fun get(key: String): String? {
        jedisPool.resource.use { jedis ->
            return jedis.get(key)
        }
    }

    override fun keyExists(key: String): Boolean {
        jedisPool.resource.use { jedis ->
            return jedis.exists(key)
        }
    }

    override fun put(key: String, content: String, ttl: Duration?) {
        jedisPool.resource.use { jedis ->
            if (ttl != null) {
                jedis.setex(key, ttl.toSeconds(), content)
            }
            else {
                jedis.set(key, content)
            }
        }
    }
}