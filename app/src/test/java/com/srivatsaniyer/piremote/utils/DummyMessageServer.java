package com.srivatsaniyer.piremote.utils;

import com.srivatsaniyer.piremote.messaging.Message;
import com.srivatsaniyer.piremote.messaging.exceptions.MessagingException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by thrustmaster on 9/30/17.
 */

public abstract class DummyMessageServer extends Thread {
    public DummyMessageServer() throws IOException {
        serverSocket = new ServerSocket(PORT);
    }

    public void run() {
        try {
            Socket client = serverSocket.accept();
            BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter writer = new PrintWriter(client.getOutputStream());
            process(br, writer);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public abstract void process(BufferedReader br, PrintWriter writer) throws MessagingException;

    private final static int PORT = 11023;
    private final ServerSocket serverSocket;
}
