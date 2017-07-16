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
package io.dugnutt.json.schema;

import io.dugnutt.jsonschema.loader.SchemaLoader;
import io.dugnutt.jsonschema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@RunWith(Parameterized.class)
public class IssueTest {

    @Parameters(name = "{1}")
    public static List<Object[]> params() {
        List<Object[]> rval = new ArrayList<>();
        try {
            File issuesDir = new File(
                    IssueTest.class.getResource("/org/everit/json/schema/issues").toURI());
            for (File issue : issuesDir.listFiles()) {
                rval.add(new Object[] { issue, issue.getName() });
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return rval;
    }

    private final File issueDir;

    private io.dugnutt.jsonschema.ServletSupport servletSupport;

    private List<String> validationFailureList;
    private List<String> expectedFailureList;

    public IssueTest(final File issueDir, final String ignored) {
        this.issueDir = requireNonNull(issueDir, "issueDir cannot be null");
    }

    private Optional<File> fileByName(final String fileName) {
        return Arrays.stream(issueDir.listFiles())
                .filter(file -> file.getName().equals(fileName))
                .findFirst();
    }

    private void initJetty(final File documentRoot) {
        servletSupport = new io.dugnutt.jsonschema.ServletSupport(documentRoot);
        servletSupport.initJetty();
    }

    private Schema loadSchema() {
        Optional<File> schemaFile = fileByName("schema.json");
        try {
            if (schemaFile.isPresent()) {
                JSONObject schemaObj = new JSONObject(
                        new JSONTokener(new FileInputStream(schemaFile.get())));
                return SchemaLoader.load(schemaObj);
            }
            throw new RuntimeException(issueDir.getCanonicalPath() + "/schema.json is not found");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void stopJetty() {
        if (servletSupport != null) {
            servletSupport.stopJetty();
        }
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

    private void validate(final File file, final Schema schema, final boolean shouldBeValid) {
        ValidationException thrown = null;

        Object subject = loadJsonFile(file);

        try {
            schema.validate(subject);
        } catch (ValidationException e) {
            thrown = e;
        }

        if (shouldBeValid && thrown != null) {
            StringBuilder failureBuilder = new StringBuilder("validation failed with: " + thrown);
            for (ValidationException e : thrown.getCausingExceptions()) {
                failureBuilder.append("\n\t").append(e.getMessage());
            }
            Assert.fail(failureBuilder.toString());
        }
        if (!shouldBeValid && thrown != null) {
            Optional<File> expectedFile = fileByName("expectedException.json");
            if (expectedFile.isPresent()) {
                if (!checkExpectedValues(expectedFile.get(), thrown)) {
                    Assert.fail("Validation failures do not match expected values: \n" +
                            "Expected: " + expectedFailureList + ",\nActual:   " +
                            validationFailureList);
                }
            }
        }
        if (!shouldBeValid && thrown == null) {
            Assert.fail("did not throw ValidationException for invalid subject");
        }
    }

    // TODO - it would be nice to see this moved out of tests to the main
    // source so that it can be used as a convenience method by users also...
    private Object loadJsonFile(final File file) {

        Object subject = null;

        try {
            JSONTokener jsonTok = new JSONTokener(new FileInputStream(file));

            // Determine if we have a single JSON object or an array of them
            Object jsonTest = jsonTok.nextValue();
            if (jsonTest instanceof JSONObject) {
                // The message contains a single JSON object
                subject = jsonTest;
            } else if (jsonTest instanceof JSONArray) {
                // The message contains a JSON array
                subject = jsonTest;
            }
        } catch (JSONException e) {
            throw new RuntimeException("failed to parse subject json file", e);
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
        return subject;
    }

    /**
     * Allow users to provide expected values for validation failures. This method reads and parses
     * files formatted like the following:
     * <p>
     * { "message": "#: 2 schema violations found", "causingExceptions": [ { "message": "#/0/name:
     * expected type: STRING, found: JSONArray", "causingExceptions": [] }, { "message": "#/1:
     * required key [price] not found", "causingExceptions": [] } ] }
     * <p>
     * The expected contents are then compared against the actual validation failures reported in the
     * ValidationException and nested causingExceptions.
     */
    private boolean checkExpectedValues(final File expectedExceptionsFile,
            final ValidationException ve) {

        // Read the expected values from user supplied file
        Object expected = loadJsonFile(expectedExceptionsFile);
        expectedFailureList = new ArrayList<String>();
        // NOTE: readExpectedValues() will update expectedFailureList
        readExpectedValues((JSONObject) expected);

        // Read the actual validation failures into a list
        validationFailureList = new ArrayList<String>();
        // NOTE: processValidationFailures() will update validationFailureList
        processValidationFailures(ve);

        // Compare expected to actual
        return expectedFailureList.equals(validationFailureList);
    }

    // Recursively process the ValidationExceptions, which can contain lists
    // of sub-exceptions...
    // TODO - it would be nice to see this moved out of tests to the main
    // source so that it can be used as a convenience method by users also...
    private void processValidationFailures(final ValidationException ve) {
        List<ValidationException> causes = ve.getCausingExceptions();
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
    private void readExpectedValues(final JSONObject expected) {
        expectedFailureList.add((String) expected.get("message"));
        if (expected.has("causingExceptions")) {
            JSONArray causingEx = expected.getJSONArray("causingExceptions");
            for (Object subJson : causingEx) {
                readExpectedValues((JSONObject) subJson);
            }
        }
    }

}
