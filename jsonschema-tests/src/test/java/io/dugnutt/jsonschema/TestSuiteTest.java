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
package io.dugnutt.jsonschema;

import com.google.common.base.Preconditions;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.six.SchemaException;
import io.dugnutt.jsonschema.validator.SchemaValidator;
import io.dugnutt.jsonschema.validator.ValidationError;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import static io.dugnutt.jsonschema.ValidationMocks.createTestValidator;
import static io.dugnutt.jsonschema.loader.JsonSchemaFactory.schemaFactory;

@RunWith(Parameterized.class)
public class TestSuiteTest {

    private static Server server;
    private final String schemaDescription;
    private final JsonObject schemaJson;
    private final String inputDescription;
    private final JsonValue input;
    private final boolean expectedToBeValid;

    public TestSuiteTest(final String schemaDescription, final JsonObject schemaJson,
                         final String inputDescription, final JsonValue input,
                         final Boolean expectedToBeValid) {
        this.schemaDescription = schemaDescription;
        this.schemaJson = schemaJson;
        this.inputDescription = inputDescription;
        this.input = input;
        this.expectedToBeValid = expectedToBeValid;
    }

    @Parameters(name = "{2}")
    public static List<Object[]> params() {
        Preconditions.checkNotNull("jsonApi must not be null");
        List<Object[]> rval = new ArrayList<>();
        Reflections refs = new Reflections("org.everit.json.schema.draft4",
                new ResourcesScanner());
        Set<String> paths = refs.getResources(Pattern.compile(".*\\.json"));
        for (String path : paths) {
            if (path.contains("/optional/") || path.contains("/remotes/")) {
                continue;
            }
            String fileName = path.substring(path.lastIndexOf('/') + 1);
            JsonArray arr = loadTests(TestSuiteTest.class.getResourceAsStream("/" + path));
            for (JsonObject schemaTest : arr.getValuesAs(JsonObject.class)) {
                JsonArray testInputs = schemaTest.getJsonArray("tests");
                for (JsonObject input : testInputs.getValuesAs(JsonObject.class)) {

                    Object[] params = new Object[5];
                    params[0] = "[" + fileName + "]/" + schemaTest.getString("description");
                    params[1] = schemaTest.getJsonObject("schema");
                    params[2] = "[" + fileName + "]/" + input.getString("description");
                    params[3] = input.get("data");
                    params[4] = input.getBoolean("valid");
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
            Schema schema = schemaFactory().load(schemaJson);
            SchemaValidator validator = createTestValidator(schema);
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
        } catch (JsonException e) {
            throw new AssertionError("schema loading error for " + schemaDescription, e);
        }
    }

    private static JsonArray loadTests(final InputStream input) {
        return JsonProvider.provider().createReader(input).readArray();
    }
}
