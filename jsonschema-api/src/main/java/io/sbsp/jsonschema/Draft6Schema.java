package io.sbsp.jsonschema;

import com.google.common.collect.SetMultimap;
import io.sbsp.jsonschema.enums.JsonSchemaType;
import io.sbsp.jsonschema.enums.JsonSchemaVersion;

import javax.json.JsonArray;
import javax.json.JsonValue;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface Draft6Schema extends DraftSchema<Draft6Schema> {

    @Override
    default JsonSchemaVersion getVersion() {
        return JsonSchemaVersion.Draft6;
    }

    SchemaLocation getLocation();

    // ###################################
    // #### Meta KEYWORDS ##############
    // ###################################

    URI getSchemaURI();
    URI getId();
    String getTitle();
    String getDescription();
    JsonArray getExamples();
    Map<String, Schema> getDefinitions();

    // ###################################
    // #### Shared KEYWORDS ##############
    // ###################################

    Set<JsonSchemaType> getTypes();
    Optional<JsonArray> getEnumValues();
    Optional<JsonValue> getDefaultValue();
    Optional<Schema> getNotSchema();
    Optional<JsonValue> getConstValue();
    List<Schema> getAllOfSchemas();
    List<Schema> getAnyOfSchemas();
    List<Schema> getOneOfSchemas();

    // ###################################
    // #### String KEYWORDS ##############
    // ###################################

    String getFormat();
    Integer getMinLength();
    Integer getMaxLength();
    String getPattern();

    // ###################################
    // #### NUMBER KEYWORDS ##############
    // ###################################

    Number getMultipleOf();
    Number getMaximum();
    Number getMinimum();
    Number getExclusiveMinimum();
    Number getExclusiveMaximum();

    // ###################################
    // #### ARRAY KEYWORDS  ##############
    // ###################################

    Integer getMinItems();
    Integer getMaxItems();
    Optional<Draft6Schema> getAllItemSchema();
    List<Schema> getItemSchemas();
    Optional<Draft6Schema> getAdditionalItemsSchema();
    Optional<Draft6Schema> getContainsSchema();
    boolean requiresUniqueItems();

    // ###################################
    // #### OBJECT KEYWORDS  ##############
    // ###################################

    Map<String, Schema> getProperties();
    Map<String, Schema> getPatternProperties();
    Optional<Draft6Schema> getAdditionalPropertiesSchema();
    Optional<Draft6Schema> getPropertyNameSchema();
    SetMultimap<String, String> getPropertyDependencies();
    Map<String, Schema> getPropertySchemaDependencies();
    Integer getMaxProperties();
    Integer getMinProperties();
    Set<String> getRequiredProperties();

    @Override
    default Draft6Schema asDraft6() {
        return this;
    }
}
