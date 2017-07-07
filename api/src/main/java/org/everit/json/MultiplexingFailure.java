package org.everit.json;

public class MultiplexingFailure extends RuntimeException{
    public MultiplexingFailure(String message, Object... args) {
        super(String.format(message, (Object[]) args));
    }
}
