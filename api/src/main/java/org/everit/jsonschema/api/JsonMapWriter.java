package org.everit.jsonschema.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class JsonMapWriter implements JsonWriter {

    private Map<String, Object> json;
    private LinkedList<List> listStack = new LinkedList<>();
    private LinkedList<String> paths = new LinkedList<>();
    private LinkedList<Map<String, Object>> currentMap = new LinkedList<>();

    @Override
    public JsonWriter array() {
        listStack.add(new ArrayList());
        return this;
    }

    @Override
    public JsonWriter endArray() {
        List list = listStack.pop();
        String path = paths.pop();
        final Map<String, Object> writing = currentMap.getLast();
        writing.put(path, list);
        return null;
    }

    @Override
    public JsonWriter object() {
        currentMap.push(new HashMap<>());
        return this;
    }

    @Override
    public JsonWriter endObject() {
        Map<String, Object> list = currentMap.pop();
        String path = paths.pop();
        final Map<String, Object> writing = currentMap.getLast();
        writing.put(path, list);
        return null;
    }

    @Override
    public JsonWriter ifFalse(String key, Boolean value) {
        if (value == null || !value) {
            currentMap.getLast().put(key, value);
        }
        return this;
    }

    @Override
    public <X> JsonWriter ifPresent(String key, X value) {
        if (value != null) {
            currentMap.getLast().put(key, value);
        }
        return this;
    }

    @Override
    public JsonWriter ifTrue(String key, Boolean value) {
        if (value != null && value) {
            currentMap.getLast().put(key, value);
        }
        return this;
    }

    @Override
    public JsonWriter key(String key) {
        paths.add(key);
        return this;
    }

    @Override
    public <X> JsonWriter value(X value) {
        String path = paths.pop();
        final Map<String, Object> writing = currentMap.getLast();
        writing.put(path, value);
        return this;
    }
}
