package com.srivatsaniyer.piremote.messaging;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.google.gson.Gson;
import com.srivatsaniyer.piremote.messaging.exceptions.BadOperation;
import com.srivatsaniyer.piremote.messaging.exceptions.InvalidMessageStructure;
import com.srivatsaniyer.piremote.messaging.exceptions.MessagingException;
import com.srivatsaniyer.piremote.messaging.exceptions.RequiredFieldsMissing;
import com.srivatsaniyer.piremote.messaging.exceptions.SchemaValidationFailed;
import com.srivatsaniyer.piremote.messaging.exceptions.WaitTimeoutError;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.WIFI_SERVICE;

/**
 * Created by thrustmaster on 9/30/17.
 */

public class MessageUtils {
    public static <X> void throwExceptionFromMessage(Message<X> msg) throws MessagingException {
        String err = msg.getHeaders().get("RES");
        if ("OK".equalsIgnoreCase(err)) {
            return;
        }

        MessagingException[] arr = {
                new BadOperation(err), new InvalidMessageStructure(err),
                new RequiredFieldsMissing(err), new SchemaValidationFailed(err),
                new WaitTimeoutError(err)
        };
        Map<String, MessagingException> exceptionMap = new HashMap<>();
        for (MessagingException e: arr) {
            exceptionMap.put(e.errorKey(), e);
        }
        MessagingException curException = exceptionMap.get(err);
        if (curException == null) {
            throw new MessagingException(err);
        }
        throw curException;
    }

    public static <X> void ensureOk(Message<X> msg) throws MessagingException {
        if (!msg.getOperation().equals(Operation.RESULT) || !msg.getHeaders().containsKey("RES")) {
            throw new MessagingException("Invalid ACK message.");
        }
        throwExceptionFromMessage(msg);
    }

    public static ServerSpecification discover(Context context) {
        WifiManager wifiMgr = (WifiManager) context.getSystemService(WIFI_SERVICE);
        WifiManager.MulticastLock lock = wifiMgr.createMulticastLock("lock");

        byte[] msg = "QUERY".getBytes(Charset.forName("UTF-8"));
        try {
            InetAddress host = InetAddress.getByName("224.108.73.1");
            int port = 23034;
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);
            DatagramPacket packet = new DatagramPacket(msg, msg.length, host, port);
            socket.send(packet);

            lock.acquire();

            byte[] bytes = new byte[1024];
            DatagramPacket resp = new DatagramPacket(bytes, bytes.length);
            socket.setSoTimeout(10000);
            socket.receive(resp);
            String response = new String(resp.getData(), 0, resp.getLength(),
                                         Charset.forName("UTF-8"));
            ServerSpecification spec = new Gson().fromJson(response, ServerSpecification.class);
            return spec;
        } catch (IOException e) {
            Log.e("ServerDiscovery", "No clients found.", e);
        } finally {
            lock.release();
        }
        return null;
    }
}
