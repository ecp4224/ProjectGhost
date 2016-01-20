package me.eddiep.ghost.client.network.packets

import box2dLight.PointLight
import com.badlogic.gdx.graphics.Color
import me.eddiep.ghost.client.Ghost
import me.eddiep.ghost.client.network.Packet
import me.eddiep.ghost.client.network.PlayerClient

class SpawnLightPacket : Packet<PlayerClient>(){
    override fun handle(){
        var id : Short = consume(2).asShort()

        var x : Float = consume(4).asFloat()
        var y : Float = consume(4).asFloat()
        var radius : Float = consume(4).asFloat()
        var intensity : Float = consume(4).asFloat()

        var color : Int = consume(4).asInt()

        PointLight(Ghost.getInstance().rayHandler, 128, Color(color), radius, x, y)
    }
}

