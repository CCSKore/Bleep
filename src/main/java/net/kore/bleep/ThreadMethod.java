package net.kore.bleep;

import java.util.ArrayList;
import java.util.List;

public class ThreadMethod implements BleepCallable {
    @Override
    public boolean canHaveInfiniteArgs(List<Object> arguments) {
        Object arg1 = arguments.get(0);
        if (arg1 instanceof BleepCallable callable) {
            List<Object> args = new ArrayList<>();
            for (Object arg : arguments) if (arg != arg1) args.add(arg);
            return callable.canHaveInfiniteArgs(args);
        }
        throw new RuntimeError(null, "Cannot determine if thread method has infinite args, the first arg is not a callable!");
    }

    @Override
    public int arity(List<Object> arguments) {
        Object arg1 = arguments.get(0);
        if (arg1 instanceof BleepCallable callable) {
            List<Object> args = new ArrayList<>();
            for (Object arg : arguments) if (arg != arg1) args.add(arg);
            return callable.arity(args);
        }
        throw new RuntimeError(null, "Cannot determine how many args the thread method should have, the first arg is not a callable!");
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Object arg1 = arguments.get(0);
        if (arg1 instanceof BleepCallable callable) {
            List<Object> args = new ArrayList<>();
            for (Object arg : arguments) if (arg != arg1) args.add(arg);
            if (!callable.canHaveInfiniteArgs(args)) {
                if (callable.arity(args) + 1 != arity(arguments)) throw new RuntimeError(null, "Invalid amount of args provided.");
            }
            new Thread(() -> callable.call(interpreter, args)).start();
            return null;
        }
        throw new RuntimeError(null, "Cannot run thread method, the first arg is not a callable!");
    }
}
