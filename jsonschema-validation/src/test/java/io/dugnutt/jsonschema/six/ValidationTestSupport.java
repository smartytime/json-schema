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
import org.junit.Assert;

import javax.json.JsonValue;
import java.util.List;
import java.util.Optional;

import static io.dugnutt.jsonschema.validator.SchemaValidatorFactory.findValidator;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ValidationTestSupport {

    public static <S extends Schema> S buildWithLocation(Schema.Builder<S> builder) {
        return builder.schemaLocation("#").build();
    }

    public static long countCauseByJsonPointer(final ValidationError root, final String pointer) {
        return root.getCauses().stream()
                .map(ValidationError::getPointerToViolation)
                .filter(ptr -> ptr.equals(pointer))
                .count();
    }

    public static long countMatchingMessage(final List<String> messages, final String expectedSubstring) {
        return messages.stream()
                .filter(message -> message.contains(expectedSubstring))
                .count();
    }

    public static void expectFailure(final Schema failingSchema,
                                     final Class<? extends Schema> expectedViolatedSchemaClass,
                                     final String expectedPointer, final JsonValue input) {

        Optional<ValidationError> errors = test(failingSchema, expectedPointer, input);
        Assert.assertTrue(errors.isPresent());
        Assert.assertSame(expectedViolatedSchemaClass, errors.get().getViolatedSchema().getClass());
    }

    public static void expectFailure(final Schema failingSchema, final JsonValue input) {
        expectFailure(failingSchema, null, input);
    }

    public static void expectFailure(final Schema failingSchema,
                                     final Schema expectedViolatedSchema,
                                     final String expectedPointer, final JsonValue input) {

        Optional<ValidationError> errors = test(failingSchema, expectedPointer, input);
        Assert.assertTrue(errors.isPresent());
        Assert.assertSame(expectedViolatedSchema, errors.get().getViolatedSchema());
    }

    public static void expectFailure(final Schema failingSchema, final String expectedPointer,
                                     final JsonValue input) {
        expectFailure(failingSchema, failingSchema, expectedPointer, input);
    }

    public static Optional<ValidationError> expectFailure(final Failure failure) {
        Optional<ValidationError> error = findValidator(failure.subject()).validate(failure.input());
        Assert.assertTrue(failure.subject() + " did not fail for " + failure.input(), error.isPresent());
        ValidationError e = error.get();
        Assert.assertSame(failure.expectedViolatedSchema(), e.getViolatedSchema());
        assertEquals(failure.expectedPointer(), e.getPointerToViolation());
        assertEquals(failure.expectedSchemaLocation(), e.getSchemaLocation());
        if (failure.expectedKeyword() != null) {
            assertEquals(failure.expectedKeyword(), e.getKeyword());
        }
        if (failure.expectedMessageFragment() != null) {
            assertThat(e.getMessage(), containsString(failure.expectedMessageFragment()));
        }
        return error;
    }

    public static Failure failureOf(Schema subject) {
        return new Failure().subject(subject);
    }

    public static Failure failureOf(Schema.Builder<?> subjectBuilder) {
        return failureOf(buildWithLocation(subjectBuilder));
    }

    private static Optional<ValidationError> test(final Schema failingSchema, final String expectedPointer,
                                                  final JsonValue input) {

        Optional<ValidationError> error = findValidator(failingSchema).validate(input);
        Assert.assertTrue(failingSchema + " did not fail for " + input, error.isPresent());
        if (expectedPointer != null) {
            assertEquals(expectedPointer, error.get().getPointerToViolation());
        }
        return error;
    }

    public static class Failure {

        private Schema subject;

        private Schema expectedViolatedSchema;

        private String expectedPointer = "#";

        private String expectedSchemaLocation = "#";

        private String expectedKeyword;

        private JsonValue input;

        private String expectedMessageFragment;

        public void expect() {
            expectFailure(this);
        }

        public Failure expectedKeyword(final String keyword) {
            this.expectedKeyword = keyword;
            return this;
        }

        public String expectedKeyword() {
            return expectedKeyword;
        }

        public String expectedMessageFragment() {
            return expectedMessageFragment;
        }

        public Failure expectedMessageFragment(String expectedFragment) {
            this.expectedMessageFragment = expectedFragment;
            return this;
        }

        public Failure expectedPointer(final String expectedPointer) {
            this.expectedPointer = expectedPointer;
            return this;
        }

        public String expectedPointer() {
            return expectedPointer;
        }

        public Failure expectedSchemaLocation(String expectedSchemaLocation) {
            this.expectedSchemaLocation = expectedSchemaLocation;
            return this;
        }

        public String expectedSchemaLocation() {
            return expectedSchemaLocation;
        }

        public Failure expectedViolatedSchema(final Schema expectedViolatedSchema) {
            this.expectedViolatedSchema = expectedViolatedSchema;
            return this;
        }

        public Schema expectedViolatedSchema() {
            if (expectedViolatedSchema != null) {
                return expectedViolatedSchema;
            }
            return subject;
        }

        public Failure input(final JsonValue input) {
            this.input = input;
            return this;
        }

        public JsonValue input() {
            return input;
        }

        public Failure subject(final Schema subject) {
            this.subject = subject;
            return this;
        }

        public Schema subject() {
            return subject;
        }
    }
}
