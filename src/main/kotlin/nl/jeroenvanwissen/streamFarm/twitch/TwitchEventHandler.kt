package nl.jeroenvanwissen.streamFarm.twitch

import com.github.twitch4j.TwitchClient
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import com.github.twitch4j.eventsub.events.ChannelCheerEvent
import com.github.twitch4j.eventsub.events.ChannelFollowEvent
import com.github.twitch4j.eventsub.events.ChannelPointsCustomRewardRedemptionEvent
import com.github.twitch4j.eventsub.events.ChannelRaidEvent
import com.github.twitch4j.eventsub.events.ChannelSubscribeEvent
import com.github.twitch4j.eventsub.events.ChannelSubscriptionGiftEvent
import com.github.twitch4j.eventsub.events.ChannelSubscriptionMessageEvent
import org.bukkit.plugin.java.JavaPlugin

class TwitchEventHandler(
    private val plugin: JavaPlugin,
    private val twitchClient: TwitchClient,
    private val twitchConfigManager: TwitchConfigManager,
    private val twitchClientManager: TwitchClientManager? = null,
) {
    fun registerEventHandlers() {
        val channelName = twitchConfigManager.getConfig().channelName ?: return

        // Initialize CommandRegistry with twitchClient
        nl.jeroenvanwissen.streamFarm.twitch.commands.CommandRegistry
            .initialize(twitchClient)

        // Register Channel Message Event
        twitchClient.eventManager.onEvent(
            ChannelMessageEvent::class.java,
            this::handleChannelMessageEvent,
        )

        // Register Channel Point Custom Reward Redemption Event
        twitchClient.eventManager.onEvent(
            ChannelPointsCustomRewardRedemptionEvent::class.java,
            this::handleChannelPointsCustomRewardsRedemptionEvent,
        )

        // Register Channel Cheer Event (Bits)
        twitchClient.eventManager.onEvent(
            ChannelCheerEvent::class.java,
            this::handleChannelCheerEvent,
        )

        // Register Channel Raid Event
        twitchClient.eventManager.onEvent(
            ChannelRaidEvent::class.java,
            this::handleChannelRaidEvent,
        )

        // Register Channel Follow Event
        twitchClient.eventManager.onEvent(
            ChannelFollowEvent::class.java,
            this::handleChannelFollowEvent,
        )

        // Register Channel Subscribe Event
        twitchClient.eventManager.onEvent(
            ChannelSubscribeEvent::class.java,
            this::handleChannelSubscribeEvent,
        )

        twitchClient.eventManager.onEvent(
            ChannelSubscriptionGiftEvent::class.java,
            this::handleChannelSubscribeEvent,
        )

        twitchClient.eventManager.onEvent(
            ChannelSubscriptionMessageEvent::class.java,
            this::handleChannelSubscribeEvent,
        )

        plugin.logger.info("Registered all Twitch event handlers")
    }

    private fun handleChannelMessageEvent(event: ChannelMessageEvent) {
        val message = event.message
        val userName = event.user.name

        // Check if message starts with '!' and extract command
        if (message.startsWith("!")) {
            val parts = message.substring(1).split(" ")
            val commandName = parts[0]
            val command =
                nl.jeroenvanwissen.streamFarm.twitch.commands.CommandRegistry
                    .getCommand(commandName)
            if (command != null) {
                command.init()
                plugin.server.scheduler.runTaskAsynchronously(
                    plugin,
                    Runnable {
                        command.callback(
                            event.channel.name,
                            event.channel.id,
                            commandName,
                            parts.drop(1),
                            event,
                        )
                    },
                )
            }
        }
    }

    private fun handleChannelPointsCustomRewardsRedemptionEvent(event: ChannelPointsCustomRewardRedemptionEvent) {
    }

    private fun handleChannelRaidEvent(event: ChannelRaidEvent) {
    }

    private fun handleChannelCheerEvent(event: ChannelCheerEvent) {
    }

    private fun handleChannelFollowEvent(event: ChannelFollowEvent) {
    }

    private fun handleChannelSubscribeEvent(event: ChannelSubscribeEvent) {
    }

    private fun handleChannelSubscribeEvent(event: ChannelSubscriptionGiftEvent) {
    }

    private fun handleChannelSubscribeEvent(event: ChannelSubscriptionMessageEvent) {
    }
}
