package com.boxtrotstudio.ghost.matchmaking.core.hosts;

import com.boxtrotstudio.ghost.matchmaking.Main;
import com.myjeeva.digitalocean.exception.DigitalOceanException;
import com.myjeeva.digitalocean.exception.RequestUnsuccessfulException;
import com.myjeeva.digitalocean.impl.DigitalOceanClient;
import com.myjeeva.digitalocean.pojo.Droplet;
import com.myjeeva.digitalocean.pojo.Image;
import com.myjeeva.digitalocean.pojo.Region;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DigitalOcean extends BoxtrotHost {
    private static long nextID;
    private final DigitalOceanClient client;

    public DigitalOcean() {
        client = new DigitalOceanClient(Main.getServer().getConfig().getDigitalOceanToken());
    }

    @Override
    public void scaleUp() throws IOException {
        Droplet spawnDroplet = new Droplet();
        spawnDroplet.setSize("2gb");
        //TODO Pick best region
        spawnDroplet.setRegion(new Region("nyc2"));
        spawnDroplet.setImage(new Image(Main.getServer().getConfig().getDigitalOceanImage()));
        spawnDroplet.setEnableBackup(false);
        spawnDroplet.setEnableIpv6(true);

        File launchScript = new File(Main.getServer().getConfig().getDigitalOceanLaunchScript());
        Path launchScriptPath = launchScript.toPath();
        String fileContents = new String(Files.readAllBytes(launchScriptPath));
        spawnDroplet.setUserData(fileContents);

        try {
            Droplet newDroplet = client.createDroplet(spawnDroplet);

        } catch (DigitalOceanException | RequestUnsuccessfulException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void scaleDown() {

    }
}
