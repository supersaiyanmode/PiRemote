package com.srivatsaniyer.piremote.messaging;

import android.util.Log;

import com.google.gson.Gson;
import com.srivatsaniyer.piremote.messaging.exceptions.MessageParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * Created by sriiyer on 9/23/17.
 */

class ServerSpecification {
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    private String host;
    private int port;
}

public class MessagingClient {
    public MessagingClient(String host, int port) throws IOException{
        socket = new Socket(host, port);
        writer = new PrintWriter(socket.getOutputStream()) {
            @Override
            public void println() {
                write('\n');
            }
        };
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public static MessagingClient discover() {
        byte[] msg = "QUERY".getBytes(Charset.forName("UTF-8"));
        try {
            InetAddress host = InetAddress.getByName("224.108.73.1");
            int port = 23034;
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(msg, msg.length, host, port);
            socket.send(packet);

            byte[] bytes = new byte[1024];
            DatagramPacket resp = new DatagramPacket(bytes, bytes.length);
            socket.setSoTimeout(10000);
            socket.receive(resp);
            String response = new String(resp.getData(), 0, resp.getLength(),
                                         Charset.forName("UTF-8"));
            ServerSpecification spec = new Gson().fromJson(response, ServerSpecification.class);
            return new MessagingClient(spec.getHost(), spec.getPort());
        } catch (IOException e) {
            Log.e(TAG, "No clients found.", e);
        }
        return null;
    }

    public void writeMessage(Message msg) {
        msg.write(writer);
        writer.flush();
    }

    public <T> Message<T> readMessage(Class<T> clazz) throws IOException, MessageParseException {
        return Message.<T>read(reader, clazz);
    }

    public void close() throws IOException {
        writer.flush();
        socket.close();
    }

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    private static String TAG = "MessagingClient";
}
