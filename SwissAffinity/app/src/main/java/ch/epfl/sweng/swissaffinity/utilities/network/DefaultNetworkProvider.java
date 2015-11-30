package ch.epfl.sweng.swissaffinity.utilities.network;


import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;

/**
 * The class that get the HTTP connection from the server URL.
 */
public class DefaultNetworkProvider implements NetworkProvider {

    @Override
    public HttpURLConnection getConnection(String serverURL) throws IOException {
        URL url = new URL(serverURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        return conn;
    }

    @Override
    public String getContent(String serverURL) throws IOException {
        HttpURLConnection conn = getConnection(serverURL);
        conn.setDoInput(true);
        conn.setRequestMethod("GET");
        conn.connect();
        if (conn.getResponseCode() != HTTP_OK) {
            throw new ConnectException();
        }
        return fetchContent(conn.getInputStream());
    }

    @Override
    public String postContent(String serverURL, JSONObject json) throws IOException {
        HttpURLConnection conn = getConnection(serverURL);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.connect();

        OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
        out.write(json.toString());
        out.flush();
        out.close();

        InputStream stream;
        switch (conn.getResponseCode()) {
            case HTTP_OK:
            case HTTP_NO_CONTENT:
                stream = conn.getInputStream();
                break;
            case HTTP_BAD_REQUEST:
                stream = conn.getErrorStream();
                break;
            default:
                throw new ConnectException();
        }
        return fetchContent(stream);
    }

    @Override
    public int deleteContent(String serverURL) throws IOException {
        HttpURLConnection conn = getConnection(serverURL);
        conn.setDoOutput(true);
        conn.setRequestMethod("DELETE");
        conn.connect();
        return conn.getResponseCode();
    }

    /**
     * Fetch the content of the given input stream
     */
    private String fetchContent(InputStream stream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(stream));
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
