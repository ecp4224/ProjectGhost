package com.boxtrotstudio.ghost.client.handlers.scenes

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
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
import okhttp3.FormBody
import okhttp3.Request
import java.io.IOException
import java.net.URL
import java.time.Duration
import java.time.Instant

class LoginScene : AbstractScene() {
    private lateinit var header: Text
    private lateinit var stage: Stage
    private lateinit var username: TextField
    private lateinit var password: TextField
    private var textReference: Scene? = null
    private lateinit var background: Sprite
    override fun onInit() {
        background = Sprite(Ghost.ASSETS.get("sprites/ui/select/select_background.png", Texture::class.java))
        background.setCenter(1280f / 2f, 720f / 2f)

        header = Text(62, Color.WHITE, Gdx.files.internal("fonts/7thservicebold.ttf"))
        header.x = 625f
        header.y = 520f
        header.text = "Login"
        header.load()
        header.onClick {
            System.out.println("IT WORKS!")
        }

        stage = Stage(
                Ghost.getInstance().viewport,
                Ghost.getInstance().batch
        )
        attachStage(stage)

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
        val registerButton = TextButton("Register", skin)

        username.setAlignment(Align.center)
        password.setAlignment(Align.center)

        table.add(username).width(200f).height(30f).padBottom(30f).padLeft(10f).padRight(10f)
        table.row()
        table.add(password).width(200f).height(30f).padBottom(30f).padLeft(10f).padRight(10f)
        table.row()

        var buttonTable = Table()
        buttonTable.width = 200f
        buttonTable.height = 300f
        buttonTable.add(loginButton).width(100f).height(35f).padRight(10f)
        buttonTable.add(registerButton).width(100f).height(35f).padLeft(10f)

        table.add(buttonTable)

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
                    login(text)
                }).start()
            }
        })

        registerButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                Gdx.net.openURI("https://projectghost.io/register")
            }
        })

        //table.debug = true
    }

    override fun render(camera: OrthographicCamera, batch: SpriteBatch) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            Thread(Runnable {
                val text = TextOverlayScene("Please wait", "Logging in", true)
                textReference = text
                Thread.sleep(100)
                Gdx.app.postRunnable {
                    text.requestOrder(-2)
                    Ghost.getInstance().addScene(text)
                    isVisible = false
                }
                login(text)
            }).start()
        }

        batch.begin()
        background.draw(batch)
        header.draw(batch)
        batch.end()

        stage.act()
        stage.draw()
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

                val body = FormBody.Builder()
                        .add("username", username.text)
                        .add("password", password.text)
                        .build()

                val request = Request.Builder()
                        .url(url)
                        .header("User-Agent", "Project Ghost")
                        .post(body)
                        .build()


                val response = Global.HTTP.newCall(request).execute()
                val responseString = response.body()?.string() ?: "{}"

                if (responseString.contains("invalid", ignoreCase = true)) {
                    throw IOException("401")
                }

                if (response.code() != 200) {
                    throw IOException("" + response.code())
                }

                System.out.println(responseString)

                var lToken = ""
                for (cookie in Global.COOKIE_MANAGER.cookieStore.cookies) {
                    val date = Instant.now().plus(
                            if (cookie.maxAge > 0) Duration.ofSeconds(cookie.maxAge) else Duration.ofDays(365)
                    ).toString()

                    lToken += cookie.name + "=" +
                            cookie.value + "=" +
                            date + "=" +
                            cookie.domain + "=" +
                            cookie.path + ";"
                }
                connectWithSession(lToken, text)

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
            text.setHeaderText("Failed to connect!")
            text.setSubText("Could not connect to server..")
            Thread(Runnable {
                Thread.sleep(3000)
                text.replaceWith(this)
            }).start()
            return
        }

        val packet = SessionPacket()
        packet.writePacket(Ghost.matchmakingClient, session, Ghost.getStream())
        if (!Ghost.matchmakingClient.ok()) {
            text.setHeaderText("Failed to connect!")
            text.setSubText("Could not connect to server..")
            throw IOException("Bad session!")
        }
        Ghost.matchmakingClient.isValidated = true

        val packet2 = SetNamePacket()
        packet2.writePacket(Ghost.matchmakingClient, username.text)

        Thread(Runnable {
            Thread.sleep(5000)
            Gdx.app.postRunnable {
                Ghost.username = username.text
                val menu = MenuScene()
                menu.requestOrder(-2)
                text.replaceWith(menu)
            }
        }).start()
    }
}
