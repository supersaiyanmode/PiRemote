package com.srivatsaniyer.piremote.messaging;

import com.google.gson.Gson;
import com.srivatsaniyer.piremote.messaging.exceptions.MessageParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by sriiyer on 9/23/17.
 */

class Message<T> {
    public Message(String queue, Operation op) {
        this(queue, op, null);
    }

    public Message(String queue, Operation op, T data) {
        this.queue = queue;
        this.operation = op;
        this.data = data;
    }

    public void write(PrintWriter writer) {
        writer.println("OP " + this.operation.serialize());
        writer.println("Q " + this.queue);
        writer.println("MSG " + new Gson().toJson(data));
        writer.println("");
    }

    public static <X> Message<X> read(BufferedReader reader, Class<X> clazz) throws IOException, MessageParseException {
        List<String> lines = new ArrayList<String>();
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                throw new MessageParseException("Blank line expected.");
            }
            if (line.isEmpty()) {
                // We are done reading the message. break out of loop.
                break;
            }
            lines.add(line);
        }

        Set<String> required_fields = new HashSet<String>() {{
            add("OP");
            add("Q");
        }};
        Map<String, String> fields = new HashMap<>();

        for (String line: lines) {
            String[] parts = line.split(" ", 2);
            if (parts.length != 2) {
                throw new MessageParseException("Bad headers.");
            }
            fields.put(parts[0].trim(), parts[1].trim());
        }

        required_fields.removeAll(fields.keySet());
        if (!required_fields.isEmpty()) {
            System.out.println(required_fields);
            throw new MessageParseException("Required fields missing.");
        }

        String data = fields.get("MSG");
        Operation op = Operation.parse(fields.get("OP"));
        if (op == null) {
            throw new MessageParseException("Bad Operation");
        }
        if (data != null) {
            X obj = new Gson().fromJson(data, clazz);
            return new Message<X>(fields.get("Q"), op, obj);
        } else {
            return new Message<X>(fields.get("Q"), op);
        }
    }

    public String getQueue() {
        return queue;
    }

    public Operation getOperation() {
        return operation;
    }

    public T getData() {
        return data;
    }

    private String queue;
    private Operation operation;
    private T data;

}
