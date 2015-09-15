package me.eddiep.ghost.matchmaking.network;

import me.eddiep.ghost.network.Server;
import me.eddiep.tinyhttp.TinyHttpServer;
import me.eddiep.tinyhttp.TinyListener;

import java.io.IOException;

public class HttpServer extends Server implements TinyListener {

    private TinyHttpServer server;

    @Override
    public void onStart() {
        super.onStart();

        server = new TinyHttpServer(8080, this, false);

        runInBackground(new Runnable() {
            @Override
            public void run() {
                try {
                    server.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
