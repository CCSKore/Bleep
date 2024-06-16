package net.kore.bleep;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    protected Environment() {
        enclosing = null;
        values.putAll(BleepAPI.PREDEFINED_VALUES);
        BleepAPI.PREDEFINED_VALUES.forEach((str, obj) -> value_types.put(str, obj.getClass()));
    }

    protected Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    protected final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();
    private final Map<String, Class<?>> value_types = new HashMap<>();

    protected Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        if (enclosing != null) return enclosing.get(name);
    
        throw new RuntimeError(name,
            "Undefined variable '" + name.lexeme + "'.");
    }

    protected void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            if (value.getClass() != value_types.get(name.lexeme)) throw new RuntimeError(name,
                    "Tried to change type of '"+name.lexeme+"'.");
            values.put(name.lexeme, value);
            return;
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }
    
        throw new RuntimeError(name,
            "Undefined variable '" + name.lexeme + "'.");
    }

    protected void define(String name, Object value) {
        values.put(name, value);
        if (value != null) {
            value_types.put(name, value.getClass());
        }
    }

    protected Environment ancestor(int distance) {
        Environment environment = this;
        for (int i = 0; i < distance; i++) {
            environment = environment.enclosing; 
        }
    
        return environment;
    }

    protected Object getAt(int distance, String name) {
        return ancestor(distance).values.get(name);
    }

    protected void assignAt(int distance, Token name, Object value) {
        ancestor(distance).values.put(name.lexeme, value);
    }
}
