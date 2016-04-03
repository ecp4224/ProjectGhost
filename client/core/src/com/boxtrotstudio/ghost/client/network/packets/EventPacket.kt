package com.boxtrotstudio.ghost.client.network.packets

import com.boxtrotstudio.ghost.client.core.game.events.Event
import com.boxtrotstudio.ghost.client.network.Packet
import com.boxtrotstudio.ghost.client.network.PlayerClient

class EventPacket : Packet<PlayerClient>() {

    override fun handle() {
        val packetNumber = consume(4).asInt()
        if (packetNumber < client.lastRead) {
            val dif = client.lastRead - packetNumber
            if (dif >= Int.MAX_VALUE - 1000) {
                client.lastRead = packetNumber
            } else return
        }

        val eventID = consume(2).asShort()
        val causeID = consume(2).asShort()
        val direction = consume(8).asDouble()

        val cause = client.game.findEntity(causeID) ?: return

        for (event in Event.EVENTS) {
            if (event.id == eventID) {
                event.trigger(cause, direction, client.game.world)
                break
            }
        }
    }
}