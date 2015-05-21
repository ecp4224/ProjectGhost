package me.eddiep.ghost.central.network.gameserv;

import me.eddiep.ghost.central.Main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

public class QueueNamer {
    private static final String CONFIG_PATH = "names.json";
    private static final Map<Byte, String> categoryNames = new HashMap<>();
    private static final Map<Byte, String> queueNames = new HashMap<>();

    public static void load() throws IOException {
        String json = new String(Files.readAllBytes(Paths.get(CONFIG_PATH)));

        QueueJsonObj obj = Main.GSON.fromJson(json, QueueJsonObj.class);

        for (String key : obj.categories.keySet()) {
            byte id;
            try {
                id = Byte.parseByte(key);
                categoryNames.put(id, obj.categories.get(key));
            } catch (Throwable t) {
                throw new IOException("Invalid ID! (Found: " + key + ")", t);
            }
        }

        for (String key : obj.queues.keySet()) {
            byte id;
            try {
                id = Byte.parseByte(key);
                queueNames.put(id, obj.queues.get(key));
            } catch (Throwable t) {
                throw new IOException("Invalid ID! (Found: " + key + ")", t);
            }
        }
    }

    public static String categoryNameFrom(byte id) {
        return categoryNames.get(id);
    }

    public static String queueNameFrom(byte id) {
        return queueNames.get(id);
    }

    private static class QueueJsonObj {
        private Map<String, String> categories;
        private Map<String, String> queues;

        private QueueJsonObj() { }
    }
}
