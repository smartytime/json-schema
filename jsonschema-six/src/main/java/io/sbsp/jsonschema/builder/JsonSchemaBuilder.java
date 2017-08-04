package io.sbsp.jsonschema.builder;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import io.sbsp.jsonschema.JsonSchema;
import io.sbsp.jsonschema.JsonSchemaDetails;
import io.sbsp.jsonschema.ReferenceSchema;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaException;
import io.sbsp.jsonschema.SchemaFactory;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.enums.JsonSchemaKeywordType;
import io.sbsp.jsonschema.enums.JsonSchemaType;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.keyword.keywords.SchemaListKeyword;
import io.sbsp.jsonschema.keyword.keywords.SimpleKeyword;
import io.sbsp.jsonschema.keyword.keywords.number.MaximumKeyword;
import io.sbsp.jsonschema.keyword.keywords.number.MinimumKeyword;
import io.sbsp.jsonschema.keyword.keywords.shared.TypeKeyword;

import javax.annotation.Nullable;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.$ID;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.$REF;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ADDITIONAL_ITEMS;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ADDITIONAL_PROPERTIES;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ALL_OF;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ANY_OF;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.CONST;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.CONTAINS;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.DEFAULT;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.DEPENDENCIES;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.DESCRIPTION;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ENUM;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.FORMAT;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ITEMS;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.MAXIMUM;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.MAX_ITEMS;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.MAX_LENGTH;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.MAX_PROPERTIES;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.MINIMUM;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.MIN_ITEMS;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.MIN_LENGTH;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.MIN_PROPERTIES;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.MULTIPLE_OF;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.NOT;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ONE_OF;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.PATTERN;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.PATTERN_PROPERTIES;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.PROPERTIES;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.PROPERTY_NAMES;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.REQUIRED;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.TITLE;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.TYPE;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.UNIQUE_ITEMS;
import static io.sbsp.jsonschema.utils.StreamUtils.safeTransform;
import static io.sbsp.jsonschema.utils.StreamUtils.supplyIfNull;

public class JsonSchemaBuilder {

    private final Map<JsonSchemaKeywordType, SimpleKeyword<String>> strings = new HashMap<>();
    private final Map<JsonSchemaKeywordType, SimpleKeyword<URI>> uris = new HashMap<>();
    private final Map<JsonSchemaKeywordType, SimpleKeyword<JsonValue>> jsonValues = new HashMap<>();
    private final Map<JsonSchemaKeywordType, SimpleKeyword<Number>> numbers = new HashMap<>();
    private final Map<JsonSchemaKeywordType, SchemaMapKeywordBuilder> schemaMapKeywords = new HashMap<>();
    private final Map<JsonSchemaKeywordType, SchemaListKeywordBuilder> schemaListKeywords = new HashMap<>();
    private final Map<JsonSchemaKeywordType, SimpleKeyword<JsonSchemaBuilder>> schemas = new HashMap<>();

    private PropertyDependencyKeyword propertyDependencyKeyword;
    private MinimumKeyword minimumKeyword;
    private MaximumKeyword maximumKeyword;
    private TypeKeyword typeKeyword;

    private JsonObject currentDocument;
    private JsonProvider provider;
    private SchemaFactory schemaFactory;
    private final SchemaLocation location;

    private JsonSchemaBuilder() {
        this.location = null;
    }

    private JsonSchemaBuilder(URI $id) {
        checkNotNull($id, "$id must not be null");
        this.handleURI($ID, $id);
        this.location = null;
    }

    private JsonSchemaBuilder(SchemaLocation location, URI $id) {
        checkNotNull(location, "location must not be null");
        checkNotNull($id, "$id must not be null");
        this.handleURI($ID, $id);
        this.location = location;
    }

    private JsonSchemaBuilder(SchemaLocation location) {
        checkNotNull(location, "location must not be null");
        this.location = location;
    }

    public JsonSchemaBuilder currentDocument(JsonObject currentDocument) {
        this.currentDocument = currentDocument;
        return this;
    }

    public JsonSchemaBuilder ref(URI ref, @Nullable SchemaFactory schemaFactory) {
        this.handleURI($REF, ref);
        this.schemaFactory = schemaFactory;
        return this;
    }

    public JsonSchemaBuilder ref(String ref) {
        checkNotNull(ref, "ref must not be null");
        return this.handleURI($REF, URI.create(ref));
    }

