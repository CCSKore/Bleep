package net.kore.bleep;

public class DefaultLogProvider implements BleepLogProvider {
    @Override
    public void info(Object info) {
        System.out.println("[Info] "+info);
    }

    @Override
    public void warn(Object info) {
        System.out.println("[Warn] "+info);
    }

    @Override
    public void error(Object info) {
        System.out.println("[Error] "+info);
    }
}
