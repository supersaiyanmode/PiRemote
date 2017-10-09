package com.srivatsaniyer.piremote;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.srivatsaniyer.piremote.messaging.MessageSender;
import com.srivatsaniyer.piremote.messaging.MessagingClient;
import com.srivatsaniyer.piremote.messaging.ServerSpecification;
import com.srivatsaniyer.piremote.structures.Device;
import com.srivatsaniyer.piremote.structures.DeviceCommand;

import java.io.IOException;
import java.util.List;

/**
 * Created by thrustmaster on 10/8/17.
 */

class SimpleListAdapter extends ArrayAdapter<DeviceCommand> {
    public SimpleListAdapter(Context context, List<DeviceCommand> commands) {
        super(context, android.R.layout.simple_list_item_1, commands.toArray(new DeviceCommand[0]));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        TextView textView = (TextView) rowView.findViewById(android.R.id.text1);
        textView.setText(getItem(position).getName());
        rowView.setTag(getItem(position));
        return rowView;
    }
}

public class SimpleDeviceController implements DeviceController {
    public SimpleDeviceController(ServerSpecification spec, Device device, ListView view,
                                  Context context) throws IOException {
        this.context = context;
        this.device = device;
        this.listView = view;
        this.commandSender = new MessageSender(device.getDeviceCommandsQueue(),
                                               new MessagingClient(spec));
    }

    @Override
    public void start() {
        this.listView.setAdapter(getAdapter());
    }

    @Override
    public void stop() {
        this.listView.setAdapter(null);
    }

    public ListAdapter getAdapter() {
        return new SimpleListAdapter(this.context, this.device.getDeviceCommands());
    }

    private final ListView listView;
    private final Device device;
    private final MessageSender commandSender;
    private final Context context;
}
