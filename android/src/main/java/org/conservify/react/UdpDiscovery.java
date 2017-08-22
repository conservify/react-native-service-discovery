package org.conservify.react;

import android.content.Context;
import android.util.Log;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.IOException;
import java.net.*;

public class UdpDiscovery implements Runnable {
    private static final String LOG_TAG = "NSD_UDP";
    private static final int PORT = 12344;

    private final ReactApplicationContext reactContext;
    private final Context applicationContext;

    private boolean running;
    private Thread thread;

    public UdpDiscovery(ReactApplicationContext reactContext) {
        this.reactContext = reactContext;
        this.applicationContext = reactContext.getApplicationContext();
    }

    public void start() {
        Log.i(LOG_TAG, "Starting...");

        this.running = true;
        this.thread = new Thread(this);
        this.thread.start();
    }

    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket(PORT, InetAddress.getByName("0.0.0.0"));
            socket.setSoTimeout(2000);
            socket.setBroadcast(true);

            DatagramPacket packet = new DatagramPacket(new byte[1], 1);

            Log.i(LOG_TAG, String.format("Listening on %d...", PORT));

            while (running) {
                try {
                    socket.receive(packet);

                    Log.i(LOG_TAG, "Received: " + packet.getAddress() + ":" + packet.getPort());

                    WritableMap eventParams = Arguments.createMap();
                    eventParams.putString("address", packet.getAddress().getHostAddress());
                    eventParams.putInt("port", packet.getPort());
                    sendEvent("udp-discovery", eventParams);
                }
                catch (SocketTimeoutException e) {
                    continue;
                }
                catch (IOException ie) {
                    Log.e(LOG_TAG, "Receive error: " + ie);
                }
            }

            socket.close();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error: " + e);
        }

        Log.i(LOG_TAG, "Done listening");
    }

    public void stop() {
        this.running = false;
    }

    public void join() {
        try {
            this.running = false;
            this.thread.join();
        } catch (InterruptedException e) {
            Log.e(LOG_TAG, "Error: " + e);
        }
    }

    private void sendEvent(String eventName, WritableMap params) {
        this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
    }
}
