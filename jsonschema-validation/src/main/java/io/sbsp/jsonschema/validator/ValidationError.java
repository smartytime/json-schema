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
import io.sbsp.jsonschema.enums.JsonSchemaKeywordType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;
import lombok.experimental.var;

import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;

/**
 * Thrown by {@link Schema} subclasses on validation failure.
 */
@Builder(toBuilder = true, builderMethodName = "validationBuilder")
@AllArgsConstructor
public class ValidationError {

    private static final long serialVersionUID = 6192047123024651924L;

    /**
     * The schema that generated this error
     */
    private final Schema violatedSchema;

    /**
     * A pointer to the violation within the input document we validated.
     */
    private final JsonPath pointerToViolation;

    private final String code;
    private final String message;
    private final String messageTemplate;

    @Singular
    private final List<ValidationError> causingExceptions;
    private final JsonSchemaKeywordType keyword;

    @Singular
    private final List<Object> arguments;

    /**
     * Returns all messages collected from all violations, including nested causing exceptions.
     *
     * @return all messages
     */
    public List<ValidationError> getAllMessages() {
        if (causingExceptions.isEmpty()) {
            return singletonList(this);
        } else {
            return unmodifiableList(getAllMessages(causingExceptions));
        }
    }

    public List<ValidationError> getCauses() {
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

    public JsonSchemaKeywordType getKeyword() {
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

    public List<Object> getArguments() {
        return arguments;
    }

    public String getCode() {
        return code;
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
        return pointerToViolation.toURIFragment().toString();
    }

    public Optional<JsonPath> getPathToViolation() {
        return Optional.ofNullable(pointerToViolation);
    }

    public URI getSchemaLocation() {
        return violatedSchema.getPointerFragmentURI();
    }

    public Schema getViolatedSchema() {
        return violatedSchema;
    }

    public int getViolationCount() {
        return getViolationCount(causingExceptions);
    }

    public JsonObject toJson() {
        return toJson(true);
    }

    public JsonObject toJson(boolean withCauses) {
        final JsonProvider provider = JsonProvider.provider();

        var errorJson = provider.createObjectBuilder();

        if (pointerToViolation == null) {
            errorJson.add("pointerToViolation", JsonValue.NULL);
        } else {
            errorJson.add("pointerToViolation", getPointerToViolation());
        }
        if (this.keyword != null) {
            errorJson.add("keyword", this.keyword.key());
        }
        if (code != null) {
            errorJson.add("code", this.code);
        }
        errorJson.add("message", this.message);
        if (violatedSchema != null) {
            errorJson.add("schemaLocation", getSchemaLocation().toString());
        }

        if (this.arguments.size() > 0) {
            errorJson.add("template", this.messageTemplate);

            final JsonArrayBuilder argArray = JsonProvider.provider().createArrayBuilder();
            this.arguments.stream()
                    .map(Object::toString)
                    .forEach(argArray::add);
            errorJson.add("arguments", argArray.build());
        }

        if (withCauses && causingExceptions.size() > 0) {
            final JsonArrayBuilder arrayBuilder = provider.createArrayBuilder();
            causingExceptions.stream()
                    .map(ValidationError::toJson)
                    .forEach(arrayBuilder::add);

            errorJson.add("causes", arrayBuilder);
        }


        return errorJson.build();
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
     * <li>{@code "causes"}: is a (possibly empty) array of violations which caused this
     * exception. See {@link #getCauses()}</li>
     * <li>{@code "documentRoot"}: a string denoting the path to the violated schema keyword in the schema
     * JSON (since version 1.6.0)</li>
     * </ul>
     *
     * @return a JSON description of the validation error
     */
    public JsonArray toJsonErrors() {
        final JsonProvider provider = JsonProvider.provider();
        JsonArrayBuilder errorArray = provider.createArrayBuilder();
        this.getAllMessages().stream()
                .map(e -> e.toJson(false))
                .forEach(errorArray::add);

        return errorArray.build();
    }

    @Override
    public String toString() {
        return "ValidationError{" +
                "pointerToViolation=" + pointerToViolation +
                ", causingExceptions=" + causingExceptions +
                ", keyword='" + keyword + '\'' +
                ", message='" + message + '\'' +
                '}';
    }

    public ValidationError withKeyword(JsonSchemaKeyword keyword, String message) {
        checkNotNull(keyword, "keyword must not be null");
        checkNotNull(message, "message must not be null");
        return toBuilder()
                .keyword(keyword)
                .message(message)
                .code("validation.keyword." + keyword.key())
                .build();
    }

    /**
     * Sort of static factory method. It is used by validators to create {@code ValidationError}s, handling the case of multiple violations
     * occuring during validation.
     * <p>
     * <ul>
     * <li>If {@code failures} is empty, then it doesn't do anything</li>
     * <li>If {@code failures} contains 1 exception instance, then that will be thrown</li>
     * <li>Otherwise a new exception instance will be created, its {@link #getViolatedSchema()
     * violated schema} will be {@code rootFailingSchema}, and its {@link #getCauses()
     * causing exceptions} will be the {@code failures} list</li>
     * </ul>
     *
     * @param rootFailingSchema the schema which detected the {@code failures}
     * @param failures          list containing validation failures to be thrown by this method
     */
    public static Optional<ValidationError> collectErrors(Schema rootFailingSchema,
                                                          JsonPath currentLocation,
                                                          List<ValidationError> failures) {
        int failureCount = failures.size();
        if (failureCount == 0) {
            return Optional.empty();
        } else if (failureCount == 1) {
            return Optional.of(failures.get(0));
        } else {
            return Optional.of(
                    validationBuilder()
                            .violatedSchema(rootFailingSchema)
                            .pointerToViolation(currentLocation)
                            .message("%d schema violations found", getViolationCount(failures))
                            .code("validation.multipleFailures")
                            .causingExceptions(unmodifiableList(failures))
                            .keyword(null)
                            .build()
            );
        }
    }

    private static List<ValidationError> getAllMessages(List<ValidationError> causes) {
        List<ValidationError> messages = causes.stream()
                .filter(cause -> cause.causingExceptions.isEmpty())
                .collect(Collectors.toList());
        messages.addAll(causes.stream()
                .filter(cause -> !cause.causingExceptions.isEmpty())
                .flatMap(cause -> getAllMessages(cause.getCauses()).stream())
                .collect(Collectors.toList()));
        return unmodifiableList(messages);
    }

    private static int getViolationCount(List<ValidationError> causes) {
        int causeCount = causes.stream().mapToInt(ValidationError::getViolationCount).sum();
        return Math.max(1, causeCount);
    }

    public static class ValidationErrorBuilder {

        private JsonPath pointerToViolation = JsonPath.rootPath();

        public ValidationErrorBuilder message(String message, Object... args) {
            this.message = String.format(message, args);
            this.messageTemplate = message;
            for (Object arg : args) {
                this.argument(arg);
            }
            return this;
        }

        public ValidationErrorBuilder message(String message) {
            this.message = message;
            this.messageTemplate = message;
            return this;
        }

        public ValidationErrorBuilder pointerToViolationURI(String uriFragment) {
            if (uriFragment == null) {
                this.pointerToViolation = null;
            } else {
                this.pointerToViolation(JsonPath.parseFromURIFragment(uriFragment));
            }
            return this;
        }
    }
}
