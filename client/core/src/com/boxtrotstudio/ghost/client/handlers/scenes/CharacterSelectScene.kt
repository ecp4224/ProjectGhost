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
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.core.game.DynamicAnimation
import com.boxtrotstudio.ghost.client.core.game.SpriteEntity
import com.boxtrotstudio.ghost.client.core.render.Text
import com.boxtrotstudio.ghost.client.core.render.scene.AbstractScene
import com.boxtrotstudio.ghost.client.handlers.GameHandler
import com.boxtrotstudio.ghost.client.handlers.MenuHandler
import com.boxtrotstudio.ghost.client.network.packets.ChangeItemPacket
import com.boxtrotstudio.ghost.client.network.packets.ChangeWeaponPacket
import com.boxtrotstudio.ghost.client.network.packets.JoinQueuePacket
import com.boxtrotstudio.ghost.client.network.packets.LeaveQueuePacket
import com.boxtrotstudio.ghost.client.utils.*
import com.boxtrotstudio.ghost.client.utils.Timer
import java.util.*

class CharacterSelectScene(val autoJoin: Boolean) : AbstractScene() {
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

    private lateinit var itemArray: com.badlogic.gdx.utils.Array<String>
    private lateinit var itemImageArray: com.badlogic.gdx.utils.Array<Texture>
    private lateinit var itemHeaderArray: com.badlogic.gdx.utils.Array<String>
    private lateinit var itemDescriptionArray: com.badlogic.gdx.utils.Array<String>
    private lateinit var items : SelectBox<String>
    private lateinit var itemImage: SpriteEntity
    private lateinit var itemDescription: Text

    private lateinit var slotTable: Table

