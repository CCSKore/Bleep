package net.kore.bleep;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogClass {
    public LogClass() {
        getMethods().forEach((str, callable) -> Interpreter.get().globals.define(str, callable));
    }

    private static Map<String, BleepCallable> getMethods() {
        Map<String, BleepCallable> methods = new HashMap<>();
        methods.put("info", new BleepCallable() {
            @Override
            public int arity(List<Object> arguments) {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                BleepAPI.logProvider.info(arguments.get(0));
                return null;
            }

            @Override
            public String toString() { return "<native fn>"; }
        });
        methods.put("warn", new BleepCallable() {
            @Override
            public int arity(List<Object> arguments) {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                BleepAPI.logProvider.warn(arguments.get(0));
                return null;
            }

            @Override
            public String toString() { return "<native fn>"; }
        });
        methods.put("error", new BleepCallable() {
            @Override
            public int arity(List<Object> arguments) {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                BleepAPI.logProvider.info(arguments.get(0));
                return null;
            }

            @Override
            public String toString() { return "<native fn>"; }
        });
        return methods;
    }

    @Override
    public String toString() {
        return "<native class>";
    }
}
