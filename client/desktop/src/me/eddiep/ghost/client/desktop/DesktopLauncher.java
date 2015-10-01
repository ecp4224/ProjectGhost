package me.eddiep.ghost.client.desktop;

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
import org.lwjgl.Sys;

import java.io.IOException;
import java.util.Scanner;

public class DesktopLauncher {
    public static void main (String[] args) {
        final String ip;
        Handler handler;
        if (args.length == 0) {
            System.err.println("Invalid args!");
        } else {
            if (ArrayHelper.contains(args, "--replay")) {
                //TODO Replay
                handler = new ReplayHandler();
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
        config.width = 1024;
        config.height = 720;
        new LwjglApplication(Ghost.getInstance(), config);
    }

    private static String createOfflineSession(String ip, String username) {
        return null; //TODO Create session
    }
}
