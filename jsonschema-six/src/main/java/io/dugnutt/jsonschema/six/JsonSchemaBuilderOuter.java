package io.dugnutt.jsonschema.six;

import com.google.common.base.Preconditions;
import io.dugnutt.jsonschema.six.JsonSchemaType;
import io.dugnutt.jsonschema.six.SchemaLocation;
import io.dugnutt.jsonschema.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;

import javax.annotation.Nullable;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

// @Builder(builderClassName = "JsonSchemaBuilder")
// @AllArgsConstructor
// public class JsonSchemaBuilderOuter {
//
//     @Nullable
//     private final String id;
//
//     /**
//      * {@see TITLE}
//      */
//     @Nullable
//     private final String title;
//
//     /**
//      * {@see DESCRIPTION}
//      */
//     @Nullable
//     private final String description;
//
//     /**
//      * {@see ALL_OF}
//      */
//     @NotNull
//     @Singular
//     private final List<JsonSchemaBuilder> allOfSchemas;
//
//     /**
//      * {@see ANY_OF}
//      */
//     @NotNull
//     @Singular
//     private final List<JsonSchemaBuilder> anyOfSchemas;
//
//     /**
//      * {@see ONE_OF}
//      */
//     @NotNull
//     @Singular
//     private final List<JsonSchemaBuilder> oneOfSchemas;
//
//     /**
//      * {@see io.dugnutt.jsonschema.six.schema.JsonSchemaKeyword.TYPE}
//      */
//     @NotNull
//     @Singular
//     private final Set<JsonSchemaType> types;
//
//     /**
//      * {@see NOT}
//      */
//     private final JsonSchemaBuilder notSchema;
//
//     /**
//      * {@see ENUM}
//      */
//     @Nullable
//     private final JsonArray enumValues;
//
//     /**
//      * {@see CONST}
//      */
//     @Nullable
//     private final JsonValue constValue;
//
//     /*
//      *
//      * STRING PROPERTIES
//      *
//      */
//
//     @Min(0)
//     @Nullable
//     private final Integer minLength;
//
//     @Min(0)
//     @Nullable
//     private final Integer maxLength;
//
//     @Nullable
//     private final Pattern pattern;
//
//     @Nullable
//     private final String format;
//
//
//     /*
//      * OBJECT PROPERTIES
//      */
//
//     @Singular
//     private final Map<String, JsonSchemaBuilder> propertySchemas;
//
//     private final JsonSchemaBuilder schemaOfAdditionalProperties;
//
//     @Singular
//     private final Set<String> requiredProperties;
//
//     @Min(0)
//     private final Integer minProperties;
//
//     @Min(0)
//     private final Integer maxProperties;
//
//     private final JsonSchemaBuilder propertyNameSchema;
//     private final Map<String, Set<String>> propertyDependencies;
//     private final Map<String, JsonSchemaBuilder> schemaDependencies;
//     private final boolean requiresObject;
//     private final Map<Pattern, JsonSchemaBuilder> patternProperties;
//
//
//      /*
//      * ARRAY PROPERTIES
//      */
//
//     @Min(0)
//     private final Integer minItems;
//
//     @Min(0)
//     private final Integer maxItems;
//
//     private final boolean needsUniqueItems;
//
//     @Singular
//     @Valid
//     //todo:ericm Items and allItems can't be non-null
//     private final List<JsonSchemaBuilder> itemSchemas;
//
//     @Valid
//     private final JsonSchemaBuilder allItemSchema;
//
//     @Valid
//     private final JsonSchemaBuilder containsSchema;
//
//     private final boolean requiresArray;
//
//     @Valid
//     private final JsonSchemaBuilder schemaOfAdditionalItems;
//
//     /*
//      NUMBER SCHEMA
//      */
//     private final boolean requiresNumber;
//     private final boolean requiresInteger;
//     private final Number minimum;
//     private final Number maximum;
//
//     @Min(1)
//     private final Number multipleOf;
//     private final Number exclusiveMinimum;
//     private final Number exclusiveMaximum;
//
//     private final String ref;
//
//     @Deprecated
//     private static JsonSchemaBuilder builder() {
//         throw new UnsupportedOperationException();
//     }
//
//     public static JsonSchemaBuilder jsonSchema() {
//         return new JsonSchemaBuilder(null);
//     }
//
//     public static JsonSchemaBuilder rootSchema(SchemaLocation schemaInfo) {
//         return new JsonSchemaBuilder(schemaInfo);
//     }
//
//     public static class JsonSchemaBuilder {
//         private SchemaLocation location;
//
//         private JsonSchemaBuilder(SchemaLocation location) {
//             checkNotNull(location, "location must not be null");
//             this.location = location;
//         }
//
//         private JsonSchema build() {
//             Preconditions.checkState(location != null, "location is required to build");
//
//
//
//             throw new UnsupportedOperationException("Not implemented yet");
//         }
//
//         private JsonSchemaDetails toJsonSchemaDetails() {
//             final JsonSchemaDetails.JsonSchemaDetailsBuilder detailsBuilder = JsonSchemaDetails.builder()
//                     .id(this.id)
//                     .title(this.title)
//                     .description(this.description)
//                     .constValue(this.constValue)
//                     .enumValues(this.enumValues)
//                     .types(this.types);
//
//             return detailsBuilder.build();
//         }
//
//         public JsonSchemaBuilder constValueString(String constValue) {
//             return constValue(JsonUtils.jsonStringValue(constValue));
//         }
//
//         public JsonSchemaBuilder constValueInteger(int constValue) {
//             return constValue(JsonUtils.jsonNumberValue(constValue));
//         }
//
//         public JsonSchemaBuilder constValueDouble(double constValue) {
//             return constValue(JsonUtils.jsonNumberValue(constValue));
//         }
//     }
//
//     public static class JsonSchemaBuilder2 extends JsonSchemaBuilder {
//         private JsonSchemaBuilder delegate;
//
//
//     }
// }
