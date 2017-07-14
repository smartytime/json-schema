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
package org.everit.jsonschema;

import com.google.common.base.Preconditions;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.everit.json.JsonApi;
import org.everit.json.JsonArray;
import org.everit.json.JsonElement;
import org.everit.json.JsonObject;
import org.everit.json.schema.IssueServlet;
import org.everit.json.schema.ServletSupport;
import org.everit.jsonschema.api.Schema;
import org.everit.jsonschema.api.SchemaException;
import org.everit.jsonschema.loader.SchemaLoader;
import org.everit.jsonschema.loader.internal.DefaultSchemaClient;
import org.everit.jsonschema.loaders.jsoniter.JsoniterApi;
import org.everit.jsonschema.validator.SchemaValidator;
import org.everit.jsonschema.validator.SchemaValidatorFactory;
import org.everit.jsonschema.validator.ValidationError;
import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@RunWith(Parameterized.class)
public class TestSuiteTest {

    private static Server server;
    private static JsonApi<?> jsonApi = new JsoniterApi();
    private final String schemaDescription;
    private final JsonObject schemaJson;
    private final String inputDescription;
    private final JsonElement<?> input;
    private final boolean expectedToBeValid;

    public TestSuiteTest(final String schemaDescription, final JsonObject<?> schemaJson,
                         final String inputDescription, final JsonElement<?> input,
                         final Boolean expectedToBeValid) {
        this.schemaDescription = schemaDescription;
        this.schemaJson = schemaJson;
        this.inputDescription = inputDescription;
        this.input = input;
        this.expectedToBeValid = expectedToBeValid;
    }

    @Parameters(name = "{2}")
    public static List<Object[]> params() {
        Preconditions.checkNotNull( "jsonApi must not be null");
        List<Object[]> rval = new ArrayList<>();
        Reflections refs = new Reflections("org.everit.json.schema.draft4",
                new ResourcesScanner());
        Set<String> paths = refs.getResources(Pattern.compile(".*\\.json"));
        for (String path : paths) {
            if (path.contains("/optional/") || path.contains("/remotes/")) {
                continue;
            }
            String fileName = path.substring(path.lastIndexOf('/') + 1);
            JsonArray<?> arr = loadTests(jsonApi, TestSuiteTest.class.getResourceAsStream("/" + path));
            for (int i = 0; i < arr.length(); ++i) {
                JsonObject<?> schemaTest = arr.get(i).asObject();
                JsonArray<?> testcaseInputs = schemaTest.git("tests").asArray();
                for (int j = 0; j < testcaseInputs.length(); ++j) {
                    JsonObject<?> input = testcaseInputs.get(j).asObject();
                    Object[] params = new Object[5];
                    params[0] = "[" + fileName + "]/" + schemaTest.git("description").asString();
                    params[1] = schemaTest.git("schema").asObject();
                    params[2] = "[" + fileName + "]/" + input.git("description").asString();
                    params[3] = input.git("data");
                    params[4] = input.git("valid").asBoolean();
                    rval.add(params);
                }
            }
        }
        return rval;
    }

    @BeforeClass
    public static void startJetty() throws Exception {
        server = new Server(1234);
        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);
        handler.addServletWithMapping(new ServletHolder(new IssueServlet(new File(ServletSupport.class
                .getResource("/org/everit/json/schema/draft4/remotes").toURI()))), "/*");
        server.start();
    }

    @AfterClass
    public static void stopJetty() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    public void test() {
        try {
            Schema schema = SchemaLoader.load(schemaJson, new DefaultSchemaClient(), jsonApi);
            SchemaValidator<?> validator = SchemaValidatorFactory.findValidator(schema);
            Optional<ValidationError> validationErrors = validator.validate(input);
            boolean failed = validationErrors.isPresent();
            if (expectedToBeValid && failed) {
                throw new AssertionError("false failure for " + inputDescription + "\n" + validationErrors.get());
            }
            if (!expectedToBeValid && !failed) {
                throw new AssertionError("false success for " + inputDescription);
            }
        } catch (SchemaException e) {
            throw new AssertionError("schema loading failure for " + schemaDescription, e);
        } catch (JSONException e) {
            throw new AssertionError("schema loading error for " + schemaDescription, e);
        }
    }

    private static JsonArray<?> loadTests(JsonApi<?> jsonApi, final InputStream input) {
        return jsonApi.readJson(input, Charset.forName("UTF-8")).asArray();
    }
}
