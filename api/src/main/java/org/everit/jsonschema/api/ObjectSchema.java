package org.everit.jsonschema.api;

import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.everit.jsonschema.api.JsonSchemaProperty.ADDITIONAL_PROPERTIES;
import static org.everit.jsonschema.api.JsonSchemaProperty.DEPENDENCIES;
import static org.everit.jsonschema.api.JsonSchemaProperty.MAX_PROPERTIES;
import static org.everit.jsonschema.api.JsonSchemaProperty.MIN_PROPERTIES;
import static org.everit.jsonschema.api.JsonSchemaProperty.PROPERTIES;
import static org.everit.jsonschema.api.JsonSchemaProperty.REQUIRED;

/**
 * Object schema validator.
 */
public class ObjectSchema extends Schema {

    private final Map<String, Schema> propertySchemas;
    private final boolean additionalProperties;
    private final Schema schemaOfAdditionalProperties;
    private final List<String> requiredProperties;
    private final Integer minProperties;
    private final Integer maxProperties;
    private final Map<String, Set<String>> propertyDependencies;
    private final Map<String, Schema> schemaDependencies;
    private final boolean requiresObject;
    private final Map<Pattern, Schema> patternProperties;

    /**
     * Constructor.
     *
     * @param builder the builder object containing validation criteria
     */
    public ObjectSchema(final Builder builder) {
        super(builder);
        this.propertySchemas = builder.propertySchemas == null ? null
                : Collections.unmodifiableMap(builder.propertySchemas);
        this.additionalProperties = builder.additionalProperties;
        this.schemaOfAdditionalProperties = builder.schemaOfAdditionalProperties;
        if (!additionalProperties && schemaOfAdditionalProperties != null) {
            throw new SchemaException(
                    "additionalProperties cannot be false if schemaOfAdditionalProperties is present");
        }
        this.requiredProperties = Collections.unmodifiableList(new ArrayList<>(
                builder.requiredProperties));
        this.minProperties = builder.minProperties;
        this.maxProperties = builder.maxProperties;
        this.propertyDependencies = copyMap(builder.propertyDependencies);
        this.schemaDependencies = copyMap(builder.schemaDependencies);
        this.requiresObject = builder.requiresObject;
        this.patternProperties = copyMap(builder.patternProperties);
    }

    public static Builder builder() {
        return new Builder();
    }

    private static <K, V> Map<K, V> copyMap(final Map<K, V> original) {
        return Collections.unmodifiableMap(new HashMap<>(original));
    }

