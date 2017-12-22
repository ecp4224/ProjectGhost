package com.boxtrotstudio.ghost.utils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;

public class WebUtils {

    public static List<String> readContentsToList(URL url) throws IOException {
        LineNumberReader reader = new LineNumberReader(new InputStreamReader(url.openStream()));
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null)
            lines.add(line);

        reader.close();
        return lines;
    }

    public static String[] readContentsToArray(URL url) throws IOException {
        List<String> lines = readContentsToList(url);
        return lines.toArray(new String[lines.size()]);
    }

    public static String readContentsToString(URL url) throws IOException {
        LineNumberReader reader = new LineNumberReader(new InputStreamReader(url.openStream()));
        StringBuilder content = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null)
            content.append(line);

        return content.toString();
    }

    private static boolean leTrusted;
    public static void trustLetsEncrypt() throws Exception {
        if (leTrusted)
            return;

        InputStream x1fis = WebUtils.class.getResourceAsStream("/cert/lets-encrypt-x1-cross-signed.der");
        InputStream x2fis = WebUtils.class.getResourceAsStream("/cert/lets-encrypt-x2-cross-signed.der");
        InputStream x3fis = WebUtils.class.getResourceAsStream("/cert/lets-encrypt-x3-cross-signed.der");
        InputStream x4fis = WebUtils.class.getResourceAsStream("/cert/lets-encrypt-x4-cross-signed.der");

        Certificate x1CA = CertificateFactory.getInstance("X.509").generateCertificate(x1fis);
        Certificate x2CA = CertificateFactory.getInstance("X.509").generateCertificate(x2fis);
        Certificate x3CA = CertificateFactory.getInstance("X.509").generateCertificate(x3fis);
        Certificate x4CA = CertificateFactory.getInstance("X.509").generateCertificate(x4fis);


        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        Path ksPath = Paths.get(System.getProperty("java.home"),
                "lib", "security", "cacerts");
        ks.load(Files.newInputStream(ksPath),
                "changeit".toCharArray());
        ks.setCertificateEntry(Integer.toString(1), x1CA);
        ks.setCertificateEntry(Integer.toString(2), x2CA);
        ks.setCertificateEntry(Integer.toString(3), x3CA);
        ks.setCertificateEntry(Integer.toString(4), x4CA);


        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);

        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, tmf.getTrustManagers(), null);

        SSLContext.setDefault(ctx);
        HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
        leTrusted = true;
    }
}
