package org.martysoft.jsoniter.jsr353;

import com.google.common.io.Resources;
import org.martysoft.json.BaseJsonPointer;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class JsoniterReaderTest {

    @Test
    public void readObject_ForBasicJson_ReadsArray() {
        String jsonInputString = readResource("/json/simple-object.json");
        JsoniterReader jsoniterReader = new JsoniterReader(jsonInputString);
        final JsonObject jsonObject = jsoniterReader.readObject();

        assertSoftly(a -> {
            final JsonArray jsonArray = jsonObject.getJsonArray("array");
            a.assertThat(jsonArray)
                    .isNotNull();
            a.assertThat(jsonArray.getValueType())
                    .as("Is ValueType.ARRAY")
                    .isEqualTo(JsonValue.ValueType.ARRAY);
            a.assertThat(jsonArray.size())
                    .as("Size")
                    .isEqualTo(4);
            a.assertThat(jsonArray.getString(0))
                    .as("Value matches")
                    .isEqualTo("1");
            a.assertThat(jsonArray.getInt(1))
                    .isEqualTo(2);
            a.assertThat(jsonArray.getJsonNumber(2).doubleValue())
                    .isEqualTo(3.3);
            a.assertThat(jsonArray.getBoolean(3))
                    .isTrue();

            a.assertThatThrownBy(() -> jsonObject.getJsonString("number"))
                    .isInstanceOf(ClassCastException.class);
        });
    }

    @Test
    public void readObject_ForBasicJson_ReadsNumber() {
        String jsonInputString = readResource("/json/simple-object.json");
        JsoniterReader jsoniterReader = new JsoniterReader(jsonInputString);
        final JsonObject jsonObject = jsoniterReader.readObject();

        assertSoftly(a -> {
            final JsonNumber jsonNumber = jsonObject.getJsonNumber("number");
            a.assertThat(jsonNumber)
                    .isNotNull();
            a.assertThat(jsonNumber.getValueType())
                    .as("Is ValueType.NUMBER")
                    .isEqualTo(JsonValue.ValueType.NUMBER);
            a.assertThat(jsonNumber.isIntegral())
                    .as("Not integral")
                    .isFalse();
            a.assertThat(jsonNumber.doubleValue())
                    .as("Value matches")
                    .isEqualTo(1.45432);

            a.assertThatThrownBy(() -> jsonObject.getJsonString("number"))
                    .isInstanceOf(ClassCastException.class);
        });
    }

    @Test
    public void readObject_ForBasicJson_ReadsString() {
        String jsonInputString = readResource("/json/simple-object.json");
        JsoniterReader jsoniterReader = new JsoniterReader(jsonInputString);
        final JsonObject jsonObject = jsoniterReader.readObject();

        assertSoftly(a -> {
            final JsonString jsonString = jsonObject.getJsonString("string");
            a.assertThat(jsonString)
                    .isNotNull();
            a.assertThat(jsonString.getValueType())
                    .as("Is ValueType.STRING")
                    .isEqualTo(JsonValue.ValueType.STRING);
            a.assertThat(jsonString.getString())
                    .as("Value matches")
                    .isNotBlank()
                    .isEqualTo("test string");
            a.assertThatThrownBy(() -> jsonObject.getJsonNumber("string"))
                    .isInstanceOf(ClassCastException.class);
        });
    }

    @Test
    public void readObject_Pointer_Works() {
        String jsonInputString = readResource("/json/simple-object.json");
        JsoniterReader jsoniterReader = new JsoniterReader(jsonInputString);
        final JsonObject jsonObject = jsoniterReader.readObject();

        final JsonValue value = new BaseJsonPointer("/object/a")
                .getValue(jsonObject);
        assertSoftly(a -> {
            a.assertThat(value)
                    .isNotNull()
                    .isInstanceOf(JsonString.class);
            a.assertThat(((JsonString) value).getString())
                    .isNotBlank()
                    .isEqualTo("a");
        });
        System.out.println(value);
    }

    @Test(expected = ClassCastException.class)
    public void read_WhenWrongTypeRequested_ThenThrowsException() {
        String jsonString = readResource("/json/simple-object.json");
        JsoniterReader jsoniterReader = new JsoniterReader(jsonString);
        jsoniterReader.readArray();
    }

    private String readResource(String url) {
        try {
            final URL resource = getClass().getResource(url);
            return Resources.toString(resource, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}