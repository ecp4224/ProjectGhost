package com.boxtrotstudio.ghost.client.handlers

import com.badlogic.gdx.graphics.Color
import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.core.game.CharacterCreator
import com.boxtrotstudio.ghost.client.core.game.Characters
import com.boxtrotstudio.ghost.client.core.game.SpriteEntity
import com.boxtrotstudio.ghost.client.core.game.maps.MapCreator
import com.boxtrotstudio.ghost.client.core.game.sprites.InputEntity
import com.boxtrotstudio.ghost.client.core.logic.Handler
import com.boxtrotstudio.ghost.client.handlers.scenes.BlurredScene
import com.boxtrotstudio.ghost.client.handlers.scenes.SpriteScene
import com.boxtrotstudio.ghost.client.utils.P2Runnable
import com.boxtrotstudio.ghost.client.utils.Vector2f

class CharacterTest(val character: Characters) : Handler {
    var ambiantColor: Color = Color(1f, 1f, 1f, 1f)
    var ambiantPower : Float = 1f

    var player1 : InputEntity? = null

    lateinit var world : SpriteScene


    override fun start() {
        world = SpriteScene()

        Ghost.getInstance().addScene(world)

        Ghost.PHYSICS.clear()

        val map = MapCreator.MAPS[Characters.values().indexOf(this.character)]

        System.out.println("Loading map " + map.name())
        map.construct(world)

        player1 = CharacterCreator.createPlayer(Ghost.selfCharacter, "DEFAULT", 0)
        player1?.velocity = Vector2f(0f, 0f)
        player1?.setCenter(1280f/2f, 720f/2f)
        world.addEntity(player1 as SpriteEntity)

        Ghost.isInMatch = true
        Ghost.isReady = false
        Ghost.matchStarted = false
    }

    override fun tick() {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}