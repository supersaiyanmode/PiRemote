package com.srivatsaniyer.piremote;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.srivatsaniyer.piremote.messaging.MessagingClient;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void discoverMessageServer() {
        new AsyncTask<Void, Void, ServerSpecification>() {

            @Override
            protected ServerSpecification doInBackground(Void... voids) {
                ServerSpecification spec = MessageUtils.discover();
                if (spec == null) {
                    return null;
                }
                try {
                    setupDeviceLister(spec);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return spec;
            }

            @Override
            protected void onPostExecute(ServerSpecification client) {
                if (client == null) {
                    Log.w("MainActivity", "Server not found.");
                    Toast.makeText(MainActivity.this, "No clients found.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }.execute();
    }

    private void setupDeviceLister(ServerSpecification spec) throws IOException {
        Log.i("MainActivity", "Setup device lister.");
        final NavigationView navigation = this.navigationView;
        final DeviceListListener listener = new DeviceListListener() {
            @Override
            public void onDeviceList(final Map<String, Device> devices) {
                Log.i("MainActivity", "Got devices list:" + devices);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Menu menu = navigation.getMenu();
                        menu.clear();
                        SubMenu devicesMenu = menu.addSubMenu("Devices");
                        for (Map.Entry<String, Device> device: devices.entrySet()) {
                            Log.i("MainActivity", "device: " + device.getValue().getClass());
                            MenuItem item = devicesMenu.add(device.getValue().getDeviceId());
                            item.setTitle(device.getValue().getDeviceId());
                            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem menuItem) {
                                    Toast.makeText(MainActivity.this, menuItem.getTitle(), Toast.LENGTH_SHORT).show();
                                    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                                    drawer.closeDrawer(GravityCompat.START);
                                    return true;
                                }
                            });
                        }
                        navigation.invalidate();
                    }
                });
            }
        };
        this.devicesLister = new DevicesLister(spec, listener);
        this.devicesLister.start();
    }

    private NavigationView navigationView;

    private ServerSpecification serverSpec;
    private DevicesLister devicesLister;
}
