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
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.core.render.Text
import com.boxtrotstudio.ghost.client.core.render.scene.AbstractScene
import com.boxtrotstudio.ghost.client.core.render.scene.Scene
import com.boxtrotstudio.ghost.client.network.PlayerClient
import com.boxtrotstudio.ghost.client.network.packets.SessionPacket
import com.boxtrotstudio.ghost.client.network.packets.SetNamePacket
import com.boxtrotstudio.ghost.client.utils.Constants
import com.boxtrotstudio.ghost.client.utils.Global
import com.boxtrotstudio.ghost.client.utils.LoginToken
import com.boxtrotstudio.ghost.client.utils.WebUtils
import java.io.IOException
import java.net.URL

class LoginScene : AbstractScene() {
    private lateinit var header: Text;
    private lateinit var stage: Stage;
    private lateinit var username: TextField;
    private lateinit var password: TextField;
    private var textReference: Scene? = null;
    override fun onInit() {

        header = Text(72, Color.WHITE, Gdx.files.internal("fonts/TitilliumWeb-SemiBold.ttf"));
        header.x = 640f
        header.y = 520f
        header.text = "Login"
        header.load()

        stage = Stage(
                Ghost.getInstance().viewport,
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

        username = TextField("", skin)
        username.messageText = "Username"
        password = TextField("", skin)
        password.setPasswordCharacter('*')
        password.isPasswordMode = true
        password.messageText = "Password"

        val loginButton = TextButton("Login", skin)

        username.setAlignment(Align.center)
        password.setAlignment(Align.center)

        table.add(username).width(130f).height(30f).padBottom(30f)
        table.row()
        table.add(password).width(130f).height(30f).padBottom(30f)
        table.row()
        table.add(loginButton).width(100f).height(35f)

        loginButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                Thread(Runnable {
                    val text = TextOverlayScene("Please wait", "Logging in", true)
                    textReference = text
                    Thread.sleep(100)
                    Gdx.app.postRunnable {
                        text.requestOrder(-2)
                        Ghost.getInstance().addScene(text)
                        isVisible = false
                    }
                    login(text);
                }).start()
            }
        })

        //table.debug = true
    }

    override fun render(camera: OrthographicCamera, batch: SpriteBatch) {
        stage.act()
        stage.draw()

        batch.begin()
        header.draw(batch)
        batch.end()
    }

    override fun dispose() {
        stage.dispose()
        if (textReference != null) {
            Ghost.getInstance().removeScene(textReference as Scene)
        }
    }

    private fun login(text: TextOverlayScene) {
        if (Ghost.isOffline()) {
            connectWithSession("qwertyuioplkjhgfdsazxcvbnbmasdfgsdfsdfsafdssdafasdfkasdfjdal", text)
        } else {
            try {
                val url = URL(Constants.LOGIN_URL)
                val response = WebUtils.postToURL(url, "username=" + username.text + "&password=" + password.text)

                val ltoken = Global.GSON.fromJson(response, LoginToken::class.java).token
                connectWithSession(ltoken, text)
            } catch (e: IOException) {
                text.showDots = false
                if (e.message != null) {
                    val message = e.message as String
                    if (message.contains("401")) {
                        text.setHeaderText("Invalid login!")
                        text.setSubText("Invalid username or password..")

                    } else {
                        text.setHeaderText("Error logging in!")
                        text.setSubText(message)
                    }
                }
                Thread(Runnable {
                    Thread.sleep(5000)
                    Gdx.app.postRunnable {
                        text.isVisible = false
                        this.isVisible = true
                    }
                }).start()
            }
        }
    }

    private fun connectWithSession(session: String, text: TextOverlayScene) {
        Ghost.matchmakingClient = PlayerClient.connect(Ghost.getIp())
        if (!Ghost.matchmakingClient.isConnected) {
            text.setHeaderText("Failed to connect!");
            text.setSubText("Could not connect to server..")
            Thread(Runnable {
                Thread.sleep(3000)
                text.replaceWith(this)
            }).start()
            return
        }

        val packet = SessionPacket()
        packet.writePacket(Ghost.matchmakingClient, session, Ghost.getStream());
        if (!Ghost.matchmakingClient.ok()) {
            text.setHeaderText("Failed to connect!");
            text.setSubText("Could not connect to server..")
            throw IOException("Bad session!");
        }
        Ghost.matchmakingClient.isValidated = true

        val packet2 = SetNamePacket()
        packet2.writePacket(Ghost.matchmakingClient, username.text)

        Thread(Runnable {
            Thread.sleep(5000)
            Gdx.app.postRunnable {

                val menu = MenuScene()
                menu.requestOrder(-2)
                text.replaceWith(menu)
            }
        }).start()
    }
}
