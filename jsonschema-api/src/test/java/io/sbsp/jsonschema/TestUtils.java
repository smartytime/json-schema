package io.sbsp.jsonschema;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;

public class TestUtils {

    public static JsonValueWithPath createValue(JsonValue value) {
        return JsonValueWithPath.fromJsonValue(value);
    }

    public static JsonValueWithPath createJsonObjectWithLocation() {
        final JsonObject jsonValue = JsonProvider.provider().createObjectBuilder()
                .add("foo", "bar")
                .add("num", 3).build();
        return JsonValueWithPath.fromJsonValue(jsonValue);
    }

    public static JsonValueWithPath createJsonNumberWithLocation(Number number) {
        final JsonNumber jsonValue = JsonProvider.provider().createValue(number.doubleValue());
        return JsonValueWithPath.fromJsonValue(jsonValue);
    }

    public static JsonValueWithPath createJsonStringWithLocation(String string) {
        final JsonString jsonValue = JsonProvider.provider().createValue(string);
        return JsonValueWithPath.fromJsonValue(jsonValue);
    }

    public static JsonValueWithPath createJsonArrayWithLocation() {
        final JsonArray jsonValue = JsonProvider.provider().createArrayBuilder()
                .add("foo")
                .add("bar")
                .add(3)
                .add(true)
                .add(JsonValue.NULL)
                .build();

        return JsonValueWithPath.fromJsonValue(jsonValue);
    }

    public static JsonString jsonString(String input) {
        return JsonProvider.provider().createValue(input);
    }
}
