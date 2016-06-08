package com.boxtrotstudio.updater;

import com.boxtrotstudio.updater.gui.Launch;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
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

        InputStream x1fis = Main.class.getResourceAsStream("/cert/lets-encrypt-x1-cross-signed.der");
        InputStream x2fis = Main.class.getResourceAsStream("/cert/lets-encrypt-x2-cross-signed.der");
        InputStream x3fis = Main.class.getResourceAsStream("/cert/lets-encrypt-x3-cross-signed.der");
        InputStream x4fis = Main.class.getResourceAsStream("/cert/lets-encrypt-x4-cross-signed.der");

        Certificate x1CA = CertificateFactory.getInstance("X.509").generateCertificate(x1fis);
        Certificate x2CA = CertificateFactory.getInstance("X.509").generateCertificate(x2fis);
        Certificate x3CA = CertificateFactory.getInstance("X.509").generateCertificate(x3fis);
        Certificate x4CA = CertificateFactory.getInstance("X.509").generateCertificate(x4fis);

        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        ks.setCertificateEntry(Integer.toString(1), x1CA);
        ks.setCertificateEntry(Integer.toString(2), x2CA);
        ks.setCertificateEntry(Integer.toString(3), x3CA);
        ks.setCertificateEntry(Integer.toString(4), x4CA);


        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);

        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, tmf.getTrustManagers(), null);

        HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
        leTrusted = true;
    }
}
