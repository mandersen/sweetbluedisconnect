# What is the correct way to disconnect + rediscover with SweetBlue?

This repo contains a reduced case Android Studio project.

It demonstrates a disconnect failure with an attempted SweetBlue implementation.

The SweetBlue Toolbox app is able to cycle through discovery > connect > disconnect > discovery > connect, etc as expected.

The implementation in this repo reliably fails to complete this cycle more than a few times.

Tested with Nexus 5, Android 6.0.1, Polar HR monitor.

The SweetBlue JARs are from the post-registration download 2018-01-19.

All the implementation is in MainActivity.

All output goes to console, nothing is displayed in the app UI.

## Repro
Modify the isDeviceYouWant method to match some BLE device at hand, watch the console for tag SweetBlueReducedCase.

## Expected
An ongoing cycle where the BLE device is discovered, connected, a timer waits 5 seconds then disconnects, BLE device is rediscovered, connected, etc.

## Actual
The cycle completes 0 - 3 times, then fails either at post-connect service discovery or after disconnect.  In either of those cases the connectionStateListener receives a BleDeviceState.DISCONNECTED event, then scanning resumes, but the DiscoveryListener no longer discovers or rediscovers the previously connected BLE device.  The BLE device appears to still be connected despite the BleDeviceState.DISCONNECTED event because it is not discoverable by other Android devices either.  Android BLE toggle off/on or BLE device power-cycle recovers the device discoverability.
