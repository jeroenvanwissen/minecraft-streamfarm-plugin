package nl.jeroenvanwissen.streamFarm.npc

import nl.jeroenvanwissen.streamFarm.npc.professions.ProfessionConfigLoader
import org.bukkit.Material
import org.bukkit.entity.Villager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.ConcurrentHashMap

class NpcProfessionTracker(
    private val plugin: JavaPlugin,
    private val npcManager: NpcManager,
) : Listener {

    // Tracks counts per NPC UUID and Material
    private val professionHarvestCounts = ConcurrentHashMap<String, MutableMap<Material, Int>>()

    // Loaded from YAML
    private val professionMaterials: Map<Villager.Profession, Set<Material>> =
        ProfessionConfigLoader.load(plugin)

    fun init() {
        plugin.server.pluginManager.registerEvents(this, plugin)
        plugin.logger.info("NPC Profession Tracker initialized")
    }

    @EventHandler
    fun onEntityChangeBlock(event: EntityChangeBlockEvent) {
        val villager = event.entity as? Villager ?: return
        if (!npcManager.isCustomNpc(villager)) return

        val profession = villager.profession
        val trackedMaterials = professionMaterials[profession] ?: return

        val blockType = event.block.type
        if (blockType !in trackedMaterials) return

        val uuid = villager.uniqueId.toString()
        val matMap = professionHarvestCounts.computeIfAbsent(uuid) { mutableMapOf() }
        matMap[blockType] = matMap.getOrDefault(blockType, 0) + 1

        plugin.logger.info("Tracked ${blockType.name} for $profession NPC: $uuid")
    }


    // Optional: add method to expose counts
    fun getHarvestsForNpc(uuid: String): Map<Material, Int> {
        return professionHarvestCounts[uuid] ?: emptyMap()
    }
    
    fun resetHarvestsForNpc(npcId: String) {
        professionHarvestCounts.remove(npcId)
    }

    fun resetAllHarvests() {
        professionHarvestCounts.clear()
    }
}
