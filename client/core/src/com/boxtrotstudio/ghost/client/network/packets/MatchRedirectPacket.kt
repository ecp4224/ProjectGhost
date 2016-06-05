package com.boxtrotstudio.ghost.client.network.packets

import com.badlogic.gdx.Gdx
import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.handlers.GameHandler
import com.boxtrotstudio.ghost.client.network.Packet
import com.boxtrotstudio.ghost.client.network.PlayerClient
import java.io.IOException

class MatchRedirectPacket : Packet<PlayerClient>() {
    override fun handle() {
        val ip = consume(consume(1).asByte().toInt()).asString()
        val port = consume(2).asShort()

        Ghost.client = PlayerClient.connect(ip + ":" + port)
        val packet = SessionPacket()

        packet.writePacket(Ghost.client, Ghost.Session);
        if (!Ghost.client.ok()) {
            throw IOException("Bad session!");
        }
        Ghost.client.isValidated = true

        Gdx.app.postRunnable {
            Ghost.getInstance().clearScreen()
            val game = GameHandler(ip, Ghost.Session)
            game.start()
            Ghost.getInstance().handler = game
        }
    }
}
