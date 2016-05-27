package com.boxtrotstudio.ghost.client.handlers.scenes

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.ScalingViewport
import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.core.game.Entity
import com.boxtrotstudio.ghost.client.core.render.Text
import com.boxtrotstudio.ghost.client.core.render.scene.AbstractScene
import com.boxtrotstudio.ghost.client.handlers.GameHandler
import com.boxtrotstudio.ghost.client.network.packets.ChangeWeaponPacket
import com.boxtrotstudio.ghost.client.network.packets.JoinQueuePacket
import com.boxtrotstudio.ghost.client.network.packets.LeaveQueuePacket
import com.boxtrotstudio.ghost.client.utils.CancelToken
import com.boxtrotstudio.ghost.client.utils.P2Runnable
import com.boxtrotstudio.ghost.client.utils.Timer

class GameSetupScene : AbstractScene() {
    private lateinit var header: Text;
    private lateinit var description: Text;
    private lateinit var stage: Stage;
    private lateinit var weaponImage: Entity;

    private lateinit var gameModeArray: com.badlogic.gdx.utils.Array<String>;
    private lateinit var gameModeTypeArray: com.badlogic.gdx.utils.Array<String>;
    private lateinit var weaponArray: com.badlogic.gdx.utils.Array<String>;
    private lateinit var weaponImageArray: com.badlogic.gdx.utils.Array<Texture>;
    private lateinit var weaponHeaderArray: com.badlogic.gdx.utils.Array<String>;
    private lateinit var weaponDescriptionArray: com.badlogic.gdx.utils.Array<String>;

    private lateinit var gameMode : SelectBox<String>;
    private lateinit var gameModeType : SelectBox<String>;
    private lateinit var weapons : SelectBox<String>;

    private lateinit var timeInQueue: Text
    private lateinit var header2: Text

    private var toJoin = 8
    override fun onInit() {
        gameModeArray = Array()
        gameModeArray.add("Casual")

        gameModeTypeArray = Array()
        gameModeTypeArray.add("1v1")
        gameModeTypeArray.add("2v2")

        weaponArray = Array()
        weaponArray.addAll("GUN", "LASER", "CIRCLE", "DASH", "BOOMERANG")

        weaponImageArray = Array()
        weaponImageArray.add(Ghost.ASSETS.get("sprites/menu/gun.png"))
        weaponImageArray.add(Ghost.ASSETS.get("sprites/menu/laser.png"))
        weaponImageArray.add(Ghost.ASSETS.get("sprites/menu/circle.png"))
        weaponImageArray.add(Ghost.ASSETS.get("sprites/menu/dash.png"))
        weaponImageArray.add(Ghost.ASSETS.get("sprites/menu/boomerang.png"))

        weaponHeaderArray = Array()
        weaponHeaderArray.add("The Gun")
        weaponHeaderArray.add("The Laser")
        weaponHeaderArray.add("The Circle")
        weaponHeaderArray.add("The Dash")
        weaponHeaderArray.add("The Boomerang")

        weaponDescriptionArray = Array()
        weaponDescriptionArray.add("Firepower for the simple man.\n Bullets fire at a medium velocity \n with an unlimited range.")
        weaponDescriptionArray.add("Shiny and deadly.\n Charges up for a short time, \n then releases an intense beam that \n ricochets off mirrors.")
        weaponDescriptionArray.add("Spawns a ring of fiery plasma, \n shedding light on enemies within its radius.\n Those who cannot escape the ring \n receive a nasty burn.")
        weaponDescriptionArray.add("A favorite among outdoorsy folks.\n It may not be fast, but its aerodynamic design \n allow its wielder to manipulate its path in mid-air.")
        weaponDescriptionArray.add("A favorite among outdoorsy folks.\n It may not be fast, but its aerodynamic design \n allow its wielder to manipulate its path in mid-air.")

        val widthMult = (Gdx.graphics.width / 1280f)
        val heightMult = (Gdx.graphics.height / 720f)

        header = Text((72 * widthMult).toInt(), Color.WHITE, Gdx.files.internal("fonts/TitilliumWeb-SemiBold.ttf"));
        header.x = 200f * widthMult
        header.y = 680f * heightMult
        header.text = "Game Setup"
        header.load()

        stage = Stage(
                ScalingViewport(Scaling.stretch, 1280f, 720f, OrthographicCamera()),
                Ghost.getInstance().batch
        )
        Gdx.input.inputProcessor = stage

        val skin = Skin(Gdx.files.internal("sprites/ui/uiskin.json"))

        var setupTable = Table()
        setupTable.width = 300f
        setupTable.height = 100f
        setupTable.x = 20f
        setupTable.y = 600f - (setupTable.height / 2f)
        stage.addActor(setupTable)

        gameMode = SelectBox<String>(skin)
        gameMode.items = gameModeArray

        gameModeType = SelectBox<String>(skin)
        gameModeType.items = gameModeTypeArray

        gameModeType.addListener(object : ChangeListener() {
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                toJoin = 8 + gameModeType.selectedIndex
            }
        })

