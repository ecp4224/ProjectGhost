package com.boxtrotstudio.ghost.client.core.game.sprites

import box2dLight.p3d.P3dData
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.CircleShape
import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.core.game.SpriteEntity
import com.boxtrotstudio.ghost.client.core.game.animations.AnimationType.*
import com.boxtrotstudio.ghost.client.core.game.sprites.effects.OrbitEffect
import com.boxtrotstudio.ghost.client.utils.Constants
import com.boxtrotstudio.ghost.client.utils.Direction
import java.util.*
import kotlin.properties.Delegates

open class NetworkPlayer(id: Short, val spritePath: String) : SpriteEntity(spritePath, id) {
    val orbits: ArrayList<OrbitEffect> = ArrayList()
    var frozen: Boolean = false
    var isFiring: Boolean = false
    var isPlayer1 = false
    var hud = GenericHud()

    lateinit var body: Body

    var lives : Byte by Delegates.observable(3.toByte()) {
        d, old, new ->
        if (isPlayer1) updateStockPlayer1() else updateStockPlayer2()
    }

    val isDot: Boolean
        get() = spritePath == "sprites/ball.png"

    var lastDirection: Direction = Direction.LEFT

    override fun tick() {
        super.tick()

        if (!isPlayer1) {
            for (stock in lifeBall) {
                stock?.alpha = alpha
            }
        }

        val pos = Vector3(centerX, centerY, 0f)

        body.setTransform(pos.x, pos.y, Math.toRadians(rotation.toDouble()).toFloat())

        val movingDirection = if (frozen) Direction.NONE else Direction.fromVector(velocity)

        if (hasAnimations()) {
            if (movingDirection != Direction.NONE) {
                if (currentAnimation == null || currentAnimation.direction != movingDirection) {
                    getAnimation(RUN, movingDirection)?.reset()?.play()
                }
            } else if (currentAnimation == null || (!currentAnimation.isOrWillBe(IDLE, IDLE2, DEATH) && !frozen && !isFiring)) {
                var animation = getAnimation(IDLE2, lastDirection)
                if (animation == null)
                    animation = getAnimation(IDLE, lastDirection)
                animation?.reset()?.play()
            }

            if (velocity.lengthSquared() > 0f && currentAnimation.type != RUN && !frozen) {
                getAnimation(RUN, movingDirection)?.play()
            }
        }

        //Only save moving direction if there is one to save
        lastDirection = if (movingDirection != Direction.NONE) movingDirection else lastDirection
    }

    private var lifeBall: Array<SpriteEntity?> = arrayOfNulls(if (lives < Constants.MAX_LIVES) Constants.MAX_LIVES else lives.toInt())

    override fun onLoad() {
        super.onLoad()

        setScale(0.75f)

        updateStockPlayer1()

        val playerDef = BodyDef()

        val pos = Vector3(centerX, centerY, 0f)

        body = Ghost.getInstance().createBody(playerDef)

        val wallBox = CircleShape()
        wallBox.radius = width / 2f

        val fixture = body.createFixture(wallBox, 0.0f)

        wallBox.dispose()

        body.setTransform(pos.x, pos.y, Math.toRadians(rotation.toDouble()).toFloat())

        val data = P3dData(2f)
        data.ignoreDirectional = true
        fixture.userData = data

        val shadow = SpriteEntity.fromImage("sprites/shadow.png")
        shadow.centerX = centerX
        shadow.centerY = centerY - 40f
        shadow.zIndex = -1
        shadow.setScale(3f)
        attach(shadow)
        parentScene.addEntity(shadow)

        if (!isPlayer1) {
            val temp = 1111f
            hud.scale(-0.5f)
            hud.setCenter(temp, 730f-hud.height)

            parentScene.addEntity(hud)
        }
    }

    fun updateStockPlayer1() {
        lifeBall.forEach {
            if (it != null) {
                parentScene.removeEntity(it)
            }
        }

        lifeBall = arrayOfNulls(if (lives < Constants.MAX_LIVES) Constants.MAX_LIVES else lives.toInt())

        for (i in 0 until lives) {
            val temp: SpriteEntity = fromImage("sprites/ui/hud/p1_stock.png")

            val y = 220f
            var newY = y - (temp.height / 2f)
            newY += (((temp.height * 1.1f) / (Constants.MAX_LIVES - 1)) * i)

            temp.scale(-0.5f)
            temp.setCenter(30f, newY)

            parentScene.addEntity(temp)
            lifeBall[i] = temp
        }
    }

    fun updateStockPlayer2() {
        lifeBall.forEach {
            if (it != null) {
                parentScene.removeEntity(it)
            }
        }

        lifeBall = arrayOfNulls(if (lives < Constants.MAX_LIVES) Constants.MAX_LIVES else lives.toInt())

        for (i in 0 until lives) {
            val temp: SpriteEntity = fromImage("sprites/ui/hud/p2_stock.png")

            val y = 220f
            var newY = y - (temp.height / 2f)
            newY += (((temp.height * 1.1f) / (Constants.MAX_LIVES - 1)) * i)

            temp.scale(-0.5f)
            temp.setCenter(1280 - 30f, newY)

            parentScene.addEntity(temp)
            lifeBall[i] = temp
        }
    }
}