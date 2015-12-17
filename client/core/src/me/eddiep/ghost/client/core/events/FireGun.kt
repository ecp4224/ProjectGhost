package me.eddiep.ghost.client.core.events

import me.eddiep.ghost.client.core.Entity

class FireGun : Event {
    override fun getID(): Short {
        return 0
    }

    override fun trigger(cause: Entity) {
        System.out.println("" + cause.id + " fired a gun!");
    }

}