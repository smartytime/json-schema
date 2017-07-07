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
package org.everit.jsonschema.validator;

import org.everit.jsonschema.api.ArraySchema;
import org.everit.jsonschema.api.ObjectSchema;
import org.everit.jsonschema.api.Schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Thrown by {@link Schema} subclasses on validation failure.
 */
public class ValidationError {
    
    private static final long serialVersionUID = 6192047123024651924L;
    /**
     * Sort of static factory method. It is used by {@link ObjectSchema} and {@link ArraySchema} to
     * create {@code ValidationException}s, handling the case of multiple violations occuring during
     * validation.
     * <p>
     * <ul>
     * <li>If {@code failures} is empty, then it doesn't do anything</li>
     * <li>If {@code failures} contains 1 exception instance, then that will be thrown</li>
     * <li>Otherwise a new exception instance will be created, its {@link #getViolatedSchema()
     * violated schema} will be {@code rootFailingSchema}, and its {@link #getCausingExceptions()
     * causing exceptions} will be the {@code failures} list</li>
     * </ul>
     *
     * @param rootFailingSchema the schema which detected the {@code failures}
     * @param failures          list containing validation failures to be thrown by this method
     */
    public static Optional<ValidationError> collectErrors(Schema rootFailingSchema,
                                                          List<ValidationError> failures) {
        int failureCount = failures.size();
        if (failureCount == 0) {
            return Optional.empty();
        } else if (failureCount == 1) {
            return Optional.of(failures.get(0));
        } else {
            return Optional.of(new ValidationError(rootFailingSchema,
                    new StringBuilder("#"),
                    getViolationCount(failures) + " schema violations found",
                    new ArrayList<>(failures),
                    null,
                    rootFailingSchema.getSchemaLocation()));
        }
    }

    private final StringBuilder pointerToViolation;
    private final String schemaLocation;
    private final transient Schema violatedSchema;
    private final List<ValidationError> causingExceptions;
    private final String keyword;
    private final String message;


    // /**
    //  * Constructor for type-mismatch failures. It is usually more convenient to use
    //  * {@link #ValidationReport(Schema, Class, Object)} instead.
    //  *
    //  * @param violatedSchema the schema instance which detected the schema violation
    //  * @param expectedType   the expected type
    //  * @param actualValue    the violating value
    //  * @param keyword        the violating keyword
    //  * @param schemaLocation a path denoting the location of the violated keyword in the schema JSON
    //  */
    // public ValidationReport(Schema violatedSchema, Class<?> expectedType,
    //                         Object actualValue, String keyword, String schemaLocation) {
    //     this(violatedSchema, new StringBuilder("#"),
    //             "expected type: " + expectedType.getSimpleName() + ", found: "
    //                     + (actualValue == null ? "null" : actualValue.getClass().getSimpleName()),
    //             Collections.emptyList(), keyword, schemaLocation);
    // }

    /**
     * Constructor.
     *
     * @param violatedSchema the schema instance which detected the schema violation
     * @param message        the readable exception message
     * @param keyword        the violated keyword
     */
    @Deprecated
    public ValidationError(Schema violatedSchema,
                           String message,
                           String keyword) {
        this(violatedSchema,
                new StringBuilder("#"),
                message,
                Collections.emptyList(),
                keyword,
                null);
    }

    /**
     * Constructor.
     *
     * @param violatedSchema the schama instance which detected the schema violation
     * @param message        the readable exception message
     * @param keyword        the violated keyword
     * @param schemaLocation the path to the violated schema fragment (from the schema root)
     */
    public ValidationError(Schema violatedSchema,
                           String message,
                           String keyword,
                           String schemaLocation) {
        this(violatedSchema,
                new StringBuilder("#"),
                message,
                Collections.emptyList(),
                keyword,
                schemaLocation);
    }

    /***
     * Constructor.
     *
     * @param violatedSchema
     *          the schema instance which detected the schema violation
     * @param pointerToViolation
     *          a JSON pointer denoting the part of the document which violates the schema
     * @param message
     *          the readable exception message
     * @param causingExceptions
     *          a (possibly empty) list of validation failures. It is used if multiple schema
     *          violations are found by violatedSchema
     * @param keyword
     *          the violated keyword
     */
    ValidationError(Schema violatedSchema, StringBuilder pointerToViolation,
                    String message,
                    List<ValidationError> causingExceptions,
                    String keyword,
                    String schemaLocation) {
        this.message = message;
        this.violatedSchema = violatedSchema;
        this.pointerToViolation = pointerToViolation;
        this.causingExceptions = Collections.unmodifiableList(causingExceptions);
        this.keyword = keyword;
        this.schemaLocation = schemaLocation;
    }

    private ValidationError(StringBuilder pointerToViolation,
                            Schema violatedSchema,
                            String message,
                            List<ValidationError> causingExceptions,
                            String keyword, String schemaLocation) {
        this(violatedSchema, pointerToViolation, message, causingExceptions, keyword, schemaLocation);
    }

    /**
     * Returns all messages collected from all violations, including nested causing exceptions.
     *
     * @return all messages
     */
    public List<String> getAllMessages() {
        if (causingExceptions.isEmpty()) {
            return Collections.singletonList(getMessage());
        } else {
            return getAllMessages(causingExceptions).stream().collect(Collectors.toList());
        }
    }

