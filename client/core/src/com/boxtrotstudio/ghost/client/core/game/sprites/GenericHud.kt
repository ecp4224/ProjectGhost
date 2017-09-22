package com.boxtrotstudio.ghost.client.core.game.sprites

import com.boxtrotstudio.ghost.client.core.game.SpriteEntity

class GenericHud : SpriteEntity("sprites/ui/hud/p2.png", 0) {

    override fun onLoad() {
        super.onLoad()

        val entity = SpriteEntity("sprites/ui/hud/p2_backdrop.png", 0)
        entity.zIndex = -1
        entity.scale(-0.5f)
        entity.setCenter(centerX + 100f, centerY - 100f)
        parentScene.addEntity(entity)
    }
}