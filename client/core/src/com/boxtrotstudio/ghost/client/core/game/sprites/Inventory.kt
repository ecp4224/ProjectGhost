package com.boxtrotstudio.ghost.client.core.game.sprites

import com.badlogic.gdx.Gdx
import com.boxtrotstudio.ghost.client.core.game.Item
import com.boxtrotstudio.ghost.client.core.game.SpriteEntity

class Inventory : SpriteEntity("sprites/ui/hud/p1.png", 0) {
    val inventory: Array<ItemHolder?> = arrayOfNulls(2)

    init {
        super.lightable = false
    }

    override fun onLoad() {
        super.onLoad()

        val entity = SpriteEntity("sprites/ui/hud/p1_backdrop.png", 0)
        entity.zIndex = -1
        entity.scale(-0.5f)
        entity.setCenter(centerX, centerY - 100f)
        parentScene.addEntity(entity)
    }

    public fun setSlot1(id: Short) {
        val widthMult = (Gdx.graphics.width / 1280f)
        val heightMult = (Gdx.graphics.height / 720f)

        val item = Item.getItem(id)
        val entity = item.createEntity(-5)
        //val text = Text(16, Color.WHITE, Gdx.files.internal("fonts/7thservice.ttf"))
        var temp = 1024f - 830f
        /*text.x = (1280f - (1024f - 854f)) * widthMult
        text.y = (100f - (entity.height * 1.3f)) * heightMult
        text.text = item.name*/

        entity.setScale(1.5f)
        entity.setCenter(temp, centerY - 110f)

        parentScene.addEntity(entity)
        //parentScene.addEntity(text)

        inventory[0] = ItemHolder(entity, item)
    }

    public fun clearSlot1() {
        clearSlot(0)
    }

    public fun setSlot2(id: Short) {
        val widthMult = (Gdx.graphics.width / 1280f)
        val heightMult = (Gdx.graphics.height / 720f)
        val item = Item.getItem(id)
        val entity = item.createEntity(-5)
        //val text = Text(16, Color.WHITE, Gdx.files.internal("fonts/7thservice.ttf"))
        var temp = 1024f - 735f
        /*text.x = (1280 - (1024 - 947f)) * widthMult
        text.y = (100f - (entity.height * 1.3f)) * heightMult
        text.text = item.name*/


        entity.setScale(1.5f)
        entity.setCenter(temp, centerY - 113f)

        parentScene.addEntity(entity)
        //parentScene.addEntity(text)

        inventory[1] = ItemHolder(entity, item)
    }

    public fun clearSlot2() {
        clearSlot(1)
    }

    public fun clearSlot(slot: Int) {
        if (inventory[slot] != null) {
            val entity = inventory[slot]?.entity
            if (entity != null)
                parentScene.removeEntity(entity)

            /*val text = inventory[slot]?.text
            if (text != null)
                parentScene.removeEntity(text)*/

            inventory[slot] = null
        }
    }

    public fun hasItem(slot: Int): Boolean {
        return inventory[slot] != null
    }

    public fun getItem(slot: Int): Short {
        if (!hasItem(slot))
            return -1

        return inventory[slot]?.item?.id as Short
    }

    class ItemHolder(val entity: SpriteEntity, val item: Item)
}
