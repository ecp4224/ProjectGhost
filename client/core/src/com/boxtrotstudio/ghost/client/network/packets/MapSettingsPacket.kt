package com.boxtrotstudio.ghost.client.network.packets

import com.badlogic.gdx.graphics.Color
import com.boxtrotstudio.ghost.client.network.Packet
import com.boxtrotstudio.ghost.client.network.PlayerClient

class MapSettingsPacket : Packet<PlayerClient>() {

    override fun handle() {
        val power = consume(4).asFloat()

        val red = consume(4).asInt()
        val green = consume(4).asInt()
        val blue = consume(4).asInt()

        val mapNameLength = consume(4).asInt()
        val mapName = consume(mapNameLength).asString()

        val color = Color(red / 255f, green / 255f, blue / 255f, 1f)
        client.game.ambiantPower = power
        client.game.ambiantColor = color

        client.game.prepareMap(mapName)
    }
}
