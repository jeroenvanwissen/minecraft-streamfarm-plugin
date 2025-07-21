package nl.jeroenvanwissen.streamFarm.twitch

import com.github.philippheuer.credentialmanager.CredentialManager
import com.github.philippheuer.credentialmanager.CredentialManagerBuilder
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential
import com.github.twitch4j.TwitchClient
import com.github.twitch4j.TwitchClientBuilder
import com.github.twitch4j.auth.providers.TwitchIdentityProvider
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask

class TwitchClientManager(
    private val plugin: JavaPlugin,
    private val twitchConfigManager: TwitchConfigManager,
) {
    private lateinit var twitchClient: TwitchClient
    private lateinit var credentialManager: CredentialManager
    private lateinit var identityProvider: TwitchIdentityProvider
    private var tokenMonitorTask: BukkitTask? = null

    fun init() {
        if (twitchConfigManager.hasRequiredConfig()) {
            connect()
            tokenMonitoring()
        } else {
            plugin.logger.severe(
                "Missing Twitch configuration! Please configure your Twitch credentials in twitch.config.yml",
            )
        }
    }

    private fun connect(): Boolean {
        val (clientId, clientSecret, channelName, accessToken, refreshToken) =
            twitchConfigManager.getConfig()

        // Initialize credential manager
        credentialManager = CredentialManagerBuilder.builder().build()

        // Setup identity provider for auto token refresh
        identityProvider = TwitchIdentityProvider(clientId, clientSecret, "")
        credentialManager.registerIdentityProvider(identityProvider)

        // Create OAuth credential and validate/refresh if needed
        var credential =
            if (!accessToken.isNullOrEmpty()) {
                OAuth2Credential("twitch", accessToken)
            } else {
                OAuth2Credential("twitch", "")
            }

        if (!refreshToken.isNullOrEmpty() &&
            (accessToken.isNullOrEmpty() || !isCredentialValid(credential.accessToken))
        ) {
            try {
                plugin.logger.info("Generating new access token using refresh token...")
                credential.refreshToken = refreshToken

                var refreshResult = identityProvider.refreshCredential(credential)
                if (refreshResult.isPresent) {
                    val refreshedCredential = refreshResult.get()
                    credential = refreshedCredential
                    credential.refreshToken = refreshToken

                    twitchConfigManager.saveAccessToken(refreshedCredential.accessToken)
                    plugin.logger.info("Successfully generated new access token from refresh token")
                } else {
                    plugin.logger.severe(
                        "Failed to generate access token: No credential returned from refresh",
                    )
                    return false
                }
            } catch (e: Exception) {
                plugin.logger.severe("Failed to generate access token: ${e.message}")
                return false
            }
        } else if (accessToken.isNullOrEmpty()) {
            plugin.logger.warning(
                "No access token found and no refresh token available. Please authenticate with Twitch.",
            )
            return false
        }

        // Set up the TwitchClient
        twitchClient =
            TwitchClientBuilder
                .builder()
                .withClientId(clientId)
                .withClientSecret(clientSecret)
                .withCredentialManager(credentialManager)
                .withDefaultAuthToken(credential)
                .withChatAccount(credential)
                .withEnableChat(true)
                .withEnableHelix(true)
                .withEnableEventSocket(true)
                .build()

        // Join the channel's chat
        twitchClient.chat.joinChannel(channelName)

        plugin.logger.info("Successfully connected to Twitch!")
        return true
    }

    private fun tokenMonitoring() {
        // Cancel any existing task
        tokenMonitorTask?.cancel()

        // Start new task that runs every 30 minutes (36000 ticks)
        tokenMonitorTask =
            Bukkit
                .getScheduler()
                .runTaskTimerAsynchronously(
                    plugin,
                    Runnable {
                        try {
                            if (!isCredentialValid(
                                    twitchConfigManager.getConfig().accessToken,
                                )
                            ) {
                                connect()
                            }
                        } catch (e: Exception) {
                            plugin.logger.severe(
                                "Failed to refresh twitch access token and reconnect: ${e.message}",
                            )
                        }
                    },
                    36000L,
                    36000L,
                )
    }

    private fun isCredentialValid(accessToken: String?): Boolean {
        if (accessToken.isNullOrEmpty() || accessToken.length <= 10) {
            return false
        }

        val (clientId, clientSecret, channelName) = twitchConfigManager.getConfig()

        try {
            val tempTwitchClient =
                TwitchClientBuilder
                    .builder()
                    .withClientId(clientId)
                    .withClientSecret(clientSecret)
                    .withEnableHelix(true)
                    .withDefaultAuthToken(OAuth2Credential("twitch", accessToken))
                    .build()

            val result = tempTwitchClient.helix.getUsers(null, null, listOf(channelName)).execute()
            tempTwitchClient.close()

            return result.users.isNotEmpty()
        } catch (e: Exception) {
            plugin.logger.warning("Access token validation failed: ${e.message}")
            return false
        }
    }

    fun close() {
        tokenMonitorTask?.cancel()
        tokenMonitorTask = null

        if (::twitchClient.isInitialized) {
            twitchClient.close()
        }
    }

    fun getTwitchClient(): TwitchClient = twitchClient

    fun getChannelId(): String? {
        val channelName = twitchConfigManager.getConfig().channelName
        try {
            plugin.logger.info("Getting channel ID for: $channelName")
            val userList = twitchClient.helix.getUsers(null, null, listOf(channelName)).execute()
            if (userList.users.isNotEmpty()) {
                val channelId = userList.users[0].id
                plugin.logger.info("Found channel ID: $channelId for: $channelName")
                return channelId
            } else {
                return null
            }
        } catch (e: Exception) {
            plugin.logger.severe("Error getting channel ID for: $channelName - ${e.message}")
            return null
        }
    }
}