    public JsonSchemaBuilder provider(JsonProvider provider) {
        this.provider = provider;
        return this;
    }

    public JsonSchemaBuilder schemaFactory(SchemaFactory schemaFactory) {
        this.schemaFactory = schemaFactory;
        return this;
    }

    private void raiseIssue(SchemaLocation location, JsonSchemaKeywordType keyword, String messageCode) {
        throw new SchemaException(location.getJsonPointerFragment(), messageCode);
    }

    public Schema build() {
        final URI $id = Optional.ofNullable(uris.get($ID))
                .map(SimpleKeyword::getKeywordValue)
                .orElse(null);

        final SchemaLocation location;
        if ($id == null) {
            location = supplyIfNull(this.location, () -> SchemaLocation.hashedRoot(this));
        } else if (this.location != null) {
            location = this.location.withId($id);
        } else {
            if ($id.isAbsolute()) {
                location = SchemaLocation.documentRoot($id);
            } else {
                location = SchemaLocation.hashedRoot(this, $id);
            }
        }
        return build(location);
    }

    private Schema build(SchemaLocation location) {
        checkNotNull(location, "location must not be null");

        final URI $id = Optional.ofNullable(uris.get($ID))
                .map(SimpleKeyword::getKeywordValue)
                .orElse(null);

        final URI $ref = Optional.ofNullable(uris.get($REF))
                .map(SimpleKeyword::getKeywordValue)
                .orElse(null);

        // Use the location provided during building as an override
        location = MoreObjects.firstNonNull(this.location, location);
        if ($id != null) {
            location = location.withId($id);
        }

        final URI thisSchemaURI = location.getUniqueURI();

        if (schemaFactory != null) {
            final Optional<Schema> cachedSchema = schemaFactory.findCachedSchema(thisSchemaURI);
            if (cachedSchema.isPresent()) {
                return cachedSchema.get();
            }
        }

        if ($ref != null) {
            return ReferenceSchema.refSchemaBuilder($ref)
                    .factory(schemaFactory)
                    .currentDocument(currentDocument)
                    .location(location)
                    .build();
        }

        final Map<JsonSchemaKeywordType, SchemaKeyword> keywords;

        ImmutableMap.Builder<JsonSchemaKeywordType, SchemaKeyword> collector = ImmutableMap.builder();
        collector.putAll(jsonValues);
        collector.putAll(strings);
        collector.putAll(numbers);
        collector.putAll(uris);

        if (typeKeyword != null) {
            collector.put(TYPE, typeKeyword);
        }

        if (propertyDependencyKeyword != null) {
            collector.put(DEPENDENCIES, propertyDependencyKeyword);
        }

        if (minimumKeyword != null) {
            collector.put(MINIMUM, minimumKeyword);
        }

        if (maximumKeyword != null) {
            collector.put(MAXIMUM, maximumKeyword);
        }

        // Build and load all schema keywords
        for (Map.Entry<JsonSchemaKeywordType, SimpleKeyword<JsonSchemaBuilder>> entry : schemas.entrySet()) {
            final JsonSchemaKeywordType keyword = entry.getKey();
            final JsonSchemaBuilder schemaBuilder = entry.getValue().getKeywordValue();
            final SchemaLocation schemaLocation = location.child(keyword);

            final Schema builtSchema = findCachedOrBuild(schemaLocation, schemaBuilder);
            collector.put(keyword, new SimpleKeyword<>(builtSchema, keyword));
        }

        for (Map.Entry<JsonSchemaKeywordType, SchemaListKeywordBuilder> entry : schemaListKeywords.entrySet()) {
            final JsonSchemaKeywordType keyword = entry.getKey();
            final List<JsonSchemaBuilder> schemaBuilder = entry.getValue().getSchemas();
            List<Schema> listOfSchema = new ArrayList<>();
            int i = 0;
            for (JsonSchemaBuilder builder : schemaBuilder) {
                final SchemaLocation idxLocation = location.child(keyword.key(), i++);
                listOfSchema.add(findCachedOrBuild(idxLocation, builder));
            }

            collector.put(keyword, new SchemaListKeyword(keyword, listOfSchema));
        }

        for (Map.Entry<JsonSchemaKeywordType, SchemaMapKeywordBuilder> entry : schemaMapKeywords.entrySet()) {
            final JsonSchemaKeywordType keyword = entry.getKey();
            final Map<String, JsonSchemaBuilder> schemaBuilder = entry.getValue().getSchemas();
            Map<String, Schema> schemaMap = new HashMap<>();
            int i = 0;
            for (Map.Entry<String, JsonSchemaBuilder> schemaEntry : schemaBuilder.entrySet()) {
                final String schemaKey = keyword.key();
                final SchemaLocation keyLocation = location.child(schemaKey, schemaEntry.getKey());
                schemaMap.put(schemaKey, findCachedOrBuild(keyLocation, schemaEntry.getValue()));
            }
        }


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
        return handleString(TITLE, title);
    }