    private lateinit var timeInQueue: Text
    private lateinit var header2: Text
    private var toJoin = 3
    private var characters = ArrayList<ImageButton>()
    override fun onInit() {
        itemArray = Array()
        itemArray.addAll("Fire Rate Module", "Life Module", "Jammer Module", "Shield Module", "Speed Module")

        itemImageArray = Array()
        itemImageArray.add(Ghost.ASSETS.get("sprites/can_of_chummy.png"))
        itemImageArray.add(Ghost.ASSETS.get("sprites/can_of_yummy.png"))
        itemImageArray.add(Ghost.ASSETS.get("sprites/can_of_glummy.png"))
        itemImageArray.add(Ghost.ASSETS.get("sprites/can_of_scummy.png"))
        itemImageArray.add(Ghost.ASSETS.get("sprites/can_of_gummy.png"))

        itemDescriptionArray = Array()
        itemDescriptionArray.add("When activated, increases your\nrate of fire by 20% for 10 seconds.")
        itemDescriptionArray.add("When activated, grants you\none extra life.")
        itemDescriptionArray.add("When activated, jams the enemy's weapon.\nThe next time they fire, they will become visible\nbut there weapon will not go off.")
        itemDescriptionArray.add("When activated, grants you a shield\nfor 5 seconds. While the shield\nis up, you can absorb 1 hit without losing a life.")
        itemDescriptionArray.add("When activated, grants you +50%\nmovement speed for 5 seconds.\nIf activated during a weapon\nchargeup, the chargeup will be canceled.")

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

        val skin = Skin(Gdx.files.internal("sprites/ui/uiskin.json"))

        stage = Stage(
                Ghost.getInstance().viewport,
                Ghost.getInstance().batch
        )
        attachStage(stage)
        Ghost.setStage(stage, skin)

        slotTable = Table()
        slotTable.width = background.width
        slotTable.height = 600f
        slotTable.x = 1280f / 4f - (background.width / 2f)
        slotTable.y = -40f

        for (i in 1..characterCount) {
            val btn = ImageButton(grabDrawable("sprites/ui/select/slot_$i.png"))
            slotTable.add(btn).padBottom(padding).row()

            btn.setOrigin(Align.center)
            btn.isTransform = true

            btn.addListener(TextTooltip(weaponDescriptionArray[i - 1], skin))
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

        var chooseItem = Table()
        chooseItem.width = 300f
        chooseItem.height = 100f
        chooseItem.x = (640f - (chooseItem.width / 2f)) + 250f
        chooseItem.y = 600f - (chooseItem.height / 2f)
        stage.addActor(chooseItem)

        items = SelectBox(skin)
        items.items = itemArray
        items.selectedIndex = Ghost.lastItem

        chooseItem.add(items).width(160f).height(40f)

        itemImage = SpriteEntity.fromImage("sprites/can_of_chummy.png")
        itemImage.setScale(1.5f)
        itemImage.setCenter(640f + 300f, 470f)

        itemDescription = Text(24, Color.WHITE, Gdx.files.internal("fonts/TitilliumWeb-Light.ttf"))
        itemDescription.x = 640f + 300f
        itemDescription.y = 300f
        itemDescription.text = itemDescriptionArray.get(0)
        itemDescription.load()

        items.addListener(object : ChangeListener() {
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                itemImage.texture = itemImageArray.get(items.selectedIndex)
                itemDescription.text = itemDescriptionArray.get(items.selectedIndex)
            }
        })

        var backButtonTable = Table()
        backButtonTable.width = 300f
        backButtonTable.height = 40f
        backButtonTable.x = 100f - (backButtonTable.width / 2f)
        backButtonTable.y = 40f - (backButtonTable.height / 2f)
        stage.addActor(backButtonTable)

        val backButton = TextButton("Back", skin)
        backButtonTable.add(backButton).width(130f).height(40f)

        backButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                if (isInQueue)
                    leaveQueue()

                if (Ghost.getInstance().handler !is MenuHandler) {
                    val menuHandler = MenuHandler()
                    menuHandler.start()
                    Ghost.getInstance().handler = menuHandler
                }

                replaceWith(MenuScene())
            }
        })

        var buttonTable = Table()
        buttonTable.width = 300f
        buttonTable.height = 40f
        buttonTable.x = 640f - (buttonTable.width / 2f)
        buttonTable.y = 40f - (buttonTable.height / 2f)
        stage.addActor(buttonTable)

        val joinQueue = TextButton("Join Queue", skin)
        buttonTable.add(joinQueue).width(130f).height(40f)

        joinQueue.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                if (!isInQueue) {
                    joinQueue(selectedCharacter.toByte(), items.selectedIndex.toByte())
                    joinQueue.setText("Cancel")
                } else {
                    leaveQueue()
                    joinQueue.setText("Join Queue")
                }
            }
        })

        timeInQueue = Text(24, Constants.Colors.PRIMARY, Gdx.files.internal("fonts/7thservice.ttf"))
        timeInQueue.x = 640f
        timeInQueue.y = 360f
        timeInQueue.text = "0:00"
        timeInQueue.load()

        header2 = Text(72, Constants.Colors.PRIMARY, Gdx.files.internal("fonts/7thservicecond.ttf"))
        header2.x = 640f
        header2.y = 600f
        header2.text = "Searching for a Game"
        header2.load()

        requestOrder(-2)

        if (autoJoin) {
            joinQueue(Ghost.lastWeapon.toByte(), Ghost.lastItem.toByte())
            joinQueue.setText("Cancel")
        }
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
        if (!isInQueue) {
            background.draw(batch)
            header.draw(batch)
            desc_background.draw(batch)
            itemDescription.draw(batch)
            itemImage.draw(batch)
        } else {
            timeInQueue.draw(batch)
            header2.draw(batch)
        }
        batch.end()

        stage.act()
        stage.draw()
    }

    override fun dispose() {
        stage.dispose()
    }

    private fun leaveQueue() {
        val packet = LeaveQueuePacket()
        packet.writePacket(Ghost.matchmakingClient)

        slotTable.isVisible = true
        items.isVisible = true

        isInQueue = false
        timerToken.cancel()
        //checkerToken.cancel()
    }

    private var isInQueue = false
    private lateinit var timerToken: CancelToken
    private lateinit var checkerToken: CancelToken
    private var queueTime = 0
    private var playersInQueue = 1
    private fun joinQueue(weapon: Byte, item: Byte) {
        val packet = ChangeWeaponPacket()
        packet.writePacket(Ghost.matchmakingClient, weapon)

        val packet3 = ChangeItemPacket()
        packet3.writePacket(Ghost.matchmakingClient, item)

        val packet2 = JoinQueuePacket()
        packet2.writePacket(Ghost.matchmakingClient, toJoin.toByte())

        slotTable.isVisible = false
        items.isVisible = false
        isInQueue = true
        queueTime = 0
        timerToken = Timer.newTimer(Runnable {
            queueTime++

            val minutes = queueTime / 60
            val seconds = queueTime % 60
            val dots = queueTime % 3

            timeInQueue.text = "Time in Queue: $minutes:" + (if (seconds < 10) "0" else "") + "$seconds"

            header2.text = "Searching for a Game"
            for (i in 0..dots) {
                header2.text += "."
            }
        }, 1000L)

        Ghost.onMatchFound = P2Runnable { x, y ->
            Gdx.app.postRunnable {
                //Save weapon/item combo
                Ghost.lastItem = item.toInt()
                Ghost.lastWeapon = weapon.toInt()

                if (Ghost.isTesting()) {
                    //Let's not disconnect
                    //Let's just reuse the connection
                    Ghost.client = Ghost.matchmakingClient
                    //Ghost.matchmakingClient.disconnect()
                    //Ghost.matchmakingClient = null

                    Ghost.getInstance().clearScreen()
                    val game = GameHandler(Ghost.getIp(), Ghost.Session)
                    game.start()
                    Ghost.getInstance().handler = game
                }

                timerToken.cancel()

            }
        }
    }
}