package org.everit.jsonschema.api;

import com.google.common.base.Strings;
import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;

import java.util.Collection;

public class JsonStringWriter implements JsonWriter {
    StringBuilder builder = new StringBuilder();
    int depth;
    int INDENT = 4;
    boolean prior;

    Escaper htmlEscaper = HtmlEscapers.htmlEscaper();

    @Override
    public JsonWriter array() {
        builder.append("[ \n");
        depth++;
        indent();
        return this;
    }

    private void indent() {
        builder.append(Strings.repeat(" ", depth * INDENT));
    }

    @Override
    public JsonWriter endArray() { builder.append(" ]");
        depth--;
        indent();
        builder.append(" ]");
        return this;
    }

    @Override
    public JsonWriter object() {
        builder.append("{ \n");
        depth++;
        indent();
        return this;
    }

    private void outdent() {
        depth--;
        indent();
    }

    @Override
    public JsonWriter endObject() {
        outdent();
        builder.append(" }");
        return this;
    }

    @Override
    public JsonWriter ifFalse(String key, Boolean value) {
        if (value == null || !value) {
            indent();
            builder.append("\"").append(htmlEscaper.escape(key)).append("\": false");
        }
        return this;
    }

    @Override
    public <X> JsonWriter ifPresent(String key, X value) {
        if (value != null) {
            indent();
            this.key(key);
            this.value(value);
        }
        return this;
    }

    @Override
    public JsonWriter ifTrue(String key, Boolean value) {
        if (value != null && value) {
            this.key(key);
            this.value(value);
        }
        return this;
    }

    @Override
    public JsonWriter key(String key) {
        indent();
        builder.append("\"").append(htmlEscaper.escape(key)).append("\": ");
        return this;
    }

    @Override
    public <X> JsonWriter value(X value) {
        if (value == null) {
            builder.append("null");
        } else if (value instanceof Number) {
            builder.append(value);
        } else if(value instanceof Boolean) {
            builder.append(value);
        } else if(value instanceof String) {
            builder.append("\"").append(htmlEscaper.escape(value.toString())).append("\"");
        } else if(value instanceof Collection) {
            array();
            Collection coll = (Collection) value;
            for (Object o : coll) {
                value(o);
            }
            endArray();
        } else {
            throw new IllegalStateException("Unable to write value of type " + value.getClass());
        }

        return this;
    }

    @Override
    public String toString() {
        return builder.toString();
    }
}
