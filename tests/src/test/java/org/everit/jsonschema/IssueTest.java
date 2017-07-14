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

import lombok.SneakyThrows;
import org.everit.json.JsonArray;
import org.everit.json.JsonElement;
import org.everit.json.JsonObject;
import org.everit.jsonschema.api.Schema;
import org.everit.jsonschema.loader.SchemaLoader;
import org.everit.jsonschema.loaders.jsoniter.JsoniterApi;
import org.everit.jsonschema.validator.SchemaValidator;
import org.everit.jsonschema.validator.SchemaValidatorFactory;
import org.everit.jsonschema.validator.ValidationError;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.io.FileInputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.everit.jsonschema.api.JsonSchemaType.Array;
import static org.everit.jsonschema.api.JsonSchemaType.Object;

@RunWith(Parameterized.class)
public class IssueTest {

    private final File issueDir;
    private ServletSupport servletSupport;
    private JsoniterApi jsoniterApi = new JsoniterApi();
    private List<String> validationFailureList;
    private List<String> expectedFailureList;

    public IssueTest(final File issueDir, final String ignored) {
        this.issueDir = requireNonNull(issueDir, "issueDir cannot be null");
    }

    @Parameters(name = "{1}")
    public static List<Object[]> params() {
        List<Object[]> rval = new ArrayList<>();
        try {
            File issuesDir = new File(
                    IssueTest.class.getResource("/org/everit/jsonschema/issues").toURI());
            for (File issue : issuesDir.listFiles()) {
                rval.add(new Object[] {issue, issue.getName()});
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return rval;
    }

    @Test
    public void test() {
        Assume.assumeFalse("issue dir starts with 'x' - ignoring", issueDir.getName().startsWith("x"));
        fileByName("remotes").ifPresent(this::initJetty);
        Schema schema = loadSchema();
        fileByName("subject-valid.json").ifPresent(file -> validate(file, schema, true));
        fileByName("subject-invalid.json").ifPresent(file -> validate(file, schema, false));
        stopJetty();
    }

    private Optional<File> fileByName(final String fileName) {
        return Arrays.stream(issueDir.listFiles())
                .filter(file -> file.getName().equals(fileName))
                .findFirst();
    }

    private void initJetty(final File documentRoot) {
        servletSupport = new ServletSupport(documentRoot);
        servletSupport.initJetty();
    }

    @SneakyThrows
    private Schema loadSchema() {
        Optional<File> schemaFile = fileByName("schema.json");
        if (schemaFile.isPresent()) {
            JsoniterApi api = new JsoniterApi();
            FileInputStream schemaStream = new FileInputStream(schemaFile.get());
            return SchemaLoader.load(schemaStream, api);
        }
        throw new RuntimeException(issueDir.getCanonicalPath() + "/schema.json is not found");
    }

    private void stopJetty() {
        if (servletSupport != null) {
            servletSupport.stopJetty();
        }
    }

    private void validate(final File file, final Schema schema, final boolean shouldBeValid) {
        JsonElement<?> subject = loadJsonFile(file);

        SchemaValidator<?> validator = SchemaValidatorFactory.findValidator(schema);
        Optional<ValidationError> errors = validator.validate(subject);

        if (shouldBeValid && errors.isPresent()) {
            StringBuilder failureBuilder = new StringBuilder("validation failed with: " + errors.get());
            for (ValidationError e : errors.get().getCauses()) {
                failureBuilder.append("\n\t").append(e.getMessage());
            }
            Assert.fail(failureBuilder.toString());
        }
        if (!shouldBeValid && errors.isPresent()) {
            Optional<File> expectedFile = fileByName("expectedException.json");
            if (expectedFile.isPresent()) {
                if (!checkExpectedValues(expectedFile.get(), errors.get())) {
                    Assert.fail("Validation failures do not match expected values: \n" +
                            "Expected: " + expectedFailureList + ",\nActual:   " +
                            validationFailureList);
                }
            }
        }
        if (!shouldBeValid && !errors.isPresent()) {
            Assert.fail("did not throw ValidationException for invalid subject");
        }
    }

    // TODO - it would be nice to see this moved out of tests to the main
    // source so that it cann be used as a convenience method by users also...
    @SneakyThrows
    private JsonElement<?> loadJsonFile(final File file) {
        JsonElement<?> subject = null;
        JsonElement<?> jsonElement = jsoniterApi.readJson(new FileInputStream(file), Charset.forName("UTF-8"));
        if (jsonElement.isAnyOf(Object, Array)) {
            subject = jsonElement;
        } else {
            throw new IllegalStateException("Unable to load tests of type: " + jsonElement.schemaType());
        }
        return subject;
    }

    /**
     * Allow users to provide expected values for validation failures. This method reads and parses
     * files formatted like the following:
     * <p>
     * { "message": "#: 2 schema violations found", "causingExceptions": [ { "message": "#/0/name:
     * expected type: String, found: JSONArray", "causingExceptions": [] }, { "message": "#/1:
     * required key [price] not found", "causingExceptions": [] } ] }
     * <p>
     * The expected contents are then compared against the actual validation failures reported in the
     * ValidationException and nested causingExceptions.
     */
    private boolean checkExpectedValues(final File expectedExceptionsFile,
                                        final ValidationError ve) {

        // Read the expected values from user supplied file
        JsonElement<?> expected = loadJsonFile(expectedExceptionsFile);
        expectedFailureList = new ArrayList<>();

        // NOTE: readExpectedValues() will update expectedFailureList
        readExpectedValues(expected.asObject());

        // Read the actual validation failures into a list
        validationFailureList = new ArrayList<>();
        // NOTE: processValidationFailures() will update validationFailureList
        processValidationFailures(ve);

        // Compare expected to actual
        return expectedFailureList.equals(validationFailureList);
    }

    // Recursively process the ValidationExceptions, which can contain lists
    // of sub-exceptions...
    // TODO - it would be nice to see this moved out of tests to the main
    // source so that it can be used as a convenience method by users also...
    private void processValidationFailures(final ValidationError ve) {
        List<ValidationError> causes = ve.getCauses();
        if (causes.isEmpty()) {
            // This was a leaf node, i.e. only one validation failure
            validationFailureList.add(ve.getMessage());
        } else {
            // Multiple validation failures exist, so process the sub-exceptions
            // to obtain them. NOTE: Not sure we should keep the message from
            // the current exception in this case. When there are causing
            // exceptions, the message in the containing exception is merely
            // summary information, e.g. "2 schema violations found".
            validationFailureList.add(ve.getMessage());
            causes.forEach(this::processValidationFailures);
        }
    }

    // Recursively process the expected values, which can contain nested arrays
    private void readExpectedValues(final JsonObject<?> expected) {
        expectedFailureList.add(expected.git("message").coerceToString());
        if (expected.hasKey("causingExceptions")) {
            JsonArray<?> causingEx = expected.git("causingExceptions").asArray();
            for (JsonElement<?> subJson : causingEx) {
                readExpectedValues(subJson.asObject());
            }
        }
    }
}
