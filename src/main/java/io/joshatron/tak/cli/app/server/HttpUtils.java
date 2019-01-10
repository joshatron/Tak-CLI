package io.joshatron.tak.cli.app.server;

import io.joshatron.tak.cli.app.server.request.Answer;
import io.joshatron.tak.cli.app.server.response.*;
import io.joshatron.tak.engine.game.Player;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
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
import org.json.JSONArray;
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;

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

    public User getUserFromUsername(String username) {
        HttpGet request = new HttpGet(serverUrl + "/account/user?user=" + username);

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String contents = EntityUtils.toString(response.getEntity());
                JSONObject json = new JSONObject(contents);

                return new User(json.getString("username"), json.getString("userId"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public User getUserFromUserId(String userId) {
        HttpGet request = new HttpGet(serverUrl + "/account/user?id=" + userId);

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String contents = EntityUtils.toString(response.getEntity());
                JSONObject json = new JSONObject(contents);

                return new User(json.getString("username"), json.getString("userId"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
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

    public User[] getIncomingFriendRequests() {
        HttpGet request = new HttpGet(serverUrl + "/social/request/incoming");
        request.setHeader("Authorization", getBasicAuthString(username, password));

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String contents = EntityUtils.toString(response.getEntity());
                JSONArray json = new JSONArray(contents);

                ArrayList<User> users = new ArrayList<>();
                for(int i = 0; i < json.length(); i++) {
                    users.add(new User(json.getJSONObject(i).getString("username"), json.getJSONObject(i).getString("userId")));
                }

                return users.toArray(new User[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public User[] getOutgoingFriendRequests() {
        HttpGet request = new HttpGet(serverUrl + "/social/request/outgoing");
        request.setHeader("Authorization", getBasicAuthString(username, password));

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String contents = EntityUtils.toString(response.getEntity());
                JSONArray json = new JSONArray(contents);

                ArrayList<User> users = new ArrayList<>();
                for(int i = 0; i < json.length(); i++) {
                    users.add(new User(json.getJSONObject(i).getString("username"), json.getJSONObject(i).getString("userId")));
                }

                return users.toArray(new User[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public User[] getFriends() {
        HttpGet request = new HttpGet(serverUrl + "/social/user/friends");
        request.setHeader("Authorization", getBasicAuthString(username, password));

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String contents = EntityUtils.toString(response.getEntity());
                JSONArray json = new JSONArray(contents);

                ArrayList<User> users = new ArrayList<>();
                for(int i = 0; i < json.length(); i++) {
                    users.add(new User(json.getJSONObject(i).getString("username"), json.getJSONObject(i).getString("userId")));
                }

                return users.toArray(new User[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public User[] getBlocking() {
        HttpGet request = new HttpGet(serverUrl + "/social/user/blocking");
        request.setHeader("Authorization", getBasicAuthString(username, password));

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String contents = EntityUtils.toString(response.getEntity());
                JSONArray json = new JSONArray(contents);

                ArrayList<User> users = new ArrayList<>();
                for(int i = 0; i < json.length(); i++) {
                    users.add(new User(json.getJSONObject(i).getString("username"), json.getJSONObject(i).getString("userId")));
                }

                return users.toArray(new User[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean createFriendRequest(String id) {
        HttpPost request = new HttpPost(serverUrl + "/social/request/create/" + id);
        request.setHeader("Authorization", getBasicAuthString(username, password));

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean deleteFriendRequest(String id) {
        HttpDelete request = new HttpDelete(serverUrl + "/social/request/cancel/" + id);
        request.setHeader("Authorization", getBasicAuthString(username, password));

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean respondToFriendRequest(String id, Answer answer) {
        HttpPost request = new HttpPost(serverUrl + "/social/request/respond/" + id);
        request.setHeader("Authorization", getBasicAuthString(username, password));
        JSONObject body = new JSONObject();
        body.put("text", answer.name());
        StringEntity entity = new StringEntity(body.toString(), ContentType.APPLICATION_JSON);
        request.setEntity(entity);

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean unfriendUser(String id) {
        HttpDelete request = new HttpDelete(serverUrl + "/social/user/" + id + "/unfriend");
        request.setHeader("Authorization", getBasicAuthString(username, password));

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean blockUser(String id) {
        HttpPost request = new HttpPost(serverUrl + "/social/user/" + id + "/block");
        request.setHeader("Authorization", getBasicAuthString(username, password));

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean unblockUser(String id) {
        HttpDelete request = new HttpDelete(serverUrl + "/social/user/" + id + "/unblock");
        request.setHeader("Authorization", getBasicAuthString(username, password));

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean sendMessage(String id, String text) {
        if(getUserFromUserId(id).getUsername().equalsIgnoreCase(username)) {
            return false;
        }
        HttpPost request = new HttpPost(serverUrl + "/social/message/send/" + id);
        request.setHeader("Authorization", getBasicAuthString(username, password));
        JSONObject body = new JSONObject();
        body.put("text", text);
        StringEntity entity = new StringEntity(body.toString(), ContentType.APPLICATION_JSON);
        request.setEntity(entity);

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public Message[] getUnreadMessages() {
        HttpGet request = new HttpGet(serverUrl + "/social/message/search?read=NOT_READ");
        request.setHeader("Authorization", getBasicAuthString(username, password));

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String contents = EntityUtils.toString(response.getEntity());
                JSONArray json = new JSONArray(contents);

                ArrayList<Message> messages = new ArrayList<>();

                for(int i = 0; i < json.length(); i++) {
                    JSONObject o = json.getJSONObject(i);
                    messages.add(new Message(o.getString("sender"), o.getString("recipient"),
                            new Date(o.getLong("timestamp")), o.getString("message"), o.getBoolean("opened")));
                }

                return messages.toArray(new Message[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Message[] getMessagesFromUser(String other) {
        HttpGet request = new HttpGet(serverUrl + "/social/message/search?others=" + other);
        request.setHeader("Authorization", getBasicAuthString(username, password));

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String contents = EntityUtils.toString(response.getEntity());
                JSONArray json = new JSONArray(contents);

                ArrayList<Message> messages = new ArrayList<>();

                for(int i = 0; i < json.length(); i++) {
                    JSONObject o = json.getJSONObject(i);
                    messages.add(new Message(o.getString("sender"), o.getString("recipient"),
                            new Date(o.getLong("timestamp")), o.getString("message"), o.getBoolean("opened")));
                }

                return messages.toArray(new Message[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean markAllRead() {
        HttpPost request = new HttpPost(serverUrl + "/social/message/markread");
        request.setHeader("Authorization", getBasicAuthString(username, password));
        JSONObject body = new JSONObject();
        StringEntity entity = new StringEntity(body.toString(), ContentType.APPLICATION_JSON);
        request.setEntity(entity);

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean markReadFromSender(String sender) {
        HttpPost request = new HttpPost(serverUrl + "/social/message/markread");
        request.setHeader("Authorization", getBasicAuthString(username, password));
        JSONObject body = new JSONObject();
        JSONArray senders = new JSONArray();
        senders.put(sender);
        body.put("senders", senders);
        StringEntity entity = new StringEntity(body.toString(), ContentType.APPLICATION_JSON);
        request.setEntity(entity);

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public RequestInfo[] getIncomingGameRequests() {
        HttpGet request = new HttpGet(serverUrl + "/games/request/incoming");
        request.setHeader("Authorization", getBasicAuthString(username, password));

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String contents = EntityUtils.toString(response.getEntity());
                JSONArray json = new JSONArray(contents);

                ArrayList<RequestInfo> requests = new ArrayList<>();
                for(int i = 0; i < json.length(); i++) {
                    JSONObject o = json.getJSONObject(i);
                    requests.add(new RequestInfo(o.getString("requester"), o.getString("acceptor"),
                            Player.valueOf(o.getString("requesterColor")), Player.valueOf(o.getString("first")),
                            o.getInt("size")));
                }

                return requests.toArray(new RequestInfo[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public RequestInfo[] getOutgoingGameRequests() {
        HttpGet request = new HttpGet(serverUrl + "/games/request/outgoing");
        request.setHeader("Authorization", getBasicAuthString(username, password));

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String contents = EntityUtils.toString(response.getEntity());
                JSONArray json = new JSONArray(contents);

                ArrayList<RequestInfo> requests = new ArrayList<>();
                for(int i = 0; i < json.length(); i++) {
                    JSONObject o = json.getJSONObject(i);
                    requests.add(new RequestInfo(o.getString("requester"), o.getString("acceptor"),
                            Player.valueOf(o.getString("requesterColor")), Player.valueOf(o.getString("first")),
                            o.getInt("size")));
                }

                return requests.toArray(new RequestInfo[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
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
