package dev.versatia.example.database

import dev.versatia.database.Database
import dev.versatia.database.playerStore
import dev.versatia.database.store.PlayerStore
import dev.versatia.di.LocalBean
import dev.versatia.example.database.Sql.Tables
import java.time.Instant
import org.bukkit.entity.Player

/** A player store: each profile is loaded before its player joins, changed in memory, and saved on quit, by autosave and when the plugin stops. */
@LocalBean
class Profiles(database: Database) : PlayerStore<Profile> by database.playerStore(
    Tables.PROFILES, Profile::uuid,
    onJoin = { player, profile -> profile.copy(name = player.name) },
    onLeave = { _, profile -> profile.copy(lastSeen = Instant.now()) },
    create = { Profile(it) },
) {
    fun give(player: Player, coins: Long) = update(player) { it.copy(coins = it.coins + coins) }

    /** False, and nothing changes, when [player] has fewer than [coins]. */
    fun trySpend(player: Player, coins: Long) = tryUpdate(player) { if (it.coins < coins) null else it.copy(coins = it.coins - coins) } != null
}
