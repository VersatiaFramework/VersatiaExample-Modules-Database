package dev.versatia.example.database

import dev.versatia.VersatiaEngine
import dev.versatia.broadcast
import dev.versatia.database.store.get
import dev.versatia.database.store.update
import dev.versatia.example.database.Sql.Tables
import java.util.UUID
import java.util.logging.Logger
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

/** One row of `payments`: `id` 0 is left out of the insert and generated, `paid_at` comes from the column's `{now}` default. */
data class Payment(val id: Long = 0, val payer: UUID, val payee: UUID, val coins: Long, val note: String)

/** A player kill moves a tenth of the victim's coins to the killer and logs the payment in one transaction; the cached profiles change only if it commits. */
class Bounties(private val profiles: Profiles, private val logger: Logger) : Listener {
    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        val victim = event.player
        val killer = victim.killer ?: return
        val note = "bounty on ${victim.name}"
        profiles.transaction(victim, killer, retries = 2) {
            val bounty = this[victim].coins / 10
            if (bounty == 0L) return@transaction null
            update(victim) { it.copy(coins = it.coins - bounty) }
            update(killer) { it.copy(coins = it.coins + bounty) }
            insertReturning(Tables.PAYMENTS, Payment(payer = victim.uniqueId, payee = killer.uniqueId, coins = bounty, note = note))
        }.thenSync { payment ->
            if (payment != null) VersatiaEngine.broadcast(
                text(killer.name, NamedTextColor.AQUA).append(text(" claims ", NamedTextColor.GOLD)).append(text("${payment.coins} coins", NamedTextColor.AQUA))
                    .append(text(", the ${payment.note} (payment #${payment.id})", NamedTextColor.GOLD)),
            )
        }.onFailureSync {
            logger.warning("The $note was not paid: ${it.message}")
            killer.sendMessage(text("The bounty could not be paid; nobody lost coins", NamedTextColor.RED))
        }
    }
}
