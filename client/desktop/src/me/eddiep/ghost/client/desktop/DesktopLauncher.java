package me.eddiep.ghost.client.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import me.eddiep.ghost.client.Ghost;
import me.eddiep.ghost.client.GhostClient;
import me.eddiep.ghost.client.Handler;
import me.eddiep.ghost.client.handlers.GameHandler;
import me.eddiep.ghost.client.handlers.ReplayHandler;
import me.eddiep.ghost.client.utils.ArrayHelper;

public class DesktopLauncher {
	public static void main (String[] arg) {
		Handler handler;
		if (arg.length == 0) {
			System.err.println("Invalid args!");
			return;
		} else {
            if (ArrayHelper.contains(arg, "--replay")) {
                //TODO Replay
                handler = new ReplayHandler();
            } else {
                handler = new GameHandler(arg[0]);

            }
		}

        Ghost.setDefaultHandler(handler);


		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		new LwjglApplication(Ghost.getInstance(), config);
	}
}
