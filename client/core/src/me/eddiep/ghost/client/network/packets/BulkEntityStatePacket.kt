package me.eddiep.ghost.client.network.packets

import me.eddiep.ghost.client.Ghost
import me.eddiep.ghost.client.network.Packet
import me.eddiep.ghost.client.network.PlayerClient
import me.eddiep.ghost.client.utils.Vector2f

class BulkEntityStatePacket : Packet<PlayerClient>() {

    override fun handle() {
        val packetNumber = consume(4).asInt()

        if (packetNumber < client.lastRead) {
            val dif = client.lastRead - packetNumber
            if (dif >= Int.MAX_VALUE - 1000) {
                client.lastRead = packetNumber
            } else return
        }

        val bulkCount = consume(4).asInt()

        for (i in 0..bulkCount) {
            val id = consume(2).asShort()
            var x = consume(4).asFloat()
            var y = consume(4).asFloat()
            val xVel = consume(4).asFloat()
            val yVel = consume(4).asFloat()
            val alpha = consume(4).asInt()
            val rotation = consume(8).asDouble()
            val serverMs = consume(8).asLong()
            val hasTarget = consume(1).asBoolean()

            if (Ghost.latency > 0) {
                val ticksPassed = Ghost.latency / (1000f / 60f)
                val xadd = xVel * ticksPassed
                val yadd = yVel * ticksPassed

                x += xadd
                y += yadd
            }

            val entity = client.game.findEntity(id) ?: continue

            entity.rotation = rotation.toFloat()

            if (Math.abs(entity.x - x) < 2 && Math.abs(entity.y - y) < 2) {
                entity.x = x + ((Ghost.latency / 60f) * xVel)
                entity.y = y + ((Ghost.latency / 60f) * yVel)
            } else {
                entity.interpolateTo(x, y, (Ghost.UPDATE_INTERVAL / 1.3f).toLong())
            }

            entity.velocity = Vector2f(xVel, yVel)

            if (hasTarget) {
                val xTarget = consume(4).asFloat()
                val yTarget = consume(4).asFloat()

                entity.target = Vector2f(xTarget, yTarget)
            }

            entity.setAlpha(alpha / 255f)
        }
    }
}
