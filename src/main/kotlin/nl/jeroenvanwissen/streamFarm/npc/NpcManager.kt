package nl.jeroenvanwissen.streamFarm.npc

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.EntityType
import org.bukkit.entity.Villager
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin

class NpcManager (private val plugin: JavaPlugin) {
    private val npcTagKey = NamespacedKey(plugin, "custom_npc")

    fun createCustomNpc(
        location: Location,
        name: String? = null,
        profession: Villager.Profession? = null
    ): Villager {
        val npc = location.world.spawnEntity(location, EntityType.VILLAGER) as Villager

        npc.persistentDataContainer.set(npcTagKey, PersistentDataType.BYTE, 1)

        if (!name.isNullOrEmpty()) {
            val uniqueName = generateUniqueName(name)
            npc.customName = uniqueName
            npc.isCustomNameVisible = true
        }

        if (profession != null) {
            npc.profession = profession
        }

        return npc
    }

    private fun generateUniqueName(baseName: String): String {
        val existingNames = mutableSetOf<String>()

        plugin.server.worlds.forEach { world ->
            world.livingEntities.filterIsInstance<Villager>().forEach { villager ->
                villager.customName()?.let { component ->
                    val name = PlainTextComponentSerializer.plainText().serialize(component)
                    existingNames.add(name)
                }
            }
        }

        if (!existingNames.contains(baseName)) {
            return baseName
        }

        var counter = 1
        var uniqueName: String
        do {
            uniqueName = "${baseName} #${counter}"
            counter++
        } while (existingNames.contains(uniqueName))

        return uniqueName
    }

    fun isCustomNpc(entity: Villager): Boolean {
        return entity.persistentDataContainer.has(npcTagKey, PersistentDataType.BYTE)
    }

    fun getAllCustomNpcs(): List<Villager> {
        return plugin.server.worlds.flatMap { world ->
            world.livingEntities.filterIsInstance<Villager>().filter { isCustomNpc(it) }
        }
    }
}
