package com.srivatsaniyer.piremote.structures;

import java.util.List;

/**
 * Created by thrustmaster on 9/30/17.
 */

public class DeviceCommand {
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private String id;
    private String name;
    private String type;
}
