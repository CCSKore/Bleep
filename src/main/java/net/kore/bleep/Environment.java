package net.kore.bleep;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Environment {
    protected Environment() {
        enclosing = null;
        values.putAll(BleepAPI.PREDEFINED_VALUES);
        BleepAPI.PREDEFINED_VALUES.forEach((str, obj) -> value_types.put(str, obj.getClass()));
        values.put("exports", new ExportsClass().call(Interpreter.get(), List.of()));
    }

    protected Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    protected final Environment enclosing;
    protected final Map<String, Object> values = new HashMap<>();
    private final Map<String, Class<?>> value_types = new HashMap<>();
    private final Map<String, Boolean> value_type_changes = new HashMap<>();
    private final Map<String, Boolean> value_changes = new HashMap<>();

    protected Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        if (enclosing != null) return enclosing.get(name);
    
        throw new RuntimeError(name,
            "Undefined variable '" + name.lexeme + "'.");
    }

    protected Map<String, Object> getExports() {
        if (values.containsKey("exports") && values.get("exports") instanceof BleepInstance instance) {
            if (instance.klass.getClass().equals(ExportsClass.class)) {
                return ((ExportsClass) instance.klass).VALUES;
            }
        }
        return new HashMap<>();
    }

    protected void assign(Token name, Object value) {
        if (value_changes.containsKey(name.lexeme)) {
            if (value_changes.get(name.lexeme)) throw new RuntimeError(name, "Cannot redefine a field.");
        }
        if (values.containsKey(name.lexeme)) {
            if (value_types.containsKey(name.lexeme)) {
                Class<?> type = value_types.get(name.lexeme);
                if (type != null && !value_type_changes.get(name.lexeme)) {
                    if (value.getClass() != value_types.get(name.lexeme)) throw new RuntimeError(name,
                            "Tried to change type of '"+name.lexeme+"'.");
                }
            }
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
        define(name, value, true, true);
    }

    protected void define(String name, Object value, boolean typeChange, boolean change) {
        values.put(name, value);
        value_type_changes.put(name, typeChange);
        value_changes.put(name, change);
        if (value != null) {
            if (typeChange) {
                value_types.put(name, null);
            } else {
                value_types.put(name, value.getClass());
            }
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
