package com.boxtrotstudio.ghost.client.network.packets

import com.badlogic.gdx.graphics.Color
import com.boxtrotstudio.ghost.client.Ghost
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

        Ghost.rayHandler.ambientLight = Color((red / 255f) * power, (green / 255f) * power, (blue / 255f) * power, 1f)

        client.game.prepareMap(mapName)
    }
}
