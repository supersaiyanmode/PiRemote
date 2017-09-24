package com.srivatsaniyer.piremote.messaging;

import android.util.Log;

import com.srivatsaniyer.piremote.messaging.exceptions.MessageParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by sriiyer on 9/23/17.
 */

public class MessagingClient {
    public MessagingClient(String host) throws IOException{
        socket = new Socket(host, PORT);
        writer = new PrintWriter(socket.getOutputStream()) {
            @Override
            public void println() {
                write('\n');
            }
        };
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public static MessagingClient discover() {
        try {
            return new MessagingClient("localhost");
        } catch (IOException e) {
            Log.i(TAG, "No clients found.");
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

    private static int PORT = 11023;
    private static String TAG = "MessagingClient";
}
