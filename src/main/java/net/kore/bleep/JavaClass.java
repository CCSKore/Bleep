package net.kore.bleep;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JavaClass extends BleepClass {
    public JavaClass() {
        super("JVM", null, getMethods());
    }

    private static Map<String, BleepCallable> getMethods() {
        Map<String, BleepCallable> methods = new HashMap<>();
        methods.put("getClass", new BleepCallable() {
            @Override
            public int arity(List<Object> arguments) {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                Object arg = arguments.get(0);
                if (arg instanceof String str) {
                    try {
                        return Class.forName(str);
                    } catch (ClassNotFoundException e) {
                        return null;
                    }
                }
                return null;
            }

            @Override
            public String toString() { return "<native fn>"; }
        });
        methods.put("getInstance", new BleepCallable() {
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
                Object arg1 = arguments.get(0);
                if (arg1 instanceof Class<?> clazz) {
                    List<Object> args = new ArrayList<>();
                    for (Object arg : arguments) if (arg != arg1) args.add(arg);
                    for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                        if (constructor.getParameterCount() == args.size()) {
                            for (Object arg : args) {
                                if (constructor.getParameterTypes()[args.indexOf(arg)].equals(arg.getClass())) {
                                    try {
                                        return constructor.newInstance(args.toArray());
                                    } catch (InstantiationException | IllegalAccessException |
                                             InvocationTargetException e) {
                                        throw new RuntimeError(null, "Exception while creating JVM class.", e);
                                    }
                                }
                            }
                        }
                    }
                    throw new RuntimeError(null, "Args do not match that of the JVM class.");
                }
                throw new RuntimeError(null, "First arg of JVM.getInstance was NOT a JVM class.");
            }

            @Override
            public String toString() { return "<native fn>"; }
        });
        methods.put("invokeMethod", new BleepCallable() {
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
                Object arg1 = arguments.get(0);
                Object arg2 = arguments.get(1);
                Object arg3 = arguments.get(2);
                if (arg3 instanceof String str && arg1 instanceof Class<?> clazz) {
                    List<Object> args = new ArrayList<>();
                    for (Object arg : arguments) if ((arg != arg2 || arg2 == null) && arg != arg1 && arg != arg3) args.add(arg);
                    for (Method method : clazz.getDeclaredMethods()) {
                        if (method.getName().equals(str) && method.getParameterCount() == args.size()) {
                            for (Object arg : args) {
                                if (method.getParameterTypes()[args.indexOf(arg)].equals(arg.getClass())) {
                                    try {
                                        return method.invoke(arg2, args.toArray());
                                    } catch (IllegalAccessException | InvocationTargetException e) {
                                        throw new RuntimeError(null, "Exception while invoking JVM method.", e);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    throw new RuntimeError(null, "First arg is not a class or the third arg is not a string!");
                }

                BleepAPI.logProvider.info(String.join(" ", arguments.stream().map(Object::toString).collect(Collectors.toUnmodifiableList())));

                throw new RuntimeError(null, "Unable to invoke JVM method.");
            }

            @Override
            public String toString() { return "<native fn>"; }
        });
        methods.put("getField", new BleepCallable() {
            @Override
            public int arity(List<Object> arguments) {
                return 3;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                Object arg1 = arguments.get(0);
                Object arg2 = arguments.get(1);
                Object arg3 = arguments.get(2);
                if (arg3 instanceof String str && arg1 instanceof Class<?> clazz) {
                    for (Field field : clazz.getDeclaredFields()) {
                        if (field.getName().equals(str)) {
                            try {
                                return field.get(arg2);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeError(null, "Exception while getting JVM field..", e);
                            }
                        }
                    }
                } else {
                    throw new RuntimeError(null, "First arg is not a class or the third arg is not a string!");
                }

                throw new RuntimeError(null, "Unable to get JVM field.");
            }

            @Override
            public String toString() { return "<native fn>"; }
        });
        methods.put("importClass", new BleepCallable() {
            @Override
            public int arity(List<Object> arguments) {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                Object arg1 = arguments.get(0);
                if (arg1 instanceof String str) {
                    try {
                        String[] packagePath = str.split("\\.");
                        Class<?> clazz = Class.forName(str);
                        interpreter.globals.define(packagePath[packagePath.length - 1], clazz, false, false, null);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
                return null;
            }

            @Override
            public String toString() { return "<native fn>"; }
        });
        return methods;
    }
}
