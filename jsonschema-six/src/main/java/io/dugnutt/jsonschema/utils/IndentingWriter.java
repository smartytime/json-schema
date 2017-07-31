package io.dugnutt.jsonschema.utils;

import java.io.IOException;
import java.io.Writer;

import static com.google.common.base.Preconditions.checkNotNull;

public class IndentingWriter extends Writer {

    private static final char NEWLINE = '\n';

    private final Writer wrapped;
    private final String append;

    public IndentingWriter(Writer wrapped, String append) {
        checkNotNull(wrapped, "wrapped must not be null");
        checkNotNull(append, "append must not be null");
        this.wrapped = wrapped;
        this.append = append;
    }

    @Override
    public void flush() throws IOException {
        wrapped.flush();
    }

    @Override
    public void close() throws IOException {
        wrapped.close();
    }


    @Override
    public void write(int c) throws IOException {
        wrapped.write(c);
        if (c == NEWLINE) {
            wrapped.write(append);
        }
    }

    @Override
    public void write(char[] cbuf) throws IOException {
        wrapped.write(replace(cbuf, 0, cbuf.length));
    }

    private char[] replace(char[] input, int offset, int length) {
        return replace(String.valueOf(input, offset, length))
                .toString()
                .toCharArray();
    }

    private CharSequence replace(CharSequence input) {
        StringBuilder replace = new StringBuilder();
        int i = 0;

        while (i < input.length()) {
            final char c = input.charAt(i++);
            replace.append(c);
            if (c == NEWLINE) {
                replace.append(append);
            }
        }
        return replace;
    }

    @Override
    public void write(String str) throws IOException {
        wrapped.write(replace(str).toString());
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        wrapped.write(replace(str).toString(), off, len);
    }

    @Override
    public Writer append(CharSequence csq) throws IOException {
        wrapped.append(replace(csq));
        return this;
    }

    @Override
    public Writer append(CharSequence csq, int start, int end) throws IOException {
        wrapped.append(replace(csq), start, end);
        return this;
    }

    @Override
    public Writer append(char c) throws IOException {
        wrapped.append(c);
        if (c == NEWLINE) {
            wrapped.append(append);
        }
        return this;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        wrapped.write(replace(cbuf, off, len));
    }
}
