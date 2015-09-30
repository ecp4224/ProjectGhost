package me.eddiep.ghost.client.handlers

import com.badlogic.gdx.Files
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import me.eddiep.ghost.client.Ghost
import me.eddiep.ghost.client.Handler
import me.eddiep.ghost.client.core.Text
import me.eddiep.ghost.client.network.PlayerClient
import me.eddiep.ghost.client.network.packets.SessionPacket

class GameHandler(val IP : String, val Session : String) : Handler {
    lateinit var text : Text
    lateinit var client : PlayerClient

    override fun start() {
        text = Text(24, Gdx.files.getFileHandle("fonts/INFO56_0.ttf", Files.FileType.Internal))

        text.x = 512f
        text.y = 360f
        text.text = "Connecting to server..."
        Ghost.getInstance().addEntity(text)

        Thread(Runnable {

            System.out.println("Connecting..")

            client = PlayerClient.connect(IP)
            val packet : SessionPacket = SessionPacket()
            packet.writePacket(client, Session);
            if (!client.ok()) {
                System.out.println("Bad session!");
                return@Runnable
            }

            client.connectUDP()
            if (!client.ok()) {
                System.out.println("Bad session!");
                return@Runnable
            }

            text.text = "Waiting for match info.."


        }).start()
    }

    override fun tick() {

    }

    fun matchFound(startX: Float, startY: Float) {
        Ghost.getInstance().removeEntity(text)


    }
}
