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
package io.dugnutt.jsonschema.six;

import io.dugnutt.jsonschema.validator.ValidationError;
import lombok.experimental.var;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonObject;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static io.dugnutt.jsonschema.six.ValidationTestSupport.verifyFailure;
import static io.dugnutt.jsonschema.six.ValidationTestSupport.expectSuccess;
import static org.junit.Assert.assertEquals;

public class ValidationErrorTest {

    public static final ResourceLoader loader = ResourceLoader.DEFAULT;
    private final Schema rootSchema = ObjectSchema.builder(SchemaLocation.schemaLocation()).build();

    @Test
    public void fragmentEscapingBoth() {
        ValidationError subject = createDummyException("#/aaa").prepend("x~y/z");
        Assert.assertEquals("#/x~0y~1z/aaa", subject.getPointerToViolation());
    }

    @Test
    public void fragmentEscapingSlash() {
        ValidationError subject = createDummyException("#/aaa").prepend("x/y");
        Assert.assertEquals("#/x~1y/aaa", subject.getPointerToViolation());
    }

    @Test
    public void fragmentEscapingTilde() {
        ValidationError subject = createDummyException("#/aaa").prepend("x~y");
        Assert.assertEquals("#/x~0y/aaa", subject.getPointerToViolation());
    }

    @Test
    public void getMessageAfterPrepend() {
        ValidationError subject = createDummyException("#/a").prepend("obj");
        Assert.assertEquals("#/obj/a: stuff went wrong", subject.getMessage());
    }

    @Test(expected = NullPointerException.class)
    public void nullPointerFragmentFailure() {
        createTestValidationError().prepend(null,
                NullSchema.INSTANCE);
    }

    @Test
    public void prependNoSchemaChange() {
        ValidationError exc = createTestValidationError();
        ValidationError changedExc = exc.prepend("frag");
        Assert.assertEquals("#/frag", changedExc.getPointerToViolation());
        Assert.assertEquals("type", changedExc.getKeyword());
        Assert.assertEquals(BooleanSchema.BOOLEAN_SCHEMA, changedExc.getViolatedSchema());
    }

    @Test
    public void prependPointer() {
        ValidationError exc = createTestValidationError();
        ValidationError changedExc = exc.prepend("frag", NullSchema.INSTANCE);
        Assert.assertEquals("#/frag", changedExc.getPointerToViolation());
        Assert.assertEquals("type", changedExc.getKeyword());
        Assert.assertEquals(NullSchema.INSTANCE, changedExc.getViolatedSchema());
    }

    @Test
    public void prependWithCausingExceptions() {
        ValidationError cause1 = createDummyException("#/a");
        ValidationError cause2 = createDummyException("#/b");
        final ValidationError e = ValidationError.collectErrors(rootSchema, Arrays.asList(cause1, cause2))
                .orElseThrow(() -> new AssertionError("Should have returned errors"));

        ValidationError actual = e.prepend("rectangle");
        Assert.assertEquals("#/rectangle", actual.getPointerToViolation());
        ValidationError changedCause1 = actual.getCauses().get(0);
        Assert.assertEquals("#/rectangle/a", changedCause1.getPointerToViolation());
        ValidationError changedCause2 = actual.getCauses().get(1);
        Assert.assertEquals("#/rectangle/b", changedCause2.getPointerToViolation());
    }

    @Test
    public void testConstructor() {
        ValidationError exc = createTestValidationError();
        Assert.assertEquals("#", exc.getPointerToViolation());
    }

    @Test
    public void testToJson() {
        ValidationError subject = new ValidationError(BooleanSchema.BOOLEAN_SCHEMA,
                        new StringBuilder("#"),
                "exception message",
                        Collections.emptyList(),
                JsonSchemaKeyword.TYPE,
                        URI.create("#/a/b"));
        JsonObject expected = loader.readObj("exception-to-json.json");
        JsonObject actual = subject.toJson();
        assertEquals(expected, actual);
    }

