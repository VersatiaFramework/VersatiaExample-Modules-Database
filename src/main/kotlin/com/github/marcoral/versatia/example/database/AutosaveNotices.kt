package com.github.marcoral.versatia.example.database

import com.github.marcoral.versatia.VersatiaEngine
import com.github.marcoral.versatia.broadcast
import com.github.marcoral.versatia.database.store.StoreAutosaveEndEvent
import com.github.marcoral.versatia.database.store.StoreAutosaveStartEvent
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

/** The autosave events come on the server thread, only when something changed; every plugin's stores fire them, so the plugin filters its own. */
class AutosaveNotices(private val plugin: MarketPlugin) : Listener {
    @EventHandler
    fun onStart(event: StoreAutosaveStartEvent) {
        if (event.plugin == plugin) VersatiaEngine.broadcast(text("Saving coins...", NamedTextColor.GRAY))
    }

    @EventHandler
    fun onEnd(event: StoreAutosaveEndEvent) {
        if (event.plugin != plugin) return
        val duration = event.duration.inWholeMilliseconds
        VersatiaEngine.broadcast(
            if (event.failed == 0) text("Saved ", NamedTextColor.GRAY).append(text(event.saved, NamedTextColor.AQUA)).append(text(" profiles in ")).append(text("$duration ms", NamedTextColor.AQUA))
            else text("Saving ", NamedTextColor.RED).append(text(event.failed, NamedTextColor.YELLOW)).append(text(" profiles failed; they are retried at the next autosave")),
        )
    }
}
