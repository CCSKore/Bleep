package net.kore.bleep;

import java.util.List;
import java.util.Map;

public class BleepClass implements BleepCallable {
    protected final String name;
    protected final BleepClass superclass;
    protected Map<String, BleepCallable> methods;

    public BleepClass(String name, BleepClass superclass, Map<String, BleepCallable> methods) {
        this.superclass = superclass;
        this.name = name;
        this.methods = methods;
    }

    protected BleepCallable findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }

        if (superclass != null) {
            return superclass.findMethod(name);
        }
    
        return null;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        BleepInstance instance = new BleepInstance(this);
        BleepCallable initializer = findMethod("init");
        if (initializer instanceof BleepFunction bleepFunction) {
            bleepFunction.bind(instance).call(interpreter, arguments);
        }

        return instance;
    }

    @Override
    public int arity(List<Object> arguments) {
        BleepCallable initializer = findMethod("init");
        if (initializer == null) return 0;
        return initializer.arity(arguments);
    }
}
