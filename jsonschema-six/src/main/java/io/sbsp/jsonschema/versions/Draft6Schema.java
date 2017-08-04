package io.sbsp.jsonschema.versions;

import com.google.common.collect.SetMultimap;
import io.sbsp.jsonschema.JsonSchema;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.enums.JsonSchemaType;

import javax.json.JsonValue;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface Draft6Schema {
    SchemaLocation getLocation();

    // ###################################
    // #### Meta KEYWORDS ##############
    // ###################################

    URI getSchemaURI();
    URI getId();
    String getTitle();
    String getDescription();
    List<?> getExamples();
    List<Schema> getDefinitions();

    // ###################################
    // #### Shared KEYWORDS ##############
    // ###################################

    Set<JsonSchemaType> getTypes();
    Optional<javax.json.JsonArray> getEnumValues();
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
    Optional<Schema> getAllItemSchema();
    List<Schema> getItemSchemas();
    Optional<Schema> getAdditionalItemsSchema();
    Optional<Schema> getContainsSchema();
    boolean requiresUniqueItems();

    // ###################################
    // #### OBJECT KEYWORDS  ##############
    // ###################################

    Map<String, Schema> getProperties();
    Map<String, Schema> getPatternProperties();
    Optional<Schema> getAdditionalPropertiesSchema();
    Optional<Schema> getPropertyNameSchema();
    SetMultimap<String, String> getPropertyDependencies();
    Map<String, Schema> getPropertySchemaDependencies();
    Integer getMaxProperties();
    Integer getMinProperties();
    Set<String> getRequiredProperties();

    public JsonSchema getJsonSchema();


}
