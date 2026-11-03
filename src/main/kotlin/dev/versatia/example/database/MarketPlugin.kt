package dev.versatia.example.database

import dev.versatia.FriendlyName
import dev.versatia.Phase
import dev.versatia.VersatiaPlugin
import dev.versatia.database.Database
import dev.versatia.database.list
import dev.versatia.example.database.Sql.Queries
import dev.versatia.lifecycle.AutoInvoke

/** `Database` is the module's prototype: this plugin's own database, its migrations from `database/migrations` already applied. */
@FriendlyName("market")
class MarketPlugin : VersatiaPlugin() {
    /** Runs at start and at `/versatia reload market`, where a blocking call on the server thread is allowed; anywhere else it belongs in `database.async { }`. */
    @AutoInvoke(Phase.PLUGIN_RELOAD_OR_START)
    fun report(database: Database) {
        val status = database.status
        logger.info("${status.engine} ${status.target}, schema V${status.schemaVersion}")
        val top = database.named(Queries.TOP_COINS, "limit" to 3).list<TopEntry>()
        logger.info(if (top.isEmpty()) "Nobody has coins yet" else "Richest: ${top.joinToString { "${it.name} (${it.coins})" }}")
    }
}