    @Test
    public void testToJsonWithSchemaLocation() {
        ValidationError subject =
                new ValidationError(BooleanSchema.BOOLEAN_SCHEMA, new StringBuilder("#/a/b"),
                        "exception message", Collections.emptyList(), JsonSchemaKeyword.TYPE, "#/schema/location");
        JsonObject expected = loader.readObj("exception-to-json-with-schema-location.json");
        JsonObject actual = subject.toJson();
        assertEquals(expected, actual);
    }

    @Test
    public void throwForMultipleFailures() {
        ValidationError input1 = new ValidationError(NullSchema.INSTANCE,
                        new StringBuilder("#"),
                "msg1",
                        Collections.emptyList(),
                JsonSchemaKeyword.TYPE,
                        URI.create("#"));
        ValidationError input2 = new ValidationError(BooleanSchema.BOOLEAN_SCHEMA,
                        new StringBuilder("#"),
                "msg2",
                        Collections.emptyList(),
                JsonSchemaKeyword.TYPE,
                        URI.create("#"));
        final ValidationError e = ValidationError.collectErrors(rootSchema, Arrays.asList(input1, input2))
                .orElseThrow(() -> new AssertionError("Should have failed"));
        Assert.fail("did not throw exception for 2 input exceptions");
        Assert.assertSame(rootSchema, e.getViolatedSchema());
        Assert.assertEquals("#: 2 schema violations found", e.getMessage());
        List<ValidationError> causes = e.getCauses();
        Assert.assertEquals(2, causes.size());
        Assert.assertSame(input1, causes.get(0));
        Assert.assertSame(input2, causes.get(1));
    }

    @Test
    public void throwForNoFailure() {
        expectSuccess(() -> ValidationError.collectErrors(rootSchema, Collections.emptyList()));
        ;
    }

    @Test
    public void throwForSingleFailure() {
        ValidationError input = new ValidationError(NullSchema.INSTANCE,
                        new StringBuilder("#"),
                "msg",
                        Collections.emptyList(),
                JsonSchemaKeyword.TYPE,
                        URI.create("#"));
        var actual = verifyFailure(() -> ValidationError.collectErrors(rootSchema, newArrayList(input)));
        Assert.assertSame(input, actual);
    }

    @Test
    public void toJsonNullPointerToViolation() {
        ValidationError subject =
                new ValidationError(BooleanSchema.BOOLEAN_SCHEMA, null,
                        "exception message", Collections.emptyList(), JsonSchemaKeyword.TYPE, (URI) null);
        JsonObject actual = subject.toJson();
        Assert.assertEquals(JsonObject.NULL, actual.get("pointerToViolation"));
    }

    @Test
    public void toJsonWithCauses() {
        ValidationError cause =
                new ValidationError(NullSchema.INSTANCE,
                        new StringBuilder("#/a/0"),
                        "cause msg",
                        Collections.emptyList(),
                        JsonSchemaKeyword.TYPE,
                        (URI)null);
        ValidationError subject =
                new ValidationError(BooleanSchema.BOOLEAN_SCHEMA, new StringBuilder("#/a"),
                        "exception message", Arrays.asList(cause), JsonSchemaKeyword.TYPE, (URI) null);
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
        return new ValidationError(BooleanSchema.BOOLEAN_SCHEMA,
                        new StringBuilder("#"),
                "Failed Validation",
                        Collections.emptyList(),
                JsonSchemaKeyword.TYPE,
                        URI.create("#"));
    }

    private ValidationError createDummyException(final String pointer) {
        return new ValidationError(BooleanSchema.BOOLEAN_SCHEMA,
                new StringBuilder(pointer), "stuff went wrong", Collections.emptyList(), JsonSchemaKeyword.TYPE, "#");
    }

    private ValidationError subjectWithCauses(final ValidationError... causes) {
        if (causes.length == 0) {
            return new ValidationError(BooleanSchema.BOOLEAN_SCHEMA,
                            new StringBuilder("#"),
                    "Failure",
                            Collections.emptyList(),
                    JsonSchemaKeyword.TYPE,
                            URI.create("#"));
        }
        return ValidationError.collectErrors(rootSchema, Arrays.asList(causes)).orElse(null);
    }
}
