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
package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.ServletSupport;
import io.dugnutt.jsonschema.six.SchemaLocation;
import io.dugnutt.jsonschema.utils.JsonUtils;
import org.junit.Test;

import javax.json.JsonObject;
import java.net.URISyntaxException;

public class RelativeURITest {

    @Test
    public void test() throws URISyntaxException {
        ServletSupport.withDocumentRoot("/io/dugnutt/jsonschema/relative-uri/")
                .run(this::run);
    }

    private void run() {
        JsonSchemaFactory schemaFactory = JsonSchemaFactory
                .schemaFactory();
        SchemaLocation schemaLocation = SchemaLocation.schemaLocation("http://localhost:1234/schema/");
        JsonObject jsonObject = JsonUtils.readResourceAsJson("/io/dugnutt/jsonschema/relative-uri/schema/main.json", JsonObject.class);
        schemaFactory.createSchema(schemaLocation, jsonObject);
    }
}
