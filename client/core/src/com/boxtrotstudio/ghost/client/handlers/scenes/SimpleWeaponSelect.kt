package com.boxtrotstudio.ghost.client.handlers.scenes

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.ScalingViewport
import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.core.render.Text
import com.boxtrotstudio.ghost.client.core.render.scene.AbstractScene
import com.boxtrotstudio.ghost.client.handlers.GameHandler
import com.boxtrotstudio.ghost.client.network.packets.ChangeWeaponPacket
import com.boxtrotstudio.ghost.client.network.packets.JoinQueuePacket
import com.boxtrotstudio.ghost.client.utils.P2Runnable

class SimpleWeaponSelect : AbstractScene() {
    private lateinit var header: Text;
    private lateinit var stage: Stage;
    override fun onInit() {
        val widthMult = (Gdx.graphics.width / 1280f)
        val heightMult = (Gdx.graphics.height / 720f)

        header = Text(72, Color.WHITE, Gdx.files.internal("fonts/INFO56_0.ttf"));
        header.x = 640f * widthMult
        header.y = 520f * heightMult
        header.text = "SELECT A WEAPON"
        header.load()

        stage = Stage(
                ScalingViewport(Scaling.stretch, 1280f, 720f, OrthographicCamera()),
                Ghost.getInstance().batch
        )
        Gdx.input.inputProcessor = stage

        val skin = Skin(Gdx.files.internal("sprites/ui/uiskin.json"))

        var table = Table()
        table.width = 200f
        table.height = 300f
        table.x = 640f - (table.width / 2f)
        table.y = 300f - (table.height / 2f)
        stage.addActor(table)

        val weapons = arrayOf("GUN", "LASER", "CIRCLE", "DASH", "BOOMERANG")
        val buttons = Array(5, {i -> TextButton(weapons[i], skin) })

        for (btn in buttons) {
            table.add(btn).width(130f).height(40f).padBottom(20f)
            table.row()
        }

        for (i in 0..weapons.size - 1) {
            buttons[i].addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    joinQueue((i + 1).toByte())
                }
            })
        }

        requestOrder(-2)

        /*val button = TextButton("GUN", skin)
        val button2 = TextButton("LASER", skin)
        val button3 = TextButton("CIRCLE", skin)
        val button4 = TextButton("DASH", skin)
        val button5 = TextButton("BOOMERANG", skin)
        table.add(button).width(130f).height(40f).padBottom(20f)
        table.row()
        table.add(button2).width(130f).height(40f).padBottom(20f)
        table.row()
        table.add(button3).width(130f).height(40f).padBottom(20f)
        table.row()
        table.add(button4).width(130f).height(40f).padBottom(20f)
        table.row()
        table.add(button5).width(130f).height(40f)

        button.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                joinQueue(1)
            }
        })*/

        //table.debug = true
    }

    private fun joinQueue(weapon: Byte) {
        val packet = ChangeWeaponPacket()
        packet.writePacket(Ghost.matchmakingClient, weapon)

        val packet2 = JoinQueuePacket()
        packet2.writePacket(Ghost.matchmakingClient, 8.toByte())

        val text = TextOverlayScene("SEARCHING FOR GAME", "Please wait", true)
        text.requestOrder(-2)
        replaceWith(text)

        Ghost.onMatchFound = P2Runnable { x, y ->
            Gdx.app.postRunnable {
                Ghost.getInstance().handler = GameHandler(Ghost.getIp(), Ghost.Session)
                Ghost.getInstance().clearScreen()
            }
        }
    }

    override fun render(camera: OrthographicCamera, batch: SpriteBatch) {
        batch.begin()
        header.draw(batch)
        batch.end()

        stage.act()
        stage.draw()
    }

    override fun dispose() {
        stage.dispose()
    }

}
