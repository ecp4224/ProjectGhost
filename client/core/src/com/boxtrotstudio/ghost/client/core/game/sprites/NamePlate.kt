package com.boxtrotstudio.ghost.client.core.game.sprites

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.boxtrotstudio.ghost.client.core.game.SpriteEntity
import com.boxtrotstudio.ghost.client.core.render.Text

class NamePlate(val username: String) : SpriteEntity("sprites/ui/hud/username_backdrop.png", 0) {

    override fun onLoad() {
        super.onLoad()

        scale(-0.5f)

        val size = if (username.length > 6) 12 else 18

        var usernameText = Text(size, Color(1f, 1f, 1f, 1f), Gdx.files.internal("fonts/7thservice.ttf"))
        usernameText.y = centerY
        usernameText.x = centerX
        usernameText.text = username

        parentScene.addEntity(usernameText)
    }
}