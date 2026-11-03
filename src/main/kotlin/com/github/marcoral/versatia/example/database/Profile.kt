package com.github.marcoral.versatia.example.database

import java.time.Instant
import java.util.UUID

/** One row of `profiles`, mapped by constructor parameter names (`lastSeen` is `last_seen`); immutable, so replacing it is what marks it for saving. */
data class Profile(val uuid: UUID, val name: String = "", val coins: Long = 0, val lastSeen: Instant? = null)

/** One row of the `top-coins` query: a result is mapped the same way, with no table of its own. */
data class TopEntry(val name: String, val coins: Long)
