package io.sbsp.jsonschema.six;

import com.google.common.base.MoreObjects;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import io.sbsp.jsonschema.six.JsonSchemaDetails.JsonSchemaDetailsBuilder;
import io.sbsp.jsonschema.six.enums.JsonSchemaKeyword;
import io.sbsp.jsonschema.six.enums.JsonSchemaType;
import io.sbsp.jsonschema.six.keywords.ArrayKeywords;
import io.sbsp.jsonschema.six.keywords.NumberKeywords;
import io.sbsp.jsonschema.six.keywords.ObjectKeywords;
import io.sbsp.jsonschema.six.keywords.StringKeywords;

import javax.annotation.Nullable;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGenerator;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.ADDITIONAL_ITEMS;
import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.ADDITIONAL_PROPERTIES;
import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.ALL_OF;
import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.ANY_OF;
import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.CONTAINS;
import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.DEPENDENCIES;
import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.ITEMS;
import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.NOT;
import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.ONE_OF;
import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.PATTERN_PROPERTIES;
import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.PROPERTIES;
import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.PROPERTY_NAMES;
import static io.sbsp.jsonschema.six.keywords.ArrayKeywords.ArrayKeywordsBuilder;
import static io.sbsp.jsonschema.six.keywords.NumberKeywords.NumberKeywordsBuilder;
import static io.sbsp.jsonschema.six.keywords.ObjectKeywords.ObjectKeywordsBuilder;
import static io.sbsp.jsonschema.six.keywords.StringKeywords.StringKeywordsBuilder;
import static io.sbsp.jsonschema.utils.StreamUtils.safeTransform;
import static io.sbsp.jsonschema.utils.StreamUtils.supplyIfNull;

public interface Schema {
    SchemaLocation getLocation();

    ArrayKeywords getArrayKeywords();

    Optional<Schema> getNotSchema();

    Optional<javax.json.JsonValue> getConstValue();

    NumberKeywords getNumberKeywords();

    URI getId();

    String getTitle();

    String getDescription();

    List<Schema> getAllOfSchemas();

    List<Schema> getAnyOfSchemas();

    List<Schema> getOneOfSchemas();

    Set<JsonSchemaType> getTypes();

    Optional<javax.json.JsonArray> getEnumValues();

    ObjectKeywords getObjectKeywords();

    StringKeywords getStringKeywords();

    Optional<JsonValue> getDefaultValue();

    boolean hasStringKeywords();

    boolean hasObjectKeywords();

    boolean hasArrayKeywords();

    boolean hasNumberKeywords();

    JsonSchemaGenerator toJson(final JsonSchemaGenerator writer);

    default void toJson(final JsonGenerator writer) {
        this.toJson(new JsonSchemaGenerator(writer));
    }

    default URI getAbsoluteURI() {
        return getLocation().getUniqueURI();
    }

    default URI getPointerFragmentURI() {
        return getLocation().getJsonPointerFragment();
    }

    static JsonSchemaBuilder jsonSchemaBuilder() {
        return new JsonSchemaBuilder();
    }

    static JsonSchemaBuilder jsonSchemaBuilder(SchemaLocation location) {
        return new JsonSchemaBuilder(location);
    }

    static JsonSchemaBuilder jsonSchemaBuilderWithId(SchemaLocation location, String id) {
        return new JsonSchemaBuilder(location, URI.create(id));
    }

    static JsonSchemaBuilder refSchemaBuilder(URI ref, SchemaLocation location, @Nullable SchemaFactory schemaFactory) {
        return jsonSchemaBuilder(location).ref(ref, schemaFactory);
    }

    static JsonSchemaBuilder jsonSchemaBuilderWithId(String id) {
        checkNotNull(id, "id must not be null");
        return new JsonSchemaBuilder(URI.create(id));
    }

    static JsonSchemaBuilder jsonSchemaBuilderWithId(URI id) {
        checkNotNull(id, "id must not be null");
        return new JsonSchemaBuilder(id);
    }

    class JsonSchemaBuilder {
        private JsonObject currentDocument;
        private JsonProvider provider;
        private SchemaFactory schemaFactory;
        private URI ref;
        private URI id;
        private final SchemaLocation location;

        private final JsonSchemaDetailsBuilder detailsBuilder;

