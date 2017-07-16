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

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

import javax.json.JsonValue;

import static io.dugnutt.jsonschema.six.ValidationTestSupport.expectSuccess;
import static io.dugnutt.jsonschema.utils.JsonUtils.jsonStringValue;
import static io.dugnutt.jsonschema.validator.SchemaValidatorFactory.createValidatorForSchema;
import static org.junit.Assert.assertEquals;

public class NotSchemaTest {

    @Test
    public void failure() {
        NotSchema subject = ValidationTestSupport.buildWithLocation(NotSchema.builder().mustNotMatch(BooleanSchema.BOOLEAN_SCHEMA));
        ValidationTestSupport.failureOf(subject)
                .input(JsonValue.TRUE)
                .expectedKeyword("not")
                .expect();
    }

    @Test
    public void success() {
        final NotSchema notSchema = NotSchema.builder().mustNotMatch(BooleanSchema.BOOLEAN_SCHEMA).build();
        expectSuccess(() -> createValidatorForSchema(notSchema).validate(jsonStringValue("foo")));;
    }

    @Test
    public void equalsVerifier() {
        EqualsVerifier.forClass(NotSchema.class)
                .withRedefinedSuperclass()
                .withIgnoredFields("schemaLocation")
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }

    @Test
    public void toStringTest() {
        NotSchema subject = NotSchema.builder()
                .mustNotMatch(BooleanSchema.BOOLEAN_SCHEMA)
                .build();
        String actual = subject.toString();
        assertEquals("{\"not\":{\"type\":\"boolean\"}}", actual);
    }

}
