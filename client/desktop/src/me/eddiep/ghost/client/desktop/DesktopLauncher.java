package me.eddiep.ghost.client.desktop;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import me.eddiep.ghost.client.Ghost;
import me.eddiep.ghost.client.Handler;
import me.eddiep.ghost.client.handlers.GameHandler;
import me.eddiep.ghost.client.handlers.ReplayHandler;
import me.eddiep.ghost.client.network.Packet;
import me.eddiep.ghost.client.network.PlayerClient;
import me.eddiep.ghost.client.network.packets.ChangeWeaponPacket;
import me.eddiep.ghost.client.network.packets.JoinQueuePacket;
import me.eddiep.ghost.client.network.packets.SessionPacket;
import me.eddiep.ghost.client.network.packets.SpectateMatchPacket;
import me.eddiep.ghost.client.utils.ArrayHelper;
import me.eddiep.ghost.client.utils.P2Runnable;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DesktopLauncher {
    private static boolean fullscreen;
    public static void main (String[] args) {
        final String ip;
        Handler handler;
        boolean autofill = args.length == 0;
        fullscreen = ArrayHelper.contains(args, "-f");

        if (autofill) {
            final String session = createOfflineSession("104.236.209.186", "Player 1");
            if (session == null) {
                System.out.println("Server is not offline!");
                System.out.println("Aborting...");
                return;
            }

            System.out.println("Created session!");

            Packet<PlayerClient> packet;
            try {
                final PlayerClient temp = PlayerClient.connect("104.236.209.186");
                packet = new SessionPacket();
                packet.writePacket(temp, session);

                if (!temp.ok()) {
                    System.out.println("Failed to connect!");
                    return;
                }

                packet = new ChangeWeaponPacket();
                packet.writePacket(temp, (byte)2);

                Ghost.onMatchFound = new P2Runnable<Float, Float>() {
                    @Override
                    public void run(Float arg1, Float arg2) {
                        try {
                            temp.disconnect();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        startGame(new GameHandler("104.236.209.186", session));
                    }
                };

                packet = new JoinQueuePacket();
                packet.writePacket(temp, (byte)3);

                if (!temp.ok()) {
                    System.out.println("Failed to join queue!");
                    System.out.println("Aborting..");
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {

            if (ArrayHelper.contains(args, "--replay")) {

                if (args.length == 1) {
                    System.err.println("No replay file specified!");
                    return;
                }

                handler = new ReplayHandler(args[1]);
                startGame(handler);
            } else {
                ip = args[0];
                //TODO -wasd -fullscreen

                if (ArrayHelper.contains(args, "--test")) {
                    Scanner scanner = new Scanner(System.in);
                    System.out.print("Please spcify a username to use: ");
                    String name = scanner.nextLine();

                    System.out.println("Attempting to connect to offline server..");

                    final String session = createOfflineSession(ip, name);
                    if (session == null) {
                        System.out.println("Server is not offline!");
                        System.out.println("Aborting...");
                        return;
                    }

                    System.out.println("Created session!");

                    Packet<PlayerClient> packet;
                    try {
                        final PlayerClient temp = PlayerClient.connect(ip);
                        packet = new SessionPacket();
                        packet.writePacket(temp, session);

                        if (!temp.ok()) {
                            System.out.println("Failed to connect!");
                            return;
                        }

                        if (ArrayHelper.contains(args, "--spectate")) {
                            System.out.print("Type match to spectate: ");
                            long id = scanner.nextLong();

                            packet = new SpectateMatchPacket();
                            packet.writePacket(temp, id);

                            Ghost.isSpectating = true;

                            if (!temp.ok()) {
                                System.out.println("Failed to spectate :c");
                                System.out.println("Aborting..");
                                return;
                            }

                            temp.disconnect();
                            handler = new GameHandler(ip, session);
                            startGame(handler);
                            return;
                        } else {

                            System.out.println();
                            System.out.println("=== Queue Types ===");
                            System.out.println("1 - 1v1 with guns");
                            System.out.println("2 - 1v1 with lasers");
                            System.out.println("3 - 1v1 choose weapon");
                            System.out.println("4 - 2v2 choose weapon");
                            System.out.println();
                            System.out.print("Please type the queue ID to join: ");

                            byte b = scanner.nextByte();

                            if (b == 3 || b == 4) {
                                byte weapon = 0;
                                do {
                                    System.out.println();
                                    System.out.println("=== Weapon Types ===");
                                    System.out.println("1 - Gun");
                                    System.out.println("2 - Laser");
                                    System.out.println("3 - Circle");
                                    System.out.println("4 - Dash");
                                    System.out.println("5 - Boomerang");
                                    System.out.println("16 - Random");
                                    System.out.println();
                                    System.out.print("Please type the weapon ID to use: ");
                                    weapon = scanner.nextByte();
                                }
                                while (weapon != 1 && weapon != 2 && weapon != 3 && weapon != 4 && weapon != 5 && weapon != 16);

                                packet = new ChangeWeaponPacket();
                                packet.writePacket(temp, weapon);
                            }

                            //Set this up before sending the packet
                            Ghost.onMatchFound = new P2Runnable<Float, Float>() {
                                @Override
                                public void run(Float arg1, Float arg2) {
                                    try {
                                        temp.disconnect();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    startGame(new GameHandler(ip, session));
                                }
                            };

                            packet = new JoinQueuePacket();
                            packet.writePacket(temp, b);

                            if (!temp.ok()) {
                                System.out.println("Failed to join queue!");
                                System.out.println("Aborting..");
                            }
                        }
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (args.length < 2) {
                        System.out.println("No session argument found!");
                        System.out.println("Aborting..");
                        return;
                    }

                    String session = args[1];

                    handler = new GameHandler(ip, session);
                    startGame(handler);
                }
            }
        }
    }

    private static void startGame(Handler handler) {
        Ghost.setDefaultHandler(handler);


        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        Graphics.DisplayMode dm = LwjglApplicationConfiguration.getDesktopDisplayMode();
        config.title = "Dots!";
        if (!fullscreen) {
            config.width = 1024;
            config.height = 720;
        } else {
            config.width = dm.width;
            config.height = dm.height;
            config.fullscreen = true;
        }

        new LwjglApplication(Ghost.getInstance(), config);
    }

    private static String createOfflineSession(String ip, String username) {
        CookieStore store = new BasicCookieStore();
        HttpClient client = HttpClientBuilder.create()
                .setDefaultCookieStore(store)
                .build();

        HttpPost post = new HttpPost("http://" + ip + ":8080/api/accounts/login");
        List<NameValuePair> parms = new ArrayList<>();
        parms.add(new BasicNameValuePair("username", username));
        parms.add(new BasicNameValuePair("password", "offline"));
        String session = null;
        try {
            HttpEntity entity = new UrlEncodedFormEntity(parms);
            post.setEntity(entity);

            HttpResponse response = client.execute(post);
            if (response.getStatusLine().getStatusCode() == 202) {
                for (Cookie cookie : store.getCookies()) {
                    if (cookie.getName().equals("session")) {
                        session = cookie.getValue();
                        break;
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return session;
    }
}
