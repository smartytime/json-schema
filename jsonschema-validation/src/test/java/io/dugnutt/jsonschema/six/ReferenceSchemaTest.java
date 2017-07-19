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
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonObject;

import static io.dugnutt.jsonschema.loader.JsonSchemaFactory.schemaFactory;
import static io.dugnutt.jsonschema.six.SchemaLocation.rootSchemaLocation;
import static org.junit.Assert.assertEquals;

public class ReferenceSchemaTest {

    @Test
    public void constructorMustRunOnlyOnce() {
        ReferenceSchema.Builder builder = ReferenceSchema.builder(rootSchemaLocation());
        Assert.assertSame(builder.build(), builder.build());
    }

    @Test
    public void equalsVerifier() {
        EqualsVerifier.forClass(ReferenceSchema.class)
                .withRedefinedSuperclass()
               .withIgnoredFields("location")
                //there are specifically some non final fields for loading of recursive schemas
                .suppress(Warning.NONFINAL_FIELDS)
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }

    @Test(expected = IllegalStateException.class)
    public void setterShouldWorkOnlyOnce() {
        Assert.fail("Not there anymore");
        ReferenceSchema subject = ReferenceSchema.builder(SchemaLocation.rootSchemaLocation()).build();
    }

    @Test
    public void toStringTest() {
        JsonObject rawSchemaJson = JsonUtils.readResourceAsJson("tostring/ref.json", JsonObject.class);
        String actual = schemaFactory().load(rawSchemaJson).toString();
        System.out.println(actual);
        assertEquals(rawSchemaJson.get("/properties"),
                JsonUtils.readJsonObject(actual).getJsonObject("properties"));
    }
}