        setupTable.add(gameMode).width(128f).height(40f).padRight(5f)
        setupTable.add().width(30f).height(40f).padRight(5f)
        setupTable.add(gameModeType).width(128f).height(40f).padRight(5f)

        var chooseWeapon = Table()
        chooseWeapon.width = 300f
        chooseWeapon.height = 100f
        chooseWeapon.x = 640f - (chooseWeapon.width / 2f)
        chooseWeapon.y = 600f - (chooseWeapon.height / 2f)
        stage.addActor(chooseWeapon)

        weapons = SelectBox<String>(skin)
        weapons.items = weaponArray

        chooseWeapon.add(weapons).width(128f).height(40f)

        weaponImage = Entity.fromImage("sprites/menu/gun.png")
        weaponImage.setScale(0.3f)
        weaponImage.setCenter(640f * widthMult, 470f * heightMult)

        description = Text((24 * widthMult).toInt(), Color.WHITE, Gdx.files.internal("fonts/TitilliumWeb-Light.ttf"));
        description.x = 640f * widthMult
        description.y = 300f * heightMult
        description.text = weaponDescriptionArray.get(0)
        description.load()

        weapons.addListener(object : ChangeListener() {
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                weaponImage.texture = weaponImageArray.get(weapons.selectedIndex)
                description.text = weaponDescriptionArray.get(weapons.selectedIndex)
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
                    joinQueue((weapons.selectedIndex + 1).toByte())
                    joinQueue.setText("Cancel")
                } else {
                    leaveQueue()
                    joinQueue.setText("Join Queue")
                }
            }
        })

        timeInQueue = Text((24 * widthMult).toInt(), Color.WHITE, Gdx.files.internal("fonts/TitilliumWeb-Light.ttf"))
        timeInQueue.x = 640f * widthMult
        timeInQueue.y = 360f * heightMult
        timeInQueue.text = "0:00"
        timeInQueue.load()

        header2 = Text((72 * widthMult).toInt(), Color.WHITE, Gdx.files.internal("fonts/TitilliumWeb-SemiBold.ttf"))
        header2.x = 640f * widthMult
        header2.y = 600f * heightMult
        header2.text = "Searching for a Game"
        header2.load()

        requestOrder(-2)
    }

    private fun leaveQueue() {
        val packet = LeaveQueuePacket()
        packet.writePacket(Ghost.matchmakingClient)

        weapons.isVisible = true
        gameMode.isVisible = true
        gameModeType.isVisible = true
        isInQueue = false
        timerToken.cancel()
        //checkerToken.cancel()
    }

    private var isInQueue = false
    private lateinit var timerToken: CancelToken
    private lateinit var checkerToken: CancelToken
    private var queueTime = 0;
    private var playersInQueue = 1
    private fun joinQueue(weapon: Byte) {
        val packet = ChangeWeaponPacket()
        packet.writePacket(Ghost.matchmakingClient, weapon)

        val packet2 = JoinQueuePacket()
        packet2.writePacket(Ghost.matchmakingClient, toJoin.toByte())

        weapons.isVisible = false
        gameMode.isVisible = false
        gameModeType.isVisible = false
        isInQueue = true
        queueTime = 0
        timerToken = Timer.newTimer({
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

        /*checkerToken = Timer.newTimer({
            playersInQueue = Integer.parseInt(WebUtils.readContentsToString(URL("http://" + Ghost.getIp() + ":8080/queue/8")))
        }, 3000L)*/

        Ghost.onMatchFound = P2Runnable { x, y ->
            Gdx.app.postRunnable {
                Ghost.getInstance().handler = GameHandler(Ghost.getIp(), Ghost.Session)
                Ghost.getInstance().clearScreen()
            }
        }
    }

    override fun render(camera: OrthographicCamera, batch: SpriteBatch) {
        batch.begin()
        if (!isInQueue) {
            header.draw(batch)
            description.draw(batch)
            weaponImage.draw(batch)
        }

        if (isInQueue) {
            timeInQueue.draw(batch)
            header2.draw(batch)
        }

        batch.end()

        stage.act()
        stage.draw()

        batch.color = Color.WHITE //reset color
    }

    override fun dispose() {
        stage.dispose()
    }

}
