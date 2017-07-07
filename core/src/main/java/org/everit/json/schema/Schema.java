package org.everit.json.schema;

import org.everit.json.JsonValue;
import org.everit.json.schema.internal.JSONPrinter;
import org.everit.json.schema.loader.orgjson.OrgJsonApi;
import org.everit.json.schema.loader.orgjson.WrappedJSONPrinter;
import org.everit.jsonschema.validator.SchemaValidator;
import org.everit.jsonschema.validator.SchemaValidatorFactory;
import org.everit.jsonschema.validator.ValidationError;
import org.json.JSONObject;
import org.json.JSONWriter;

import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Converted to wrap the modular schema, to include validation.
 *
 */
public class Schema {

    private final org.everit.jsonschema.api.Schema wrappedSchema;

    public Schema(org.everit.jsonschema.api.Schema wrappedSchema) {
        this.wrappedSchema = checkNotNull(wrappedSchema);
    }

    /**
     * Performs the schema validation.
     *
     * @param subject the object to be validated
     * @throws ValidationException if the {@code subject} is invalid against this schema.
     */
    public void validate(final Object subject) {
        SchemaValidator<?> validator = SchemaValidatorFactory.findValidator(wrappedSchema);
        OrgJsonApi orgJsonApi = new OrgJsonApi();
        JsonValue of = orgJsonApi.of((JSONObject) subject);
        validator.validate(of).ifPresent(error -> {
            throw copyError(error);
        });
    }

    ValidationException copyError(ValidationError error) {
        Schema violationSchema = new Schema(error.getViolatedSchema());
        List<ValidationException> causingExceptions = error.getCausingExceptions().stream()
                .map(this::copyError)
                .collect(Collectors.toList());

        return new ValidationException(violationSchema, new StringBuilder(error.getPointerToViolation()), error.getMessage(),
                causingExceptions, error.getKeyword(), error.getSchemaLocation());
    }

    /**
     * Determines if this {@code Schema} instance defines any restrictions for the object property
     * denoted by {@code field}. The {@code field} should be a JSON pointer, denoting the property to
     * be queried.
     * <p>
     * For example the field {@code "#/rectangle/a"} is defined by the following schema:
     *
     * <pre>
     * <code>
     * objectWithSchemaRectangleDep" : {
     *   "type" : "object",
     *   "dependencies" : {
     *       "d" : {
     *           "type" : "object",
     *           "properties" : {
     *               "rectangle" : {"$ref" : "#/definitions/Rectangle" }
     *           }
     *       }
     *   },
     *   "definitions" : {
     *       "size" : {
     *           "type" : "number",
     *           "minimum" : 0
     *       },
     *       "Rectangle" : {
     *           "type" : "object",
     *           "properties" : {
     *               "a" : {"$ref" : "#/definitions/size"},
     *               "b" : {"$ref" : "#/definitions/size"}
     *           }
     *       }
     *    }
     * }
     * </code>
     * </pre>
     *
     * The default implementation of this method always returns false.
     *
     * @param field should be a JSON pointer in its string representation.
     * @return {@code true} if the propertty denoted by {@code field} is defined by this schema
     * instance
     */
    public boolean definesProperty(final String field) {
        return wrappedSchema.definesProperty(field);
    }

    @Override
    public boolean equals(final Object o) {
        return wrappedSchema.equals(o);
    }

    public String getDescription() {
        return wrappedSchema.getDescription();
    }

    public String getId() {
        return wrappedSchema.getId();
    }

    public String getSchemaLocation() {
        return wrappedSchema.getSchemaLocation();
    }

    public String getTitle() {
        return wrappedSchema.getTitle();
    }

    /**
     * Describes the instance as a JSONObject to {@code writer}.
     *
     * First it adds the {@code "title} , {@code "description"} and {@code "id"} properties then calls
     * {@link #describePropertiesTo(JSONPrinter)}, which will add the subclass-specific properties.
     *
     * It is used by {@link #toString()} to serialize the schema instance into its JSON representation.
     *
     * @param writer it will receive the schema description
     */
    public final void describeTo(final JSONPrinter writer) {
        wrappedSchema.describeTo(new WrappedJSONPrinter(writer));
    }

    /**
     * Subclasses are supposed to override this method to describe the subclass-specific attributes.
     * This method is called by {@link #describeTo(JSONPrinter)} after adding the generic properties if
     * they are present ({@code id}, {@code title} and {@code description}). As a side effect,
     * overriding subclasses don't have to open and close the object with {@link JSONWriter#object()}
     * and {@link JSONWriter#endObject()}.
     *
     * @param writer it will receive the schema description
     */
    void describePropertiesTo(final JSONPrinter writer) {
        //todo:ericm Figure out
        //wrappedSchema.describeTo(//);..
    }

    @Override
    public String toString() {
        StringWriter w = new StringWriter();
        describeTo(new JSONPrinter(w));
        return w.getBuffer().toString();
    }

    protected ValidationException failure(String message, String keyword) {
        return new ValidationException(this, message, keyword, getSchemaLocation());
    }

    protected ValidationException failure(Class<?> expectedType, Object actualValue) {
        return new ValidationException(this, expectedType, actualValue, "type", getSchemaLocation());
    }
    /**
     * Since we add state in subclasses, but want those subclasses to be non final, this allows us to
     * have equals methods that satisfy the equals contract.
     *
     * http://www.artima.com/lejava/articles/equality.html
     *
     * @param other the subject of comparison
     * @return {@code true } if {@code this} can be equal to {@code other}
     */
    protected boolean canEqual(final Object other) {
        return (other instanceof Schema);
    }
}
