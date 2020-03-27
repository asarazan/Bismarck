package net.sarazan.bismarck.test

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import net.sarazan.bismarck.BismarckState.*
import net.sarazan.bismarck.NuBismarck
import net.sarazan.bismarck.persisters.MemoryPersister
import net.sarazan.bismarck.ratelimit.SimpleRateLimiter
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class JvmTests {

    @Test
    fun testInsert() = runBlockingTest {
        val bismarck = NuBismarck<String>()
        assertEquals(null, bismarck.value)
        bismarck.insert("Foo")
        assertEquals("Foo", bismarck.value)
    }

    @Test
    fun testFreshness() {
        val bismarck = NuBismarck<String> {
            rateLimiter = SimpleRateLimiter(100)
        }
        assertEquals(Stale, bismarck.state)
        bismarck.insert("Foo")
        assertEquals(Fresh, bismarck.state)
        bismarck.invalidate()
        assertEquals(Stale, bismarck.state)
        bismarck.insert("Foo")
        assertEquals(Fresh, bismarck.state)
        runBlocking {
            delay(200)
        }
        assertEquals(Stale, bismarck.state)
    }

    @Test
    fun testFetch() {
        val bismarck = NuBismarck<String> {
            rateLimiter = SimpleRateLimiter(100)
            fetcher = {
                delay(100)
                "Foo"
            }
        }
        assertEquals(Stale, bismarck.state)
        bismarck.invalidate()
        assertEquals(Fetching, bismarck.state)
        assertEquals(null, bismarck.value)
        runBlocking {
            delay(50)
        }
        assertEquals(Fetching, bismarck.state)
        assertEquals(null, bismarck.value)
        runBlocking {
            delay(100)
        }
        assertEquals("Foo", bismarck.value)
        assertEquals(Fresh, bismarck.state)
    }

    @Test
    fun testPersisterInit() {
        val persister = MemoryPersister<String>()
        var bismarck = NuBismarck<String> {
            this.persister = persister
        }
        bismarck.insert("Foo")
        bismarck = NuBismarck {
            this.persister = persister
        }
        assertEquals("Foo", bismarck.value)
    }
}