package io.sbsp.jsonschema.six.keywords;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import io.sbsp.jsonschema.six.JsonSchemaGenerator;
import io.sbsp.jsonschema.six.Schema;
import io.sbsp.jsonschema.six.enums.JsonSchemaKeyword;
import io.sbsp.jsonschema.six.enums.JsonSchemaType;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;

import javax.validation.constraints.Min;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Object schema validator.
 */
@Getter
@Builder
public class ObjectKeywords implements SchemaKeywords {

    @NonNull
    @Singular
    private final Map<String, Schema> propertySchemas;

    private final Schema schemaOfAdditionalProperties;

    @Singular
    @NonNull
    private final Set<String> requiredProperties;

    @Min(0)
    private final Integer minProperties;

    @Min(0)
    private final Integer maxProperties;

    private final Schema propertyNameSchema;

    @NonNull
    private final SetMultimap<String, String> propertyDependencies;

    @NonNull
    @Singular
    private final Map<String, Schema> schemaDependencies;

    @NonNull
    @Singular
    private final Map<Pattern, Schema> patternProperties;

    @Override
    public Set<JsonSchemaType> getApplicableTypes() {
        return Collections.singleton(JsonSchemaType.OBJECT);
    }

    @Override
    public JsonSchemaGenerator toJson(JsonSchemaGenerator writer) {
        writer.optionalWrite(JsonSchemaKeyword.PROPERTIES, propertySchemas)
                .optionalWrite(JsonSchemaKeyword.MIN_PROPERTIES, minProperties)
                .optionalWrite(JsonSchemaKeyword.MAX_PROPERTIES, maxProperties)
                .optionalWrite(JsonSchemaKeyword.REQUIRED, requiredProperties)
                .optionalWrite(JsonSchemaKeyword.ADDITIONAL_PROPERTIES, schemaOfAdditionalProperties)
                .optionalWrite(JsonSchemaKeyword.PROPERTY_NAMES, propertyNameSchema)
                .optionalWrite(JsonSchemaKeyword.DEPENDENCIES, schemaDependencies)
                .optionalWritePatternProperties(patternProperties);

        describePropertyDependenciesTo(writer);
        return writer;
    }

    public Optional<Schema> getPropertyNameSchema() {
        return Optional.ofNullable(propertyNameSchema);
    }

    public Optional<Schema> getSchemaOfAdditionalProperties() {
        return Optional.ofNullable(schemaOfAdditionalProperties);
    }

    private void describePropertyDependenciesTo(JsonSchemaGenerator writer) {
        if (propertyDependencies != null && propertyDependencies.size() > 0) {
            writer.writeKey(JsonSchemaKeyword.DEPENDENCIES);
            writer.object();
            propertyDependencies.asMap().forEach((k, v) -> {
                writer.writePropertyName(k);
                writer.array();
                v.forEach(writer::write);
                writer.endArray();
            });
            writer.endObject();
        }
    }

    /**
     * Pattern doesn't work with equals/hashcode.  This method allows us to verify equals/hashCode based on
     * the pattern string.
     */
    private Map<String, Schema> patternPropForEquals() {
        return patternProperties.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        k -> k.getKey().pattern(),
                        Map.Entry::getValue,
                        (v1, v2) -> v1));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ObjectKeywords)) {
            return false;
        }
        final ObjectKeywords that = (ObjectKeywords) o;
        return Objects.equal(propertySchemas, that.propertySchemas) &&
                Objects.equal(schemaOfAdditionalProperties, that.schemaOfAdditionalProperties) &&
                Objects.equal(requiredProperties, that.requiredProperties) &&
                Objects.equal(minProperties, that.minProperties) &&
                Objects.equal(maxProperties, that.maxProperties) &&
                Objects.equal(propertyNameSchema, that.propertyNameSchema) &&
                Objects.equal(propertyDependencies, that.propertyDependencies) &&
                Objects.equal(schemaDependencies, that.schemaDependencies) &&
                Objects.equal(patternPropForEquals(), that.patternPropForEquals());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(propertySchemas, schemaOfAdditionalProperties, requiredProperties, minProperties,
                maxProperties, propertyNameSchema, propertyDependencies, schemaDependencies, patternPropForEquals());
    }

    /**
     * Builder class for {@link ObjectKeywords}.
     */
    public static final class ObjectKeywordsBuilder {
        SetMultimap<String, String> propertyDependencies = ImmutableSetMultimap.of();
        private ImmutableSetMultimap.Builder<String, String> propertyDependencyBuilder = ImmutableSetMultimap.builder();

        /**
         * Adds a property dependency.
         *
         * @param ifPresent     the name of the property which if is present then a property with name
         *                      {@code mustBePresent} is mandatory
         * @param mustBePresent a property with this name must exist in the subject under validation if a property
         *                      named {@code ifPresent} exists
         * @return {@code this}
         */
        public ObjectKeywordsBuilder propertyDependency(final String ifPresent, final String mustBePresent) {
            propertyDependencyBuilder.put(ifPresent, mustBePresent);
            this.propertyDependencies = propertyDependencyBuilder.build();
            return this;
        }

        @Override
        public final boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final ObjectKeywordsBuilder that = (ObjectKeywordsBuilder) o;
            return Objects.equal(propertyDependencies, that.propertyDependencies) &&
                    Objects.equal(propertyDependencyBuilder, that.propertyDependencyBuilder) &&
                    Objects.equal(propertySchemas$key, that.propertySchemas$key) &&
                    Objects.equal(propertySchemas$value, that.propertySchemas$value) &&
                    Objects.equal(schemaOfAdditionalProperties, that.schemaOfAdditionalProperties) &&
                    Objects.equal(requiredProperties, that.requiredProperties) &&
                    Objects.equal(minProperties, that.minProperties) &&
                    Objects.equal(maxProperties, that.maxProperties) &&
                    Objects.equal(propertyNameSchema, that.propertyNameSchema) &&
                    Objects.equal(schemaDependencies$key, that.schemaDependencies$key) &&
                    Objects.equal(schemaDependencies$value, that.schemaDependencies$value) &&
                    Objects.equal(patternPropertiesKeys(), that.patternPropertiesKeys()) &&
                    Objects.equal(patternProperties$value, that.patternProperties$value);
        }

        @Override
        public final int hashCode() {
            return Objects.hashCode(propertyDependencies, propertyDependencyBuilder, propertySchemas$key, propertySchemas$value,
                    schemaOfAdditionalProperties, requiredProperties, minProperties, maxProperties, propertyNameSchema,
                    schemaDependencies$key, schemaDependencies$value, patternPropertiesKeys(), patternProperties$value);
        }

        private List<String> patternPropertiesKeys() {
            return patternProperties$key != null
                    ? patternProperties$key.stream()
                    .map(Pattern::pattern)
                    .collect(Collectors.toList())
                    : null;
        }
    }

    private static final ObjectKeywords BLANK_OBJECT_KEYWORDS = builder().build();

    public static final ObjectKeywords getBlankObjectKeywords() {
        return BLANK_OBJECT_KEYWORDS;
    }
}
