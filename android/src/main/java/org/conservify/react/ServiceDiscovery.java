package org.conservify.react;

import android.util.Log;
import com.facebook.react.bridge.*;

public class ServiceDiscovery extends ReactContextBaseJavaModule implements LifecycleEventListener {
    private static final String LOG_TAG = "ServiceDiscovery";

    private final ReactApplicationContext reactContext;
    private boolean running;
    private boolean jmDnsEnabled = false;
    private JmDnsDiscovery jmDnsDiscovery;
    private UdpDiscovery udpDiscovery;

    public ServiceDiscovery(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        this.reactContext.addLifecycleEventListener(this);
        this.jmDnsDiscovery = new JmDnsDiscovery(reactContext);
        this.udpDiscovery = new UdpDiscovery(reactContext);
        Log.i(LOG_TAG, "Created");
    }

    @Override
    public String getName() {
        return LOG_TAG;
    }

    @ReactMethod
    public void start(int udpPort) {
        if (!running) {
            Log.i(LOG_TAG, "Starting...");
            if (jmDnsEnabled) {
                jmDnsDiscovery.start();
            }
            udpDiscovery.start(udpPort);
            running = true;
        }
    }

    @ReactMethod
    public void stop() {
        if (running) {
            Log.i(LOG_TAG, "Stopping...");
            if (jmDnsEnabled) {
                jmDnsDiscovery.stop();
            }
            udpDiscovery.stop();
            if (jmDnsEnabled) {
                jmDnsDiscovery.join();
            }
            udpDiscovery.join();
            Log.i(LOG_TAG, "Stopped");
            running = false;
        }
    }

    @Override
    public void onHostResume() {
        Log.i(LOG_TAG, "HostResume");
    }

    @Override
    public void onHostPause() {
        Log.i(LOG_TAG, "HostPause");
        stop();
    }

    @Override
    public void onHostDestroy() {
        Log.i(LOG_TAG, "HostDestroy");
    }
}
