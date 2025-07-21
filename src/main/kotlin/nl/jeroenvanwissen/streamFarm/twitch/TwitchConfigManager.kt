package nl.jeroenvanwissen.streamFarm.twitch

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class TwitchConfigManager(
    private val plugin: JavaPlugin,
) {
    private lateinit var configFile: File
    private lateinit var configYaml: YamlConfiguration

    data class TwitchConfig(
        val clientId: String?,
        val clientSecret: String?,
        val channelName: String?,
        val accessToken: String?,
        val refreshToken: String?,
    )

    fun init() {
        // Make sure the data folder exists
        if (!plugin.dataFolder.exists()) {
            plugin.dataFolder.mkdirs()
        }

        // Load configuration
        configFile = File(plugin.dataFolder, "twitch.config.yml")
        if (!configFile.exists()) {
            plugin.saveResource("twitch.config.yml", false)
        }
        configYaml = YamlConfiguration.loadConfiguration(configFile)
    }

    fun reloadConfig() {
        configYaml = YamlConfiguration.loadConfiguration(configFile)
        plugin.logger.info("Twitch configuration reloaded")
    }

    fun getConfig(): TwitchConfig =
        TwitchConfig(
            clientId = configYaml.getString("client_id"),
            clientSecret = configYaml.getString("client_secret"),
            channelName = configYaml.getString("channel_name"),
            accessToken = configYaml.getString("access_token"),
            refreshToken = configYaml.getString("refresh_token"),
        )

    fun saveConfig() {
        configYaml.save(configFile)
    }

    fun saveAccessToken(accessToken: String) {
        configYaml.set("access_token", accessToken)
        saveConfig()
        plugin.logger.info("Updated Twitch access token in twitch.config.yml")
    }

    fun hasRequiredConfig(): Boolean {
        val (clientId, clientSecret, channelName, accessToken, refreshToken) = getConfig()
        return !clientId.isNullOrEmpty() &&
            !clientSecret.isNullOrEmpty() &&
            !channelName.isNullOrEmpty()
    }
}
