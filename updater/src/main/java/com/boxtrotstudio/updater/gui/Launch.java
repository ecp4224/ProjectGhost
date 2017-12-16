package com.boxtrotstudio.updater.gui;

import com.boxtrotstudio.updater.ProgramConfig;
import com.boxtrotstudio.updater.api.Update;
import com.boxtrotstudio.updater.api.UpdateType;
import com.boxtrotstudio.updater.api.Version;
import com.google.gson.Gson;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import me.eddiep.jconfig.JConfig;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.oxbow.swingbits.dialog.task.TaskDialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class Launch {
    private static Gson GSON = new Gson();
    private Path tempDirectory;
    private File gamePath;
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


    private boolean didError;

    private void getGamePath() {
        final String OS = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);

        if (OS.contains("win")) {
            gamePath = new File(System.getenv("AppData"), "ghost");
        } else if (OS.contains("mac")) {
            gamePath = new File("~/Library/Application Support", "Ghost");
        } else {
            gamePath = new File("~/.ghost");
        }
    }

    public void show() {
        getGamePath();

        config = JConfig.newConfigObject(ProgramConfig.class);
        File configFile = new File(gamePath, "program.json");
        if (configFile.exists())
            config.load(configFile);
        else {
            try {
                if (!gamePath.mkdirs()) {
                    System.out.println("Failed to make game directory!");
                }
                PrintWriter writer = new PrintWriter(configFile);
                Scanner scanner = new Scanner(getClass().getResourceAsStream("/program.json"));
                while (scanner.hasNextLine()) {
                    writer.println(scanner.nextLine());
                }

                writer.close();
                scanner.close();

                config.load(configFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }


        final JFrame frame = new JFrame("Launcher");
        frame.setContentPane(panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        frame.setSize(500, 400);

        label44.setForeground(new Color(39, 39, 39, 1));
        label44.setBackground(new Color(39, 39, 39, 1));

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

        runUpdateCheck(frame);

        button1.addActionListener(e -> {
            if (didError) {
                runUpdateCheck(frame);
            } else {
                try {
                    launch(config);
                } catch (final Throwable e1) {
                    //Spawn both at the same time
                    new Thread(() -> TaskDialogs.showException(e1)).start();

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

    private void runUpdateCheck(final JFrame frame) {
        new Thread(() -> {
            button1.setVisible(false);
            button1.setEnabled(false);
            progressBar1.setVisible(true);
            label2.setVisible(true);
            try {
                checkForUpdates();
            } catch (final IOException e) {
                progressBar1.setVisible(false);
                label2.setVisible(false);
                button1.setEnabled(true);
                button1.setVisible(true);
                button1.setText("Try Again");
                didError = true;

                //Spawn both at the same time
                new Thread(() -> TaskDialogs.showException(e)).start();

                try {
                    Thread.sleep(800);
                } catch (InterruptedException ignored) {
                }

                JOptionPane.showMessageDialog(frame,
                        "There was an error checking for updates.\nPlease report this at http://boxtrotstudio.com/bugs",
                        "Error checking for updates",
                        JOptionPane.ERROR_MESSAGE);
            }
        }).start();
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
                label2.setText("Installing " + toApply.getVersion());
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
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (file.isDirectory()) {
                    file = zis.getNextEntry();
                    stepProgressbar();
                    continue;
                }

                File newFile = new File(gamePath, file.getName()).getAbsoluteFile();

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

                            File newFile = new File(gamePath, zipFile.getName()).getAbsoluteFile();

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
        Runtime.getRuntime().exec(config.execute(), null, gamePath);
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

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(5, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel3, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        label = new JLabel();
        label.setFont(new Font(label.getFont().getName(), label.getFont().getStyle(), 36));
        label.setHorizontalAlignment(0);
        label.setHorizontalTextPosition(0);
        label.setText("Project Ghost");
        panel3.add(label, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel3.add(spacer1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel1.add(spacer3, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        panel1.add(spacer4, new GridConstraints(1, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        label2 = new JLabel();
        label2.setFont(new Font(label2.getFont().getName(), label2.getFont().getStyle(), 16));
        label2.setText("Checking for updates...");
        panel1.add(label2, new GridConstraints(2, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        progressBar1 = new JProgressBar();
        panel1.add(progressBar1, new GridConstraints(3, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        button1 = new JButton();
        button1.setFont(new Font(button1.getFont().getName(), button1.getFont().getStyle(), 18));
        button1.setText("Launch Game");
        panel2.add(button1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        panel2.add(spacer5, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer6 = new Spacer();
        panel2.add(spacer6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer7 = new Spacer();
        panel2.add(spacer7, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel4, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        label44 = new JLabel();
        label44.setBackground(new Color(-15853247));
        label44.setEnabled(true);
        label44.setForeground(new Color(-15353025));
        label44.setHorizontalAlignment(0);
        label44.setHorizontalTextPosition(0);
        label44.setIcon(new ImageIcon(getClass().getResource("/boxtrotlogo.png")));
        label44.setText("");
        label44.setVerticalAlignment(1);
        label44.setVerticalTextPosition(0);
        panel4.add(label44, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(217, 37), new Dimension(217, 37), 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }
}
