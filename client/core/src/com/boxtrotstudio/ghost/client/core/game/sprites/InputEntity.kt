package com.boxtrotstudio.ghost.client.core.game.sprites

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.Vector3
import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.network.packets.ActionRequestPacket
import com.boxtrotstudio.ghost.client.network.packets.ItemUsePacket
import com.boxtrotstudio.ghost.client.utils.ButtonChecker
import com.boxtrotstudio.ghost.client.utils.Direction
import com.boxtrotstudio.ghost.client.utils.Vector2f

class InputEntity(id: Short, texture: String) : NetworkPlayer(id, texture) {
    var fireRateStat: Double = 0.0
    var speedStat: Double = 0.0

    var inventory = Inventory();

    private var leftWasPressed: Boolean = false;
    private var rightWasPressed: Boolean = false;
    private val packet : ActionRequestPacket = ActionRequestPacket()
    private val itemPacket : ItemUsePacket = ItemUsePacket()
    private var clickedDirection : Direction = Direction.NONE

    override fun onLoad() {
        super.onLoad()

        val temp = 1024f - 900f
        inventory.setCenter(1280f - temp, 100f)
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
        val esc = Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)

        if (!Ghost.client.game.isPaused) {
            var direction = Direction.NONE
            if (w)
                direction = direction.add(Direction.UP)
            if (a)
                direction = direction.add(Direction.LEFT)
            if (s)
                direction = direction.add(Direction.DOWN)
            if (d)
                direction = direction.add(Direction.RIGHT)

            if (direction != Direction.NONE) {
                Thread(Runnable {
                    Ghost.startPingTimer(target);
                    val vector = direction.toVector()
                    packet.writePacket(Ghost.client, 2.toByte(), vector.x, vector.y)
                }).start()
                lastDirection = direction
                clickedDirection = Direction.NONE
            }

            if (lastDirection != Direction.NONE && direction == Direction.NONE && clickedDirection == Direction.NONE) {
                Thread(Runnable {
                    val vector = direction.toVector()
                    packet.writePacket(Ghost.client, 2.toByte(), vector.x, vector.y)
                }).start()
                lastDirection = direction
            }
        }

        if (esc) {
            Ghost.client.game.togglePause()
        }
    }

    private fun checkMouse() {
        if (Ghost.client != null && Ghost.client.game != null && Ghost.client.game.isPaused)
            return

        val leftPressed = Gdx.input.isButtonPressed(Input.Buttons.LEFT)
        val rightPressed = Gdx.input.isButtonPressed(Input.Buttons.RIGHT)

        if (leftPressed && !leftWasPressed) {
            leftWasPressed = true;

            var mousePos = Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
            Ghost.getInstance().camera.unproject(mousePos)

            moveTowards(Vector2f(mousePos.x, mousePos.y))
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

    private fun moveTowards(target: Vector2f) {
        val x = position.x
        val y = position.y

        val asdx = target.x - x
        val asdy = target.y - y
        val inv = Math.atan2(asdy.toDouble(), asdx.toDouble()).toFloat()

        velocity.x = (Math.cos(inv.toDouble()) * speedStat).toFloat()
        velocity.y = (Math.sin(inv.toDouble()) * speedStat).toFloat()

        this.target = Vector2f(target.x, target.y)

        clickedDirection = Direction.fromVector(velocity)

        isMoving = true
    }
}