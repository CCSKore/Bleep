package net.kore.bleep;

import java.util.HashMap;
import java.util.Map;

public class BleepInstance {
    private BleepClass klass;
    private final Map<String, Object> fields = new HashMap<>();

    protected BleepInstance(BleepClass klass) {
        this.klass = klass;
    }

    protected Object get(Token name) {
        if (fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme);
        }

        BleepCallable method = klass.findMethod(name.lexeme);
        if (method != null) return method.bind(this);
    
        throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'.");
    }

    protected void set(Token name, Object value) {
        fields.put(name.lexeme, value);
    }

    @Override
    public String toString() {
        return klass.name + " instance";
    }
}
