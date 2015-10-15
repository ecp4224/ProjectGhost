package me.eddiep.ghost.client.android;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import me.eddiep.ghost.client.Ghost;
import me.eddiep.ghost.client.GhostClient;
import me.eddiep.ghost.client.handlers.GameHandler;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();

		//TODO Display a handler for joining a queue
		initialize(Ghost.getInstance(), config);
	}
}
