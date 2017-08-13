package io.sbsp.jsonschema;

import com.google.common.collect.SetMultimap;
import io.sbsp.jsonschema.enums.JsonSchemaType;

import javax.json.JsonArray;
import javax.json.JsonValue;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.sbsp.jsonschema.KeyMissingException.missingProperty;

public interface DraftSchema<D extends DraftSchema> extends Schema {

    Set<JsonSchemaType> getTypes();

    Optional<JsonArray> getEnumValues();

    Optional<JsonValue> getDefaultValue();

    String getFormat();

    Integer getMinLength();

    Integer getMaxLength();

    String getPattern();

    Number getMaximum();

    Number getMinimum();

    Integer getMinItems();

    Integer getMaxItems();

    Optional<D> getAllItemSchema();

    List<Schema> getItemSchemas();

    Optional<D> getAdditionalItemsSchema();

    Map<String, Schema> getProperties();

    Map<String, Schema> getPatternProperties();

    Optional<D> getAdditionalPropertiesSchema();

    SetMultimap<String, String> getPropertyDependencies();

    Map<String, Schema> getPropertySchemaDependencies();

    boolean requiresUniqueItems();

    @SuppressWarnings("unchecked")
    D asType(Schema source);

    default Optional<D> findPropertySchema(String schemaName) {
        final Schema found = getProperties().get(schemaName);
        return Optional.ofNullable(found).map(this::asType);
    }

    default Optional<D> findPatternSchema(String pattern) {
        final Schema found = getPatternProperties().get(pattern);
        return Optional.ofNullable(found).map(this::asType);
    }

    default D getPropertySchema(String property) {
        return findPropertySchema(property).orElseThrow(missingProperty(this, property));
    }

    default D getPatternSchema(String pattern) {
        return findPatternSchema(pattern).orElseThrow(missingProperty(this, pattern));
    }

}
