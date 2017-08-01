package io.sbsp.jsonschema;

import io.sbsp.jsonschema.six.JsonValueWithLocation;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;

public class TestUtils {

    public static JsonValueWithLocation createValue(JsonValue value) {
        return JsonValueWithLocation.fromJsonValue(value);
    }

    public static JsonValueWithLocation createJsonObjectWithLocation() {
        final JsonObject jsonValue = JsonProvider.provider().createObjectBuilder()
                .add("foo", "bar")
                .add("num", 3).build();
        return JsonValueWithLocation.fromJsonValue(jsonValue);
    }

    public static JsonValueWithLocation createJsonNumberWithLocation(Number number) {
        final JsonNumber jsonValue = JsonProvider.provider().createValue(number.doubleValue());
        return JsonValueWithLocation.fromJsonValue(jsonValue);
    }

    public static JsonValueWithLocation createJsonStringWithLocation(String string) {
        final JsonString jsonValue = JsonProvider.provider().createValue(string);
        return JsonValueWithLocation.fromJsonValue(jsonValue);
    }

    public static JsonValueWithLocation createJsonArrayWithLocation() {
        final JsonArray jsonValue = JsonProvider.provider().createArrayBuilder()
                .add("foo")
                .add("bar")
                .add(3)
                .add(true)
                .add(JsonValue.NULL)
                .build();

        return JsonValueWithLocation.fromJsonValue(jsonValue);
    }

    public static JsonString jsonString(String input) {
        return JsonProvider.provider().createValue(input);
    }
}