    public boolean definesProperty(String field) {
        field = field.replaceFirst("^#", "").replaceFirst("^/", "");
        int firstSlashIdx = field.indexOf('/');
        String nextToken, remaining;
        if (firstSlashIdx == -1) {
            nextToken = field;
            remaining = null;
        } else {
            nextToken = field.substring(0, firstSlashIdx);
            remaining = field.substring(firstSlashIdx + 1);
        }
        return !field.isEmpty() && (definesSchemaProperty(nextToken, remaining)
                || definesPatternProperty(nextToken, remaining)
                || definesSchemaDependencyProperty(field));
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), propertySchemas, additionalProperties, schemaOfAdditionalProperties, requiredProperties,
                minProperties, maxProperties, propertyDependencies, schemaDependencies, requiresObject, patternProperties);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof ObjectSchema) {
            ObjectSchema that = (ObjectSchema) o;
            return that.canEqual(this) &&
                    additionalProperties == that.additionalProperties &&
                    requiresObject == that.requiresObject &&
                    Objects.equals(propertySchemas, that.propertySchemas) &&
                    Objects.equals(schemaOfAdditionalProperties, that.schemaOfAdditionalProperties) &&
                    Objects.equals(requiredProperties, that.requiredProperties) &&
                    Objects.equals(minProperties, that.minProperties) &&
                    Objects.equals(maxProperties, that.maxProperties) &&
                    Objects.equals(propertyDependencies, that.propertyDependencies) &&
                    Objects.equals(schemaDependencies, that.schemaDependencies) &&
                    Objects.equals(patternProperties, that.patternProperties) &&
                    super.equals(that);
        } else {
            return false;
        }
    }

    @Override
    protected boolean canEqual(Object other) {
        return other instanceof ObjectSchema;
    }

    void describePropertiesTo(JsonSchemaGenerator writer) {

        writer.writeType(JsonSchemaType.Object, requiresObject)
                .optionalWrite(PROPERTIES, propertySchemas)
                .optionalWrite(MIN_PROPERTIES, minProperties)
                .optionalWrite(MAX_PROPERTIES, maxProperties)
                .optionalWrite(REQUIRED, requiredProperties)
                .optionalWrite(ADDITIONAL_PROPERTIES, schemaOfAdditionalProperties)
                .optionalWrite(DEPENDENCIES, schemaDependencies)
                .optionalWritePatternProperties(patternProperties)
                .writeIfFalse(ADDITIONAL_PROPERTIES, additionalProperties);

        describePropertyDependenciesTo(writer);
    }

    public Stream<String> getAdditionalProperties(final JsonObject subject) {
        Set<String> names = subject.keySet();
        return names.stream()
                .filter(key -> !propertySchemas.containsKey(key))
                .filter(key -> !matchesAnyPattern(key));
    }

    public Integer getMaxProperties() {
        return maxProperties;
    }

    public Integer getMinProperties() {
        return minProperties;
    }

    public Map<Pattern, Schema> getPatternProperties() {
        return patternProperties;
    }

    public Map<String, Set<String>> getPropertyDependencies() {
        return propertyDependencies;
    }

    public Map<String, Schema> getPropertySchemas() {
        return propertySchemas;
    }

    public List<String> getRequiredProperties() {
        return requiredProperties;
    }

    public Map<String, Schema> getSchemaDependencies() {
        return schemaDependencies;
    }

    public Schema getSchemaOfAdditionalProperties() {
        return schemaOfAdditionalProperties;
    }

    public boolean permitsAdditionalProperties() {
        return additionalProperties;
    }

    public boolean requiresObject() {
        return requiresObject;
    }

    private boolean matchesAnyPattern(final String key) {
        return patternProperties.keySet().stream()
                .anyMatch(pattern -> pattern.matcher(key).find());
    }

    private void describePropertyDependenciesTo(JsonSchemaGenerator writer) {
        writer.writeKey(DEPENDENCIES);
        writer.object();
        propertyDependencies.forEach((k,v) -> {
            writer.writePropertyName(k);
            writer.array();
            v.forEach(writer::write);
            writer.endArray();
        });
        writer.endObject();
    }

    private boolean definesSchemaProperty(String current, final String remaining) {
        current = unescape(current);
        boolean hasSuffix = !(remaining == null);
        if (propertySchemas.containsKey(current)) {
            if (hasSuffix) {
                return propertySchemas.get(current).definesProperty(remaining);
            } else {
                return true;
            }
        }
        return false;
    }

    private boolean definesPatternProperty(final String current, final String remaining) {
        return patternProperties.keySet()
                .stream()
                .filter(pattern -> pattern.matcher(current).matches())
                .map(patternProperties::get)
                .anyMatch(schema -> remaining == null || schema.definesProperty(remaining));
    }

    private boolean definesSchemaDependencyProperty(final String field) {
        return schemaDependencies.containsKey(field)
                || schemaDependencies.values().stream()
                .anyMatch(schema -> schema.definesProperty(field));
    }

    private String unescape(final String value) {
        return value.replace("~1", "/").replace("~0", "~");
    }

    /**
     * Builder class for {@link ObjectSchema}.
     */
    public static class Builder extends Schema.Builder<ObjectSchema> {

        private final Map<Pattern, Schema> patternProperties = new HashMap<>();
        private final Map<String, Schema> propertySchemas = new HashMap<>();
        private final List<String> requiredProperties = new ArrayList<String>(0);
        private final Map<String, Set<String>> propertyDependencies = new HashMap<>();
        private final Map<String, Schema> schemaDependencies = new HashMap<>();
        private boolean requiresObject = true;
        private boolean additionalProperties = true;
        private Schema schemaOfAdditionalProperties;
        private Integer minProperties;
        private Integer maxProperties;

        /**
         * Adds a property schema.
         *
         * @param propName the name of the property which' expected schema must be {@code schema}
         * @param schema   if the subject under validation has a property named {@code propertyName} then its
         *                 value will be validated using this {@code schema}
         * @return {@code this}
         */
        public Builder addPropertySchema(final String propName, final Schema schema) {
            requireNonNull(propName, "propName cannot be null");
            requireNonNull(schema, "schema cannot be null");
            propertySchemas.put(propName, schema);
            return this;
        }

        public Builder addRequiredProperty(final String propertyName) {
            requiredProperties.add(propertyName);
            return this;
        }

        public Builder additionalProperties(final boolean additionalProperties) {
            this.additionalProperties = additionalProperties;
            return this;
        }

        @Override
        public ObjectSchema build() {
            return new ObjectSchema(this);
        }

        public Builder maxProperties(final Integer maxProperties) {
            this.maxProperties = maxProperties;
            return this;
        }

        public Builder minProperties(final Integer minProperties) {
            this.minProperties = minProperties;
            return this;
        }

        public Builder patternProperty(final Pattern pattern, final Schema schema) {
            this.patternProperties.put(pattern, schema);
            return this;
        }

        public Builder patternProperty(final String pattern, final Schema schema) {
            return patternProperty(Pattern.compile(pattern), schema);
        }

        /**
         * Adds a property dependency.
         *
         * @param ifPresent     the name of the property which if is present then a property with name
         *                      {@code mustBePresent} is mandatory
         * @param mustBePresent a property with this name must exist in the subject under validation if a property
         *                      named {@code ifPresent} exists
         * @return {@code this}
         */
        public Builder propertyDependency(final String ifPresent, final String mustBePresent) {
            Set<String> dependencies = propertyDependencies.get(ifPresent);
            if (dependencies == null) {
                dependencies = new HashSet<String>(1);
                propertyDependencies.put(ifPresent, dependencies);
            }
            dependencies.add(mustBePresent);
            return this;
        }

        public Builder requiresObject(final boolean requiresObject) {
            this.requiresObject = requiresObject;
            return this;
        }

        public Builder schemaDependency(final String ifPresent, final Schema expectedSchema) {
            schemaDependencies.put(ifPresent, expectedSchema);
            return this;
        }

        public Builder schemaOfAdditionalProperties(final Schema schemaOfAdditionalProperties) {
            this.schemaOfAdditionalProperties = schemaOfAdditionalProperties;
            return this;
        }
    }
}
