package org.everit.jsonschema.loaders.jsoniter;

import com.jsoniter.any.Any;
import com.jsoniter.output.JsonStream;
import com.jsoniter.spi.Config;
import org.everit.jsonschema.api.Schema;
import org.everit.jsonschema.loader.SchemaLoader;
import org.junit.Assert;
import org.junit.Test;

public class JsoniterApiTest {

    @Test
    public void testLoadJson() {
        JsoniterApi jsoniterApi = new JsoniterApi();

        String jsonSchema = "{\n" +
                "  \"$schema\": \"http://schema.sbsp.io/sbsp-schema-base.json#\",\n" +
                "  \"id\": \"http://schema.sbsp.io/sbsp-account-profile.json#\",\n" +
                "  \"version\": \"0.0.0\",\n" +
                "  \"title\": \"Profile\",\n" +
                "  \"description\": \"Information about your company\",\n" +
                "  \"properties\": {\n" +
                "    \"name\": {\n" +
                "      \"$ref\": \"/primitives.json#/definitions/phone\"\n" +
                "    },\n" +
                "    \"contact\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"properties\": {\n" +
                "        \"first_name\": {\n" +
                "          \"$ref\": \"/primitives.json#/definitions/phone\"\n" +
                "        },\n" +
                "        \"last_name\": {\n" +
                "          \"$ref\": \"/primitives.json#/definitions/phone\"\n" +
                "        },\n" +
                "        \"email\": {\n" +
                "          \"$ref\": \"/primitives.json#/definitions/email\"\n" +
                "        },\n" +
                "        \"phone\": {\n" +
                "          \"$ref\": \"/primitives.json#/definitions/phone\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"required\": [\n" +
                "        \"first_name\",\n" +
                "        \"last_name\",\n" +
                "        \"email\",\n" +
                "        \"phone\"\n" +
                "      ]\n" +
                "    },\n" +
                "    \"company\": {\n" +
                "      \"$ref\": \"/primitives.json#/definitions/url\"\n" +
                "    },\n" +
                "    \"primary_color\": {\n" +
                "      \"allOf\": [\n" +
                "        {\n" +
                "          \"$ref\": \"/primitives.json#/definitions/color\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"title\": \"Primary Color\",\n" +
                "          \"description\": \"Your primary color\",\n" +
                "          \"minLuminance\": 100\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    \"secondary_color\": {\n" +
                "      \"$ref\": \"/primitives.json#/definitions/color\"\n" +
                "    },\n" +
                "    \"logo_url\": {\n" +
                "      \"$ref\": \"/primitives.json#/definitions/imageUrl\"\n" +
                "    },\n" +
                "    \"website_url\": {\n" +
                "      \"$ref\": \"/primitives.json#/definitions/url\"\n" +
                "    },\n" +
                "    \"time_zone\": {\n" +
                "      \"$ref\": \"/primitives.json#/definitions/timeZone\"\n" +
                "    },\n" +
                "    \"locale\": {\n" +
                "      \"$ref\": \"/primitives.json#/definitions/languageTag\"\n" +
                "    }\n" +
                "  }\n" +
                "}\n";

        String faker = "{\n" +
                "  \"$schema\": \"http://schema.sbsp.io/sbsp-schema-base.json#\",\n" +
                "  \"id\": \"http://schema.sbsp.io/sbsp-account-profile.json#\",\n" +
                "  \"version\": \"0.0.0\",\n" +
                "  \"title\": \"Profile\",\n" +
                "  \"description\": \"Information about your company\",\n" +
                "  \"properties\": {\n" +
                "    \"name\": {\n" +
                "      \"$ref\": \"/primitives.json#/definitions/phone\"\n" +
                "    },\n" +
                "    \"contact\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"properties\": {\n" +
                "        \"first_name\": {\n" +
                "          \"$ref\": \"/primitives.json#/definitions/phone\"\n" +
                "        },\n" +
                "        \"last_name\": {\n" +
                "          \"$ref\": \"/primitives.json#/definitions/phone\"\n" +
                "        },\n" +
                "        \"email\": {\n" +
                "          \"$ref\": \"/primitives.json#/definitions/email\"\n" +
                "        },\n" +
                "        \"phone\": {\n" +
                "          \"$ref\": \"/primitives.json#/definitions/phone\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"required\": [\n" +
                "        \"first_name\",\n" +
                "        \"last_name\",\n" +
                "        \"email\",\n" +
                "        \"phone\"\n" +
                "      ]\n" +
                "    },\n" +
                "    \"company\": {\n" +
                "      \"$ref\": \"/primitives.json#/definitions/url\"\n" +
                "    },\n" +
                "    \"primary_color\": {\n" +
                "      \"allOf\": [\n" +
                "        {\n" +
                "          \"$ref\": \"/primitives.json#/definitions/color\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"title\": \"Primary Color\",\n" +
                "          \"description\": \"Your primary color\",\n" +
                "          \"minLuminance\": 100\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    \"secondary_color\": {\n" +
                "      \"$ref\": \"/primitives.json#/definitions/color\"\n" +
                "    },\n" +
                "    \"logo_url\": {\n" +
                "      \"$ref\": \"/primitives.json#/definitions/imageUrl\"\n" +
                "    },\n" +
                "    \"website_url\": {\n" +
                "      \"$ref\": \"/primitives.json#/definitions/url\"\n" +
                "    },\n" +
                "    \"time_zoner\": {\n" +
                "      \"$ref\": \"/primitives.json#/definitions/timeZone\"\n" +
                "    },\n" +
                "    \"locale\": {\n" +
                "      \"$ref\": \"/primitives.json#/definitions/languageTag\"\n" +
                "    }\n" +
                "  }\n" +
                "}\n";

        Schema loadedSchema = SchemaLoader.load(jsonSchema, new JsoniterApi());
        Schema fakerSchema = SchemaLoader.load(faker, new JsoniterApi());
        final JsonMapWriter jsonWriter = loadedSchema.describeTo(new JsonMapWriter());
        Any wrap = Any.wrap(jsonWriter.getRoot());
        Config config = Config.INSTANCE.copyBuilder()
                .indentionStep(2)
                .build();
        String serialized = JsonStream.serialize(config, wrap);
        System.out.println(serialized);
        Schema reloaded = SchemaLoader.load(serialized, new JsoniterApi());

        Assert.assertEquals(loadedSchema, reloaded);
        Assert.assertNotEquals(fakerSchema, loadedSchema);
        Assert.assertNotEquals(reloaded, fakerSchema);

    }

}