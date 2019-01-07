/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.jacquelynoelle.padfoot;

import java.util.HashMap;

/**
 * Includes relevant GATT Services and Characteristics
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String RUNNING_SPEED_AND_CADENCE = "00001814-0000-1000-8000-00805f9b34fb";
    public static String DEVICE_INFORMATION = "0000180a-0000-1000-8000-00805f9b34fb";
    public static String BATTERY = "0000180f-0000-1000-8000-00805f9b34fb";
    public static String STEP_COUNT = "00002a56-0000-1000-8000-00805f9b34fb";
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
