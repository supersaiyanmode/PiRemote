package com.srivatsaniyer.piremote;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.srivatsaniyer.piremote.messaging.MessageSender;
import com.srivatsaniyer.piremote.messaging.MessagingClient;
import com.srivatsaniyer.piremote.messaging.ServerSpecification;
import com.srivatsaniyer.piremote.messaging.exceptions.MessagingException;
import com.srivatsaniyer.piremote.structures.Device;
import com.srivatsaniyer.piremote.structures.DeviceCommand;
import com.srivatsaniyer.piremote.structures.ExecuteDeviceCommand;

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

public class SimpleDeviceController implements DeviceController, AdapterView.OnItemClickListener {
    public SimpleDeviceController(ServerSpecification spec, Device device, ListView view,
                                  Activity activity) throws IOException {
        this.activity = activity;
        this.device = device;
        this.listView = view;
        this.commandSender = new MessageSender(device.getDeviceCommandsQueue(),
                                               new MessagingClient(spec));
    }

    @Override
    public void start() {
        this.listView.setAdapter(getAdapter());
        this.listView.setOnItemClickListener(this);
    }

    @Override
    public void stop() {
        this.listView.setAdapter(null);
        this.listView.setOnItemClickListener(null);
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Log.i("SimpleDeviceController", "Item clicked.");
        DeviceCommand command = (DeviceCommand) view.getTag();
        final ExecuteDeviceCommand executeCommand = new ExecuteDeviceCommand();
        executeCommand.setCommandId(command.getId());
        executeCommand.setDeviceId(device.getDeviceId());
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    commandSender.<ExecuteDeviceCommand>send(executeCommand);
                    return true;
                } catch (IOException e) {
                    return false;
                } catch (MessagingException e) {
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (!result) {
                    View view = activity.findViewById(android.R.id.content);
                    Snackbar.make(view, "Failed to send command", Snackbar.LENGTH_LONG)
                            .setAction("Action", null)
                            .show();
                }
            }
        }.execute();
    }

    public ListAdapter getAdapter() {
        return new SimpleListAdapter(this.activity, this.device.getDeviceCommands());
    }

    private final ListView listView;
    private final Device device;
    private final MessageSender commandSender;
    private final Activity activity;
}
