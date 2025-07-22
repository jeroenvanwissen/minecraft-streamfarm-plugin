package nl.jeroenvanwissen.streamFarm.twitch.commands

import com.github.twitch4j.TwitchClient
import org.bukkit.plugin.java.JavaPlugin

object CommandRegistry {
    private lateinit var twitchClient: TwitchClient
    private lateinit var javaPlugin: JavaPlugin

    fun initialize(plugin: JavaPlugin, client: TwitchClient) {
        twitchClient = client
        javaPlugin = plugin
    }

    private val commands: List<Command<*>> by lazy {
        listOf(
            SfCommand(javaPlugin, twitchClient),
            // Add more commands here
        )
    }

    fun getCommand(name: String): Command<*>? = commands.find { it.name.equals(name, ignoreCase = true) }

    fun getAllCommands(): List<Command<*>> = commands
}