    private <X> JsonSchemaBuilder handleString(JsonSchemaKeywordType keyword, String value) {
        if (value == null) {
            strings.remove(keyword);
            return this;
        }
        final SimpleKeyword<String> keywordValue = new SimpleKeyword<>(value, keyword);
        strings.put(keyword, keywordValue);
        return this;
    }

    private <X> JsonSchemaBuilder handleURI(JsonSchemaKeywordType keyword, URI value) {
        if (value == null) {
            uris.remove(keyword);
            return this;
        }
        final SimpleKeyword<URI> keywordValue = new SimpleKeyword<>(value, keyword);
        uris.put(keyword, keywordValue);
        return this;
    }

    private JsonSchemaBuilder handleNumber(JsonSchemaKeywordType keyword, Number value) {
        if (value == null) {
            numbers.remove(keyword);
            return this;
        }
        final SimpleKeyword<Number> keywordValue = new SimpleKeyword<>(value, keyword);
        numbers.put(keyword, keywordValue);
        return this;
    }

    public JsonSchemaBuilder defaultValue(JsonValue defaultValue) {
        return handleString(DEFAULT, defaultValue);
    }

    public JsonSchemaBuilder description(String description) {
        return handleString(DESCRIPTION, description);
    }

    public JsonSchemaBuilder type(JsonSchemaType requiredType) {
        final TypeKeyword existingValue = (TypeKeyword) schemaMapKeywords.get(TYPE);
        if (existingValue == null) {
            schemaMapKeywords.put(TYPE, new TypeKeyword(requiredType));
        } else {
            schemaMapKeywords.put(TYPE, existingValue.withAdditionalType(requiredType));
        }
        return this;
    }

    public JsonSchemaBuilder orType(JsonSchemaType requiredType) {
        return type(requiredType);
    }

    public JsonSchemaBuilder types(Set<JsonSchemaType> requiredTypes) {
        if(requiredTypes != null) {
            schemaMapKeywords.put(TYPE, new TypeKeyword(requiredTypes));
        } else {
            schemaMapKeywords.remove(TYPE);
        }
        return this;
    }

    public JsonSchemaBuilder clearTypes() {
        schemaMapKeywords.remove(TYPE);
        return this;
    }

    private <T> JsonSchemaBuilder handleListKeyword(JsonSchemaKeywordType keyword, T value) {
        final SchemaListKeywordBuilder<T> existingValue = (SchemaListKeywordBuilder<T>) schemaMapKeywords.get(keyword);
        if (existingValue == null) {
            schemaMapKeywords.put(TYPE, new SchemaListKeywordBuilder<T>(value, keyword));
        } else {
            schemaMapKeywords.put(TYPE, existingValue.withAnotherSchema(value));
        }
        return this;
    }

    private <T> JsonSchemaBuilder handleListKeyword(JsonSchemaKeywordType keyword, Collection<T> value) {
        if (value == null) {
            schemaMapKeywords.remove(keyword);
        } else {
            schemaMapKeywords.put(keyword, new SchemaListKeywordBuilder<T>(value, keyword));
        }
        return this;
    }

