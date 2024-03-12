import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import okhttp3.OkHttpClient;
import android.content.Context;
import android.util.Log;

public class HttpsClient {

    public static OkHttpClient getHttpsClient(Context context) {
        OkHttpClient client = null;
        try {
            // self signed certificate
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream certInputStream = context.getResources().openRawResource(R.raw.mycert);
            X509Certificate cert = (X509Certificate) cf.generateCertificate(certInputStream);

            // create a KeyStore include the self-signed certificate
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("server", cert);

            // create TrustManager by KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // 创建一个SSLContext，使用我们的TrustManager
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, tmf.getTrustManagers(), null);

            // 使用SSLContext创建OkHttpClient
            client = new OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) tmf.getTrustManagers()[0])
                    .build();
        } catch (Exception e) {
            Log.e("HttpsClient", "Error setting up HTTPS client", e);
        }
        return client;
    }
}
