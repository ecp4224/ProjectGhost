package me.eddiep.ghost.client.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import me.eddiep.ghost.client.GhostClient;
import me.eddiep.ghost.client.Handler;

public class DesktopLauncher {
	public static void main (String[] arg) {
		Handler handler;
		if (arg.length == 0) {
			System.err.println("Invalid args!");
			return;
		} else {
			if ()
			String IP = arg[0];
		}


		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		new LwjglApplication(new GhostClient(), config);
	}
}
