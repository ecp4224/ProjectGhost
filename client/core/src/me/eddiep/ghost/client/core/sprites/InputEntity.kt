package me.eddiep.ghost.client.core.sprites

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import me.eddiep.ghost.client.Ghost
import me.eddiep.ghost.client.core.Logical
import me.eddiep.ghost.client.network.packets.ActionRequestPacket
import me.eddiep.ghost.client.network.packets.ItemUsePacket
import me.eddiep.ghost.client.utils.Vector2f

class InputEntity(id: Short) : NetworkPlayer(id, "") {
    var fireRateStat: Double = 0.0
    var speedStat: Double = 0.0

    var inventory = Inventory();

    private var leftWasPressed: Boolean = false;
    private var rightWasPressed: Boolean = false;
    private var oneWasPressed: Boolean = false;
    private var twoWasPressed: Boolean = false;
    private val packet : ActionRequestPacket = ActionRequestPacket()
    private val itemPacket : ItemUsePacket = ItemUsePacket()

    override fun onLoad() {
        super.onLoad()

        color = Color(0f, 81 / 255f, 197 / 255f, 1f)

        inventory.setCenter(900f, 100f)
        Ghost.getInstance().addEntity(inventory)
    }

    override fun tick() {
        super.tick()

        checkMouse()
        checkKeyboard()
    }

    private fun checkKeyboard() {
        val oneKeyPressed = Gdx.input.isKeyPressed(Input.Keys.NUM_1)
        val twoKeyPressed = Gdx.input.isKeyPressed(Input.Keys.NUM_2)

        if (oneKeyPressed && !oneWasPressed) {
            Thread(Runnable {
                itemPacket.writePacket(Ghost.client, 0.toByte())
            }).start()

            oneWasPressed = true;
        } else if (!oneKeyPressed && oneWasPressed)
            oneWasPressed = false;

        if (twoKeyPressed && !twoWasPressed) {
            itemPacket.writePacket(Ghost.client, 1.toByte())

            Thread(Runnable {
                itemPacket.writePacket(Ghost.client, 1.toByte())
            }).start()

            twoWasPressed = true;
        } else if (!twoKeyPressed && twoWasPressed)
            twoWasPressed = false;
    }

    private fun checkMouse() {
        val leftPressed = Gdx.input.isButtonPressed(Input.Buttons.LEFT)
        val rightPressed = Gdx.input.isButtonPressed(Input.Buttons.RIGHT)

        if (leftPressed && !leftWasPressed) {
            leftWasPressed = true;

            var mousePos = Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
            Ghost.getInstance().camera.unproject(mousePos)

            if (Ghost.matchStarted) {

                Thread(Runnable { //Maybe buffer this?
                    packet.writePacket(Ghost.client, 0.toByte(), mousePos.x, mousePos.y)
                }).start()
            }

            val feedback = FeedbackCircle()
            feedback.setCenter(mousePos.x, mousePos.y)

            Ghost.getInstance().addEntity(feedback)
        } else if (!leftPressed && leftWasPressed) leftWasPressed = false

        if (rightPressed && !rightWasPressed) {
            rightWasPressed = true;

            if (Ghost.matchStarted) {
                var mousePos = Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
                Ghost.getInstance().camera.unproject(mousePos)

                Thread(Runnable {
                    packet.writePacket(Ghost.client, 1.toByte(), mousePos.x, mousePos.y)
                }).start()
            }
        } else if (!rightPressed && rightWasPressed) rightWasPressed = false
    }
}