        private ArrayKeywordsBuilder arrayKeywords;
        private StringKeywordsBuilder stringKeywords;
        private ObjectKeywordsBuilder objectKeywords;
        private NumberKeywordsBuilder numberKeywords;

        //Shared subschemas
        private final Multimap<JsonSchemaKeyword, JsonSchemaBuilder> combinedSchemas = LinkedListMultimap.create();
        private JsonSchemaBuilder notSchema;

        //Array subschemas
        private JsonSchemaBuilder schemaOfAdditionalItems;
        private final List<JsonSchemaBuilder> itemSchemas = new ArrayList<>();
        private JsonSchemaBuilder allItemSchema;
        private JsonSchemaBuilder containsSchema;

        //Object subschemas
        private final Map<Pattern, JsonSchemaBuilder> patternProperties = new LinkedHashMap<>();
        private final Map<String, JsonSchemaBuilder> propertySchemas = new LinkedHashMap<>();
        private final Map<String, JsonSchemaBuilder> schemaDependencies = new LinkedHashMap<>();
        private JsonSchemaBuilder propertyNameSchema;
        private JsonSchemaBuilder schemaOfAdditionalProperties;

        private JsonSchemaBuilder() {
            this.location = null;
            this.detailsBuilder = JsonSchemaDetails.builder();
        }

        private JsonSchemaBuilder(URI $id) {
            checkNotNull($id, "$id must not be null");
            this.id = $id;
            this.location = null;
            this.detailsBuilder = JsonSchemaDetails.builder().id($id);
        }

        private JsonSchemaBuilder(SchemaLocation location, URI $id) {
            checkNotNull(location, "location must not be null");
            checkNotNull($id, "$id must not be null");
            this.id = $id;
            this.location = location;
            this.detailsBuilder = JsonSchemaDetails.builder().id($id);
        }

        private JsonSchemaBuilder(SchemaLocation location) {
            checkNotNull(location, "location must not be null");
            this.location = location;
            this.detailsBuilder = JsonSchemaDetails.builder();
        }

        public JsonSchemaBuilder currentDocument(JsonObject currentDocument) {
            this.currentDocument = currentDocument;
            return this;
        }

        public JsonSchemaBuilder ref(URI ref, @Nullable SchemaFactory schemaFactory) {
            this.ref = ref;
            this.schemaFactory = schemaFactory;
            return this;
        }


        public JsonSchemaBuilder ref(String ref) {
            this.ref = URI.create(ref);
            return this;
        }

        public JsonSchemaBuilder provider(JsonProvider provider) {
            this.provider = provider;
            return this;
        }

        public JsonSchemaBuilder schemaFactory(SchemaFactory schemaFactory) {
            this.schemaFactory = schemaFactory;
            return this;
        }

        private void raiseIssue(SchemaLocation location, JsonSchemaKeyword keyword, String messageCode) {
            throw new SchemaException(location.getJsonPointerFragment(), messageCode);
        }

        public Schema build() {
            final SchemaLocation location;
            if (this.id == null) {
                location = supplyIfNull(this.location, SchemaLocation::anonymousRoot);
            } else if(this.location != null) {
                location = this.location.withId(this.id);
            } else {
                location = SchemaLocation.documentRoot(this.id);
            }
            return build(location);
        }

