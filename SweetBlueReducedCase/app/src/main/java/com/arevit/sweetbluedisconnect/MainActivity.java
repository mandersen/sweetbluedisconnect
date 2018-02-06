package com.arevit.sweetbluedisconnect;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleDeviceState;
import com.idevicesinc.sweetblue.BleManager;
import com.idevicesinc.sweetblue.utils.BluetoothEnabler;
import com.idevicesinc.sweetblue.utils.Interval;

public class MainActivity extends AppCompatActivity {

    BleManager sweetBlue_BleManager;

    private static final String TAG = "SweetBlueReducedCase";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sweetBlue_BleManager = BleManager.get(this);
        runBluetoothEnabler();
    }

    @Override protected void onResume() {
        super.onResume();
        sweetBlue_BleManager.onResume();
    }

    @Override protected void onPause() {
        super.onPause();
        sweetBlue_BleManager.onPause();
    }


    public void startScan() {
        Log.v(TAG, "Starting periodic scan...");
        sweetBlue_BleManager.startPeriodicScan(Interval.TEN_SECS, Interval.FIVE_SECS, sweetBlue_DiscoveryListener);
    }

    void stopScan() {
        sweetBlue_BleManager.stopPeriodicScan();
    }

    public void connect(BleDevice bleDevice) {
        bleDevice.connect(connectionStateListener, connectionFailListener);
    }

    public void disconnect(BleDevice bleDevice) {
        bleDevice.disconnect();
    }

    public void runBluetoothEnabler() {
        BluetoothEnabler.start(this, new BluetoothEnabler.DefaultBluetoothEnablerFilter() {
            @Override public BluetoothEnabler.BluetoothEnablerFilter.Please onEvent(BluetoothEnablerEvent e) {
                if( e.isDone() ) {
                    startScan();
                }
                return super.onEvent(e);
            }
        });
    }

    private BleManager.DiscoveryListener sweetBlue_DiscoveryListener = new BleManager.DiscoveryListener() {
        @Override public void onEvent(DiscoveryEvent event) {
            if( event.was(LifeCycle.DISCOVERED) || event.was(LifeCycle.REDISCOVERED) ) {
                if (isDeviceYouWant(event)) {
                    Log.v(TAG, "Discovered: " + event.toString());
                    BleDevice bleDevice = event.device();
                    connect(bleDevice);
                }
            }
        }
    };

    private Boolean isDeviceYouWant(BleManager.DiscoveryListener.DiscoveryEvent event) {

        // Test with third-party BLE device, same repro.
        String name = event.device().getName_native();
        if (name.toLowerCase().startsWith("polar")) {
            Log.v(TAG, event.device().getName_native());
            return true;
        }
        return false;

//        // Test with prototype BLE device, same repro.
//        byte[] b = event.device().getScanRecord();
//        if (null == b) {
//            return false;
//        }
//        if (b.length < 22) {
//            return false;
//        }
//
//        byte[] kCompanyIdentifier = new byte[] {(byte)0x01, (byte)0x02, (byte)0x03};
//        byte[]  companyIdentifier = new byte[] {b[19], b[20], b[21]};
//        if (Arrays.equals(kCompanyIdentifier, companyIdentifier)){
//            return true;
//        }
//        return false;
    };

    BleDevice.ConnectionFailListener connectionFailListener = new BleDevice.ConnectionFailListener() {
        @Override
        public Please onEvent(ConnectionFailEvent e) {
            Log.v(TAG, "Connection failed: " + e.toString());
            return null;
        }
    };

    BleDevice.StateListener connectionStateListener = new BleDevice.StateListener()
    {
        @Override public void onEvent(StateEvent event)
        {
            if( event.didEnter(BleDeviceState.INITIALIZED) ) {
                Log.v(TAG, "Connected");
                stopScan();
                // Simulate user-initiated disconnect after some interval of use.
                disconnectAfterDelay(event.device());
            }
            else if( event.didEnter(BleDeviceState.DISCONNECTED) ) {
                Log.v(TAG, "Disconnected, resume scan...");
                // Expected is when this scan starts, the previously connected BLE device will be
                // discovered or rediscovered.  Actual is the BLE device remains connected even
                // after we receive this event.didEnter(BleDeviceState.DISCONNECTED callback.
                // So it is no longer discovered/rediscovered by this Android device or any other
                // Until the Android BLE is toggled off/on or the BLE device is power-cycled.
                startScan();
            }
        }
    };

    private void disconnectAfterDelay(final BleDevice bleDevice) {
        new CountDownTimer(5000, 1000) {
            public void onFinish() {
                Log.v(TAG, "Attempting disconnect...");
                disconnect(bleDevice);
            }
            public void onTick(long millisUntilFinished) {
                Log.v(TAG, "Disconnecting in " + millisUntilFinished/1000 + " seconds...");
            }
        }.start();
    }
}
