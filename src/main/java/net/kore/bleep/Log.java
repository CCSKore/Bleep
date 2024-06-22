package net.kore.bleep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Log {
    public Log() {
        getMethods().forEach((str, callable) -> Interpreter.get().globals.define(str, callable));
    }

    private static Map<String, BleepCallable> getMethods() {
        Map<String, BleepCallable> methods = new HashMap<>();
        methods.put("info", new BleepCallable() {
            @Override
            public boolean canHaveInfiniteArgs(List<Object> arguments) {
                return true;
            }

            @Override
            public int arity(List<Object> arguments) {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                List<String> strs = new ArrayList<>();
                arguments.forEach(obj -> strs.add(obj.toString()));
                BleepAPI.logProvider.info(String.join(" ", strs));
                return null;
            }

            @Override
            public String toString() { return "<native fn>"; }
        });
        methods.put("warn", new BleepCallable() {
            @Override
            public boolean canHaveInfiniteArgs(List<Object> arguments) {
                return true;
            }

            @Override
            public int arity(List<Object> arguments) {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                List<String> strs = new ArrayList<>();
                arguments.forEach(obj -> strs.add(obj.toString()));
                BleepAPI.logProvider.warn(String.join(" ", strs));
                return null;
            }

            @Override
            public String toString() { return "<native fn>"; }
        });
        methods.put("error", new BleepCallable() {
            @Override
            public boolean canHaveInfiniteArgs(List<Object> arguments) {
                return true;
            }

            @Override
            public int arity(List<Object> arguments) {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                List<String> strs = new ArrayList<>();
                arguments.forEach(obj -> strs.add(obj.toString()));
                BleepAPI.logProvider.error(String.join(" ", strs));
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
