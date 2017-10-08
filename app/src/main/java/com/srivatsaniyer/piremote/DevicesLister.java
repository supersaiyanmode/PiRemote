package com.srivatsaniyer.piremote;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.srivatsaniyer.piremote.messaging.MessageReceiver;
import com.srivatsaniyer.piremote.messaging.Message;
import com.srivatsaniyer.piremote.messaging.MessagingClient;
import com.srivatsaniyer.piremote.structures.Device;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by thrustmaster on 9/30/17.
 */

public class DevicesLister {
    public DevicesLister(String host, final DeviceListListener listener) throws IOException {
        MessagingClient client = new MessagingClient(host);
        this.receiver = new MessageReceiver<Map<String, Device>>("/devices", client) {
            @Override
            public void onMessage(Message<Map<String, Device>> msg) {
                listener.onMessage(msg);
            }
        };
    }

    void start() {
        this.receiver.start();
    }

    void stop() {
        this.receiver.stop();
    }
    protected static Map<String, Device> parseDeviceListing(String json) {
        Type type = new TypeToken<Map<String, Device>>(){}.getType();
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
        return gson.fromJson(json, type);
    }

    private final MessageReceiver<Map<String, Device>> receiver;
}
