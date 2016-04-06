package com.boxtrotstudio.ghost.client.network.packets

import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.network.Packet
import com.boxtrotstudio.ghost.client.network.PlayerClient


class DisconnectReason : Packet<PlayerClient>() {

    override fun handle() {
        val length = consume(1).asByte()
        val reason = consume(length.toInt()).asString()

        Ghost.createInfoDialog("Disconnected", reason, null)
    }
}
