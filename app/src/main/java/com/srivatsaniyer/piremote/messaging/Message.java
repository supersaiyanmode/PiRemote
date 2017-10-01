package com.srivatsaniyer.piremote.messaging;

import com.google.gson.Gson;
import com.srivatsaniyer.piremote.messaging.exceptions.InvalidMessageStructure;
import com.srivatsaniyer.piremote.messaging.exceptions.MessagingException;
import com.srivatsaniyer.piremote.messaging.exceptions.RequiredFieldsMissing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by sriiyer on 9/23/17.
 */

public class Message<T> {

    public Message(Operation op) {
        this(op, null);
    }

    public Message(Operation op, T data) {
        this.operation = op;
        this.data = data;
        this.headers = new HashMap<String, String>();
    }

    public void write(PrintWriter writer) {
        writer.println("OP " + this.operation.serialize());
        if (data != null) {
            writer.println("MSG " + new Gson().toJson(data));
        }
        for (Map.Entry<String, String> entry: headers.entrySet()) {
            writer.println(entry.getKey() + " " + entry.getValue());
        }
        writer.println("");
    }

    public static <X> Message<X> read(BufferedReader reader, Type type)
            throws MessagingException {
        List<String> lines = new ArrayList<String>();
        while (true) {
            final String line;
            try {
                line = reader.readLine();
            } catch (IOException e) {
                throw new InvalidMessageStructure("IOException occured.");
            }

            if (line == null) {
                throw new InvalidMessageStructure("Blank line expected.");
            }
            if (line.isEmpty()) {
                // We are done reading the message. break out of loop.
                break;
            }
            lines.add(line);
        }

        Set<String> required_fields = new HashSet<String>() {{
            add("OP");
        }};
        Map<String, String> fields = new HashMap<>();

        for (String line: lines) {
            String[] parts = line.split(" ", 2);
            if (parts.length != 2) {
                throw new InvalidMessageStructure("Bad headers.");
            }
            fields.put(parts[0].trim(), parts[1].trim());
        }

        required_fields.removeAll(fields.keySet());
        if (!required_fields.isEmpty()) {
            System.out.println(required_fields);
            throw new RequiredFieldsMissing("Required fields missing.");
        }

        String data = fields.get("MSG");
        Operation op = Operation.parse(fields.get("OP"));
        fields.remove("OP");

        if (data != null) {
            fields.remove("MSG");
            X obj = new Gson().fromJson(data, type);
            Message<X> msg = new Message<X>(op, obj);
            msg.getHeaders().putAll(fields);
            return msg;
        } else {
            Message<X> msg = new Message<X>(op);
            msg.getHeaders().putAll(fields);
            return msg;
        }
    }

    public Operation getOperation() {
        return operation;
    }

    public T getData() {
        return data;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }


    private final Operation operation;
    private final T data;


    private final HashMap<String, String> headers;
}
