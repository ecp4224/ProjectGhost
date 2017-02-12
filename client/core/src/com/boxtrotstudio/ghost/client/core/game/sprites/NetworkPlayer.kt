package com.boxtrotstudio.ghost.client.core.game.sprites

import box2dLight.p3d.P3dData
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.CircleShape
import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.core.game.SpriteEntity
import com.boxtrotstudio.ghost.client.core.game.animations.AnimationType
import com.boxtrotstudio.ghost.client.core.game.sprites.effects.OrbitEffect
import com.boxtrotstudio.ghost.client.utils.Constants
import com.boxtrotstudio.ghost.client.utils.Direction
import java.util.*
import kotlin.properties.Delegates

open class NetworkPlayer(id: Short, name: String) : SpriteEntity(name, id) {
    val orbits: ArrayList<OrbitEffect> = ArrayList()
    var frozen: Boolean = false
    var isFiring: Boolean = false
    lateinit var body: Body;

    var lives : Byte by Delegates.observable(3.toByte()) {
        d, old, new ->
        updateLifeBalls()
    }

    protected var lastDirection: Direction = Direction.LEFT

    override fun tick() {
        super.tick()

        val pos = Vector3(centerX, centerY, 0f)

        body.setTransform(pos.x, pos.y, Math.toRadians(rotation.toDouble()).toFloat())

        val movingDirection = if (frozen) Direction.NONE else Direction.fromVector(velocity)

        if (hasAnimations()) {
            if (movingDirection != Direction.NONE) {
                if (currentAnimation == null || currentAnimation.direction != movingDirection) {
                    getAnimation(AnimationType.RUN, movingDirection)?.reset()?.play()
                }
            } else if (currentAnimation == null || (currentAnimation.type != AnimationType.IDLE && !frozen)) {
                var animation = getAnimation(AnimationType.IDLE2, lastDirection)
                if (animation == null)
                    animation = getAnimation(AnimationType.IDLE, lastDirection)
                animation?.reset()?.play()
            }

            if (velocity.lengthSquared() > 0f && currentAnimation.type != AnimationType.RUN && !frozen) {
                getAnimation(AnimationType.RUN, movingDirection)?.play()
            }
        }

        lastDirection = movingDirection
    }

    private var lifeBall: Array<SpriteEntity?> = arrayOfNulls(if (lives < Constants.MAX_LIVES) Constants.MAX_LIVES else lives.toInt())

    override fun onLoad() {
        super.onLoad()

        setScale(0.75f)

        updateLifeBalls()

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
    }

    fun updateLifeBalls() {
        lifeBall.forEach {
            if (it != null) {
                deattach(it)
                parentScene.removeEntity(it)
            }
        }

        lifeBall = arrayOfNulls<SpriteEntity>(if (lives < Constants.MAX_LIVES) Constants.MAX_LIVES else lives.toInt())

        for (i in 0..lives-1) {
            val temp: SpriteEntity = fromImage("sprites/ball.png")

            var newX = centerX - ((width / 1.5f) / 2f)
            newX += (((width / 1.5f) / (Constants.MAX_LIVES - 1)) * i)

            temp.setScale(0.2f)
            temp.setCenter(newX, centerY - 80f)
            temp.color = Color(20 / 255f, 183 / 255f, 52 / 255f, 1f)
            temp.setAlpha(color.a)

            parentScene.addEntity(temp)

            attach(temp)

            lifeBall[i] = temp
        }
    }
}