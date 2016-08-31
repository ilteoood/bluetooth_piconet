package ilteoood.bluetoothpiconet;

import static xdroid.toaster.Toaster.toast;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothManager {

    private final BluetoothAdapter adapter;
    private final ArrayList<BluetoothConnection> openSocket = new ArrayList<>();
    private final ArrayList<UUID> uuidList = new ArrayList<>();
    private final ArrayList<String> connectedDevice = new ArrayList<>();
    private final Handler updateHandler;

    public BluetoothManager(final Handler updateHandler, final BluetoothAdapter adapter)
    {
        adapter.enable();
        this.adapter = adapter;
        this.updateHandler = updateHandler;
        uuidList.add(UUID.fromString("fe964a9c-184c-11e6-b6ba-3e1d05defe78"));
        uuidList.add(UUID.fromString("fe964e02-184c-11e6-b6ba-3e1d05defe78"));
        uuidList.add(UUID.fromString("fe964f9c-184c-11e6-b6ba-3e1d05defe78"));
        uuidList.add(UUID.fromString("fe965438-184c-11e6-b6ba-3e1d05defe78"));
        uuidList.add(UUID.fromString("fe9655be-184c-11e6-b6ba-3e1d05defe78"));
        uuidList.add(UUID.fromString("fe965708-184c-11e6-b6ba-3e1d05defe78"));
        uuidList.add(UUID.fromString("fe965848-184c-11e6-b6ba-3e1d05defe78"));
        new Thread(new BluetoothOpen()).start();
    }

    public void start(final List<BluetoothDevice> devices)
    {
        new Thread(new BluetoothStart(devices)).start();
    }

    public void broadcast(final String msg)
    {
        for(BluetoothConnection connection: openSocket)
            connection.sendMessage(msg);
    }

    public void sendMsg(final int receiver, final String msg)
    {
        if(receiver != -1)
            openSocket.get(receiver).sendMessage(msg);
        else
            toast("You must select a receiver");
    }

    private class BluetoothOpen implements Runnable
    {
        private int slave_number;
        private BluetoothConnection bluetoothConnection;
        private BluetoothServerSocket serverSocket;
        private BluetoothSocket bluetoothSocket;

        @Override
        public void run()
        {
            for (slave_number = 0; slave_number < uuidList.size(); slave_number++) {
                try
                {
                    serverSocket = adapter.listenUsingInsecureRfcommWithServiceRecord("BLUETOOTH_PICONET", uuidList.get(slave_number));
                    bluetoothSocket = serverSocket.accept();
                    toast("Connected to " + bluetoothSocket.getRemoteDevice().getName());
                    serverSocket.close();
                    bluetoothConnection = new BluetoothConnection(bluetoothSocket);
                    new Thread(bluetoothConnection).start();
                } catch (IOException e) {}
            }
        }
    }
    private class BluetoothStart implements Runnable
    {
        private BluetoothSocket tempSocket;
        private BluetoothConnection connection;
        private ArrayList<UUID> uuidAvailable = new ArrayList<>(uuidList);
        private final List<BluetoothDevice> deviceList;

        public BluetoothStart(List<BluetoothDevice> devices)
        {
            deviceList = devices;
        }

        @Override
        public void run()
        {
            toast("Connecting to the devices...");
            connectToDevice();
            toast("Finished!");
        }

        private void connectToDevice()
        {
            for(BluetoothDevice device: deviceList)
                if (!connectedDevice.contains(device.getName()) && isValidDevice(device.getBluetoothClass().getDeviceClass()))
                    for (int uuidIndex = 0; uuidIndex < uuidAvailable.size(); uuidIndex++) {
                        try {
                            tempSocket = device.createInsecureRfcommSocketToServiceRecord(uuidAvailable.get(uuidIndex));
                            tempSocket.connect();
                            connection = new BluetoothConnection(tempSocket);
                            openSocket.add(connection);
                            connectedDevice.add(device.getName());
                            new Thread(connection).start();
                            uuidAvailable.remove(uuidIndex);
                            break;
                        } catch (IOException e) {
                        }
            }
            sendDevicesList();
        }

        private boolean isValidDevice(final int deviceClass)
        {
            return deviceClass == BluetoothClass.Device.PHONE_SMART || deviceClass == BluetoothClass.Device.COMPUTER_PALM_SIZE_PC_PDA;
        }

        private void sendDevicesList()
        {
            Bundle bundle = new Bundle();
            Message message = new Message();
            bundle.putStringArrayList("devices", connectedDevice);
            message.setData(bundle);
            updateHandler.sendMessage(message);
        }

    }

}
