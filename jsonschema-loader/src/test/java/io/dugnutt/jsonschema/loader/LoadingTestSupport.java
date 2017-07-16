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

import java.util.List;

public class LoadingTestSupport {

    public static class Failure {

        private Schema subject;

        private Schema expectedViolatedSchema;

        private String expectedPointer = "#";

        private String expectedSchemaLocation = "#";

        private String expectedKeyword;

        private Object input;

        private String expectedMessageFragment;

        public Failure subject(final Schema subject) {
            this.subject = subject;
            return this;
        }

        public Schema subject() {
            return subject;
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

        public Failure input(final Object input) {
            this.input = input;
            return this;
        }

        public Object input() {
            return input;
        }


        public Failure expectedMessageFragment(String expectedFragment) {
            this.expectedMessageFragment = expectedFragment;
            return this;
        }
    }

    public static Failure failureOf(Schema subject) {
        return new Failure().subject(subject);
    }

    public static Failure failureOf(Schema.Builder<?> subjectBuilder) {
        return failureOf(buildWithLocation(subjectBuilder));
    }

    public static <S extends Schema> S buildWithLocation(Schema.Builder<S> builder) {
        return builder.schemaLocation("#").build();
    }

    public static long countMatchingMessage(final List<String> messages, final String expectedSubstring) {
        return messages.stream()
                .filter(message -> message.contains(expectedSubstring))
                .count();
    }



}
