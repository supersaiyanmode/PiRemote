package com.srivatsaniyer.piremote;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.SubMenu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.srivatsaniyer.piremote.messaging.MessageUtils;
import com.srivatsaniyer.piremote.messaging.ServerSpecification;
import com.srivatsaniyer.piremote.structures.Device;

import java.io.IOException;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements MenuItem.OnMenuItemClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        this.listView = (ListView) findViewById(R.id.mainListView);

        this.navigationView = (NavigationView) findViewById(R.id.nav_view);
    }

    @Override
    protected void onResume() {
        super.onResume();
        discoverMessageServer();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void discoverMessageServer() {
        new AsyncTask<Void, Void, ServerSpecification>() {
            @Override
            protected ServerSpecification doInBackground(Void... voids) {
                ServerSpecification spec = MessageUtils.discover(MainActivity.this);
                if (spec == null) {
                    return null;
                }
                try {
                    setupDeviceLister(spec);
                } catch (IOException e) {
                    Log.e("MainActivity", "Error while querying for devices", e);
                    showError("Unable to connect to server.");
                }
                return spec;
            }

            @Override
            protected void onPostExecute(ServerSpecification client) {
                if (client == null) {
                    Log.w("MainActivity", "Server not found.");
                    showError("Server not found.");
                    return;
                }
                serverSpec = client;
            }
        }.execute();
    }

    private void setupDeviceLister(final ServerSpecification spec) throws IOException {
        Log.i("MainActivity", "Setup device lister.");
        final DeviceListListener listener = new DeviceListListener() {
            @Override
            public void onDeviceList(final Map<String, Device> devices) {
                Log.i("MainActivity", "Got devices list:" + devices);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       populateDevices(spec, devices);
                    }
                });
            }
        };
        this.devicesLister = new DevicesLister(spec, listener);
        this.devicesLister.start();
    }

    private void populateDevices(final ServerSpecification spec, final Map<String, Device> devices) {
        final MainActivity activity = this;
        final NavigationView navigation = this.navigationView;

        this.devices = devices;
        Menu menu = navigation.getMenu();
        menu.clear();
        SubMenu devicesMenu = menu.addSubMenu("Devices");
        for (final Map.Entry<String, Device> device: devices.entrySet()) {
            Log.i("MainActivity", "device: " + device.getValue().getClass());
            MenuItem item = devicesMenu.add(device.getValue().getDeviceId());
            item.setTitle(device.getValue().getDeviceId());
            item.setOnMenuItemClickListener(activity);
        }
        navigation.invalidate();
    }

    @Override
    public boolean onMenuItemClick(final MenuItem menuItem) {
        if (currentController != null) {
            currentController.stop();
        }
        final MainActivity activity = this;
        new AsyncTask<Void, Void, DeviceController>() {

            @Override
            protected DeviceController doInBackground(Void... voids) {
                Log.i("MainActivity", "" + devices);
                Log.i("MainActivity", "" + menuItem.getTitle());
                try {
                    return new SimpleDeviceController(serverSpec, devices.get(menuItem.getTitle()),
                                                      activity.listView, activity);
                } catch (IOException e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(DeviceController obj) {
                drawer.closeDrawer(GravityCompat.START);
                if (obj == null) {
                    showError("Unable to connect to device.");
                    return;
                }
                obj.start();
                activity.currentController = obj;
            }
        }.execute();

        return true;
    }
    private void showError(final String msg) {
        View view = findViewById(android.R.id.content);
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    private DrawerLayout drawer;
    private NavigationView navigationView;
    private ListView listView;
    private ServerSpecification serverSpec;
    private DevicesLister devicesLister;
    private Map<String, Device> devices;
    private DeviceController currentController;
}