        private Schema build(SchemaLocation location) {
            checkNotNull(location, "location must not be null");

            // Use the location provided during building as an override
            location = MoreObjects.firstNonNull(this.location, location);
            if (this.id != null) {
                location = location.withId(id);
            }

            final URI thisSchemaURI = location.getUniqueURI();

            if (schemaFactory != null) {
                final Optional<Schema> cachedSchema = schemaFactory.findCachedSchema(thisSchemaURI);
                if (cachedSchema.isPresent()) {
                    return cachedSchema.get();
                }
            }

            if (this.ref != null) {
                return ReferenceSchema.refSchemaBuilder(this.ref)
                        .factory(schemaFactory)
                        .currentDocument(currentDocument)
                        .location(location)
                        .build();
            }

            /*
            ############################################################################################################

            The {@code detailsBuilder} already has most of the parameters.  We've overloaded any of the schema-related
            options here so we can do a JIT build that includes any path-related information.

            That's why some of the options are represented here while others aren't

            ############################################################################################################
             */

            buildKeywordSubschema(location, NOT, notSchema).ifPresent(detailsBuilder::notSchema);

            buildKeywordSubschemaList(location, ALL_OF, combinedSchemas.get(ALL_OF)).ifPresent(detailsBuilder::allOfSchemas);
            buildKeywordSubschemaList(location, ANY_OF, combinedSchemas.get(ANY_OF)).ifPresent(detailsBuilder::anyOfSchemas);
            buildKeywordSubschemaList(location, ONE_OF, combinedSchemas.get(ONE_OF)).ifPresent(detailsBuilder::oneOfSchemas);

            // ##############################
            // BUILD ARRAY KEYWORD SUBSCHEMAS
            // ##############################

            buildKeywordSubschema(location, ADDITIONAL_ITEMS, this.schemaOfAdditionalItems).ifPresent(jsonSchema -> arrayKeywords().schemaOfAdditionalItems(jsonSchema));
            buildKeywordSubschema(location, CONTAINS, this.containsSchema).ifPresent(jsonSchema -> arrayKeywords().containsSchema(jsonSchema));

            //Verify that they didn't provide both an indexed array item checker AND a global array item checker.
            if (allItemSchema != null && itemSchemas.size() > 0) {
                raiseIssue(location, ITEMS, "invalidSchema.indexedSchemaWithAllItemSchema");
            }
            buildKeywordSubschema(location, ITEMS, allItemSchema).ifPresent(schema -> arrayKeywords().allItemSchema(schema));
            buildKeywordSubschemaList(location, ITEMS, this.itemSchemas).ifPresent(list -> arrayKeywords().itemSchemas(list));
            buildKeywordSubschema(location, CONTAINS, this.containsSchema).ifPresent(contains -> arrayKeywords().containsSchema(contains));

            // ###############################
            // BUILD OBJECT KEYWORD SUBSCHEMAS
            // ###############################

            buildKeywordSubschemaMap(location, PATTERN_PROPERTIES, this.patternProperties).ifPresent(map -> objectKeywords().patternProperties(map));
            buildKeywordSubschemaMap(location, PROPERTIES, this.propertySchemas).ifPresent(map -> objectKeywords().propertySchemas(map));
            buildKeywordSubschemaMap(location, DEPENDENCIES, this.schemaDependencies).ifPresent(map -> objectKeywords().schemaDependencies(map));
            buildKeywordSubschema(location, PROPERTY_NAMES, this.propertyNameSchema).ifPresent(schema -> objectKeywords().propertyNameSchema(schema));
            buildKeywordSubschema(location, ADDITIONAL_PROPERTIES, this.schemaOfAdditionalProperties).ifPresent(schema -> objectKeywords().schemaOfAdditionalProperties(schema));

            // Load all keywords that are not null
            buildObjectKeywords().ifPresent(detailsBuilder::objectKeywords);
            buildStringKeywords().ifPresent(detailsBuilder::stringKeywords);
            buildArrayKeywords().ifPresent(detailsBuilder::arrayKeywords);
            buildNumberKeywords().ifPresent(detailsBuilder::numberKeywords);

            final JsonSchemaDetails schemaDetails = detailsBuilder.build();
            return new JsonSchema(location, schemaDetails);
        }

        // #############################
        // BASIC SCHEMA METADATA SETTERS
        // #############################

        // public JsonSchemaBuilder id(String id) {
        //     if (id != null) {
        //         this.id = URI.create(id);
        //         if (this.location.isGenerated()) {
        //             this.location = SchemaLocation.documentRoot(this.id);
        //         }
        //         detailsBuilder.id(id);
        //     }
        //     return this;
        // }

        public JsonSchemaBuilder title(String title) {
            detailsBuilder.title(title);
            return this;
        }

        public JsonSchemaBuilder defaultValue(JsonValue defaultValue) {
            this.detailsBuilder.defaultValue(defaultValue);
            return this;
        }

        public JsonSchemaBuilder description(String description) {
            detailsBuilder.description(description);
            return this;
        }

        public JsonSchemaBuilder type(JsonSchemaType requiredType) {
            detailsBuilder.type(requiredType);
            return this;
        }

        public JsonSchemaBuilder orType(JsonSchemaType requiredType) {
            return type(requiredType);
        }

        public JsonSchemaBuilder types(Set<JsonSchemaType> requiredTypes) {
            detailsBuilder.types(requiredTypes);
            return this;
        }

