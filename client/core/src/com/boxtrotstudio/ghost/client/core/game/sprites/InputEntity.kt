package com.boxtrotstudio.ghost.client.core.game.sprites

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.network.packets.ActionRequestPacket
import com.boxtrotstudio.ghost.client.network.packets.ItemUsePacket
import com.boxtrotstudio.ghost.client.utils.ButtonChecker
import com.boxtrotstudio.ghost.client.utils.Vector2f

class InputEntity(id: Short) : NetworkPlayer(id, "") {
    var fireRateStat: Double = 0.0
    var speedStat: Double = 0.0

    var inventory = Inventory();

    private var leftWasPressed: Boolean = false;
    private var rightWasPressed: Boolean = false;
    private val packet : ActionRequestPacket = ActionRequestPacket()
    private val itemPacket : ItemUsePacket = ItemUsePacket()
    private var lastDirection = Vector2f(0f, 0f)

    override fun onLoad() {
        super.onLoad()

        color = Color(0f, 81 / 255f, 197 / 255f, 1f)

        inventory.setCenter(900f, 100f)
        parentScene.addEntity(inventory)
    }

    override fun tick() {
        super.tick()

        checkMouse()
        checkKeyboard()
    }

    private fun checkKeyboard() {
        ButtonChecker.checkKey(Input.Keys.NUM_1, Runnable {
            Thread(Runnable {
                itemPacket.writePacket(Ghost.client, 0.toByte())
            }).start()
        }, null)

        ButtonChecker.checkKey(Input.Keys.NUM_2, Runnable {
            Thread(Runnable {
                itemPacket.writePacket(Ghost.client, 1.toByte())
            }).start()
        }, null)

        val w = Gdx.input.isKeyPressed(Input.Keys.W)
        val a = Gdx.input.isKeyPressed(Input.Keys.A)
        val s = Gdx.input.isKeyPressed(Input.Keys.S)
        val d = Gdx.input.isKeyPressed(Input.Keys.D)

        val direction = Vector2f(0f, 0f)
        if (w)
            direction.y += 1f
        if (a)
            direction.x -= 1f
        if (s)
            direction.y -= 1f
        if (d)
            direction.x += 1f

        if (!(direction.x == lastDirection.x && direction.y == lastDirection.y)) {
            Thread(Runnable {
                Ghost.startPingTimer(target);
                packet.writePacket(Ghost.client, 2.toByte(), direction.x, direction.y)
            }).start()
            lastDirection = direction
        }
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
                    Ghost.startPingTimer(target);
                    packet.writePacket(Ghost.client, 0.toByte(), mousePos.x, mousePos.y)
                }).start()
            }

            val feedback = FeedbackCircle()
            feedback.setCenter(mousePos.x, mousePos.y)

            parentScene.addEntity(feedback)
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