package com.srivatsaniyer.piremote.messaging;

import com.srivatsaniyer.piremote.messaging.exceptions.InvalidMessageStructure;
import com.srivatsaniyer.piremote.messaging.exceptions.MessagingException;
import com.srivatsaniyer.piremote.messaging.exceptions.SchemaValidationFailed;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

/**
 * Created by thrustmaster on 9/30/17.
 */

public class MessageUtilsTest {
    @Test(expected = MessagingException.class)
    public void testEnsureOkNotResult() throws IOException, MessagingException {
        String msg =
                "OP enqueue\n" +
                "Q a.b.c\n" +
                "\n";
        BufferedReader reader = new BufferedReader(new StringReader(msg));
        Message<Command> obj = Message.<Command>read(reader, Command.class);
        MessageUtils.ensureOk(obj);
    }

    @Test(expected = MessagingException.class)
    public void testEnsureOkNoResHeader() throws IOException, MessagingException {
        String msg =
                "OP result\n" +
                "Q a.b.c\n" +
                    "\n";
        BufferedReader reader = new BufferedReader(new StringReader(msg));
        Message<Command> obj = Message.<Command>read(reader, Command.class);
        MessageUtils.ensureOk(obj);
    }

    @Test
    public void testEnsureOk() throws MessagingException, IOException {
        String msg =
                "OP result\n" +
                "RES ok\n" +
                "\n";
        BufferedReader reader = new BufferedReader(new StringReader(msg));
        Message<Command> obj = Message.<Command>read(reader, Command.class);
        MessageUtils.ensureOk(obj);
    }

    @Test(expected = SchemaValidationFailed.class)
    public void testException() throws MessagingException, IOException {
        String msg =
                "OP result\n" +
                "RES SchemaValidationFailed\n" +
                "\n";
        BufferedReader reader = new BufferedReader(new StringReader(msg));
        Message<Command> obj = Message.<Command>read(reader, Command.class);
        MessageUtils.ensureOk(obj);
    }

    @Test(expected = MessagingException.class)
    public void testUnknownException() throws MessagingException, IOException {
        String msg =
                "OP result\n" +
                "RES SomeRandomMessage\n" +
                "\n";
        BufferedReader reader = new BufferedReader(new StringReader(msg));
        Message<Command> obj = Message.<Command>read(reader, Command.class);
        MessageUtils.ensureOk(obj);
    }
}
