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
import java.net.MulticastSocket;
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

}
