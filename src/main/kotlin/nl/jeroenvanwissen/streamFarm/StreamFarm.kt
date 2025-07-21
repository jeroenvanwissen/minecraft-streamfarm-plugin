package nl.jeroenvanwissen.streamFarm

import nl.jeroenvanwissen.streamFarm.twitch.TwitchClientManager
import nl.jeroenvanwissen.streamFarm.twitch.TwitchConfigManager
import nl.jeroenvanwissen.streamFarm.twitch.TwitchEventHandler
import org.bukkit.plugin.java.JavaPlugin

class StreamFarm : JavaPlugin() {
    private lateinit var twitchConfigManager: TwitchConfigManager
    private lateinit var twitchClientManager: TwitchClientManager
    private lateinit var twitchEventHandler: TwitchEventHandler

    override fun onEnable() {
        initializeComponents()
    }

    override fun onDisable() {
        if (::twitchClientManager.isInitialized) {
            twitchClientManager.close()
        }
    }

    private fun initializeComponents() {
        twitchConfigManager = TwitchConfigManager(this)
        twitchConfigManager.init()

        twitchClientManager = TwitchClientManager(this, twitchConfigManager)
        twitchClientManager.init()

        twitchEventHandler =
            TwitchEventHandler(this, twitchClientManager.getTwitchClient(), twitchConfigManager, twitchClientManager)
        twitchEventHandler.registerEventHandlers()
    }
}
