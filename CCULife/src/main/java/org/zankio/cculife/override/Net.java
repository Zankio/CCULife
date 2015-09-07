package org.zankio.cculife.override;

import android.content.Context;

import org.jsoup.Connection;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
public class Net {
    public final static int CONNECT_TIMEOUT = 10000;

    public static Connection connect(String url) {
        return org.jsoup.Jsoup.connect(url).timeout(CONNECT_TIMEOUT);
    }

    public static SSLSocketFactory generateSSLSocketFactory(Context context) {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = new BufferedInputStream(context.getAssets().open("ssl.crt"));
            Certificate ca;

            try {
                ca = cf.generateCertificate(caInput);
            } finally {
                caInput.close();
            }

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            SSLContext ssl_context = SSLContext.getInstance("TLS");
            ssl_context.init(null, tmf.getTrustManagers(), null);

;
            return ssl_context.getSocketFactory();
        }
        catch (CertificateException e) {}
        catch (KeyStoreException e) {}
        catch (KeyManagementException e) {}
        catch (NoSuchAlgorithmException e) {}
        catch (IOException e) { }

        return null;
    }
}
