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

import io.sbsp.jsonschema.enums.JsonSchemaKeywordType;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.Schema.JsonSchemaBuilder;

import javax.json.JsonValue;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.sbsp.jsonschema.utils.JsonUtils.jsonNumberValue;
import static io.sbsp.jsonschema.utils.JsonUtils.jsonStringValue;
import static io.sbsp.jsonschema.validator.SchemaValidatorFactory.createValidatorForSchema;
import static javax.json.spi.JsonProvider.provider;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ValidationTestSupport {

    public static Schema buildWithLocation(JsonSchemaBuilder builder) {
        return builder.build();
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
        assertTrue(errors.isPresent());
        assertSame(expectedViolatedSchemaClass, errors.get().getViolatedSchema().getClass());
    }

    public static void expectFailure(final Schema failingSchema, final double num) {
        expectFailure(failingSchema, null, provider().createValue(num));
    }

    public static void expectFailure(final Schema failingSchema, final JsonValue input) {
        expectFailure(failingSchema, null, input);
    }

    public static void expectFailure(final Schema failingSchema,
                                     final Schema expectedViolatedSchema,
                                     final String expectedPointer, final JsonValue input) {

        Optional<ValidationError> errors = test(failingSchema, expectedPointer, input);
        assertTrue(errors.isPresent());
        assertEquals("Matching violation schemas", expectedViolatedSchema, errors.get().getViolatedSchema());
    }

    public static void expectFailure(final Schema failingSchema, final String expectedPointer,
                                     final JsonValue input) {
        expectFailure(failingSchema, failingSchema, expectedPointer, input);
    }

    public static ValidationError expectFailure(final Failure failure) {
        final SchemaValidator validator = failure.validator()
                .orElse(ValidationMocks.createTestValidator(failure.schema()));
        if (failure.input() == null) {
            throw new RuntimeException("Invalid test configuration.  Must provide input value");
        }
        ValidationError error = validator.validate(failure.input())
                .orElseThrow(() -> new AssertionError(failure.schema() + " did not fail for " + failure.input()));
        failure.expected().ifPresent(p-> assertTrue("Predicate failed validation", p.test(error)));
        failure.expectedConsumer().ifPresent(consumer-> consumer.accept(error));
        assertEquals("Expected violated schema", failure.expectedViolatedSchema(), error.getViolatedSchema());
        assertEquals("Pointer to violation", failure.expectedPointer(), error.getPointerToViolation());
        assertEquals("Schema location", failure.expectedSchemaLocation(), error.getSchemaLocation().toString());
        if (failure.expectedKeyword() != null) {
            assertNotNull("Error expected to have a keyword, but didn't", error.getKeyword());
            assertEquals(failure.expectedKeyword(), error.getKeyword().key());
        }
        if (failure.expectedMessageFragment() != null) {
            assertThat("Message fragment matches", error.getMessage(), containsString(failure.expectedMessageFragment()));
        }

        return error;
    }

    public static Failure failureOf(SchemaValidator validator, Schema schema) {
        return new Failure().schema(schema).validator(validator);

    }
    public static Failure failureOf(SchemaValidator validator) {
        return new Failure().schema(validator.getSchema()).validator(validator);
    }

    public static Failure failureOf(Schema schema) {
        return new Failure().schema(schema);
    }

    public static Failure failureOf(JsonSchemaBuilder subjectBuilder) {
        return failureOf(buildWithLocation(subjectBuilder));
    }

    private static Optional<ValidationError> test(final Schema failingSchema, final String expectedPointer,
                                                  final JsonValue input) {

        Optional<ValidationError> error = createValidatorForSchema(failingSchema).validate(input);
        assertTrue(failingSchema + " did not fail for " + input, error.isPresent());
        if (expectedPointer != null) {
            assertEquals(expectedPointer, error.get().getPointerToViolation());
        }
        return error;
    }

    public static class Failure {

        private Schema subject;

        private SchemaValidator validator;

        private Schema expectedViolatedSchema;

        private String expectedPointer = "#";

        private String expectedSchemaLocation = "#";

        private String expectedKeyword;

        private JsonValue input;

        private String expectedMessageFragment;

        private Predicate<ValidationError> expectedPredicate;

        private Consumer<ValidationError> expectedConsumer;

        public void expect() {
            expectFailure(this);
        }

        public Failure expectedKeyword(final JsonSchemaKeywordType keyword) {
            this.expectedKeyword = keyword.key();
            return this;
        }

        public Failure expectedKeyword(final String keyword) {
            this.expectedKeyword = keyword;
            return this;
        }

        public String expectedKeyword() {
            return expectedKeyword;
        }

        public Optional<Predicate<ValidationError>> expected() {
            return Optional.ofNullable(expectedPredicate);
        }

        public Optional<Consumer<ValidationError>> expectedConsumer() {
            return Optional.ofNullable(expectedConsumer);
        }

        public Failure expected(Predicate<ValidationError> expected) {
            this.expectedPredicate = expected;
            return this;
        }

        public Failure expected(Consumer<ValidationError> expected) {
            this.expectedConsumer = expected;
            return this;
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

        public Failure nullInput() {
            this.input = JsonValue.NULL;
            return this;
        }

        public Failure input(final String input) {
            this.input = provider().createValue(input);
            return this;
        }

        public Failure input(final boolean input) {
            this.input = input ? JsonValue.TRUE : JsonValue.FALSE;
            return this;
        }

        public Failure input(final int i) {
            this.input = provider().createValue(i);
            return this;
        }

        public Failure input(final JsonValue input) {
            this.input = input;
            return this;
        }

        public JsonValue input() {
            return input;
        }

        public Failure schema(final Schema subject) {
            this.subject = subject;
            return this;
        }

        public Failure validator(final SchemaValidator validator) {
            this.validator = validator;
            return this;
        }

        public Schema schema() {
            return subject;
        }

        public Optional<SchemaValidator> validator() {
            return Optional.ofNullable(validator);
        }
    }

    public static ValidationError verifyFailure(Supplier<Optional<ValidationError>> validationFn) {
        Optional<ValidationError> error = validationFn.get();
        assertTrue("Should have failed", error.isPresent());
        return error.get();
    }

    public static void expectSuccess(Supplier<Optional<ValidationError>> validationFn) {
        Optional<ValidationError> error = validationFn.get();
        error.ifPresent(e -> fail("Should have succeeded: " + e));
    }

    public static void expectSuccess(Schema schema, long input) {
        expectSuccess(schema, jsonNumberValue(input));
    }

    public static void expectSuccess(Schema schema, double input) {
        expectSuccess(schema, jsonNumberValue(input));
    }

    public static void expectSuccess(Schema schema, String input) {
        expectSuccess(schema, jsonStringValue(input));
    }

    public static void expectSuccess(Schema schema, boolean input) {
        expectSuccess(schema, input ? JsonValue.TRUE : JsonValue.FALSE);
    }

    public static void expectSuccess(Schema schema, JsonValue input) {
        final Optional<ValidationError> error = createValidatorForSchema(schema).validate(input);
        if (error.isPresent()) {
            assertFalse("Found errors: " + error.toString(), error.isPresent());
        }
    }
}
