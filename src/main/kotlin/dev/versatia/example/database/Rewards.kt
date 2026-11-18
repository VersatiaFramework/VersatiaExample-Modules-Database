package dev.versatia.example.database

import dev.versatia.text.Text
import dev.versatia.text.arg
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.PlayerDeathEvent

/** Coins change on the server thread without touching the database: a coin per mob killed, and a fee for dying to anything but a player, skipped when it cannot be paid. */
class Rewards(private val profiles: Profiles, private val text: Text) : Listener {
    @EventHandler
    fun onKill(event: EntityDeathEvent) {
        val killer = event.entity.killer ?: return
        if (event.entity !is Player) profiles.give(killer, 1)
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        val player = event.player
        if (player.killer == null && profiles.trySpend(player, 5)) player.sendMessage(text.parse("<negative>Dying cost you {coins} coins", arg("coins", 5)))
    }
}
