package nl.jeroenvanwissen.streamFarm.npc.professions

import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Villager
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

object ProfessionConfigLoader {
    fun load(plugin: JavaPlugin): Map<Villager.Profession, Set<Material>> {
        val configFile = File(plugin.dataFolder, "profession-materials.yml")
        if (!configFile.exists()) {
            plugin.saveResource("profession-materials.yml", false)
        }

        val config = YamlConfiguration.loadConfiguration(configFile)
        val result = mutableMapOf<Villager.Profession, Set<Material>>()

        for (key in config.getKeys(false)) {
            val profession = runCatching {
                Villager.Profession.valueOf(key.uppercase())
            }.getOrNull() ?: continue

            val materials = config.getStringList(key)
                .mapNotNull { runCatching { Material.valueOf(it.uppercase()) }.getOrNull() }
                .toSet()

            result[profession] = materials
        }

        plugin.logger.info("Loaded profession-materials for ${result.size} professions")
        return result
    }
}
