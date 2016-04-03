package com.boxtrotstudio.ghost.client.network.packets

import com.boxtrotstudio.ghost.client.network.Packet
import com.boxtrotstudio.ghost.client.network.PlayerClient

class ChangeWeaponPacket : Packet<PlayerClient>() {

    override fun write(vararg args : Any) {
        val weapon = args[0] as Byte;

        write(0x22.toByte())
        write(weapon)
        endTCP()
    }
}
