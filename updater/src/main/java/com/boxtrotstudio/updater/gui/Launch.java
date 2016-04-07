package com.boxtrotstudio.updater.gui;

import com.boxtrotstudio.updater.ProgramConfig;
import com.boxtrotstudio.updater.api.Update;
import com.boxtrotstudio.updater.api.UpdateType;
import com.boxtrotstudio.updater.api.Version;
import com.ezware.dialog.task.TaskDialogs;
import com.google.gson.Gson;
import me.eddiep.jconfig.JConfig;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class Launch {
    private static Gson GSON = new Gson();
    private Path tempDirectory;
    private ProgramConfig config;

    private JPanel panel1;
    private JButton button1;
    private JPanel panel2;
    private JPanel panel3;
    private JLabel label;
    private JLabel label2;
    private JProgressBar progressBar1;
    private JPanel panel4;
    private JLabel label44;

    public void show() {
        config = JConfig.newConfigObject(ProgramConfig.class);
        File configFile = new File("program.json");
        if (configFile.exists())
            config.load(configFile);
        else {
            System.err.println("program.json could not be found!");
            System.err.println("(Looked in " + configFile.getAbsolutePath() + ")");
            System.exit(1);
            return;
        }


        final JFrame frame = new JFrame("Launcher");
        frame.setContentPane(panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        frame.setSize(500, 400);

        label44.setForeground(new Color(39,39,39, 1));
        label44.setBackground(new Color(39,39,39, 1));

        label44.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("http://boxtrotstudio.com"));
                } catch (IOException | URISyntaxException e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                label44.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                label44.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        button1.setBackground(new Color(93, 148, 86));
        button1.setForeground(new Color(255, 255, 255, 180));

        button1.setEnabled(false);
        button1.setVisible(false);

        panel1.setBackground(new Color(39, 39, 39));
        panel2.setBackground(new Color(39, 39, 39));
        panel3.setBackground(new Color(39, 39, 39));
        label.setForeground(Color.WHITE);
        label2.setForeground(Color.WHITE);

        progressBar1.setIndeterminate(true);

        panel1.setBorder(new EmptyBorder(15, 15, 15, 15));

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    checkForUpdates();
                } catch (final IOException e) {
                    progressBar1.setVisible(false);
                    label2.setVisible(false);

                    //Spawn both at the same time
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            TaskDialogs.showException(e);
                        }
                    }).start();

                    try {
                        Thread.sleep(800);
                    } catch (InterruptedException ignored) {
                    }

                    JOptionPane.showMessageDialog(frame,
                            "There was an error checking for updates.\nPlease report this at http://boxtrotstudio.com/bugs",
                            "Error checking for updates",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }).start();

        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                try {
                    launch(config);
                } catch (final Throwable e1) {
                    //Spawn both at the same time
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            TaskDialogs.showException(e1);
                        }
                    }).start();

                    try {
                        Thread.sleep(800);
                    } catch (InterruptedException ignored) {
                    }

                    JOptionPane.showMessageDialog(frame,
                            "There was an error launching the game.\nPlease report this at http://boxtrotstudio.com/bugs",
                            "Error checking for updates",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void checkForUpdates() throws IOException {
        URL url = new URL(config.updateLocation());
        Version currentVersion = Version.parseVersion(config.currentVersion());

        System.out.println("Downloading remote update list");

        String json = IOUtils.toString(url, Charset.defaultCharset());

        List<Update> updates = Arrays.asList(GSON.fromJson(json, Update[].class));

        System.out.println("Checking latest update");

        if (!currentVersion.equals(updates.get(updates.size() - 1).getVersion())) {
            System.out.println("Potential update found..");

            ArrayList<Update> updatesMissed = new ArrayList<>();
            Update latest = updates.get(updates.size() - 1);

            updatesMissed.add(latest);
            for (int i = updates.size() - 2; i >= 0; i--) {
                Update temp = updates.get(i);

                if (temp.getVersion().isHigherThan(currentVersion)) {
                    updatesMissed.add(temp);
                } else {
                    break;
                }
            }

            System.out.println(updatesMissed.size() + " update" + (updatesMissed.size() == 1 ? "" : "s") + " found!");
            System.out.println(currentVersion + " -> " + latest.getVersion());

            tempDirectory = Files.createTempDirectory(config.getName());

            int taskCount = calculateTaskCount(updatesMissed);
            progressBar1.setIndeterminate(false);
            progressBar1.setMaximum(taskCount);

            for (int i = updatesMissed.size() - 1; i >= 0; i--) {
                Update toApply = updatesMissed.get(i);
                applyUpdate(toApply, updates, updates.indexOf(toApply));
                stepProgressbar();
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            System.out.println();
            System.out.println("All updates applied, launching");

            progressBar1.setVisible(false);
            label2.setVisible(false);
            button1.setEnabled(true);
            button1.setVisible(true);
        } else {
            System.out.println("No updates found, launching");

            progressBar1.setVisible(false);
            label2.setVisible(false);
            button1.setEnabled(true);
            button1.setVisible(true);
        }
    }

    private void stepProgressbar() {
        progressBar1.setValue(progressBar1.getValue() + 1);
    }

    private void applyUpdate(Update update, List<Update> updateList, int currentIndex) throws IOException {
        System.out.println();
        System.out.println("Applying update " + update.getVersion());

        if (update.getType() != UpdateType.ROLLBACK) {
            File tempFile = new File(tempDirectory.toFile(), update.getVersion() + ".zip");

            URL downloadURL = new URL(update.getArchiveLocation());

            FileUtils.copyURLToFile(downloadURL, tempFile);

            ZipInputStream zis = new ZipInputStream(new FileInputStream(tempFile));

            ZipEntry file = zis.getNextEntry();
            byte[] buffer = new byte[1024];
            while (file != null) {
                System.out.println("Extracting " + file.getName() + "...");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                File newFile = new File(file.getName()).getAbsoluteFile();

                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);


                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();

                file = zis.getNextEntry();
                stepProgressbar();
            }

            zis.closeEntry();
            zis.close();
        } else {
            System.out.println("Rollback update detected!");

            for (String file : update.getFilesModified()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Searching for " + file + "...");
                String md5 = update.getMd5().get(file);

                for (int i = currentIndex - 1; i >= 0; i--) {
                    Update temp = updateList.get(i);

                    if (temp.equals(update))
                        continue;

                    if (arrayContains(temp.getFilesModified(), file)) {
                        System.out.println("Found in " + temp.getVersion());

                        File tempFile = new File(tempDirectory.toFile(), temp.getVersion() + ".zip");

                        if (!tempFile.exists()) {
                            System.out.println("Downloading update " + temp.getVersion() + "...");

                            URL downloadURL = new URL(temp.getArchiveLocation());

                            FileUtils.copyURLToFile(downloadURL, tempFile);
                        }

                        ZipFile zzzz = new ZipFile(tempFile);

                        ZipInputStream zis = new ZipInputStream(new FileInputStream(tempFile));

                        ZipEntry zipFile = zis.getNextEntry();
                        byte[] buffer = new byte[1024];

                        boolean badupdate = false;

                        while (zipFile != null) {
                            if (!zipFile.getName().equals(file)) {
                                zipFile = zis.getNextEntry();
                                continue;
                            }

                            String their_md5 = DigestUtils.md5Hex(zzzz.getInputStream(zipFile));
                            if (their_md5.equals(md5)) {
                                badupdate = true;
                                break;
                            }

                            System.out.println("Extracting " + zipFile.getName() + "...");

                            File newFile = new File(zipFile.getName()).getAbsoluteFile();

                            new File(newFile.getParent()).mkdirs();

                            FileOutputStream fos = new FileOutputStream(newFile);


                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                            fos.close();
                            stepProgressbar();
                            break;
                        }
                        zis.closeEntry();
                        zis.close();

                        if (badupdate)
                            continue;

                        break;
                    }
                }
            }

            System.out.println("Updating program.json..");
            config.setCurrentVersion(update.getVersion().toString());
            config.save(new File("program.json"));

            System.out.println("Rollback complete!");
        }

        System.out.println("Update applied successfully!");
    }

    private void launch(ProgramConfig config) throws IOException {
        Runtime.getRuntime().exec(config.execute());
        System.exit(0);
    }

    private static boolean arrayContains(String[] array, String contains) {
        for (String s : array) {
            if (s.equals(contains))
                return true;
        }
        return false;
    }

    private static int calculateTaskCount(List<Update> updateList) {
        int taskCount = updateList.size();

        for (Update u : updateList) {
            taskCount += u.getFilesModified().length;
        }

        return taskCount;
    }

}
