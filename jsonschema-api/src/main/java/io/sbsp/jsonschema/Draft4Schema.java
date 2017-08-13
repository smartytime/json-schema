package io.sbsp.jsonschema;

import com.google.common.collect.SetMultimap;
import io.sbsp.jsonschema.enums.JsonSchemaType;
import io.sbsp.jsonschema.enums.JsonSchemaVersion;

import javax.json.JsonValue;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface Draft4Schema extends DraftSchema<Draft4Schema> {

    SchemaLocation getLocation();

    // ###################################
    // #### Meta KEYWORDS ##############
    // ###################################

    URI getSchemaURI();
    URI getId();
    String getTitle();
    String getDescription();

    // ###################################
    // #### Shared KEYWORDS ##############
    // ###################################

    Set<JsonSchemaType> getTypes();
    Optional<javax.json.JsonArray> getEnumValues();
    Optional<JsonValue> getDefaultValue();
    Optional<Draft4Schema> getNotSchema();
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
    boolean isExclusiveMinimum();
    boolean isExclusiveMaximum();

    // ###################################
    // #### ARRAY KEYWORDS  ##############
    // ###################################

    Integer getMinItems();
    Integer getMaxItems();
    Optional<Draft4Schema> getAllItemSchema();
    List<Schema> getItemSchemas();
    boolean isAllowAdditionalItems();
    Optional<Draft4Schema> getAdditionalItemsSchema();
    boolean requiresUniqueItems();

    // ###################################
    // #### OBJECT KEYWORDS  ##############
    // ###################################

    boolean isAllowAdditionalProperties();
    Optional<Draft4Schema> getAdditionalPropertiesSchema();
    SetMultimap<String, String> getPropertyDependencies();
    Map<String, Schema> getPropertySchemaDependencies();
    Integer getMaxProperties();
    Integer getMinProperties();
    Set<String> getRequiredProperties();

    @Override
    default JsonSchemaVersion getVersion() {
        return JsonSchemaVersion.Draft4;
    }

    @Override
    default Draft4Schema asDraft4() {
        return this;
    }
}
