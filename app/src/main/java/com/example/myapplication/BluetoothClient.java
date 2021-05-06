package com.example.myapplication;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Handler;
/**
 * BluetoothClient
 *
 * @author Alexandre Louis <Alexandre_Louis@outlook.fr>
 *
 * 08/12/2020
 */
public class BluetoothClient extends Thread{

    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private boolean isAlive = true;
    private Handler myHandler;

    public BluetoothClient( BluetoothDevice device ) {
        try {
            bluetoothDevice = device;
            bluetoothSocket = device.createRfcommSocketToServiceRecord( device.getUuids()[0].getUuid() );
            bluetoothSocket.connect();

            inputStream = bluetoothSocket.getInputStream();
            outputStream = bluetoothSocket.getOutputStream();

        } catch ( IOException exception ) {
            Log.e( "DEBUG", "Cannot establish connection", exception );
        }
    }


    // Inutile dans le code actuel. Mais cela permettrait de recevoir
    // des informations du véhicule dans une future version.
    @Override
    public void run() {
        try {
            while (isAlive) {
                if ( inputStream.available() > 0 ) {
                    Log.i("DEBUG", String.format("%c", inputStream.read()));
                } else {
                    Thread.sleep( 100 );
                }
            }
        } catch( Exception exception ) {
            Log.e( "DEBUG", "Cannot read data", exception );
            close();
        }
    }

    //38400
    public void writeChar(String code) {
        try {
            byte[] send = code.getBytes();
            outputStream.write(send);
            System.out.println(send);
            String readMessage = new String(send, 0,6);
            System.out.println(readMessage);
            Thread.sleep( 0);
            //outputStream.flush();
        } catch (IOException | InterruptedException e) {
            Log.e( "DEBUG", "Cannot write message", e );
        }
    }

    // Termine la connexion en cours et tue le thread
    public void close() {
        try {
            bluetoothSocket.close();
            isAlive = false;
        } catch (IOException e) {
            Log.e( "DEBUG", "Cannot close socket", e );
        }
    }
}