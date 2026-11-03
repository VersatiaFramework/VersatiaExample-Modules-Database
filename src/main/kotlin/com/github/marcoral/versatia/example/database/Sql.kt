package com.github.marcoral.versatia.example.database

/** Every table and named query of the plugin in one place, so a name is written once and a typo does not compile. */
object Sql {
    /** Table names without the prefix, as stores and builders take them. */
    object Tables {
        const val PROFILES = "profiles"
        const val PAYMENTS = "payments"
    }

    /** The `-- name:` blocks of `database/queries`. */
    object Queries {
        const val TOP_COINS = "top-coins"
        const val RANK_OF = "rank-of"
    }
}
