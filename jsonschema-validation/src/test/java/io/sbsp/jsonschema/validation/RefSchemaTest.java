/*
 * Copyright (C) 2017 SBSP (http://sbsp.io)
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
package io.sbsp.jsonschema.validation;

import io.sbsp.jsonschema.RefSchema;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.loading.SchemaLoaderImpl;
import io.sbsp.jsonschema.utils.JsonUtils;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

import javax.json.JsonObject;

import static io.sbsp.jsonschema.ResourceLoader.*;
import static io.sbsp.jsonschema.loading.SchemaLoaderImpl.schemaLoader;
import static org.junit.Assert.assertEquals;

public class RefSchemaTest {

    @Test
    public void toStringTest() {
        JsonObject rawSchemaJson = resourceLoader().readJsonObject("tostring/ref.json");
        final Schema schema = SchemaLoaderImpl.schemaLoader().readSchema(rawSchemaJson);
        String actual = schema.toString();
        System.out.println(actual);
        assertEquals(rawSchemaJson.get("properties"),
                JsonUtils.readJsonObject(actual).getJsonObject("properties"));
    }

    @Test
    public void equalsTest() {
        EqualsVerifier.forClass(RefSchema.class)
                .withOnlyTheseFields("refURI")
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }
}
