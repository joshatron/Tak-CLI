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

    private void processFailure(HttpResponse response) {
        try {
            String contents = EntityUtils.toString(response.getEntity());
            JSONObject json = new JSONObject(contents);

            if(json.getString("reason") != null) {
                System.out.println("Operation could not be performed because of: " + json.getString("reason"));
            }
        } catch (IOException e) {
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

    public boolean isConnected() {
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

    public boolean isAuthenticated(String username, String password) {
        HttpGet request = new HttpGet(serverUrl + "/account/authenticate");
        request.setHeader("Authorization", getBasicAuthString(username, password));

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                this.username = username;
                this.password = password;
                return true;
            }
            else {
                processFailure(response);
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
            else {
                processFailure(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void changePassword(String newPass) {
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
                System.out.println("Password successfully changed");
            }
            else {
                processFailure(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void changeUsername(String newName) {
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
                System.out.println("Username successfully changed");
            }
            else {
                processFailure(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            else {
                processFailure(response);
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
            else {
                processFailure(response);
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
            else {
                processFailure(response);
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
            else {
                processFailure(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void createFriendRequest(String id) {
        HttpPost request = new HttpPost(serverUrl + "/social/request/create/" + id);
        request.setHeader("Authorization", getBasicAuthString(username, password));

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                System.out.println("Friend request created");
            }
            else {
                processFailure(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteFriendRequest(String id) {
        HttpDelete request = new HttpDelete(serverUrl + "/social/request/cancel/" + id);
        request.setHeader("Authorization", getBasicAuthString(username, password));

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                System.out.println("Friend request deleted");
            }
            else {
                processFailure(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void respondToFriendRequest(String id, Answer answer) {
        HttpPost request = new HttpPost(serverUrl + "/social/request/respond/" + id);
        request.setHeader("Authorization", getBasicAuthString(username, password));
        JSONObject body = new JSONObject();
        body.put("text", answer.name());
        StringEntity entity = new StringEntity(body.toString(), ContentType.APPLICATION_JSON);
        request.setEntity(entity);

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                if(answer == Answer.ACCEPT) {
                    System.out.println("Successfully accepted friend request");
                }
                else {
                    System.out.println("Successfully denied friend request");
                }
            }
            else {
                processFailure(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void unfriendUser(String id) {
        HttpDelete request = new HttpDelete(serverUrl + "/social/user/" + id + "/unfriend");
        request.setHeader("Authorization", getBasicAuthString(username, password));

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                System.out.println("User unfriended");
            }
            else {
                processFailure(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void blockUser(String id) {
        HttpPost request = new HttpPost(serverUrl + "/social/user/" + id + "/block");
        request.setHeader("Authorization", getBasicAuthString(username, password));

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                System.out.println("User blocked");
            }
            else {
                processFailure(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void unblockUser(String id) {
        HttpDelete request = new HttpDelete(serverUrl + "/social/user/" + id + "/unblock");
        request.setHeader("Authorization", getBasicAuthString(username, password));

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                System.out.println("User unblocked");
            }
            else {
                processFailure(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String id, String text) {
        if(getUserFromUserId(id).getUsername().equalsIgnoreCase(username)) {
            System.out.println("Cannot send message to yourself");
        }
        else {
            HttpPost request = new HttpPost(serverUrl + "/social/message/send/" + id);
            request.setHeader("Authorization", getBasicAuthString(username, password));
            JSONObject body = new JSONObject();
            body.put("text", text);
            StringEntity entity = new StringEntity(body.toString(), ContentType.APPLICATION_JSON);
            request.setEntity(entity);

            try {
                HttpResponse response = client.execute(request);
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                    System.out.println("Message sent");
                }
                else {
                    processFailure(response);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
            else {
                processFailure(response);
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
            else {
                processFailure(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void markAllRead() {
        HttpPost request = new HttpPost(serverUrl + "/social/message/markread");
        request.setHeader("Authorization", getBasicAuthString(username, password));
        JSONObject body = new JSONObject();
        StringEntity entity = new StringEntity(body.toString(), ContentType.APPLICATION_JSON);
        request.setEntity(entity);

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                System.out.println("All messages marked as read");
            }
            else {
                processFailure(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void markReadFromSender(String sender) {
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
                System.out.println("Messages marked as read");
            }
            else {
                processFailure(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            else {
                processFailure(response);
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
            else {
                processFailure(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void requestGame(RequestInfo requestInfo) {
        HttpPost request = new HttpPost(serverUrl + "/games/request/create/" + requestInfo.getAcceptor());
        request.setHeader("Authorization", getBasicAuthString(username, password));
        JSONObject body = new JSONObject();
        body.put("size", requestInfo.getSize());
        body.put("requesterColor", requestInfo.getRequesterColor().name());
        body.put("first", requestInfo.getFirst().name());
        StringEntity entity = new StringEntity(body.toString(), ContentType.APPLICATION_JSON);
        request.setEntity(entity);

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                System.out.println("Game request created");
            }
            else {
                processFailure(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cancelGameRequest(String id) {
        HttpDelete request = new HttpDelete(serverUrl + "/games/request/cancel/" + id);
        request.setHeader("Authorization", getBasicAuthString(username, password));

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                System.out.println("Game request cancelled");
            }
            else {
                processFailure(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void respondToGameRequest(String id, Answer answer) {
        HttpPost request = new HttpPost(serverUrl + "/games/request/respond/" + id);
        request.setHeader("Authorization", getBasicAuthString(username, password));
        JSONObject body = new JSONObject();
        body.put("text", answer.name());
        StringEntity entity = new StringEntity(body.toString(), ContentType.APPLICATION_JSON);
        request.setEntity(entity);

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                if(answer == Answer.ACCEPT) {
                    System.out.println("Game request accepted");
                }
                else {
                    System.out.println("Game request denied");
                }
            }
            else {
                processFailure(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getRandomGameSize() {
        HttpGet request = new HttpGet(serverUrl + "/games/request/random/outgoing");
        request.setHeader("Authorization", getBasicAuthString(username, password));

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return Integer.parseInt(EntityUtils.toString(response.getEntity()));
            }
            else {
                processFailure(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public void createRandomGameRequest(int size) {
        HttpPost request = new HttpPost(serverUrl + "/games/request/random/create/" + size);
        request.setHeader("Authorization", getBasicAuthString(username, password));

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                System.out.println("Created random game request");
            }
            else {
                processFailure(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cancelRandomGameRequest() {
        HttpDelete request = new HttpDelete(serverUrl + "/games/request/random/cancel");
        request.setHeader("Authorization", getBasicAuthString(username, password));

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                System.out.println("Cancelled random game request");
            }
            else {
                processFailure(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public GameInfo[] getOpenGames() {
        HttpGet request = new HttpGet(serverUrl + "/games/search?complete=INCOMPLETE");
        request.setHeader("Authorization", getBasicAuthString(username, password));

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String contents = EntityUtils.toString(response.getEntity());
                JSONArray json = new JSONArray(contents);

                ArrayList<GameInfo> gameInfos = new ArrayList<>();

                for(int i = 0; i < json.length(); i++) {
                    JSONObject o = json.getJSONObject(i);

                    JSONArray turnArray = o.getJSONArray("turns");
                    String[] turns = new String[turnArray.length()];
                    for(int j = 0; j < turnArray.length(); j++) {
                        turns[j] = turnArray.getString(j);
                    }

                    gameInfos.add(new GameInfo(o.getString("gameId"), o.getString("white"), o.getString("black"),
                            o.getInt("size"), Player.valueOf(o.getString("first")), Player.valueOf(o.getString("current")),
                            null, false, turns));
                }

                return gameInfos.toArray(new GameInfo[0]);
            }
            else {
                processFailure(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public GameInfo[] getYourTurnGames() {
        HttpGet request = new HttpGet(serverUrl + "/games/search?pending=PENDING");
        request.setHeader("Authorization", getBasicAuthString(username, password));

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String contents = EntityUtils.toString(response.getEntity());
                JSONArray json = new JSONArray(contents);

                ArrayList<GameInfo> gameInfos = new ArrayList<>();

                for(int i = 0; i < json.length(); i++) {
                    JSONObject o = json.getJSONObject(i);

                    JSONArray turnArray = o.getJSONArray("turns");
                    String[] turns = new String[turnArray.length()];
                    for(int j = 0; j < turnArray.length(); j++) {
                        turns[j] = turnArray.getString(j);
                    }

                    gameInfos.add(new GameInfo(o.getString("gameId"), o.getString("white"), o.getString("black"),
                            o.getInt("size"), Player.valueOf(o.getString("first")), Player.valueOf(o.getString("current")),
                            null, false, turns));
                }

                return gameInfos.toArray(new GameInfo[0]);
            }
            else {
                processFailure(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public GameInfo getGameWithUser(String user) {
        HttpGet request = new HttpGet(serverUrl + "/games/search?opponents=" + user + "&complete=INCOMPLETE");
        request.setHeader("Authorization", getBasicAuthString(username, password));

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String contents = EntityUtils.toString(response.getEntity());
                JSONArray json = new JSONArray(contents);

                JSONObject o = json.getJSONObject(0);

                JSONArray turnArray = o.getJSONArray("turns");
                String[] turns = new String[turnArray.length()];
                for(int j = 0; j < turnArray.length(); j++) {
                    turns[j] = turnArray.getString(j);
                }

                return new GameInfo(o.getString("gameId"), o.getString("white"), o.getString("black"),
                        o.getInt("size"), Player.valueOf(o.getString("first")), Player.valueOf(o.getString("current")),
                        null, false, turns);
            }
            else {
                processFailure(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void playTurn(String gameId, String turn) {
        HttpPost request = new HttpPost(serverUrl + "/games/game/" + gameId + "/play");
        request.setHeader("Authorization", getBasicAuthString(username, password));
        JSONObject body = new JSONObject();
        body.put("text", turn);
        StringEntity entity = new StringEntity(body.toString(), ContentType.APPLICATION_JSON);
        request.setEntity(entity);

        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                System.out.println("Turn successfully made");
            }
            else {
                processFailure(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            else {
                processFailure(response);
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
            else {
                processFailure(response);
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
