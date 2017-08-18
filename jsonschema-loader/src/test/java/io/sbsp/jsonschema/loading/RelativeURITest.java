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

import io.sbsp.jsonschema.ServletSupport;
import org.junit.After;
import org.junit.Test;

import javax.json.JsonObject;
import java.net.URISyntaxException;

import static io.sbsp.jsonschema.ResourceLoader.resourceLoaderForInstance;

public class RelativeURITest {

    private ServletSupport servletSupport;
    @Test
    public void test() throws URISyntaxException {
        servletSupport = ServletSupport.withDocumentRoot("/io/sbsp/jsonschema/loading/relative-uri/");
        servletSupport.run(this::run);
    }

    @After
    public void after() {
        servletSupport.stopJetty();
    }

    private void run() {
        SchemaReader schemaLoader = SchemaLoaderImpl.schemaLoader();
        JsonObject jsonObject = resourceLoaderForInstance(this).readJsonObject("relative-uri/schema/main.json");
        schemaLoader.readSchema(jsonObject);
    }
}
