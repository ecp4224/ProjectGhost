package com.boxtrotstudio.ghost.client.desktop;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglGraphics;
import com.badlogic.gdx.utils.StringBuilder;
import org.apache.http.util.ExceptionUtils;

import java.awt.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class CrashReporterApplication extends LwjglApplication {
    public CrashReporterApplication(ApplicationListener listener, String title, int width, int height) {
        super(listener, title, width, height);
    }

    public CrashReporterApplication(ApplicationListener listener) {
        super(listener);
    }

    public CrashReporterApplication(ApplicationListener listener, LwjglApplicationConfiguration config) {
        super(listener, config);
    }

    public CrashReporterApplication(ApplicationListener listener, Canvas canvas) {
        super(listener, canvas);
    }

    public CrashReporterApplication(ApplicationListener listener, LwjglApplicationConfiguration config, Canvas canvas) {
        super(listener, config, canvas);
    }

    public CrashReporterApplication(ApplicationListener listener, LwjglApplicationConfiguration config, LwjglGraphics graphics) {
        super(listener, config, graphics);
    }

    public void initLogging() {
        mainLoopThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                StringWriter errors = new StringWriter();
                e.printStackTrace(new PrintWriter(errors));
                String text = errors.toString();

                try {
                    FileOutputStream out = new FileOutputStream("crash-" + System.currentTimeMillis() + ".txt");
                    out.write(text.getBytes(Charset.defaultCharset()));
                    out.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }
}
