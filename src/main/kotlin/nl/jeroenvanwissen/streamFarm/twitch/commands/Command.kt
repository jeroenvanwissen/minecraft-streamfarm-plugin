package nl.jeroenvanwissen.streamFarm.twitch.commands

import com.github.twitch4j.TwitchClient
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent

enum class Permission {
    BROADCASTER,
    MODERATOR,
    VIP,
    SUBSCRIBER,
    EVERYONE,
}

enum class CommandType {
    COMMAND,
    EVENT,
    MESSAGE,
}

interface Command<T : Any> {
    val name: String
    val permission: Permission
    val type: CommandType
    var storage: T
    val twitchClient: TwitchClient

    fun init()

    fun callback(
        channel: String,
        broadcasterId: String,
        commandName: String,
        params: List<String>,
        message: ChannelMessageEvent,
    )
}
