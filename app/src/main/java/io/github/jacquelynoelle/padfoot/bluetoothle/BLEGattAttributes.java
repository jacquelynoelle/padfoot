package io.github.jacquelynoelle.padfoot.bluetoothle;

import java.util.HashMap;

/**
 * Includes relevant GATT Services and Characteristics
 */
public class BLEGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String RUNNING_SPEED_AND_CADENCE = "00001814-0000-1000-8000-00805f9b34fb";
    public static String DEVICE_INFORMATION = "0000180a-0000-1000-8000-00805f9b34fb";
    public static String BATTERY = "0000180f-0000-1000-8000-00805f9b34fb";
    public static String STEP_COUNT = "00002a56-0000-1000-8000-00805f9b34fb";
    public static String STEP_COUNT1 = "00002a57-0000-1000-8000-00805f9b34fb";
    public static String MANUFACTURER_NAME = "00002a29-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    static {
        // GATT Services
        attributes.put(RUNNING_SPEED_AND_CADENCE, "Running Speed and Cadence Service");
        attributes.put(DEVICE_INFORMATION, "Device Information Service");
        attributes.put(BATTERY, "Battery Service");
        // GATT Characteristics
        attributes.put(STEP_COUNT, "Step Count");
        attributes.put(MANUFACTURER_NAME, "Manufacturer Name String");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
