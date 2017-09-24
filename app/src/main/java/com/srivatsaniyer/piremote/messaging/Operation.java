package com.srivatsaniyer.piremote.messaging;

/**
 * Created by sriiyer on 9/23/17.
 */

enum Operation {
    ENQUEUE("enqueue"),
    DEQUEUE("dequeue"),
    INFORM("inform");

    public String serialize() {
        return this.tag;
    }

    public static Operation parse(String op) {
        if ("enqueue".equalsIgnoreCase(op)) {
            return Operation.ENQUEUE;
        } else if ("dequeue".equalsIgnoreCase(op)) {
            return Operation.DEQUEUE;
        } else if ("inform".equalsIgnoreCase(op)) {
            return Operation.INFORM;
        }
        return null;
    }

    Operation(String tag) {
        this.tag = tag;
    }

    private final String tag;
}
