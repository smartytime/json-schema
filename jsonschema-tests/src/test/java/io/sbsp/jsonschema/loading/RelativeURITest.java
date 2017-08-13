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
package io.sbsp.jsonschema.loading;

import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.ServletSupport;
import io.sbsp.jsonschema.utils.JsonUtils;
import org.junit.Test;

import javax.json.JsonObject;
import java.net.URISyntaxException;

public class RelativeURITest {

    @Test
    public void test() throws URISyntaxException {
        ServletSupport.withDocumentRoot("/io/sbsp/jsonschema/relative-uri/")
                .run(this::run);
    }

    private void run() {
        JsonSchemaFactory schemaFactory = JsonSchemaFactory
                .schemaFactory();
        SchemaLocation schemaLocation = SchemaLocation.documentRoot("http://localhost:1234/schema/");
        JsonObject jsonObject = JsonUtils.readResourceAsJson("/io/sbsp/jsonschema/relative-uri/schema/main.json", JsonObject.class);
        final LoadingReport report = new LoadingReport();
        schemaFactory.loadRootSchema(schemaLocation, jsonObject);
    }
}
