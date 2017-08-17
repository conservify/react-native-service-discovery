package org.conservify.react;

import android.util.Log;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class ServiceDiscovery extends ReactContextBaseJavaModule {
    private static final String LOG_TAG = "ServiceDiscovery";

    private final ReactApplicationContext reactContext;
    private JmDnsDiscovery jmDnsDiscovery;
    private UdpDiscovery udpDiscovery;

    public ServiceDiscovery(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        this.jmDnsDiscovery = new JmDnsDiscovery(reactContext);
        this.udpDiscovery = new UdpDiscovery(reactContext);
    }

    @Override
    public String getName() {
        return LOG_TAG;
    }

    @ReactMethod
    public void start() {
        Log.i(LOG_TAG, "Starting...");
        jmDnsDiscovery.start();
        udpDiscovery.start();
    }

    @ReactMethod
    public void stop() {
        Log.i(LOG_TAG, "Stopping...");
        jmDnsDiscovery.stop();
        udpDiscovery.stop();
        jmDnsDiscovery.join();
        udpDiscovery.join();
        Log.i(LOG_TAG, "Stopped");
    }
}