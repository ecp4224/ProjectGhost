package com.boxtrotstudio.ghost.client.handlers.scenes

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.core.game.DynamicAnimation
import com.boxtrotstudio.ghost.client.core.game.SpriteEntity
import com.boxtrotstudio.ghost.client.core.render.Text
import com.boxtrotstudio.ghost.client.core.render.scene.AbstractScene
import com.boxtrotstudio.ghost.client.utils.PRunnable
import java.util.*

class CharacterSelectScene : AbstractScene() {
    private val BUTTON_DURATION = 200
    private val characterCount = 5
    private val padding = 20f
    private var currentAnimation : DynamicAnimation? = null
    private var selectedCharacter = -1
    private lateinit var weaponDescriptionArray: com.badlogic.gdx.utils.Array<String>

    private lateinit var stage: Stage
    private lateinit var background: Sprite
    private lateinit var desc_background: Sprite
    private lateinit var header: Sprite
    private lateinit var description: Text
    private var characters = ArrayList<ImageButton>()
    override fun onInit() {
        weaponDescriptionArray = Array()
        weaponDescriptionArray.add("Firepower for the simple man.\n Nothing fancy, but bullets have an unlimited range.")
        weaponDescriptionArray.add("Shiny and deadly.\n Charges up for a short time, \n then releases an intense beam that \n ricochets off mirrors.")
        weaponDescriptionArray.add("Spawns a ring of fiery plasma, \n sucking in enemies within its radius.\n Those who cannot escape receive a nasty burn.")
        weaponDescriptionArray.add("Handcrafted by the universe's finest cobblers, \nthese boots allow the wearer to dash forth at dangerous velocities.\n Requires a running start.")
        weaponDescriptionArray.add("A favorite among outdoorsy folks.\n It may not be very fast, but its aerodynamic design \n allows the wielder to manipulate its path in mid-air.")

        description = Text(24, Color.WHITE, Gdx.files.internal("fonts/TitilliumWeb-Light.ttf"))
        description.x = (1280f / 4f) * 3f
        description.y = 300f
        description.text = weaponDescriptionArray.get(0)
        description.load()

        background = Sprite(Ghost.ASSETS.get("sprites/ui/select/select_background.png", Texture::class.java))
        background.setCenter(1280f / 4f, 720f / 2f)

        desc_background = Sprite(Ghost.ASSETS.get("sprites/ui/select/select_background.png", Texture::class.java))
        desc_background.setCenter((1280f / 4f) * 3f, 720f / 2f)

        header = Sprite(Ghost.ASSETS.get("sprites/ui/select/header.png", Texture::class.java))
        header.setCenter(1280f / 4f, 600f)

        stage = Stage(
                Ghost.getInstance().viewport,
                Ghost.getInstance().batch
        )
        attachStage(stage)

        var slotTable = Table()
        slotTable.width = background.width
        slotTable.height = 600f
        slotTable.x = 1280f / 4f - (background.width / 2f)
        slotTable.y = -40f

        for (i in 1..characterCount) {
            val btn = ImageButton(grabDrawable("sprites/ui/select/slot_$i.png"))
            slotTable.add(btn).padBottom(padding).row()

            btn.setOrigin(Align.center)
            btn.isTransform = true
            btn.addListener(object : ClickListener() {
                override fun enter(event: InputEvent?, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                    if (selectedCharacter != i) {
                        onHover(btn)
                    }

                    if (selectedCharacter == -1) {
                        description.text = weaponDescriptionArray.get(i - 1)
                    }
                }

                override fun exit(event: InputEvent?, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                    if (selectedCharacter != i) {
                        onLeave()
                        btn.setScale(1f)
                    }
                }

                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    if (selectedCharacter != -1) {
                        characters[selectedCharacter - 1].setScale(1f)
                    }

                    if (selectedCharacter != i) {
                        selectedCharacter = i
                        description.text = weaponDescriptionArray.get(i - 1)
                    } else {
                        selectedCharacter = -1 //Deselect
                        description.text = ""
                    }
                }
            })

            characters.add(btn)
        }

        stage.addActor(slotTable)
    }

    fun onHover(actor: Actor) {
        val startX = actor.scaleX
        val startY = actor.scaleY
        currentAnimation = DynamicAnimation(PRunnable { time ->
            val nx = SpriteEntity.ease(startX, 1.25f, BUTTON_DURATION.toFloat(), time.toFloat())
            val ny = SpriteEntity.ease(startY, 1.25f, BUTTON_DURATION.toFloat(), time.toFloat())

            actor.scaleX = nx
            actor.scaleY = ny
        }, BUTTON_DURATION.toLong()).start()
    }

    fun onLeave() {
        currentAnimation?.end()
    }

    override fun render(camera: OrthographicCamera, batch: SpriteBatch) {

        batch.begin()
        background.draw(batch)
        desc_background.draw(batch)
        description.draw(batch)
        header.draw(batch)
        batch.end()

        stage.act()
        stage.draw()
    }

    override fun dispose() {

    }

}