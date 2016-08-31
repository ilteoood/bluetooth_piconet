package ilteoood.bluetoothpiconet;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import static xdroid.toaster.Toaster.toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button broadcast = (Button) findViewById(R.id.broadcast);
        final Button discover = (Button) findViewById(R.id.discover);
        final Button send = (Button) findViewById(R.id.send);
        final EditText message = (EditText) findViewById(R.id.message);
        final Spinner clientSpinner = (Spinner) findViewById(R.id.clientSpinner);
        final List<BluetoothDevice> devices = new ArrayList<>();
        final Handler updateHandler = new Handler() {

            @Override
            public void handleMessage(final Message message) {
                Bundle b = message.getData();
                ArrayAdapter<String> adapter = new ArrayAdapter(MainActivity.this,
                        android.R.layout.simple_spinner_item, b.getStringArrayList("devices"));
                clientSpinner.setAdapter(adapter);
            }
        };
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        final BluetoothManager manager = new BluetoothManager(updateHandler, adapter);

        discover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (!adapter.isDiscovering()) {
                    devices.clear();
                    adapter.startDiscovery();
                    toast("Starting device discovery...");
                } else
                    toast("Discovering already started!");
            }
        });

        BroadcastReceiver deviceReceiver = new BroadcastReceiver() {
            public void onReceive(final Context context, final Intent intent) {
                switch (intent.getAction().toString()) {
                    case BluetoothDevice.ACTION_FOUND:
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        devices.add(device);
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        toast("Discovery finished!");
                        manager.start(devices);
                        break;
                }
            }
        };

        registerReceiver(deviceReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(deviceReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

        broadcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                manager.broadcast(message.getText().toString());
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                manager.sendMsg(clientSpinner.getSelectedItemPosition(), message.getText().toString());
            }
        });

    }
}
