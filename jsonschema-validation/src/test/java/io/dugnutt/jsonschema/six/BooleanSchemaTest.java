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
import org.junit.Assert;
import org.junit.Test;

import static io.dugnutt.jsonschema.six.SchemaLocation.rootSchemaLocation;
import static io.dugnutt.jsonschema.six.ValidationTestSupport.*;

public class BooleanSchemaTest {

    @Test
    public void failure() {
        failureOf(BooleanSchema.builder(rootSchemaLocation()))
                .expectedKeyword("type")
                .input("false")
                .expect();
    }

    @Test
    public void success() {
        expectSuccess(BooleanSchema.BOOLEAN_SCHEMA, true);
    }

    @Test
    public void toStringTest() {
        Assert.assertEquals("{\"type\":\"boolean\"}", BooleanSchema.BOOLEAN_SCHEMA.toString());
    }

    public void equalsVerifier() {
        EqualsVerifier.forClass(BooleanSchema.class)
                .withRedefinedSuperclass()
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }

}
