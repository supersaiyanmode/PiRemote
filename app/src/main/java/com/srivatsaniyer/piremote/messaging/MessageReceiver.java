package com.srivatsaniyer.piremote.messaging;

import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.srivatsaniyer.piremote.messaging.exceptions.MessagingException;

import java.io.IOException;
import java.lang.reflect.Type;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

/**
 * Created by thrustmaster on 9/30/17.
 */

public abstract class MessageReceiver<T> {
    public MessageReceiver(String queueName, MessagingClient client) {
        this.client = client;
        this.queueName = queueName;
        this.active = true;

        final MessageReceiver receiver = this;
        this.receiverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                receiver.run();
            }
        });
    }

    public void start() {
        this.receiverThread.start();
    }

    public void run() {
        Type type = new TypeToken<T>() {}.getType();
        while (this.active) {
            Message<Void> dequeueMsg = new Message<Void>(Operation.DEQUEUE);
            dequeueMsg.getHeaders().put("Q", this.queueName);
            client.writeMessage(dequeueMsg);
            try {
                this.onMessage(client.<T>readMessage(type));
            } catch (MessagingException e) {
                Log.w("MessageReceiver", "Got an exception.", e);
            }
        }
    }

    public abstract void onMessage(Message<T> msg);

    public void stop() {
        this.active = false;
        this.receiverThread.interrupt();
    }


    private final MessagingClient client;
    private final String queueName;
    private volatile boolean active;
    private final Thread receiverThread;
}