    public List<ValidationError> getCausingExceptions() {
        return causingExceptions;
    }

    /**
     * Returns a programmer-readable error description. Unlike {@link #getMessage()} this doesn't
     * contain the JSON pointer denoting the violating document fragment.
     *
     * @return the error description
     */
    public String getErrorMessage() {
        return message;
    }

    public String getKeyword() {
        return keyword;
    }

    /**
     * Returns a programmer-readable error description prepended by {@link #getPointerToViolation()
     * the pointer to the violating fragment} of the JSON document.
     *
     * @return the error description
     */
    public String getMessage() {
        return getPointerToViolation() + ": " + message;
    }

    /**
     * A JSON pointer denoting the part of the document which violates the schema. It always points
     * from the root of the document to the violating data fragment, therefore it always starts with
     * <code>#</code>.
     *
     * @return the JSON pointer
     */
    public String getPointerToViolation() {
        if (pointerToViolation == null) {
            return null;
        }
        return pointerToViolation.toString();
    }

    /**
     * @return a path denoting the location of the violated keyword in the schema
     * @since 1.6.0
     */
    public String getSchemaLocation() {
        return schemaLocation;
    }

    public Schema getViolatedSchema() {
        return violatedSchema;
    }

    public int getViolationCount() {
        return getViolationCount(causingExceptions);
    }

    /**
     * Creates a new {@code ViolationException} instance based on this one, but with changed
     * {@link #getPointerToViolation() JSON pointer}.
     *
     * @param fragment the fragment of the JSON pointer to be prepended to existing pointers
     * @return the new instance
     */
    public ValidationError prepend(String fragment) {
        return prepend(fragment, this.violatedSchema);
    }

    /**
     * Creates a new {@code ViolationException} instance based on this one, but with changed
     * {@link #getPointerToViolation() JSON pointer} and {link {@link #getViolatedSchema() violated
     * schema}.
     *
     * @param fragment       the fragment of the JSON pointer to be prepended to existing pointers
     * @param violatedSchema the violated schema, which may not be the same as {@link #getViolatedSchema()}
     * @return the new {@code ViolationException} instance
     */
    public ValidationError prepend(String fragment, Schema violatedSchema) {
        String escapedFragment = escapeFragment(requireNonNull(fragment, "fragment cannot be null"));
        StringBuilder newPointer = this.pointerToViolation.insert(1, '/').insert(2, escapedFragment);
        List<ValidationError> prependedCausingExceptions = causingExceptions.stream()
                .map(exc -> exc.prepend(escapedFragment))
                .collect(Collectors.toList());
        return new ValidationError(newPointer, violatedSchema, this.message,
                prependedCausingExceptions, this.keyword, this.schemaLocation);
    }

    private static int getViolationCount(List<ValidationError> causes) {
        int causeCount = causes.stream().mapToInt(ValidationError::getViolationCount).sum();
        return Math.max(1, causeCount);
    }

    private static List<String> getAllMessages(List<ValidationError> causes) {
        List<String> messages = causes.stream()
                .filter(cause -> cause.causingExceptions.isEmpty())
                .map(ValidationError::getMessage)
                .collect(Collectors.toList());
        messages.addAll(causes.stream()
                .filter(cause -> !cause.causingExceptions.isEmpty())
                .flatMap(cause -> getAllMessages(cause.getCausingExceptions()).stream())
                .collect(Collectors.toList()));
        return messages;
    }

    /**
     * Creates a JSON representation of the failure.
     * <p>
     * The returned {@code JSONObject} contains the following keys:
     * <ul>
     * <li>{@code "message"}: a programmer-friendly exception message. This value is a non-nullable
     * string.</li>
     * <li>{@code "keyword"}: a JSON Schema keyword which was used in the schema and violated by the
     * input JSON. This value is a nullable string.</li>
     * <li>{@code "pointerToViolation"}: a JSON Pointer denoting the path from the root of the
     * document to the invalid fragment of it. This value is a non-nullable string. See
     * {@link #getPointerToViolation()}</li>
     * <li>{@code "causingExceptions"}: is a (possibly empty) array of violations which caused this
     * exception. See {@link #getCausingExceptions()}</li>
     * <li>{@code "schemaLocation"}: a string denoting the path to the violated schema keyword in the schema
     * JSON (since version 1.6.0)</li>
     * </ul>
     *
     * @return a JSON description of the validation error
     */
    //todo:ericmRedo this
    // public JSONObject toJSON() {
    //     JSONObject rval = new JSONObject();
    //     rval.put("keyword", keyword);
    //     if (pointerToViolation == null) {
    //         rval.put("pointerToViolation", JSONObject.NULL);
    //     } else {
    //         rval.put("pointerToViolation", getPointerToViolation());
    //     }
    //     rval.put("message", super.getMessage());
    //     List<JSONObject> causeJsons = causingExceptions.stream()
    //             .map(ValidationException::toJSON)
    //             .collect(Collectors.toList());
    //     rval.put("causingExceptions", new JSONArray(causeJsons));
    //     if (schemaLocation != null) {
    //         rval.put("schemaLocation", schemaLocation);
    //     }
    //     return rval;
    // }

    private String escapeFragment(String fragment) {
        return fragment.replace("~", "~0").replace("/", "~1");
    }
}
