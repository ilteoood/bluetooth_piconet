package ilteoood.bluetoothpiconet;

import static xdroid.toaster.Toaster.toast;
import android.bluetooth.BluetoothSocket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BluetoothConnection implements Runnable
{
    public final String devName;
    private DataInputStream dis;
    private DataOutputStream dos;

    public BluetoothConnection(final BluetoothSocket bluetoothSocket)
    {
        devName = bluetoothSocket.getRemoteDevice().getName();
        try
        {
            dis = new DataInputStream(bluetoothSocket.getInputStream());
            dos = new DataOutputStream(bluetoothSocket.getOutputStream());
        }
        catch(IOException e){}
    }
    @Override
    public void run()
    {
        String msg;
        try
        {
            while ((msg = dis.readUTF()) != "")
            {
                toast("Received: " + msg);
            }
        }
        catch (IOException e){}
        toast("Device disconnected: " + devName);
    }

    public void sendMessage(final String msg)
    {
        try
        {
            dos.writeUTF(msg);
            dos.flush();
        }
        catch (IOException e){}
    }

}
