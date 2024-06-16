package net.kore.bleep;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExportsClass extends BleepClass {
    protected Map<String, Object> VALUES = new HashMap<>();

    public ExportsClass() {
        super("exports", null, null);
        this.methods = Map.of("set", new BleepCallable() {
            @Override
            public int arity(List<Object> arguments) {
                return 2;
            }
    
            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                Object arg1 = arguments.get(0);
                if (arg1 instanceof String str) {
                    handlePut(str, arguments.get(1));
                    return null;
                }
                throw new RuntimeError(null, "Cannot export item with a name which is not of type string.");
            }

            @Override
            public String toString() { return "<native fn>"; }
        });
    }

    private void handlePut(String key, Object value) {
        VALUES.put(key, value);
    }
}