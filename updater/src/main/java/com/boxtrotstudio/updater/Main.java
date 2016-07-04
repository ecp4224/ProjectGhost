package com.boxtrotstudio.updater;

import com.boxtrotstudio.updater.gui.Launch;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

public class Main {


    public static void main(String[] args) throws Exception {
        trustLetsEncrypt();
        new Launch().show();
    }

    private static boolean leTrusted = false;
    public static void trustLetsEncrypt() throws Exception {
        if (leTrusted)
            return;

        System.setProperty("http.agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");

        InputStream x1fis = Main.class.getResourceAsStream("/cert/lets-encrypt-x1-cross-signed.der");
        InputStream x2fis = Main.class.getResourceAsStream("/cert/lets-encrypt-x2-cross-signed.der");
        InputStream x3fis = Main.class.getResourceAsStream("/cert/lets-encrypt-x3-cross-signed.der");
        InputStream x4fis = Main.class.getResourceAsStream("/cert/lets-encrypt-x4-cross-signed.der");

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
    }
}
