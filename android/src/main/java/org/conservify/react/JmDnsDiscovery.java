package org.conservify.react;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;
import java.net.*;
import java.util.Collections;
import java.util.List;

public class JmDnsDiscovery implements Runnable, ServiceListener {
    private static final String LOG_TAG = "NSD_JMDNS";
    private static final String SERVICE_TYPE = "_workstation._tcp.local.";

    private final ReactApplicationContext reactContext;
    private final Context applicationContext;

    private Thread thread;
    private boolean running;

    public JmDnsDiscovery(ReactApplicationContext reactContext) {
        this.reactContext = reactContext;
        this.applicationContext = reactContext.getApplicationContext();
    }

    public void start() {
        Log.i(LOG_TAG, "Starting, looking for " + SERVICE_TYPE);

        this.running = true;
        this.thread = new Thread(this);
        this.thread.start();
    }
    @Override
    public void run() {
        WifiManager wifi = (WifiManager)applicationContext.getSystemService(android.content.Context.WIFI_SERVICE);
        WifiManager.MulticastLock lock = wifi.createMulticastLock(getClass().getSimpleName());
        lock.setReferenceCounted(false);

        try {
            lock.acquire();

            InetAddress ip = getIpAddress();

            Log.i(LOG_TAG, "Starting JmDns " + ip);
            JmDNS jmdns4 = JmDNS.create(ip, "ServiceDiscovery");
            Log.i(LOG_TAG, "JmDns4 " + jmdns4.getInetAddress() + " " + jmdns4.getHostName());
            jmdns4.addServiceListener(SERVICE_TYPE, this);
            Log.i(LOG_TAG, "Starting JmDns (default)");
            JmDNS jmdnsDefault = JmDNS.create();
            Log.i(LOG_TAG, "JmDnsDefault " + jmdnsDefault.getInetAddress() + " " + jmdnsDefault.getHostName());
            jmdnsDefault.addServiceListener(SERVICE_TYPE, this);

            while (running) {
                Thread.sleep(1000L);
            }

            Log.i(LOG_TAG, "Closing...");

            jmdns4.close();
            jmdnsDefault.close();

            lock.release();

            Log.i(LOG_TAG, "Done");
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error: " + e);
        }
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

    @Override
    public void serviceAdded(ServiceEvent ev) {
        Log.d(LOG_TAG, "Added: " + ev.getName() + " " + ev.getType());
        ev.getDNS().requestServiceInfo(ev.getType(), ev.getName(), 5000);
    }

    @Override
    public void serviceRemoved(ServiceEvent ev) {
        Log.d(LOG_TAG, "Service removed: " + ev.getName());
    }

    @Override
    public void serviceResolved(ServiceEvent ev) {
        Log.d(LOG_TAG, "Service resolved: " + ev.getInfo().getQualifiedName() + " port:" + ev.getInfo().getPort());
        Log.d(LOG_TAG, "Service type : " + ev.getInfo().getType());

        WritableArray addresses = Arguments.createArray();
        for (int i = 0; i < ev.getInfo().getHostAddresses().length; ++i) {
            addresses.pushString(ev.getInfo().getHostAddresses()[i].toString());
        }
        WritableMap eventParams = Arguments.createMap();
        eventParams.putString("type", ev.getInfo().getType());
        eventParams.putString("name", ev.getName());
        eventParams.putArray("addresses", addresses);
        eventParams.putInt("port", ev.getInfo().getPort());

        sendEvent("service-resolved", eventParams);
    }

    private void sendEvent(String eventName, WritableMap params) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
    }

    private static InetAddress getIpAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        if (addr instanceof  Inet4Address) {
                            return addr;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
