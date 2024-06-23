package net.kore.bleep;

import java.util.List;

public interface BleepCallable {
    default boolean canHaveInfiniteArgs(List<Object> arguments) {
        return false;
    }

    int arity(List<Object> arguments);
    Object call(Interpreter interpreter, List<Object> arguments);
    default BleepCallable bind(BleepInstance instance, Interpreter interpreter) {
        Environment environment = new Environment(interpreter.globals);
        environment.define("this", instance);
        return this;
    }
}
