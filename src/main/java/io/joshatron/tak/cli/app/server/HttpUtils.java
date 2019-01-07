package io.joshatron.tak.cli.app.server;

import io.joshatron.tak.cli.app.server.response.GameNotifications;
import io.joshatron.tak.cli.app.server.response.SocialNotifications;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class HttpUtils {

    private HttpClient client;
    private String serverUrl;

    private String username;
    private String password;

    public HttpUtils(String serverUrl) {
        this.serverUrl = serverUrl;
        try {
            SSLContextBuilder builder = SSLContexts.custom();
            builder.loadTrustMaterial(null, new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                    return true;
                }
            });
            SSLContext sslContext = builder.build();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    sslContext, new X509HostnameVerifier() {
                @Override
                public void verify(String host, SSLSocket ssl)
                        throws IOException {
                }

                @Override
                public void verify(String host, X509Certificate cert)
                        throws SSLException {
                }

                @Override
                public void verify(String host, String[] cns,
                                   String[] subjectAlts) throws SSLException {
                }

                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            });

            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
                    .<ConnectionSocketFactory>create().register("https", sslsf)
                    .build();

            PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(
                    socketFactoryRegistry);

            client = HttpClients.custom().setConnectionManager(cm).build();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

    private static String getBasicAuthString(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

    public boolean checkConnection() {
        HttpGet request = new HttpGet(serverUrl + "/account/authenticate");
        request.setHeader("Authorization", getBasicAuthString("test", "test"));

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT ||
               response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean authenticate(String username, String password) {
        HttpGet request = new HttpGet(serverUrl + "/account/authenticate");
        request.setHeader("Authorization", getBasicAuthString(username, password));

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                this.username = username;
                this.password = password;
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean register(String username, String password) {
        HttpPost request = new HttpPost(serverUrl + "/account/register");
        JSONObject body = new JSONObject();
        body.put("username", username);
        body.put("password", password);
        StringEntity entity = new StringEntity(body.toString(), ContentType.APPLICATION_JSON);
        request.setEntity(entity);

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                this.username = username;
                this.password = password;
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean changePassword(String newPass) {
        HttpPost request = new HttpPost(serverUrl + "/account/changepass");
        request.setHeader("Authorization", getBasicAuthString(username, password));
        JSONObject body = new JSONObject();
        body.put("text", newPass);
        StringEntity entity = new StringEntity(body.toString(), ContentType.APPLICATION_JSON);
        request.setEntity(entity);

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                this.password = newPass;
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean changeUsername(String newName) {
        HttpPost request = new HttpPost(serverUrl + "/account/changename");
        request.setHeader("Authorization", getBasicAuthString(username, password));
        JSONObject body = new JSONObject();
        body.put("text", newName);
        StringEntity entity = new StringEntity(body.toString(), ContentType.APPLICATION_JSON);
        request.setEntity(entity);

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                this.username = newName;
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public SocialNotifications getSocialNotifications() {
        HttpGet request = new HttpGet(serverUrl + "/social/notifications");
        request.setHeader("Authorization", getBasicAuthString(username, password));

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String contents = EntityUtils.toString(response.getEntity());
                JSONObject json = new JSONObject(contents);
                return new SocialNotifications(json.getInt("incomingRequests"), json.getInt("incomingMessages"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public GameNotifications getGameNotifications() {
        HttpGet request = new HttpGet(serverUrl + "/games/notifications");
        request.setHeader("Authorization", getBasicAuthString(username, password));

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String contents = EntityUtils.toString(response.getEntity());
                JSONObject json = new JSONObject(contents);
                return new GameNotifications(json.getInt("incomingRequests"), json.getInt("pendingGames"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getUsername() {
        return username;
    }

    public void logout() {
        username = null;
        password = null;
    }

    public void nullServerUrl() {
        this.serverUrl = null;
    }
}
