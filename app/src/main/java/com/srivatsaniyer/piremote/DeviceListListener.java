package com.srivatsaniyer.piremote;

import com.srivatsaniyer.piremote.messaging.Message;
import com.srivatsaniyer.piremote.structures.Device;

import java.util.Map;

/**
 * Created by thrustmaster on 9/30/17.
 */

public interface DeviceListListener {
    public void onDeviceList(Map<String, Device> devices);
}
