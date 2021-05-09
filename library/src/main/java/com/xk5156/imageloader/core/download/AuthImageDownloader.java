package com.xk5156.imageloader.core.download;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

/**
 * author: xk
 * date: 2020/9/16
 * description:
 * version: 1.0
 */
public class AuthImageDownloader extends BaseImageDownloader{

    private SSLSocketFactory mSSLSocketFactory;
    public AuthImageDownloader(Context context) {
        super(context);
        SSLContext sslContext = sslContextForTrustedCertificates();
        mSSLSocketFactory = sslContext.getSocketFactory();
    }
    public AuthImageDownloader(Context context, int connectTimeout, int readTimeout) {
        super(context, connectTimeout, readTimeout);
        SSLContext sslContext = sslContextForTrustedCertificates();
        mSSLSocketFactory = sslContext.getSocketFactory();
    }
    @Override
    protected InputStream getStreamFromNetwork(String imageUri, Object extra) throws IOException {
        URL url = null;
        String newUrl=imageUri;
//        KLog.d("====getStreamFromNetwork imageUri:"+imageUri);
        try {
            String redirectUrl = getRedirectUrl(imageUri);
            if(redirectUrl!=null&&!redirectUrl.equals(imageUri)){
                newUrl=redirectUrl;
            }
            url = new URL(newUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//        KLog.d("====getStreamFromNetwork imageUri:"+imageUri+"\n  conn.getURL():"+conn.getURL().toString()+"\n  newUrl:"+newUrl);
        conn.setConnectTimeout(connectTimeout);
        conn.setReadTimeout(readTimeout);

        if (conn instanceof HttpsURLConnection) {
            ((HttpsURLConnection)conn).setSSLSocketFactory(mSSLSocketFactory);
            ((HttpsURLConnection)conn).setHostnameVerifier((DO_NOT_VERIFY));
        }
        return new BufferedInputStream(conn.getInputStream(), BUFFER_SIZE);
    }
    // always verify the host - dont check for certificate
    final HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };




    public SSLContext sslContextForTrustedCertificates() {
        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
        javax.net.ssl.TrustManager tm = new miTM();
        trustAllCerts[0] = tm;
        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, null);
            //javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }catch (KeyManagementException e) {
            e.printStackTrace();
        }finally {
            return sc;
        }
    }

    public static class miTM implements javax.net.ssl.TrustManager,
            javax.net.ssl.X509TrustManager {
        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }
        public boolean isServerTrusted(
                java.security.cert.X509Certificate[] certs) {
            return true;
        }
        public boolean isClientTrusted(
                java.security.cert.X509Certificate[] certs) {
            return true;
        }
        @Override
        public void checkServerTrusted(
                java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }
        @Override
        public void checkClientTrusted(
                java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }
    }

    /**
     * 获取重定向地址
     * @param path
     * @return
     * @throws Exception
     */
    private String getRedirectUrl(String path){
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(path)
                    .openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.setConnectTimeout(5000);
            return conn.getHeaderField("Location");
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

}
