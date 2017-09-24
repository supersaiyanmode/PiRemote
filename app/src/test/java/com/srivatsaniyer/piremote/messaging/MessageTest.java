package com.srivatsaniyer.piremote.messaging;

import com.srivatsaniyer.piremote.messaging.exceptions.MessageParseException;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;

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
        Assert.assertEquals(obj.getQueue(), "a.b.c");
        Assert.assertEquals(obj.getOperation(), Operation.ENQUEUE);
        Assert.assertEquals(obj.getData().getCommand(), "POWER");
    }

    @Test(expected = MessageParseException.class)
    public void testBadMessage() throws IOException, MessageParseException {
        String msg =
                "OP enqueue\n" +
                "Q a.b.c\n" +
                "MSG {\"command\": \"POWER\"}\n";
        BufferedReader reader = new BufferedReader(new StringReader(msg));
        Message.<Command>read(reader, Command.class);
    }

    @Test(expected = MessageParseException.class)
    public void testWithoutRequiredHeaders() throws IOException, MessageParseException {
        String msg =
                "OP enqueue\n" +
                "MSG {\"command\": \"POWER\"}\n" +
                "\n";
        BufferedReader reader = new BufferedReader(new StringReader(msg));
        Message.<Command>read(reader, Command.class);
    }

    @Test(expected = MessageParseException.class)
    public void testBadHeaderFormat() throws IOException, MessageParseException {
        String msg =
                "OP-enqueue_\n" +
                "MSG {\"command\": \"POWER\"}\n" +
                "\n";
        BufferedReader reader = new BufferedReader(new StringReader(msg));
        Message.<Command>read(reader, Command.class);
    }

    @Test(expected = MessageParseException.class)
    public void testBadOperation() throws IOException, MessageParseException {
        String msg =
                "OP some-random-op\n" +
                "Q some.queue\n" +
                "MSG {\"command\": \"POWER\"}\n" +
                "\n";
        BufferedReader reader = new BufferedReader(new StringReader(msg));
        Message.<Command>read(reader, Command.class);
    }

    @Test
    public void testNoMessage() throws IOException, MessageParseException {
        String msg =
                "OP dequeue\n" +
                "Q some.queue\n" +
                "\n";
        BufferedReader reader = new BufferedReader(new StringReader(msg));
        Message obj = Message.<Command>read(reader, Command.class);
        Assert.assertNull(obj.getData());
    }

    @Test
    public void testWriteMessage() throws  IOException {
        Command cmd = new Command();
        cmd.setCommand("PWR");
        Message<Command> msg = new Message<Command>("some.queue", Operation.ENQUEUE, cmd);

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
                "Q some.queue\n" +
                "MSG {\"command\":\"PWR\"}\n" +
                "\n";
        Assert.assertEquals(expected, out.toString("UTF-8"));
    }
}