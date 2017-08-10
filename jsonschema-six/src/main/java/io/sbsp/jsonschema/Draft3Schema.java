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

public interface Draft3Schema extends DraftSchema<Draft3Schema> {

    @Override
    default JsonSchemaVersion getVersion() {
        return JsonSchemaVersion.Draft3;
    }

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
    boolean isAnyType();
    Set<JsonSchemaType> getDisallow();
    Optional<Schema> getExtendsSchema();
    Boolean isRequired();
    Optional<JsonArray> getEnumValues();
    Optional<JsonValue> getDefaultValue();


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
    Number getDivisibleBy();
    Number getMaximum();
    Number getMinimum();
    boolean isExclusiveMinimum();
    boolean isExclusiveMaximum();

    // ###################################
    // #### ARRAY KEYWORDS  ##############
    // ###################################

    Integer getMinItems();
    Integer getMaxItems();
    Optional<Draft3Schema> getAllItemSchema();
    List<Schema> getItemSchemas();
    boolean isAllowAdditionalItems();
    Optional<Draft3Schema> getAdditionalItemsSchema();
    boolean requiresUniqueItems();

    // ###################################
    // #### OBJECT KEYWORDS  ##############
    // ###################################

    Map<String, Schema> getProperties();
    Map<String, Schema> getPatternProperties();
    boolean isAllowAdditionalProperties();
    Optional<Draft3Schema> getAdditionalPropertiesSchema();
    SetMultimap<String, String> getPropertyDependencies();
    Map<String, Schema> getPropertySchemaDependencies();
}
