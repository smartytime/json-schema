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

import io.sbsp.jsonschema.JsonPath;
import io.sbsp.jsonschema.Schema;
import lombok.experimental.var;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonObject;
import java.util.Arrays;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static io.sbsp.jsonschema.builder.JsonSchemaBuilder.jsonSchema;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.TYPE;
import static io.sbsp.jsonschema.validator.ValidationMocks.mockBooleanSchema;
import static io.sbsp.jsonschema.validator.ValidationMocks.mockNullSchema;
import static io.sbsp.jsonschema.validator.ValidationTestSupport.expectSuccess;
import static io.sbsp.jsonschema.validator.ValidationTestSupport.verifyFailure;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class ValidationErrorTest {

    public static final ResourceLoader loader = ResourceLoader.DEFAULT;
    private final Schema rootSchema = jsonSchema().build();

    @Test
    public void testConstructor() {
        ValidationError exc = createTestValidationError();
        Assert.assertEquals("#", exc.getPointerToViolation());
    }

    @Test
    public void testToJson() {
        ValidationError subject = ValidationError.validationBuilder().
                violatedSchema(mockBooleanSchema().build())
                .pointerToViolationURI("#/a/b")
                .message("exception message")
                .keyword(TYPE)
                .build();

        JsonObject expected = loader.readObj("exception-to-json.json");
        JsonObject actual = subject.toJson();
        assertEquals(expected, actual);
    }

    @Test
    public void testToJsonWithSchemaLocation() {
        Schema failedSchema = mockBooleanSchema("#/schema/location").build();
        ValidationError subject = ValidationError.validationBuilder()
                .violatedSchema(failedSchema)
                .code("code")
                .message("exception message")
                .keyword(TYPE)
                .pointerToViolationURI("#/a/b")
                .build();

        JsonObject expected = loader.readObj("exception-to-json-with-schema-location.json");
        JsonObject actual = subject.toJson();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void throwForMultipleFailures() {
        Schema failedSchema = mockNullSchema().build();
        ValidationError input1 =ValidationError.validationBuilder()
                .violatedSchema(failedSchema)
                .code("code")
                .message("msg1")
                .keyword(TYPE)
                .pointerToViolationURI("#")
                .build();

        ValidationError input2 =ValidationError.validationBuilder()
                .violatedSchema(failedSchema)
                .code("code")
                .message("msg2")
                .keyword(TYPE)
                .pointerToViolationURI("#")
                .build();

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
    public void collectError_WhenSingleFailure_ThenFailureIsReturned() {
        Schema failedSchema = mockNullSchema().build();
        ValidationError input =ValidationError.validationBuilder()
                .violatedSchema(failedSchema)
                .code("code")
                .message("msg")
                .keyword(TYPE)
                .pointerToViolationURI("#")
                .build();

        var actual = verifyFailure(() -> ValidationError.collectErrors(rootSchema, JsonPath.rootPath(), newArrayList(input)));
        Assert.assertSame(input, actual);
    }

    @Test
    public void toJsonNullPointerToViolation() {
        ValidationError subject = ValidationError.validationBuilder()
                .violatedSchema(mockBooleanSchema().build())
                .code("exception message")
                .message("msg")
                .keyword(null)
                .pointerToViolationURI(null)
                .build();
        JsonObject actual = subject.toJson();
        Assert.assertEquals(JsonObject.NULL, actual.get("pointerToViolation"));
    }

    @Test
    public void toJsonWithCauses() {
        ValidationError cause = ValidationError.validationBuilder()
                .violatedSchema(mockNullSchema().build())
                .code("code")
                .message("cause msg %s", "foo")
                .keyword(TYPE)
                .pointerToViolationURI("#/a/0")
                .argument("bar")
                .build();

        ValidationError subject = ValidationError.validationBuilder()
                .violatedSchema(mockNullSchema().build())
                .message("exception message")
                .keyword(null)
                .causingException(cause)
                .pointerToViolationURI("#/a")
                .build();

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
        return ValidationError.validationBuilder()
                .violatedSchema(mockBooleanSchema().build())
                .code("code")
                .message("Failed Validation")
                .keyword(TYPE)
                .pointerToViolationURI("#")
                .build();

    }

    private ValidationError createDummyException(final String pointer) {
        return ValidationError.validationBuilder()
                .violatedSchema(mockBooleanSchema().build())
                .code("code")
                .message("stuff went wrong")
                .keyword(TYPE)
                .pointerToViolationURI(pointer)
                .build();
    }

    private ValidationError subjectWithCauses(final ValidationError... causes) {
        if (causes.length == 0) {
            return ValidationError.validationBuilder()
                    .violatedSchema(mockBooleanSchema().build())
                    .code("code")
                    .message("Failure")
                    .keyword(TYPE)
                    .pointerToViolationURI("#")
                    .build();
        }
        return ValidationError.collectErrors(rootSchema, JsonPath.rootPath(), Arrays.asList(causes)).orElse(null);
    }
}
