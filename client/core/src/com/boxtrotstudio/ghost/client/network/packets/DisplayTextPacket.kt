package com.boxtrotstudio.ghost.client.network.packets

import com.badlogic.gdx.graphics.Color
import com.boxtrotstudio.ghost.client.network.Packet
import com.boxtrotstudio.ghost.client.network.PlayerClient

class DisplayTextPacket : Packet<PlayerClient>() {
    override fun handle() {
        val textLength = consume(4).asInt()
        val text = consume(textLength).asString()

        val size = consume(4).asInt()
        val colorInt = consume(4).asInt()
        val x = consume(4).asFloat()
        val y = consume(4).asFloat()
        val options = consume(4).asInt()
        val id = consume(8).asLong()

        val color = Color(colorInt)
        client.game.displayText(text, size, color, x, y, options, id)
    }
}