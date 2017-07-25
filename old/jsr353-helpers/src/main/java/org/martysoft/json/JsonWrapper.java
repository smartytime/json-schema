package io.dugnutt.json;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.util.List;
import java.util.Map;

/**
 * Interface that allows a provider to specify how to wrap certain keywords of data,
 */
public interface JsonWrapper {

    JsonNumber wrap(Number input);
    JsonString wrap(String input);

    JsonObject wrap(Map<String, JsonValue> values);

    JsonArray wrap(List<JsonValue> values);


}