    private <T> JsonSchemaBuilder handleSchema(JsonSchemaKeywordType keyword, JsonSchemaBuilder value) {
        if (value == null) {
            schemasMapKeywords.remove(keyword);
        } else {
            schemaMapKeywords.put(keyword, new SchemaListKeywordBuilder<T>(value, keyword));
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    private <T> JsonSchemaBuilder handleMapKeyword(JsonSchemaKeywordType keyword, String key, T value) {
        final SchemaMapKeywordBuilder<T> existingValue = (SchemaMapKeywordBuilder<T>) schemaMapKeywords.get(keyword);
        if (existingValue == null) {
            schemaMapKeywords.put(TYPE, new SchemaListKeywordBuilder<T>(value, keyword));
        } else {
            schemaMapKeywords.put(TYPE, existingValue.addSchema(key, value));
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    private <T> JsonSchemaBuilder handleMultimapKeyword(JsonSchemaKeywordType keyword, String key, T value) {
        final PropertyDependencyKeyword<T> existingValue = (PropertyDependencyKeyword<T>) schemaMapKeywords.get(keyword);
        if (existingValue == null) {
            schemaMapKeywords.put(TYPE, new SchemaListKeywordBuilder<T>(value, keyword));
        } else {
            schemaMapKeywords.put(TYPE, existingValue.propertyDependency(key, value));
        }
        return this;
    }

    private <T> JsonSchemaBuilder handleMapKeyword(JsonSchemaKeywordType keyword, Map<String, T> value) {
        if (value == null) {
            schemaMapKeywords.remove(keyword);
        } else {
            schemaMapKeywords.put(keyword, new SchemaMapKeywordBuilder<>(value, keyword));
        }
        return this;
    }

    // #################################
    // SHARED VALIDATION KEYWORD SETTERS
    // #################################

    public JsonSchemaBuilder allOfSchema(JsonSchemaBuilder allOfSchema) {
        return handleListKeyword(ALL_OF, allOfSchema);
    }

    public JsonSchemaBuilder allOfSchemas(Collection<? extends JsonSchemaBuilder> allOfSchemas) {
        return handleListKeyword(ALL_OF, allOfSchemas);
    }

    public JsonSchemaBuilder clearAllOfSchemas() {
        return handleListKeyword(ALL_OF, null);
    }

    public JsonSchemaBuilder anyOfSchema(JsonSchemaBuilder anyOfSchema) {
        return handleListKeyword(ANY_OF, anyOfSchema);
    }
    
    public JsonSchemaBuilder anyOfSchemas(Collection<? extends JsonSchemaBuilder> anyOfSchemas) {
        return handleListKeyword(ANY_OF, anyOfSchemas);
    }

    public JsonSchemaBuilder clearAnyOfSchemas() {
        return handleListKeyword(ANY_OF, null);
    } 
    
    public JsonSchemaBuilder oneOfSchema(JsonSchemaBuilder oneOfSchema) {
        return handleListKeyword(ONE_OF, oneOfSchema);
    }
    
    public JsonSchemaBuilder oneOfSchemas(Collection<? extends JsonSchemaBuilder> oneOfSchemas) {
        return handleListKeyword(ONE_OF, oneOfSchemas);
    }

    public JsonSchemaBuilder clearOneOfSchemas() {
        return handleListKeyword(ONE_OF, null);
    }

    public JsonSchemaBuilder constValue(JsonValue constValue) {
        return handleString(CONST, constValue);
    }

    public JsonSchemaBuilder constValueDouble(double constValue) {
        return this.constValue(provider().createValue(constValue));
    }

    public JsonSchemaBuilder constValueInteger(int constValue) {
        return this.constValue(provider().createValue(constValue));
    }

    public JsonSchemaBuilder constValueString(String constValue) {
        return this.constValue(provider().createValue(constValue));
    }

    public JsonSchemaBuilder enumValues(JsonArray enumValues) {
        return this.handleString(ENUM, enumValues);
    }

    public JsonSchemaBuilder notSchema(JsonSchemaBuilder notSchema) {
        return this.handleString(NOT, notSchema);
    }

    // #######################################################
    // ARRAY KEYWORDS
    // @see ArrayKeywords
    // #######################################################

    public JsonSchemaBuilder allItemSchema(@Valid JsonSchemaBuilder allItemSchema) {
        return this.handleString(ITEMS, allItemSchema);
    }

    public JsonSchemaBuilder containsSchema(@Valid JsonSchemaBuilder containsSchema) {
        return this.handleString(CONTAINS, containsSchema);
    }

    public JsonSchemaBuilder itemSchema(JsonSchemaBuilder itemSchema) {
        return this.handleListKeyword(ITEMS, itemSchema);
    }

    public JsonSchemaBuilder itemSchemas(List<JsonSchemaBuilder> itemSchemas) {
        return this.handleListKeyword(ITEMS, itemSchemas);
    }

    public JsonSchemaBuilder minItems(@Min(0) Integer minItems) {
        return this.handleString(MIN_ITEMS, minItems);
    }

    public JsonSchemaBuilder maxItems(@Min(0) Integer maxItems) {
        return this.handleString(MAX_ITEMS, maxItems);
    }

    public JsonSchemaBuilder needsUniqueItems(boolean needsUniqueItems) {
        if (needsUniqueItems) {
            return this.handleString(UNIQUE_ITEMS, true);
        } else {
            schemaMapKeywords.remove(UNIQUE_ITEMS);
            return this;
        }
    }

    public JsonSchemaBuilder schemaOfAdditionalItems(@Valid JsonSchemaBuilder schemaOfAdditionalItems) {
        this.handleString(ADDITIONAL_ITEMS, schemaOfAdditionalItems);
        return this;
    }

    // #######################################################
    // NUMBER KEYWORDS
    // @see NumberKeywords
    // #######################################################

    public JsonSchemaBuilder exclusiveMaximum(Number exclusiveMaximum) {
        if (exclusiveMaximum == null) {
            schemaMapKeywords.remove(MAXIMUM);
            return this;
        }
        final MaximumKeyword existing = (MaximumKeyword) schemaMapKeywords.get(MAXIMUM);
        final MaximumKeyword update;
        if (existing != null) {
            update = existing.toBuilder().exclusiveMaximum(exclusiveMaximum).build();
        } else {
            update = MaximumKeyword.builder().exclusiveMaximum(exclusiveMaximum).build();
        }
        schemaMapKeywords.put(MAXIMUM, update);
        return this;
    }

    public JsonSchemaBuilder exclusiveMinimum(Number exclusiveMinimum) {
        if (exclusiveMinimum == null) {
            schemaMapKeywords.remove(MINIMUM);
            return this;
        }
        final MinimumKeyword existing = (MinimumKeyword) schemaMapKeywords.get(MINIMUM);
        final MinimumKeyword update;
        if (existing != null) {
            update = existing.toBuilder().exclusiveMinimum(exclusiveMinimum).build();
        } else {
            update = MinimumKeyword.builder().exclusiveMinimum(exclusiveMinimum).build();
        }
        schemaMapKeywords.put(MINIMUM, update);
        return this;
    }

    public JsonSchemaBuilder minimum(Number minimum) {
        if (minimum == null) {
            schemaMapKeywords.remove(MINIMUM);
            return this;
        }
        final MinimumKeyword existing = (MinimumKeyword) schemaMapKeywords.get(MINIMUM);
        final MinimumKeyword update;
        if (existing != null) {
            update = existing.toBuilder().minimum(minimum).build();
        } else {
            update = MinimumKeyword.builder().minimum(minimum).build();
        }
        schemaMapKeywords.put(MINIMUM, update);
        return this;
    }

    public JsonSchemaBuilder maximum(Number maximum) {
        if (maximum == null) {
            schemaMapKeywords.remove(MAXIMUM);
            return this;
        }
        final MaximumKeyword existing = (MaximumKeyword) schemaMapKeywords.get(MAXIMUM);
        final MaximumKeyword update;
        if (existing != null) {
            update = existing.toBuilder().maximum(maximum).build();
        } else {
            update = MaximumKeyword.builder().maximum(maximum).build();
        }
        schemaMapKeywords.put(MAXIMUM, update);
        return this;
    }


    public JsonSchemaBuilder multipleOf(@Min(1) Number multipleOf) {
        this.handleNumber(MULTIPLE_OF, multipleOf);
        return this;
    }

    // #######################################################
    // OBJECT KEYWORDS
    // @see ObjectKeywords
    // #######################################################

    public JsonSchemaBuilder clearPropertySchemas() {
        schemaMapKeywords.remove(PROPERTIES);
        return this;
    }

    public JsonSchemaBuilder clearRequiredProperties() {
        schemaMapKeywords.remove(REQUIRED);
        return this;
    }

    public JsonSchemaBuilder maxProperties(@Min(0) Integer maxProperties) {
        this.handleNumber(MAX_PROPERTIES, maxProperties);
        return this;
    }

    public JsonSchemaBuilder minProperties(@Min(0) Integer minProperties) {
        this.handleNumber(MIN_PROPERTIES, minProperties);
        return this;
    }

    public JsonSchemaBuilder patternProperty(Pattern pattern, JsonSchemaBuilder schema) {
        checkNotNull(pattern, "pattern must not be null");
        this.handleMapKeyword(PATTERN_PROPERTIES, pattern.pattern(), schema);
        return this;
    }

    public JsonSchemaBuilder patternProperty(String pattern, JsonSchemaBuilder schema) {
        this.handleMapKeyword(PATTERN_PROPERTIES, pattern, schema);
        return this;
    }

    public JsonSchemaBuilder propertyDependency(String ifPresent, String thenRequireThisProperty) {
        checkNotNull(ifPresent, "ifPresent must not be null");
        this.handleMapKeyword(DEPENDENCIES, ifPresent, thenRequireThisProperty);
        return this;
    }

    public JsonSchemaBuilder propertyNameSchema(JsonSchemaBuilder propertyNameSchema) {
        this.handleSchema(PROPERTY_NAMES, propertyNameSchema);
        return this;
    }

    public JsonSchemaBuilder propertySchema(String propertySchemaKey, JsonSchemaBuilder propertySchemaValue) {
        checkNotNull(propertySchemaKey, "propertySchemaKey must not be null");
        checkNotNull(propertySchemaValue, "propertySchemaValue must not be null");
        this.handleMapKeyword(PROPERTIES, propertySchemaKey, propertySchemaValue);
        return this;
    }

    public JsonSchemaBuilder requiredProperty(String requiredProperty) {
        checkNotNull(requiredProperty, "requiredProperty must not be null");
        this.handleListKeyword(REQUIRED, requiredProperty);
        return this;
    }

    public JsonSchemaBuilder schemaDependency(String property, JsonSchemaBuilder dependency) {
        checkNotNull(property, "property must not be null");
        checkNotNull(dependency, "dependency must not be null");
        this.handleMapKeyword(DEPENDENCIES, property, dependency);
        return this;
    }

    public JsonSchemaBuilder schemaOfAdditionalProperties(JsonSchemaBuilder schemaOfAdditionalProperties) {
        checkNotNull(schemaOfAdditionalProperties, "schemaOfAdditionalProperties must not be null");
        this.handleString(ADDITIONAL_PROPERTIES, schemaOfAdditionalProperties);
        return this;
    }

    // #######################################################
    // STRING KEYWORDS
    // @see StringKeywords
    // #######################################################

    public JsonSchemaBuilder format(String format) {
        this.handleString(FORMAT, format);
        return this;
    }

    public JsonSchemaBuilder maxLength(@Min(0) Integer maxLength) {
        this.handleString(MAX_LENGTH, maxLength);
        return this;
    }

    public JsonSchemaBuilder minLength(@Min(0) Integer minLength) {
        this.handleString(MIN_LENGTH, minLength);
        return this;
    }

    public JsonSchemaBuilder pattern(Pattern pattern) {
        this.handleString(PATTERN, pattern.pattern());
        return this;
    }

    public JsonSchemaBuilder pattern(String pattern) {
        return pattern(Pattern.compile(pattern));
    }

    // #######################################################
    // BUILDING SUBSCHEMAS
    // #######################################################

    private Optional<Schema> buildKeywordSubschema(SchemaLocation parentSchema, JsonSchemaKeywordType keyword, JsonSchemaBuilder subschemaBuilder) {
        if (subschemaBuilder == null) {
            return Optional.empty();
        }
        final SchemaLocation subschemaLocation = parentSchema.child(keyword);
        return Optional.of(
                findCachedOrBuild(subschemaLocation, subschemaBuilder)
        );
    }

    private <X> Optional<Map<X, Schema>> buildKeywordSubschemaMap(SchemaLocation parentSchema, JsonSchemaKeywordType keyword, Map<X, JsonSchemaBuilder> builders) {
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

    private Optional<List<Schema>> buildKeywordSubschemaList(SchemaLocation parentSchema, JsonSchemaKeywordType keyword, Collection<JsonSchemaBuilder> builders) {
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

    // #######################################################
    // LAZY GETTERS
    // #######################################################

    private JsonProvider provider() {
        return MoreObjects.firstNonNull(provider, JsonProvider.provider());
    }


}