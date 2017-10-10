package com.srivatsaniyer.piremote.messaging;

import com.google.gson.reflect.TypeToken;
import com.srivatsaniyer.piremote.messaging.exceptions.BadOperation;
import com.srivatsaniyer.piremote.messaging.exceptions.InvalidMessageStructure;
import com.srivatsaniyer.piremote.messaging.exceptions.MessagingException;
import com.srivatsaniyer.piremote.messaging.exceptions.RequiredFieldsMissing;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.Map;

class Command {
    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    private String command;
}

public class MessageTest {
    @Test
    public void testParseMessage() throws Exception {
        String msg =
                "OP enqueue\n" +
                "Q a.b.c\n" +
                "MSG {\"command\": \"POWER\"}\n" +
                "\n";
        BufferedReader reader = new BufferedReader(new StringReader(msg));
        Message<Command> obj = Message.<Command>read(reader, Command.class);
        Assert.assertEquals(obj.getOperation(), Operation.ENQUEUE);
        Assert.assertEquals(obj.getData().getCommand(), "POWER");
    }

    @Test(expected = InvalidMessageStructure.class)
    public void testBadMessage() throws IOException, MessagingException {
        String msg =
                "OP enqueue\n" +
                "Q a.b.c\n" +
                "MSG {\"command\": \"POWER\"}\n";
        BufferedReader reader = new BufferedReader(new StringReader(msg));
        Message.<Command>read(reader, Command.class);
    }

    @Test(expected = RequiredFieldsMissing.class)
    public void testWithoutRequiredHeaders() throws IOException, MessagingException {
        String msg =
                "Q enqueue\n" +
                "MSG {\"command\": \"POWER\"}\n" +
                "\n";
        BufferedReader reader = new BufferedReader(new StringReader(msg));
        Message.<Command>read(reader, Command.class);
    }

    @Test(expected = InvalidMessageStructure.class)
    public void testBadHeaderFormat() throws IOException, MessagingException {
        String msg =
                "OP-enqueue_\n" +
                "MSG {\"command\": \"POWER\"}\n" +
                "\n";
        BufferedReader reader = new BufferedReader(new StringReader(msg));
        Message.<Command>read(reader, Command.class);
    }

    @Test(expected = BadOperation.class)
    public void testBadOperation() throws IOException, MessagingException {
        String msg =
                "OP some-random-op\n" +
                "Q some.queue\n" +
                "MSG {\"command\": \"POWER\"}\n" +
                "\n";
        BufferedReader reader = new BufferedReader(new StringReader(msg));
        Message.<Command>read(reader, Command.class);
    }

    @Test
    public void testNoMessage() throws IOException, MessagingException {
        String msg =
                "OP dequeue\n" +
                "Q some.queue\n" +
                "\n";
        BufferedReader reader = new BufferedReader(new StringReader(msg));
        Message obj = Message.<Command>read(reader, Command.class);
        Assert.assertNull(obj.getData());
    }

    @Test
    public void testWriteMessage() throws IOException {
        Command cmd = new Command();
        cmd.setCommand("PWR");
        Message<Command> msg = new Message<Command>(Operation.ENQUEUE, cmd);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out) {
            @Override
            public void println() {
                write('\n');
            }
        };
        msg.write(writer);
        writer.flush();

        String expected =
                "OP enqueue\n" +
                "MSG {\"command\":\"PWR\"}\n" +
                "\n";
        Assert.assertEquals(expected, out.toString("UTF-8"));
    }

    @Test
    public void testWriteMessageNoMsgHeader() throws  IOException {
        Message<Void> msg = new Message<Void>(Operation.ENQUEUE);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out) {
            @Override
            public void println() {
                write('\n');
            }
        };
        msg.write(writer);
        writer.flush();

        String expected =
                "OP enqueue\n" +
                "\n";
        Assert.assertEquals(expected, out.toString("UTF-8"));
    }

    @Test
    public void testWriteMessageHeaders() throws IOException {
        Message<Void> msg = new Message<Void>(Operation.ENQUEUE);
        msg.getHeaders().put("a", "b");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out) {
            @Override
            public void println() {
                write('\n');
            }
        };
        msg.write(writer);
        writer.flush();

        String expected =
                "OP enqueue\n" +
                "a b\n" +
                "\n";
        Assert.assertEquals(expected, out.toString("UTF-8"));
    }

    @Test
    public void testAdditionalHeaders() throws IOException, MessagingException {
        String msg =
                "OP enqueue\n" +
                "Q a.b.c\n" +
                "X temp-a.b.c\n" +
                "MSG {\"command\": \"POWER\"}\n" +
                "\n";
        BufferedReader reader = new BufferedReader(new StringReader(msg));
        Message<Command> obj = Message.<Command>read(reader, Command.class);
        Assert.assertEquals(obj.getOperation(), Operation.ENQUEUE);
        Assert.assertEquals(obj.getData().getCommand(), "POWER");
        Assert.assertEquals(obj.getHeaders().size(), 2);
        Assert.assertEquals(obj.getHeaders().get("X"), "temp-a.b.c");
        Assert.assertEquals(obj.getHeaders().get("Q"), "a.b.c");
    }

    @Test
    public void testNestedGenericsParse() throws IOException, MessagingException {
        String msg =
                "OP enqueue\n" +
                "MSG {\"a\": {\"command\": \"POWER\"}}\n" +
                "\n";
        BufferedReader reader = new BufferedReader(new StringReader(msg));
        Type type = TypeToken.getParameterized(Map.class, String.class, Command.class).getType();
        Message<Map<String, Command>> obj = Message.<Map<String, Command>>read(reader, type);
        Assert.assertEquals(obj.getOperation(), Operation.ENQUEUE);
        Assert.assertEquals(obj.getData().get("a").getCommand(), "POWER");
    }
}