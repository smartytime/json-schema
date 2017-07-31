package io.dugnutt.jsonschema.utils;

import org.junit.Test;

import javax.json.JsonObject;
import javax.json.spi.JsonProvider;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonUtilsTest {

    @Test
    public void testToPrettyString_Indent() {
        final JsonProvider provider = JsonProvider.provider();
        final JsonObject json = provider.createObjectBuilder()
                .add("name", "Eric")
                .add("age", 34)
                .add("address", provider.createObjectBuilder()
                        .add("line1", "123 W. East")
                        .add("city", "Gilbert")
                        .add("state", "AZ")
                        .add("postalCode", "85295"))
                .build();
        final String indentedValue = JsonUtils.toPrettyString(json, true);
        assertThat(indentedValue).isEqualTo("{\n" +
                "\t  \"name\":\"Eric\",\n" +
                "\t  \"age\":34,\n" +
                "\t  \"address\":{\n" +
                "\t    \"line1\":\"123 W. East\",\n" +
                "\t    \"city\":\"Gilbert\",\n" +
                "\t    \"state\":\"AZ\",\n" +
                "\t    \"postalCode\":\"85295\"\n" +
                "\t  }\n" +
                "\t}");
    }

    @Test
    public void testToPrettyString_NoIndent() {
        final JsonProvider provider = JsonProvider.provider();
        final JsonObject json = provider.createObjectBuilder()
                .add("name", "Eric")
                .add("age", 34)
                .add("address", provider.createObjectBuilder()
                        .add("line1", "123 W. East")
                        .add("city", "Gilbert")
                        .add("state", "AZ")
                        .add("postalCode", "85295"))
                .build();
        final String prettyPrintValue = JsonUtils.toPrettyString(json, false);
        assertThat(prettyPrintValue).isEqualTo("{\n" +
                "  \"name\":\"Eric\",\n" +
                "  \"age\":34,\n" +
                "  \"address\":{\n" +
                "    \"line1\":\"123 W. East\",\n" +
                "    \"city\":\"Gilbert\",\n" +
                "    \"state\":\"AZ\",\n" +
                "    \"postalCode\":\"85295\"\n" +
                "  }\n" +
                "}");
    }
}