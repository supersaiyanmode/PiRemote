package com.srivatsaniyer.piremote;

import com.srivatsaniyer.piremote.messaging.Message;
import com.srivatsaniyer.piremote.messaging.Operation;
import com.srivatsaniyer.piremote.messaging.exceptions.MessagingException;
import com.srivatsaniyer.piremote.structures.Device;
import com.srivatsaniyer.piremote.structures.DeviceCommand;
import com.srivatsaniyer.piremote.utils.DummyMessageServer;

import junit.framework.Assert;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by thrustmaster on 9/30/17.
 */

public class DeviceListerTest {
    @Test
    public void testDeviceListingParse() {
        String json =
                "{\"abc\":" +
                " {" +
                        "\"device_id\": \"abc\"," +
                        "\"device_commands_queue\": \"x\", " +
                        "\"device_commands\": [" +
                                "{\"name\": \"a\", \"id\": \"a1\", \"type\": \"click\"}" +
                         "]" +
                " }" +
                "}";
        Map<String, Device> devices = DevicesLister.parseDeviceListing(json);
        Assert.assertEquals(devices.size(), 1);
        Assert.assertNotNull(devices.get("abc"));
        Assert.assertEquals(devices.get("abc").getDeviceId(), "abc");
        Assert.assertEquals(devices.get("abc").getDeviceCommandsQueue(), "x");
        Assert.assertEquals(devices.get("abc").getDeviceCommands().size(), 1);
        Assert.assertEquals(devices.get("abc").getDeviceCommands().get(0).getName(), "a");
        Assert.assertEquals(devices.get("abc").getDeviceCommands().get(0).getId(), "a1");
        Assert.assertEquals(devices.get("abc").getDeviceCommands().get(0).getType(), "click");
    }

    @Test
    public void testListing() throws IOException, InterruptedException {
        final DummyMessageServer server = new DummyMessageServer() {
            @Override
            public void process(BufferedReader br, PrintWriter writer) throws MessagingException {
                Message<Void> msg = Message.read(br, Void.TYPE);
                Map<String, Device> map = new HashMap<String, Device>() {{
                    Device device = new Device();
                    device.setDeviceId("a");
                    device.setDeviceCommandsQueue("queue");
                    device.setDeviceCommands(new ArrayList<DeviceCommand>());
                    device.getDeviceCommands().add(new DeviceCommand());
                    device.getDeviceCommands().get(0).setId("id");
                    device.getDeviceCommands().get(0).setName("name");
                    device.getDeviceCommands().get(0).setType("click");
                    put("a", device);
                }};
                new Message<Map<String, Device>>(Operation.INFORM, map).write(writer);
            }
        };
        server.start();
        final Thread thread = Thread.currentThread();
        final DevicesLister lister = new DevicesLister("localhost", new DeviceListListener() {
            @Override
            public void onMessage(Message<Map<String, Device>> msg) {
                Assert.assertEquals(msg.getData().size(), 1);
                Assert.assertNotNull(msg.getData().get("a"));
                Assert.assertEquals(msg.getData().get("a").getDeviceId(), "a");
                Assert.assertEquals(msg.getData().get("a").getDeviceCommandsQueue(), "queue");
                Assert.assertEquals(msg.getData().get("a").getDeviceCommands().get(0).getId(),
                        "id");
                Assert.assertEquals(msg.getData().get("a").getDeviceCommands().get(0).getName(),
                        "name");
                Assert.assertEquals(msg.getData().get("a").getDeviceCommands().get(0).getType(),
                        "click");
                thread.notify();
            }
        });
        lister.start();
        thread.wait();
        lister.stop();
    }
}