        public JsonSchemaBuilder clearTypes() {
            detailsBuilder.clearTypes();
            return this;
        }

        // #################################
        // SHARED VALIDATION KEYWORD SETTERS
        // #################################

        public JsonSchemaBuilder allOfSchema(JsonSchemaBuilder allOfSchema) {
            combinedSchemas.put(ALL_OF, allOfSchema);
            return this;
        }

        public JsonSchemaBuilder allOfSchemas(Collection<? extends JsonSchemaBuilder> allOfSchemas) {
            checkNotNull(allOfSchemas, "allOfSchemas must not be null");
            combinedSchemas.replaceValues(ALL_OF, allOfSchemas);
            return this;
        }

        public JsonSchemaBuilder clearAllOfSchemas() {
            combinedSchemas.removeAll(ALL_OF);
            return this;
        }

        public JsonSchemaBuilder anyOfSchema(JsonSchemaBuilder anyOfSchema) {
            combinedSchemas.put(ANY_OF, anyOfSchema);
            return this;
        }

        public JsonSchemaBuilder anyOfSchemas(Collection<? extends JsonSchemaBuilder> anyOfSchemas) {
            combinedSchemas.replaceValues(ANY_OF, anyOfSchemas);
            return this;
        }

        public JsonSchemaBuilder clearAnyOfSchemas() {
            combinedSchemas.removeAll(ANY_OF);
            return this;
        }

        public JsonSchemaBuilder oneOfSchema(JsonSchemaBuilder oneOfSchema) {
            combinedSchemas.put(ONE_OF, oneOfSchema);
            return this;
        }

        public JsonSchemaBuilder oneOfSchemas(Collection<JsonSchemaBuilder> oneOfSchemas) {
            combinedSchemas.replaceValues(ONE_OF, oneOfSchemas);
            return this;
        }

        public JsonSchemaBuilder clearOneOfSchemas() {
            combinedSchemas.removeAll(ONE_OF);
            return this;
        }

        public JsonSchemaBuilder combinedSchema(JsonSchemaKeyword type, JsonSchemaBuilder schemaBuilder) {
            checkNotNull(schemaBuilder, "schemaBuilder must not be null");
            combinedSchemas.put(type, schemaBuilder);
            return this;
        }

        public JsonSchemaBuilder constValue(JsonValue constValue) {
            detailsBuilder.constValue(constValue);
            return this;
        }

        public JsonSchemaBuilder constValueDouble(double constValue) {
            detailsBuilder.constValue(provider().createValue(constValue));
            return this;
        }

        public JsonSchemaBuilder constValueInteger(int constValue) {
            detailsBuilder.constValue(provider().createValue(constValue));
            return this;
        }

        public JsonSchemaBuilder constValueString(String constValue) {
            detailsBuilder.constValue(provider().createValue(constValue));
            return this;
        }

        public JsonSchemaBuilder enumValues(JsonArray enumValues) {
            detailsBuilder.enumValues(enumValues);
            return this;
        }

        public JsonSchemaBuilder notSchema(JsonSchemaBuilder notSchema) {
            this.notSchema = notSchema;
            return this;
        }

        // #######################################################
        // ARRAY KEYWORDS
        // @see ArrayKeywords
        // #######################################################

        public JsonSchemaBuilder allItemSchema(@Valid JsonSchemaBuilder allItemSchema) {
            this.allItemSchema = allItemSchema;
            return this;
        }

        public JsonSchemaBuilder containsSchema(@Valid JsonSchemaBuilder containsSchema) {
            this.containsSchema = containsSchema;
            return this;
        }

        public JsonSchemaBuilder itemSchema(JsonSchemaBuilder itemSchema) {
            this.itemSchemas.add(itemSchema);
            return this;
        }

        public JsonSchemaBuilder itemSchemas(List<JsonSchemaBuilder> itemSchemas) {
            this.itemSchemas.clear();
            this.itemSchemas.addAll(itemSchemas);
            return this;
        }

        public JsonSchemaBuilder minItems(@Min(0) Integer minItems) {
            arrayKeywords().minItems(minItems);
            return this;
        }

        public JsonSchemaBuilder maxItems(@Min(0) Integer maxItems) {
            arrayKeywords().maxItems(maxItems);
            return this;
        }

