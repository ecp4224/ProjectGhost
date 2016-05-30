package com.boxtrotstudio.ghost.game.match.world.timeline;

import com.google.gson.*;
import com.boxtrotstudio.ghost.utils.Global;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

public class TimelineSerializer implements JsonSerializer<Timeline>, JsonDeserializer<Timeline> {
    @Override
    public JsonElement serialize(Timeline worldSnapshots, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject object = new JsonObject();
        JsonArray array = new JsonArray();

        for (WorldSnapshot snapshot : worldSnapshots) {
            JsonElement element = Global.GSON.toJsonTree(snapshot);
            array.add(element);
        }

        object.addProperty("size", worldSnapshots.size());
        object.add("timeline", array);

        if (worldSnapshots.getWorld() != null && worldSnapshots.getWorld().getWorldMap() != null) {
            JsonElement map = Global.GSON.toJsonTree(worldSnapshots.getWorld().getWorldMap());
            object.add("map", map);
        }

        return object;
    }

    @Override
    public Timeline deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonArray array = jsonElement.getAsJsonObject().getAsJsonArray("timeline");
        List<WorldSnapshot> list = new LinkedList<>();

        for (JsonElement element : array) {
            WorldSnapshot snapshot = Global.GSON.fromJson(element, WorldSnapshot.class);
            list.add(snapshot);
        }

        return new Timeline(list);
    }
}
