package nl.jeroenvanwissen.streamFarm.twitch.commands

import com.github.twitch4j.TwitchClient
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import org.bukkit.plugin.java.JavaPlugin

class SfCommand(
    private val plugin: JavaPlugin,
    override val twitchClient: TwitchClient,
) : Command<Map<String, Any>> {
    override val name = "sf"
    override val permission = Permission.EVERYONE
    override val type = CommandType.COMMAND
    override var storage: Map<String, Any> = emptyMap()

    override fun init() {
        // Initialization logic here
    }

    override fun callback(
        channel: String,
        broadcasterId: String,
        commandName: String,
        params: List<String>,
        message: ChannelMessageEvent,
    ) {
        var respondChatMessage: String? = null
        val userName = message.user.name

        when (params.first()) {
            "help" -> {
                respondChatMessage = "@${userName} Help is on the way..."
            }
            "link" -> {

            }
        }

        if (!respondChatMessage.isNullOrEmpty()) {
            twitchClient.chat.sendMessage(
                channel,
                respondChatMessage,
            )
        }
    }
}