        public JsonSchemaBuilder needsUniqueItems(boolean needsUniqueItems) {
            arrayKeywords().needsUniqueItems(needsUniqueItems);
            return this;
        }

        public JsonSchemaBuilder schemaOfAdditionalItems(@Valid JsonSchemaBuilder schemaOfAdditionalItems) {
            this.schemaOfAdditionalItems = schemaOfAdditionalItems;
            return this;
        }

        // #######################################################
        // NUMBER KEYWORDS
        // @see NumberKeywords
        // #######################################################

        public JsonSchemaBuilder exclusiveMaximum(Number exclusiveMaximum) {
            numberKeywords().exclusiveMaximum(exclusiveMaximum);
            return this;
        }

        public JsonSchemaBuilder exclusiveMinimum(Number exclusiveMinimum) {
            numberKeywords().exclusiveMinimum(exclusiveMinimum);
            return this;
        }

        public JsonSchemaBuilder maximum(Number maximum) {
            numberKeywords().maximum(maximum);
            return this;
        }

        public JsonSchemaBuilder minimum(Number minimum) {
            numberKeywords().minimum(minimum);
            return this;
        }

        public JsonSchemaBuilder multipleOf(@Min(1) Number multipleOf) {
            numberKeywords().multipleOf(multipleOf);
            return this;
        }

        // #######################################################
        // OBJECT KEYWORDS
        // @see ObjectKeywords
        // #######################################################

        public JsonSchemaBuilder clearPropertySchemas() {
            propertySchemas.clear();
            return this;
        }

        public JsonSchemaBuilder clearRequiredProperties() {
            objectKeywords().clearRequiredProperties();
            return this;
        }

        public JsonSchemaBuilder maxProperties(@Min(0) Integer maxProperties) {
            objectKeywords().maxProperties(maxProperties);
            return this;
        }

        public JsonSchemaBuilder minProperties(@Min(0) Integer minProperties) {
            objectKeywords().minProperties(minProperties);
            return this;
        }

        public JsonSchemaBuilder patternProperty(Pattern pattern, JsonSchemaBuilder schema) {
            this.patternProperties.put(pattern, schema);
            return this;
        }

        public JsonSchemaBuilder patternProperty(String pattern, JsonSchemaBuilder schema) {
            this.patternProperties.put(Pattern.compile(pattern), schema);
            return this;
        }

        public JsonSchemaBuilder propertyDependency(String ifPresent, String thenRequireThisProperty) {
            checkNotNull(ifPresent, "ifPresent must not be null");
            objectKeywords().propertyDependency(ifPresent, thenRequireThisProperty);
            return this;
        }

        public JsonSchemaBuilder propertyNameSchema(JsonSchemaBuilder propertyNameSchema) {
            this.propertyNameSchema = propertyNameSchema;
            return this;
        }

        public JsonSchemaBuilder propertySchema(String propertySchemaKey, JsonSchemaBuilder propertySchemaValue) {
            checkNotNull(propertySchemaKey, "propertySchemaKey must not be null");
            checkNotNull(propertySchemaValue, "propertySchemaValue must not be null");
            propertySchemas.put(propertySchemaKey, propertySchemaValue);
            return this;
        }

        public JsonSchemaBuilder requiredProperty(String requiredProperty) {
            checkNotNull(requiredProperty, "requiredProperty must not be null");
            objectKeywords().requiredProperty(requiredProperty);
            return this;
        }

        public JsonSchemaBuilder schemaDependency(String property, JsonSchemaBuilder dependency) {
            checkNotNull(property, "property must not be null");
            checkNotNull(dependency, "dependency must not be null");
            schemaDependencies.put(property, dependency);
            return this;
        }

        public JsonSchemaBuilder schemaOfAdditionalProperties(JsonSchemaBuilder schemaOfAdditionalProperties) {
            checkNotNull(schemaOfAdditionalProperties, "schemaOfAdditionalProperties must not be null");
            this.schemaOfAdditionalProperties = schemaOfAdditionalProperties;
            return this;
        }

        // #######################################################
        // STRING KEYWORDS
        // @see StringKeywords
        // #######################################################

        public JsonSchemaBuilder format(String format) {
            stringKeywords().format(format);
            return this;
        }

        public JsonSchemaBuilder maxLength(@Min(0) Integer maxLength) {
            stringKeywords().maxLength(maxLength);
            return this;
        }

