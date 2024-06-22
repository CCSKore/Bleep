package net.kore.bleep;

import java.util.List;
import java.util.Map;

public class ExportDataClass extends BleepClass {
    private final Map<String, Object> EXPORTED_VALUES;

    public ExportDataClass(Environment previousEnv) {
        super("exports", null, null);
        EXPORTED_VALUES = previousEnv.getExports();
        this.methods = Map.of("get", new BleepCallable() {
            @Override
            public int arity(List<Object> arguments) {
                return 1;
            }
    
            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                if (arguments.get(0) instanceof String str) {
                    return EXPORTED_VALUES.get(str);
                }

                throw new RuntimeError(null, "First argument must be of type string");
            }

            @Override
            public String toString() { return "<native fn>"; }
        });
    }
}
