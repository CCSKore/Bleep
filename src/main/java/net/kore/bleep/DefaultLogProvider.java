package net.kore.bleep;

public class DefaultLogProvider implements BleepLogProvider {
    @Override
    public void info(Object info) {
        System.out.println(Colors.TEXT_BRIGHT_BLUE + "[Info] " + Colors.TEXT_RESET + info);
    }

    @Override
    public void warn(Object info) {
        System.out.println(Colors.TEXT_BRIGHT_YELLOW + "[Warn] " + info + Colors.TEXT_RESET);
    }

    @Override
    public void error(Object info) {
        System.out.println(Colors.TEXT_BRIGHT_RED + "[Error] " + info + Colors.TEXT_RESET);
    }

    @Override
    public void raw(Object info) {
        System.out.println(info);
    }
}
