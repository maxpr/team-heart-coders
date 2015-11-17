package ch.epfl.sweng.swissaffinity.utilities.network;


import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * The class that get the HTTP connection from the server URL.
 */
public class DefaultNetworkProvider implements NetworkProvider {

    public final static int HTTP_SUCCESS_START = 200;
    public final static int HTTP_SUCCESS_END = 299;

    @Override
    public HttpURLConnection getConnection(URL url) throws IOException {
        return (HttpURLConnection) url.openConnection();
    }

    @Override
    public String getContent(String serverURL) throws IOException {
        URL url = new URL(serverURL);
        HttpURLConnection conn = getConnection(url);
        if (!isConnectionSuccess(conn)) {
            throw new IOException("Connection bad response code: " + conn.getResponseCode());
        }
        return fetchContent(conn);
    }

    /**
     *The method to check if the connection is successful
     * @param conn the urlConnection
     * @return true if the server return 200 as response code , false else
     * @throws IOException
     */
    public static boolean isConnectionSuccess(HttpURLConnection conn) throws IOException {
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        int response = conn.getResponseCode();
        return response == HTTP_SUCCESS_START;
    }

    /**
     * Checkthe connection using the IsConnectionSuccess
     * @param serverURL the server Url
     * @return The boolean to see if you get http response code == 200
     * @throws IOException
     */
    public static Boolean checkConnection(String serverURL) throws IOException {
        URL url = new URL(serverURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        return isConnectionSuccess(conn);
    }

    /**
     *
     * @param serverURL
     * @param json
     * @return
     * @throws IOException
     */
    public  String postContent(String serverURL, JSONObject json) throws IOException {
        URL url = new URL(serverURL);
        String response = null;
        HttpURLConnection conn = getConnection(url);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.connect();
        OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
        out.write(json.toString());
        out.flush();
        out.close();
        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            response =fetchContent(conn);
        } else if(responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR){
            throw new ConnectException();
        } else if(responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
            response =fetchErrorContent(conn);
        } else {
            throw new ConnectException();
        }
        return response;
    }

    /**
     * make a String out of the GET request to the server
     */
    private String fetchContent(HttpURLConnection connection) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            return stringBuilder.toString();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    /**
     *
     * @param connection the HttpUrlConnection from where you're reading the errorStream
     * @return The errorStream that the server sent you
     * @throws IOException If the connection.getErrorStream cannot be open
     */
    private String fetchErrorContent(HttpURLConnection connection) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            return stringBuilder.toString();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

}
