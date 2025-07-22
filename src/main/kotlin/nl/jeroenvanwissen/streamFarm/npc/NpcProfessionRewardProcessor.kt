package nl.jeroenvanwissen.streamFarm.npc

import me.gypopo.economyshopgui.api.EconomyShopGUIHook
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

class NpcProfessionRewardProcessor(
    private val plugin: JavaPlugin,
    private val npcManager: NpcManager,
    private val professionTracker: NpcProfessionTracker,
) {
    private var rewardTask: BukkitTask? = null
    private var intervalInMinutes: Long = 5

    // The processor needs to start / stop processing on an interval
    // Load configuration from yml where we define if we want autoprocessing, on how many ticks the selling of goods
    // must happen etc..

    fun start() {
        // check if we want autoprocessing, else return

        stop()

        val intervalTicks = intervalInMinutes * 60 * 20

        rewardTask = object : BukkitRunnable() {
            override fun run() {
                val npcList = npcManager.getAllCustomNpcs()

                // For every custom NPC
                for (npc in npcList) {
                    val npcId = npc.uniqueId.toString()
                    val harvested = professionTracker.getHarvestsForNpc(npcId)
                    if (harvested.isEmpty()) continue

                    var totalReward = 0.0

                    for ((material, amount) in harvested) {
                        val pricePerUnit = getSellPrice(material) ?: continue
                        totalReward += pricePerUnit * amount
                    }

                    if (totalReward <= 0.0) continue

                    val ownerUuid = npcManager.getOwnerOfNpc(npc) ?: continue

                    // Pay on main thread
                    Bukkit.getScheduler().runTask(
                        plugin,
                        Runnable {
                            val econ = Bukkit.getServer().servicesManager
                                .getRegistration(net.milkbowl.vault.economy.Economy::class.java)?.provider

                            if (econ == null) {
                                plugin.logger.warning("Vault economy provider not found!")
                                return@Runnable
                            }

                            val result = econ.depositPlayer(Bukkit.getOfflinePlayer(ownerUuid), totalReward)
                            if (result.transactionSuccess()) {
                                plugin.logger.info("Paid $totalReward to $ownerUuid from NPC $npcId")
                                professionTracker.resetHarvestsForNpc(npcId)
                            } else {
                                plugin.logger.warning("Failed to pay $ownerUuid for NPC $npcId")
                            }
                        },
                    )
                }
            }

        }.runTaskLaterAsynchronously(plugin, intervalTicks.toLong())
    }

    fun stop() {
        rewardTask?.cancel()
        rewardTask = null
    }

    fun getSellPrice(material: Material): Double? {
        val itemStack = ItemStack(material)
        val shopItem = EconomyShopGUIHook.getShopItem(itemStack) ?: return null
        return EconomyShopGUIHook.getItemSellPrice(shopItem, itemStack)
    }

}
