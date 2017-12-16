package com.boxtrotstudio.ghost.client.network.packets

import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.network.Packet
import com.boxtrotstudio.ghost.client.network.PlayerClient
import com.boxtrotstudio.ghost.client.utils.Vector2f

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
        //System.out.println("Update " + bulkCount + " entities @ " + System.currentTimeMillis())
        for (i in 0 until bulkCount) {
            val id = consume(2).asShort()
            var x = consume(4).asFloat()
            var y = consume(4).asFloat()
            val xVel = consume(4).asFloat()
            val yVel = consume(4).asFloat()
            val alpha = consume(4).asInt()
            val rotation = consume(8).asDouble()
            val serverMs = consume(8).asLong()
            val hasTarget = consume(1).asBoolean()

            val entity = client.game.findEntity(id) ?: continue

            //translate x and y to libgdx's fucked up joke of a coordinate system
            x -= entity.width / 2f
            y -= entity.height / 2f

            /*if (Ghost.latency > 0) {
                val ticksPassed = Ghost.latency / (1000f / 60f)
                if (ticksPassed >= 2) {
                    val xadd = xVel * ticksPassed
                    val yadd = yVel * ticksPassed

                    System.out.println("" + ticksPassed + " ticks skipped since sent. " + xadd + ":" + yadd + " being added to movement");

                    x += xadd
                    y += yadd
                }
            }*/

            var xTarget = 0f
            var yTarget = 0f
            if (hasTarget) {
                xTarget = consume(4).asFloat()
                yTarget = consume(4).asFloat()
            }

            /*var shouldUpdate = !entity.isMoving
            when {
                hasTarget && entity.target != null -> {
                    if (xTarget != entity.target.x || yTarget != entity.target.y) shouldUpdate = true
                    else entity.isMoving = false
                }
                hasTarget && entity.target == null -> {
                    shouldUpdate = true
                }
            }*/
            entity.rotation = Math.toDegrees(rotation).toFloat()

            if (Math.abs(entity.x - x) < 2 && Math.abs(entity.y - y) < 2) {
                entity.x = x + ((Ghost.latency / 60f) * xVel)
                entity.y = y + ((Ghost.latency / 60f) * yVel)
            } else {
                entity.interpolateTo(x, y, Ghost.UPDATE_INTERVAL)
            }

            entity.velocity = Vector2f(xVel, yVel)

            if (hasTarget) {
                xTarget -= entity.width / 2f
                yTarget -= entity.height / 2f

                entity.target = Vector2f(xTarget, yTarget)

                //Ghost.endPingTimer(entity.target)
            }

            entity.alpha = alpha / 255f
        }
    }
}
