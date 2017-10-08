package com.srivatsaniyer.piremote.messaging;

import android.util.Log;

import com.google.gson.Gson;
import com.srivatsaniyer.piremote.messaging.exceptions.MessageParseException;
import com.srivatsaniyer.piremote.messaging.exceptions.InvalidMessageStructure;
import com.srivatsaniyer.piremote.messaging.exceptions.MessagingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.lang.reflect.Type;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * Created by sriiyer on 9/23/17.
 */


public class MessagingClient {
    public MessagingClient(ServerSpecification spec) throws IOException{
        socket = new Socket(spec.getHost(), spec.getPort());
        writer = new PrintWriter(socket.getOutputStream()) {
            @Override
            public void println() {
                write('\n');
            }
        };
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void writeMessage(Message msg) {
        msg.write(writer);
        writer.flush();
    }

    public <T> Message<T> readMessage(Type type) throws MessagingException {
        return Message.<T>read(reader, type);
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
