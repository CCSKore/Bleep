package net.kore.bleep;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BleepClass implements BleepCallable {
    protected final String name;
    protected final Object superclass;
    protected Map<String, BleepCallable> methods;

    public BleepClass(String name, Object superclass, Map<String, BleepCallable> methods) {
        this.superclass = superclass;
        this.name = name;
        this.methods = methods;
    }

    protected BleepCallable findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }

        if (superclass != null) {
            if (superclass instanceof BleepClass bleepClass) {
                return bleepClass.findMethod(name);
            } else {
                Class<?> clazz = superclass.getClass();
                if (name.equals("init")) {
                    return new BleepCallable() {
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
                            try {
                                List<Class<?>> clazzes = new ArrayList<>();
                                arguments.forEach(obj -> clazzes.add(obj.getClass()));
                                return clazz.getConstructor(clazzes.toArray(new Class[]{})).newInstance(arguments.toArray());
                            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                                     InstantiationException e) {
                                throw new RuntimeError(null, "Unable to execute JVM method", e);
                            }
                        }
                    };
                } else {
                    return new BleepCallable() {
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
                            try {
                                List<Class<?>> clazzes = new ArrayList<>();
                                arguments.forEach(obj -> clazzes.add(obj.getClass()));
                                return clazz.getMethod(name, clazzes.toArray(new Class[]{})).invoke(superclass, arguments.toArray());
                            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                                throw new RuntimeError(null, "Unable to execute JVM method", e);
                            }
                        }
                    };
                }
            }
        }
    
        return null;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        BleepInstance instance = new BleepInstance(this, interpreter);
        BleepCallable initializer = findMethod("init");
        if (initializer instanceof BleepFunction bleepFunction) {
            bleepFunction.bind(instance, interpreter).call(interpreter, arguments);
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
