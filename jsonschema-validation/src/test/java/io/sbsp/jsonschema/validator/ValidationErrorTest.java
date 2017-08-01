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
package io.sbsp.jsonschema.validator;

import io.sbsp.jsonschema.six.JsonPath;
import io.sbsp.jsonschema.six.Schema;
import lombok.experimental.var;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonObject;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static io.sbsp.jsonschema.six.Schema.jsonSchemaBuilder;
import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.TYPE;
import static io.sbsp.jsonschema.validator.ValidationMocks.mockBooleanSchema;
import static io.sbsp.jsonschema.validator.ValidationMocks.mockNullSchema;
import static io.sbsp.jsonschema.validator.ValidationTestSupport.expectSuccess;
import static io.sbsp.jsonschema.validator.ValidationTestSupport.verifyFailure;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class ValidationErrorTest {

    public static final ResourceLoader loader = ResourceLoader.DEFAULT;
    private final Schema rootSchema = jsonSchemaBuilder().build();

    @Test
    public void testConstructor() {
        ValidationError exc = createTestValidationError();
        Assert.assertEquals("#", exc.getPointerToViolation());
    }

    @Test
    public void testToJson() {
        ValidationError subject = ValidationError.validationBuilder().
                violatedSchema(mockBooleanSchema().build())
                .uriFragmentPointerToViolation("#/a/b")
                .message("exception message")
                .keyword(TYPE)
                .build();

        JsonObject expected = loader.readObj("exception-to-json.json");
        JsonObject actual = subject.toJson();
        assertEquals(expected, actual);
    }

    @Test
    public void testToJsonWithSchemaLocation() {
        ValidationError subject =
                new ValidationError(mockBooleanSchema().build(),
                        JsonPath.parseFromURIFragment("#/a/b"),
                        "exception message",
                        emptyList(),
                        TYPE,
                        "code",
                        URI.create("#/schema/location"),
                        emptyList()
                );
        JsonObject expected = loader.readObj("exception-to-json-with-schema-location.json");
        JsonObject actual = subject.toJson();
        assertThat(expected)
                .isEqualTo(actual);
    }

    @Test
    public void throwForMultipleFailures() {
        ValidationError input1 = new ValidationError(mockNullSchema().build(),
                JsonPath.rootPath(),
                "msg1",
                emptyList(),
                TYPE,
                "code",
                URI.create("#"), emptyList());

        ValidationError input2 = new ValidationError(mockBooleanSchema().build(),
                JsonPath.parseFromURIFragment("#"),
                "msg2",
                emptyList(),
                TYPE,
                "code",
                URI.create("#"), emptyList());
        final ValidationError e = ValidationError.collectErrors(rootSchema, JsonPath.rootPath(), Arrays.asList(input1, input2))
                .orElseThrow(() -> new AssertionError("Should have failed"));
        Assert.assertSame(rootSchema, e.getViolatedSchema());
        Assert.assertEquals("#: 2 schema violations found", e.getMessage());
        List<ValidationError> causes = e.getCauses();
        Assert.assertEquals(2, causes.size());
        Assert.assertSame(input1, causes.get(0));
        Assert.assertSame(input2, causes.get(1));
    }

    @Test
    public void throwForNoFailure() {
        expectSuccess(() -> ValidationError.collectErrors(rootSchema, JsonPath.rootPath(), emptyList()));
    }

    @Test
    public void throwForSingleFailure() {
        ValidationError input = new ValidationError(mockNullSchema().build(),
                JsonPath.parseFromURIFragment("#"),
                "msg",
                emptyList(),
                TYPE,
                "code",
                URI.create("#"),
                emptyList());
        var actual = verifyFailure(() -> ValidationError.collectErrors(rootSchema, JsonPath.rootPath(), newArrayList(input)));
        Assert.assertSame(input, actual);
    }

    @Test
    public void toJsonNullPointerToViolation() {
        ValidationError subject =
                new ValidationError(mockBooleanSchema().build(),
                        null,
                        "exception message",
                        emptyList(),
                        TYPE,
                        "code",
                        null,
                        emptyList());
        JsonObject actual = subject.toJson();
        Assert.assertEquals(JsonObject.NULL, actual.get("pointerToViolation"));
    }

    @Test
    public void toJsonWithCauses() {
        ValidationError cause =
                new ValidationError(mockNullSchema().build(),
                        JsonPath.parseFromURIFragment("#/a/0"),
                        "cause msg",
                        emptyList(),
                        TYPE,
                        null,
                        null, singletonList("Joe"));
        ValidationError subject =
                new ValidationError(mockBooleanSchema().build(),
                        JsonPath.parseFromURIFragment("#/a"),
                        "exception message",
                        Arrays.asList(cause),
                        TYPE,
                        "code",
                        null,
                        emptyList());
        JsonObject expected = ResourceLoader.DEFAULT.readObj("exception-to-json-with-causes.json");
        JsonObject actual = subject.toJson();
        assertEquals(expected, actual);
    }

    @Test
    public void toStringWithCauses() {
        ValidationError subject =
                subjectWithCauses(subjectWithCauses(subjectWithCauses(), subjectWithCauses()),
                        subjectWithCauses());
        Assert.assertEquals("#: 3 schema violations found", subject.getMessage());
    }

    @Test
    public void violationCountWithCauses() {
        ValidationError subject = subjectWithCauses(subjectWithCauses(), subjectWithCauses());
        Assert.assertEquals(2, subject.getViolationCount());
    }

    @Test
    public void violationCountWithNestedCauses() {
        ValidationError subject =
                subjectWithCauses(
                        subjectWithCauses(),
                        subjectWithCauses(subjectWithCauses(),
                                subjectWithCauses(subjectWithCauses(), subjectWithCauses())));
        Assert.assertEquals(4, subject.getViolationCount());
    }

    @Test
    public void violationCountWithoutCauses() {
        ValidationError subject = subjectWithCauses();
        Assert.assertEquals(1, subject.getViolationCount());
    }

    private ValidationError createTestValidationError() {
        return new ValidationError(mockBooleanSchema().build(),
                JsonPath.parseFromURIFragment("#"),
                "Failed Validation",
                emptyList(),
                TYPE,
                "code",
                URI.create("#"), emptyList());
    }

    private ValidationError createDummyException(final String pointer) {
        return new ValidationError(mockBooleanSchema().build(),
                JsonPath.parseFromURIFragment(pointer),
                "stuff went wrong",
                emptyList(), TYPE,
                "code",
                URI.create("#"),
                emptyList()
        );
    }

    private ValidationError subjectWithCauses(final ValidationError... causes) {
        if (causes.length == 0) {
            return new ValidationError(mockBooleanSchema().build(),
                    JsonPath.parseFromURIFragment("#"),
                    "Failure",
                    emptyList(),
                    TYPE,
                    "code",
                    URI.create("#"), emptyList());
        }
        return ValidationError.collectErrors(rootSchema, JsonPath.rootPath(), Arrays.asList(causes)).orElse(null);
    }
}
