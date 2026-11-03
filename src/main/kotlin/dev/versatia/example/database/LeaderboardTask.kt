package dev.versatia.example.database

import dev.versatia.VersatiaEngine
import dev.versatia.broadcast
import dev.versatia.database.await
import dev.versatia.database.list
import dev.versatia.database.single
import dev.versatia.example.database.Sql.Queries
import dev.versatia.task.BackgroundTask
import dev.versatia.task.every
import kotlin.time.Duration.Companion.minutes
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit

/** Every 5 minutes the pending coins are saved and the ranking is read on a database thread; `await()` suspends the task without blocking, and it goes on on the server thread. */
class LeaderboardTask(private val profiles: Profiles) : BackgroundTask {
    override suspend fun run() {
        every(period = 5.minutes) {
            val online = Bukkit.getOnlinePlayers().associateBy { it.uniqueId }
            val (top, ranks) = profiles.saveAll().then {
                named(Queries.TOP_COINS, "limit" to 3).list<TopEntry>() to online.keys.associateWith { named(Queries.RANK_OF, "uuid" to it).single<Long>() }
            }.await()
            if (top.isEmpty()) return@every
            VersatiaEngine.broadcast(text("Richest: ", NamedTextColor.GOLD).append(Component.join(JoinConfiguration.commas(true), top.map { text(it.name, NamedTextColor.AQUA).append(text(" (${it.coins})", NamedTextColor.GRAY)) })))
            ranks.forEach { (uuid, rank) -> online.getValue(uuid).sendMessage(text("You are number ").append(text(rank, NamedTextColor.AQUA))) }
        }
    }
}
