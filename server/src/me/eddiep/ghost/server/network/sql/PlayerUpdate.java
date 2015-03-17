package me.eddiep.ghost.server.network.sql;

import me.eddiep.ghost.server.game.entities.Player;
import me.eddiep.ghost.server.game.queue.QueueType;
import org.bson.Document;

public class PlayerUpdate extends PlayerData {

    public PlayerUpdate(Player p) {
        //TODO Fix this constructor
        super(p.getUsername(), p.getUsername(), null, null);
        //TODO setID to player's id
    }

    public void updateDisplayName(String name) {
        super.displayname = name;
        update("displayName", name);
    }

    public void updateWinsFor(QueueType type, int wins) {
        super.winHash.put(type.asByte(), wins);

        Document w = new Document();
        for (Byte t : super.winHash.keySet()) {
            w.append(t.toString(), super.winHash.get(t));
        }
        update("wins", w);
    }

    public void updateLosesFor(QueueType type, int loses) {
        super.loseHash.put(type.asByte(), loses);

        Document w = new Document();
        for (Byte t : super.loseHash.keySet()) {
            w.append(t.toString(), super.loseHash.get(t));
        }
        update("loses", w);
    }


    private void update(String key, Object value) {
        construct.append("$set", new Document().append(key, value));
    }

    private Document construct = new Document();
    @Override
    public Document asDocument() {
        return construct;
    }
}
