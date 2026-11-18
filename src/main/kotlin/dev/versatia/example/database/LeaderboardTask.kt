package dev.versatia.example.database

import dev.versatia.VersatiaEngine
import dev.versatia.broadcast
import dev.versatia.database.await
import dev.versatia.database.list
import dev.versatia.database.single
import dev.versatia.example.database.Sql.Queries
import dev.versatia.task.BackgroundTask
import dev.versatia.task.every
import dev.versatia.text.Text
import dev.versatia.text.arg
import kotlin.time.Duration.Companion.minutes
import org.bukkit.Bukkit

/** Every 5 minutes the pending coins are saved and the ranking is read on a database thread; `await()` suspends the task without blocking, and it goes on on the server thread. */
class LeaderboardTask(private val profiles: Profiles, private val text: Text) : BackgroundTask {
    override suspend fun run() {
        every(period = 5.minutes) {
            val online = Bukkit.getOnlinePlayers().associateBy { it.uniqueId }
            val (top, ranks) = profiles.saveAll().then {
                named(Queries.TOP_COINS, "limit" to 3).list<TopEntry>() to online.keys.associateWith { named(Queries.RANK_OF, "uuid" to it).single<Long>() }
            }.await()
            if (top.isEmpty()) return@every
            val entries = top.map { text.parse("<neutral>{name} ({coins})", arg("name", it.name), arg("coins", it.coins)) }
            VersatiaEngine.broadcast(text.parse("<neutral><label>Richest:</label> {top}", arg("top", entries)))
            ranks.forEach { (uuid, rank) -> online.getValue(uuid).sendMessage(text.parse("<neutral>You are number {rank}", arg("rank", rank))) }
        }
    }
}
