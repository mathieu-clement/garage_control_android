package com.mathieuclement.android.garage_opener.app.webrequest;

import android.content.Context;
import com.mathieuclement.android.garage_opener.app.R;
import com.squareup.okhttp.OkHttpClient;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;

/**
 * @author Mathieu Clément
 * @since 06.08.2013
 */
class CustomTrustHttpClientBuilder {

    private final Context context;

    public CustomTrustHttpClientBuilder(Context context) {
        this.context = context;
    }

    public OkHttpClient build() {
        OkHttpClient client = new OkHttpClient();
        client.setSslSocketFactory(newSslContextFactory());
        return client;
    }

    private SSLSocketFactory newSslContextFactory() {
        // Copied from
        // http://stackoverflow.com/questions/4065379/how-to-create-a-bks-bouncycastle-format-java-keystore-that-contains-a-client-c
        // To generate BKS keystore, use "Portecle"
        // http://sourceforge.net/projects/portecle/

        try {
            KeyStore trustStore = KeyStore.getInstance("BKS");
            String trustStorePassword = "mysecretgaragedoor";
            InputStream in = context.getResources().openRawResource(R.raw.keystore);
            try {
                trustStore.load(in, trustStorePassword.toCharArray());

                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(trustStore, trustStorePassword.toCharArray());

                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(trustStore);

                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());
                return sslContext.getSocketFactory();
            } finally {
                in.close();
            }
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
