package penguinencounter.fsbcarpetaddon

import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import org.figuramc.figura.server.FiguraServer
import org.figuramc.figura.server.packets.s2c.S2CConnectedPacket
import java.util.*

class FSBCarpetAddon : DedicatedServerModInitializer {
    private val lastConnectedSet: MutableSet<UUID> = mutableSetOf()

    override fun onInitializeServer() {
        ServerTickEvents.END_SERVER_TICK.register { server ->
            if (!FiguraServer.initialized()) return@register
            val figura = FiguraServer.getInstance()!!
            val currentPlayerList = server.playerList!!.players
            val uuids = mutableListOf<UUID>()
            val added = currentPlayerList.filter {
                uuids.add(it.uuid)
                it.uuid !in lastConnectedSet
            }
            val userManager = figura.userManager()!!
            for (player in added) {
                try {
                    val user = userManager.setupOnlinePlayer(player.uuid)
                    currentPlayerList.forEach {
                        if (it.uuid == user.uuid()) return@forEach // continue
                        figura.sendPacket(it.uuid, S2CConnectedPacket(user.uuid()))
                    }
                    lastConnectedSet.add(player.uuid)
                } catch (_: ConcurrentModificationException) {
                    // It can happen. No, I don't know why.
                }
            }
            // Remove disconnected players (we'll have to re-send them again later)
            lastConnectedSet.retainAll {
                it in uuids
            }
        }
    }
}
