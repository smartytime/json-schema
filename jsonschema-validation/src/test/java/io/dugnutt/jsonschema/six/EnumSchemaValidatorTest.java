/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dugnutt.jsonschema.six;

import io.dugnutt.jsonschema.utils.JsonUtils;
import io.dugnutt.jsonschema.validator.EnumSchemaValidator;
import io.dugnutt.jsonschema.validator.SchemaValidatorFactory;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import static io.dugnutt.jsonschema.six.ValidationTestSupport.failureOf;
import static io.dugnutt.jsonschema.six.ValidationTestSupport.expectSuccess;
import static io.dugnutt.jsonschema.utils.JsonUtils.blankJsonArray;
import static io.dugnutt.jsonschema.utils.JsonUtils.jsonArray;
import static io.dugnutt.jsonschema.utils.JsonUtils.jsonObjectBuilder;
import static io.dugnutt.jsonschema.utils.JsonUtils.jsonStringValue;
import static io.dugnutt.jsonschema.utils.JsonUtils.readValue;
import static javax.json.spi.JsonProvider.provider;
import static org.junit.Assert.assertEquals;

public class EnumSchemaValidatorTest {

    private JsonArrayBuilder possibleValues;

    @Before
    public void before() {
        possibleValues = provider().createArrayBuilder()
                .add(JsonValue.TRUE)
                .add("foo");
    }

    @Test
    public void equalsVerifier() {
        EqualsVerifier.forClass(EnumSchema.class)
                .withRedefinedSuperclass()
                .withIgnoredFields("schemaLocation")
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }

    @Test
    public void failure() {
        failureOf(subject())
                .expectedPointer("#")
                .expectedKeyword("enum")
                .input(readValue("[1]"))
                .expect();
    }

    @Test
    public void objectInArrayMatches() {
        JsonArray possibleValues = this.possibleValues
                .add(jsonObjectBuilder()
                        .add("a", true)
                        .build())
                .build();

        EnumSchema subject = subject().build();

        JsonArray testValues = provider().createArrayBuilder()
                .add(jsonObjectBuilder()
                        .add("a", true)
                        .build())
                .build();
        expectSuccess(() -> SchemaValidatorFactory.createValidatorForSchema(subject).validate(testValues));
    }

    @Test
    public void success() {
        possibleValues.add(blankJsonArray());
        final JsonValue validJsonObject = JsonUtils.readValue("{\"a\" : 0}");
        possibleValues.add(validJsonObject);
        EnumSchemaValidator subject = new EnumSchemaValidator(subject().build());
        expectSuccess(() -> subject.validate(JsonValue.TRUE));
        expectSuccess(() -> subject.validate(jsonStringValue("foo")));
        expectSuccess(() -> subject.validate(blankJsonArray()));
        expectSuccess(() -> subject.validate(validJsonObject));
    }

    @Test
    public void toStringTest() {
        StringWriter buffer = new StringWriter();
        final JsonGenerator generator = provider().createGenerator(buffer);
        subject().build().toJson(generator);
        JsonObject actual = JsonUtils.readJsonObject(buffer.getBuffer().toString());
        Assert.assertEquals(2, actual.keySet().size());
        Assert.assertEquals("enum", actual.getString("type"));
        JsonArray pv = jsonArray(true, "foo");
        assertEquals(asSet(pv), asSet(actual.getJsonArray("enum")));
        Assert.fail("Check toString");
    }

    private EnumSchema.Builder subject() {
        return EnumSchema.builder().possibleValues(possibleValues.build());
    }

    private Set<Object> asSet(final JsonArray array) {
        return new HashSet<>(JsonUtils.extractArray(array));
    }
}
