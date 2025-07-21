package nl.jeroenvanwissen.streamFarm.twitch.commands

import com.github.twitch4j.TwitchClient

object CommandRegistry {
    private lateinit var twitchClient: TwitchClient

    fun initialize(client: TwitchClient) {
        twitchClient = client
    }

    private val commands: List<Command<*>> by lazy {
        listOf(
            SfCommand(twitchClient),
            // Add more commands here
        )
    }

    fun getCommand(name: String): Command<*>? = commands.find { it.name.equals(name, ignoreCase = true) }

    fun getAllCommands(): List<Command<*>> = commands
}
