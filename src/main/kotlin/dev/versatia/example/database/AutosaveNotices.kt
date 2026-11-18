package dev.versatia.example.database

import dev.versatia.VersatiaEngine
import dev.versatia.broadcast
import dev.versatia.database.store.StoreAutosaveEndEvent
import dev.versatia.database.store.StoreAutosaveStartEvent
import dev.versatia.text.arg
import dev.versatia.text.text
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

/** The autosave events come on the server thread, only when something changed; every plugin's stores fire them, so the plugin filters its own. */
class AutosaveNotices(private val plugin: MarketPlugin) : Listener {
    @EventHandler
    fun onStart(event: StoreAutosaveStartEvent) {
        if (event.plugin == plugin) VersatiaEngine.broadcast(plugin.text.parse("<neutral>Saving coins..."))
    }

    @EventHandler
    fun onEnd(event: StoreAutosaveEndEvent) {
        if (event.plugin != plugin) return
        val duration = event.duration.inWholeMilliseconds
        VersatiaEngine.broadcast(
            if (event.failed == 0) plugin.text.parse("<neutral>Saved {saved} profiles in {duration} ms", arg("saved", event.saved), arg("duration", duration))
            else plugin.text.parse("<negative>Saving {failed} profiles failed; they are retried at the next autosave", arg("failed", event.failed)),
        )
    }
}