        public JsonSchemaBuilder minLength(@Min(0) Integer minLength) {
            stringKeywords().minLength(minLength);
            return this;
        }

        public JsonSchemaBuilder pattern(Pattern pattern) {
            stringKeywords().pattern(pattern);
            return this;
        }

        public JsonSchemaBuilder pattern(String pattern) {
            return pattern(Pattern.compile(pattern));
        }

        // #######################################################
        // BUILDING SUBSCHEMAS
        // #######################################################

        private Optional<Schema> buildKeywordSubschema(SchemaLocation parentSchema, JsonSchemaKeyword keyword, JsonSchemaBuilder subschemaBuilder) {
            if (subschemaBuilder == null) {
                return Optional.empty();
            }
            final SchemaLocation subschemaLocation = parentSchema.child(keyword);
            return Optional.of(
                    findCachedOrBuild(subschemaLocation, subschemaBuilder)
            );
        }

        private <X> Optional<Map<X, Schema>> buildKeywordSubschemaMap(SchemaLocation parentSchema, JsonSchemaKeyword keyword, Map<X, JsonSchemaBuilder> builders) {
            checkNotNull(keyword, "keyword must not be null");
            if (builders == null || builders.size() == 0) {
                return Optional.empty();
            }

            Map<X, Schema> values = new LinkedHashMap<>();
            builders.forEach((x, builder) -> {
                final SchemaLocation childLocation = parentSchema.child(keyword.key(), x.toString());
                values.put(x, findCachedOrBuild(childLocation, builder));
            });
            return Optional.of(values);
        }

        private Optional<List<Schema>> buildKeywordSubschemaList(SchemaLocation parentSchema, JsonSchemaKeyword keyword, Collection<JsonSchemaBuilder> builders) {
            if (builders == null || builders.size() == 0) {
                return Optional.empty();
            }
            AtomicInteger idx = new AtomicInteger(0);

            return Optional.ofNullable(safeTransform(builders, builder -> {
                final SchemaLocation idxInfo = parentSchema.child(keyword.key(), idx.getAndIncrement());
                return findCachedOrBuild(idxInfo, builder);
            }));
        }

        private Schema findCachedOrBuild(SchemaLocation location, JsonSchemaBuilder builder) {
            if (schemaFactory != null) {
                final Optional<Schema> cachedSchema = schemaFactory.findCachedSchema(location.getUniqueURI());
                if (cachedSchema.isPresent()) {
                    return cachedSchema.get();
                }
            }

            return builder
                    .currentDocument(currentDocument)
                    .schemaFactory(schemaFactory)
                    .build(location);
        }

        private Optional<StringKeywords> buildStringKeywords() {
            return Optional.ofNullable(stringKeywords).map(StringKeywordsBuilder::build);
        }

        private Optional<ObjectKeywords> buildObjectKeywords() {
            return Optional.ofNullable(objectKeywords).map(ObjectKeywordsBuilder::build);
        }

        private Optional<ArrayKeywords> buildArrayKeywords() {
            return Optional.ofNullable(arrayKeywords).map(ArrayKeywordsBuilder::build);
        }

        private Optional<NumberKeywords> buildNumberKeywords() {
            return Optional.ofNullable(numberKeywords).map(NumberKeywordsBuilder::build);
        }

        // #######################################################
        // LAZY GETTERS
        // #######################################################

        private JsonProvider provider() {
            return MoreObjects.firstNonNull(provider, JsonProvider.provider());
        }

        private ObjectKeywordsBuilder objectKeywords() {
            if (objectKeywords == null) {
                objectKeywords = ObjectKeywords.builder();
            }
            return objectKeywords;
        }

        private ArrayKeywordsBuilder arrayKeywords() {
            if (arrayKeywords == null) {
                arrayKeywords = ArrayKeywords.builder();
            }
            return arrayKeywords;
        }

        private StringKeywordsBuilder stringKeywords() {
            if (stringKeywords == null) {
                stringKeywords = StringKeywords.builder();
            }
            return stringKeywords;
        }

        private NumberKeywordsBuilder numberKeywords() {
            if (numberKeywords == null) {
                numberKeywords = NumberKeywords.builder();
            }
            return numberKeywords;
        }
        //@formatter:on
    }
}
