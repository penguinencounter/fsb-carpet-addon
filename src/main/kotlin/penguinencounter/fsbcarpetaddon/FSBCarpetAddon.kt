package penguinencounter.fsbcarpetaddon

import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.level.ServerPlayer
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
            val difference = currentPlayerList.filter {
                it.uuid !in lastConnectedSet
            }
            val userManager = figura.userManager()!!
            for (player in difference) {
                val user = userManager.getUser(player.uuid)
                userManager.forEachUser {
                    if (it.uuid() == user.uuid()) return@forEachUser // continue
                    it.sendPacket(S2CConnectedPacket(user.uuid()))
                }
                lastConnectedSet.add(player.uuid)
            }
        }
    }
}
