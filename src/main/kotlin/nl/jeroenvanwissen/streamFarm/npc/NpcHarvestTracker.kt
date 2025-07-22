package nl.jeroenvanwissen.streamFarm.npc

import org.bukkit.Material
import org.bukkit.entity.Villager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.ConcurrentHashMap

// We'd need to refactor this into NpcMaterialTracker when we make it more generic
// and not only FARMERS

class NpcHarvestTracker(private val plugin: JavaPlugin, private val npcManager: NpcManager): Listener {
    // Map to store harvest by NPC UUID and Crops type
    private val harvestCounts = ConcurrentHashMap<String, MutableMap<Material, Int>>()

    private val cropMaterials = listOf(
        Material.WHEAT,
        Material.CARROTS,
        Material.POTATOES,
        Material.BEETROOTS,
        Material.MELON,
        Material.PUMPKIN
    )

    fun init() {
        plugin.server.pluginManager.registerEvents(this, plugin)
        plugin.logger.info("NPC Harvest Tracker initialized")
    }

    @EventHandler
    fun onEntityChangeBlock(event: EntityChangeBlockEvent) {
        // Check if the entity is a Villager
        if (event.entity !is Villager) return

        val villager = event.entity as Villager

        // Check if the Villager is a custom NPC
        if (!npcManager.isCustomNpc(villager)) {
            return
        }

        //TODO: Do things here...
    }
}
