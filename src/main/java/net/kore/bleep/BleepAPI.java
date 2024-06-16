package net.kore.bleep;

import java.util.HashMap;
import java.util.Map;

public class BleepAPI {
    protected static Map<String, Object> PREDEFINED_VALUES = new HashMap<>();
    protected static BleepLogProvider logProvider = new DefaultLogProvider();

    public static void setLogProvider(BleepLogProvider logProvider) {
        BleepAPI.logProvider = logProvider;
    }

    public static void presentValue(String name, double value) {
        PREDEFINED_VALUES.put(name, value);
    }
    public static void presentValue(String name, String value) {
        PREDEFINED_VALUES.put(name, value);
    }
    public static void presentValue(String name, boolean value) {
        PREDEFINED_VALUES.put(name, value);
    }
    public static void presentValue(String name, BleepCallable value) {
        PREDEFINED_VALUES.put(name, value);
    }
    public static void presentValue(String name, BleepClass value) {
        PREDEFINED_VALUES.put(name, value);
    }
}
