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
package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.six.SchemaException;
import org.junit.Assert;

import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.utils.JsonUtils.readJsonObject;

public class LoadingTestSupport {

    public static Failure failure() {
        return new Failure();
    }

    public static <E extends Exception> Failure<?, E> failWith(Class<E> expectedException) {
        checkNotNull(expectedException, "expectedException must not be null");
        return new Failure<Schema, E>().expectedException(expectedException);
    }

    public static <S extends Schema, E extends Exception> E expectFailure(final Failure<S, E> failure) {
        try {
            final Schema loadedSchema = failure.schemaFactory().orElse(SchemaFactory.schemaFactory())
                    .load(failure.input());
        } catch (Throwable e) {
            failure.expectedException()
                    .ifPresent(expected -> {
                        if (!expected.isAssignableFrom(e.getClass())) {
                            Assert.fail(String.format("Exception is of wrong type.  Expected %s but got %s",
                                    expected, e));
                        } else {
                            final E expectedException = expected.cast(e);
                            failure.expected().ifPresent(predicate -> {
                                Assert.assertTrue("Exception testing predicate failed", predicate.test(expectedException));
                            });

                            failure.expectedConsumer().ifPresent(consumer -> {
                                consumer.accept(expectedException);
                            });
                        }
                    });
            failure.expectedPointer().ifPresent(pointer -> {
                if (!(e instanceof SchemaException)) {
                    Assert.fail("Trying to test pointer, but exception wasn't SchemaException, it was: " + e);
                    SchemaException schemaException = (SchemaException) e;
                    final String schemaLocation = schemaException.getSchemaLocation();
                    Assert.assertEquals("Error's location should have failed", pointer, schemaLocation);
                }
            });
            return (E) e;
        }
        Assert.fail("Should have failed but didn't");
        return null; //Won't ever get here.
    }

    public static class Failure<S extends Schema, E extends Exception> {

        private JsonObject input;

        private String expectedPointer = "#";

        private String expectedSchemaLocation = "#";

        private SchemaFactory schemaFactory;

        private Class<E> expectedException;

        private String expectedMessageFragment;

        private Predicate<E> expectedPredicate;

        private Consumer<E> expectedConsumer;

        public void expect() {
            expectFailure(this);
        }

        public Optional<Predicate<E>> expected() {
            return Optional.ofNullable(expectedPredicate);
        }

        public Failure expected(Predicate<E> expected) {
            this.expectedPredicate = expected;
            return this;
        }

        public Failure expected(Consumer<E> expected) {
            this.expectedConsumer = expected;
            return this;
        }

        public Optional<Consumer<E>> expectedConsumer() {
            return Optional.ofNullable(expectedConsumer);
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

        public Optional<String> expectedPointer() {
            return Optional.ofNullable(expectedPointer);
        }

        public Failure expectedSchemaLocation(String expectedSchemaLocation) {
            this.expectedSchemaLocation = expectedSchemaLocation;
            return this;
        }

        public Failure expectedException(Class<E> exceptionClass) {
            this.expectedException = exceptionClass;
            return this;
        }

        public String expectedSchemaLocation() {
            return expectedSchemaLocation;
        }

        public Failure input(final String input) {
            this.input = readJsonObject(input);
            return this;
        }

        public Failure input(final JsonObject input) {
            this.input = input;
            return this;
        }

        public Failure input(final JsonValue input) {
            this.input = input.asJsonObject();
            return this;
        }

        public JsonObject input() {
            return input;
        }

        public Failure nullInput() {
            this.input = null;
            return this;
        }

        public Failure schemaFactory(SchemaFactory schemaFactory) {
            this.schemaFactory = schemaFactory;
            return this;
        }

        public Optional<SchemaFactory> schemaFactory() {
            return Optional.ofNullable(this.schemaFactory);
        }

        private Optional<Class<E>> expectedException() {
            return Optional.ofNullable(expectedException);
        }
    }
}
