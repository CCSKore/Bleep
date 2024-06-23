package net.kore.bleep;

public interface BleepLogProvider {
    void info(Object info);
    void warn(Object info);
    void error(Object info);
    void raw(Object info);
}
