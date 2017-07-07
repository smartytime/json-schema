package org.everit.json;

/**
 * @author erosb
 */
@FunctionalInterface
public interface JsonObjectIterator {

    void apply(String key, JsonElement value);

}
