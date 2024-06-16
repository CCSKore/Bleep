package net.kore.bleep;

public class Return extends RuntimeException{
    protected final Object value;

    protected Return(Object value) {
        super(null, null, false, false);
        this.value = value;
    }
}
