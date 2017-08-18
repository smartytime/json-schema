package io.sbsp.jsonschema;

import io.sbsp.jsonschema.enums.JsonSchemaType;
import io.sbsp.jsonschema.keyword.KeywordInfo;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.loading.LoadingReport;
import io.sbsp.jsonschema.loading.SchemaLoader;

import javax.annotation.Nullable;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public interface SchemaBuilder<SELF extends SchemaBuilder> {

    // ##################################################################
    // ########           COUPLE OF GETTERS                ##############
    // ##################################################################

    @Nullable
    URI $id();

    @Nullable
    URI $ref();

    // ##################################################################
    // ########           METADATA KEYWORDS                ##############
    // ##################################################################

    SELF ref(URI ref);
    SELF title(String title);
    SELF defaultValue(JsonValue defaultValue);
    SELF description(String description);
    SELF type(JsonSchemaType requiredType);
    SELF orType(JsonSchemaType requiredType);
    SELF types(Set<JsonSchemaType> requiredTypes);
    SELF clearTypes();


    // ##################################################################
    // ########           STRING KEYWORDS                  ##############
    // ##################################################################

    SELF pattern(String pattern);
    SELF pattern(Pattern pattern);
    SELF minLength(@Min(0) Integer minLength);
    SELF maxLength(@Min(0) Integer maxLength);
    SELF format(String format);

    // ##################################################################
    // ########           OBJECT KEYWORDS                  ##############
    // ##################################################################

    SELF schemaOfAdditionalProperties(SchemaBuilder schemaOfAdditionalProperties);
    SELF schemaDependency(String property, SchemaBuilder dependency);
    SELF propertyDependency(String ifPresent, String thenRequireThisProperty);
    SELF requiredProperty(String requiredProperty);
    SELF propertySchema(String propertySchemaKey, SchemaBuilder propertySchemaValue);
    SELF propertyNameSchema(SchemaBuilder propertyNameSchema);
    SELF patternProperty(String pattern, SchemaBuilder schema);
    SELF patternProperty(Pattern pattern, SchemaBuilder schema);
    SELF minProperties(@Min(0) Integer minProperties);
    SELF maxProperties(@Min(0) Integer maxProperties);
    SELF clearRequiredProperties();
    SELF clearPropertySchemas();

    // ##################################################################
    // ########           NUMBER KEYWORDS                  ##############
    // ##################################################################

    SELF multipleOf(@Min(1) Number multipleOf);
    SELF exclusiveMinimum(Number exclusiveMinimum);
    SELF minimum(Number minimum);
    SELF exclusiveMaximum(Number exclusiveMaximum);
    SELF maximum(Number maximum);

    // ##################################################################
    // ########           ARRAY KEYWORDS                  ##############
    // ##################################################################

    SELF needsUniqueItems(boolean needsUniqueItems);
    SELF maxItems(@Min(0) Integer maxItems);
    SELF minItems(@Min(0) Integer minItems);
    SELF containsSchema(@Valid SchemaBuilder containsSchema);
    SELF noAdditionalItems();
    SELF schemaOfAdditionalItems(@Valid SchemaBuilder schemaOfAdditionalItems);
    SELF itemSchemas(List<? extends SchemaBuilder> itemSchemas);
    SELF itemSchema(SchemaBuilder itemSchema);
    SELF allItemSchema(@Valid SchemaBuilder allItemSchema);

    // ##################################################################
    // ########           COMMON KEYWORDS                  ##############
    // ##################################################################

    SELF notSchema(SchemaBuilder notSchema);
    SELF enumValues(JsonArray enumValues);
    SELF constValueString(String constValue);
    SELF constValueInteger(int constValue);
    SELF constValueDouble(double constValue);
    SELF constValue(JsonValue constValue);
    SELF oneOfSchemas(Collection<? extends SchemaBuilder<?>> oneOfSchemas);
    SELF oneOfSchema(SchemaBuilder oneOfSchema);
    SELF anyOfSchemas(Collection<? extends SchemaBuilder> anyOfSchemas);
    SELF anyOfSchema(SchemaBuilder anyOfSchema);
    SELF allOfSchemas(Collection<? extends SchemaBuilder> allOfSchemas);
    SELF allOfSchema(SchemaBuilder allOfSchema);

    // ##################################################################
    // ########           INNER KEYWORDS                   ##############
    // ##################################################################
    /**
     * Adds a keyword directly to the builder - this bypasses any convenience methods, and can be used for loading custom
     * keywords.
     * @return reference to self
     */
    <K extends SchemaKeyword> SELF keyword(KeywordInfo<K> keyword, K keywordValue);

    @SuppressWarnings("unchecked")
    <X extends SchemaKeyword> X getKeyword(KeywordInfo<X> keyword);
    SELF extraProperty(String propertyName, JsonValue jsonValue);


    Schema build(SchemaLocation itemsLocation, LoadingReport report);
    Schema build();

    SELF withLoadingReport(LoadingReport report);
    SELF provider(JsonProvider provider);
    SELF withSchemaLoader(SchemaLoader factory);
    SELF withCurrentDocument(JsonObject currentDocument);
}
