package me.eddiep.ghost.client.network.packets

import me.eddiep.ghost.client.network.Packet
import me.eddiep.ghost.client.network.PlayerClient

class ChangeWeaponPacket : Packet<PlayerClient>() {

    override fun write(vararg args : Any) {
        val weapon = args[0] as Byte;

        write(0x22.toByte())
        write(weapon)
        endTCP()
    }
}
