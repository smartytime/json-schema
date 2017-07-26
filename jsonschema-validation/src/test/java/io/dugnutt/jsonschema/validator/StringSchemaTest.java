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

import io.dugnutt.jsonschema.validator.PartialSchemaValidator;
import io.dugnutt.jsonschema.validator.SchemaValidatorFactory;
import lombok.experimental.var;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonObject;
import java.util.Optional;

import static io.dugnutt.jsonschema.loader.JsonSchemaFactory.schemaFactory;
import static io.dugnutt.jsonschema.six.ResourceLoader.DEFAULT;
import static io.dugnutt.jsonschema.validator.ValidationTestSupport.buildWithLocation;
import static io.dugnutt.jsonschema.validator.ValidationTestSupport.expectSuccess;
import static io.dugnutt.jsonschema.validator.ValidationTestSupport.failureOf;
import static io.dugnutt.jsonschema.validator.ValidationTestSupport.verifyFailure;
import static io.dugnutt.jsonschema.utils.JsonUtils.jsonStringValue;
import static io.dugnutt.jsonschema.utils.JsonUtils.readJsonObject;
import static javax.json.spi.JsonProvider.provider;
import static org.junit.Assert.assertEquals;

public class StringSchemaTest {

    // @Test
    // public void stringSchemaWithFormat() {
    //     StringSchema subject = (StringSchema) getSchemaForKey("stringSchemaWithFormat");
    //     ValidationTestSupport.expectFailure(subject, "asd");
    // }
    //

    private final SchemaValidatorFactory validatorFactory = SchemaValidatorFactory.builder()
            .customFormatValidator("test-format-failure", sub -> Optional.of("violation"))
            .customFormatValidator("test-format-success", sub -> Optional.empty())
            .build();

    @Test
    public void formatFailure() {
        var schemaValidator = validatorFactory.createValidator(
                buildWithLocation(StringSchema.builder().format("test-format-failure"))
        );
        failureOf(schemaValidator)
                .expectedKeyword("format")
                .input("string")
                .expect();
    }

    @Test
    public void formatSuccess() {
        var schemaValidator = validatorFactory.createValidator(StringSchema.builder().format("test-format-success").build());
        expectSuccess(() -> schemaValidator.validate(jsonStringValue("string")));
    }

    public void issue38Pattern() {
        final StringSchema schema = StringSchema.builder().requiresString(true).pattern("\\+?\\d+").build();
        final PartialSchemaValidator<StringSchema> validator = validatorFactory.createValidator(schema);
        verifyFailure(() -> validator.validate(jsonStringValue("aaa")));
    }

    @Test
    public void maxLength() {
        StringSchema subject = buildWithLocation(StringSchema.builder().maxLength(3));
        failureOf(subject)
                .expectedKeyword("maxLength")
                .input("foobar")
                .expect();
    }

    @Test
    public void minLength() {
        StringSchema subject = buildWithLocation(StringSchema.builder().minLength(2));
        failureOf(subject)
                .expectedKeyword("minLength")
                .input("a")
                .expect();
    }

    @Test
    public void multipleViolations() {
        final StringSchema schema = StringSchema.builder().minLength(3).maxLength(1).pattern("^b.*").build();
        failureOf(schema)
                .input("ab")
                .expected(e -> {
                    Assert.assertEquals(3, e.getCauses().size());
                })
                .expect();
    }

    @Test
    public void notRequiresString() {
        final StringSchema schema = StringSchema.builder().requiresString(false).build();
        expectSuccess(schema, 2);
    }

    @Test
    public void patternFailure() {
        StringSchema subject = buildWithLocation(StringSchema.builder().pattern("^a*$"));
        failureOf(subject).expectedKeyword("pattern").input("abc").expect();
    }

    @Test
    public void patternSuccess() {

        final StringSchema schema = StringSchema.builder().pattern("^a*$").build();
        expectSuccess(schema, "aaaa");
    }

    @Test
    public void success() {

        expectSuccess(StringSchema.builder().build(), "foo");
    }

    @Test
    public void toStringNoExplicitType() {
        final JsonObject rawSchemaJson = provider().createObjectBuilder(DEFAULT.readObj("tostring/stringschema.json"))
                .remove("type")
                .build();
        final Schema schema = schemaFactory().load(rawSchemaJson);
        String actual = schemaFactory().load(schema.toString()).toString();
        assertEquals(rawSchemaJson, readJsonObject(actual));
    }

    @Test
    public void toStringTest() {
        JsonObject rawSchemaJson = DEFAULT.readObj("tostring/stringschema.json");
        String actual = schemaFactory().load(rawSchemaJson).toString();
        assertEquals(rawSchemaJson, readJsonObject(actual));
    }

    @Test
    public void typeFailure() {
        failureOf(StringSchema.builder())
                .expectedKeyword("type")
                .nullInput()
                .expect();
    }
}
