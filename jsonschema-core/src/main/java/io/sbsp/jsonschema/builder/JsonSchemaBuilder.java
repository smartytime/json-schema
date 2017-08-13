package io.sbsp.jsonschema.builder;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import io.sbsp.jsonschema.RefSchema;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.SchemaException;
import io.sbsp.jsonschema.SchemaFactory;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.enums.JsonSchemaKeywordType;
import io.sbsp.jsonschema.enums.JsonSchemaType;
import io.sbsp.jsonschema.impl.Draft6SchemaImpl;
import io.sbsp.jsonschema.impl.RefSchemaImpl;
import io.sbsp.jsonschema.keyword.BooleanKeyword;
import io.sbsp.jsonschema.keyword.JsonArrayKeyword;
import io.sbsp.jsonschema.keyword.JsonValueKeyword;
import io.sbsp.jsonschema.keyword.KeywordMetadata;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.LimitKeyword;
import io.sbsp.jsonschema.keyword.NumberKeyword;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.keyword.SchemaKeywordImpl;
import io.sbsp.jsonschema.keyword.SchemaListKeyword;
import io.sbsp.jsonschema.keyword.SchemaMapKeyword;
import io.sbsp.jsonschema.keyword.SingleSchemaKeyword;
import io.sbsp.jsonschema.keyword.StringKeyword;
import io.sbsp.jsonschema.keyword.StringSetKeyword;
import io.sbsp.jsonschema.keyword.TypeKeyword;
import io.sbsp.jsonschema.keyword.URIKeyword;
import io.sbsp.jsonschema.loading.LoadingReport;
import io.sbsp.jsonschema.loading.SchemaLoadingException;
import lombok.EqualsAndHashCode;

import javax.annotation.Nullable;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.sbsp.jsonschema.utils.StreamUtils.supplyIfNull;

@EqualsAndHashCode(of = {"keywords", "keywordBuilders"})
public class JsonSchemaBuilder implements SchemaBuilder {

    public static JsonSchemaBuilder jsonSchema() {
        return new JsonSchemaBuilder();
    }

    public static JsonSchemaBuilder jsonSchema(SchemaLocation location) {
        return new JsonSchemaBuilder(location);
    }

    public static JsonSchemaBuilder jsonSchemaBuilderWithId(SchemaLocation location, String id) {
        return new JsonSchemaBuilder(location, URI.create(id));
    }

    public static JsonSchemaBuilder refSchemaBuilder(URI ref, SchemaLocation location, @Nullable SchemaFactory schemaFactory) {
        return jsonSchema(location).ref(ref, schemaFactory);
    }

    public static JsonSchemaBuilder jsonSchemaBuilderWithId(String id) {
        checkNotNull(id, "id must not be null");
        return new JsonSchemaBuilder(URI.create(id));
    }

    public static JsonSchemaBuilder jsonSchemaBuilderWithId(URI id) {
        checkNotNull(id, "id must not be null");
        return new JsonSchemaBuilder(id);
    }

    private final Map<KeywordMetadata<?>, SchemaKeyword> keywords = new HashMap<>();
    private final Map<KeywordMetadata<?>, SchemaKeywordBuilder> keywordBuilders = new HashMap<>();

    private JsonObject currentDocument;
    private JsonProvider provider;
    private SchemaFactory schemaFactory;
    private final SchemaLocation location;

    protected JsonSchemaBuilder() {
        this.location = null;
    }

    protected JsonSchemaBuilder(URI $id) {
        checkNotNull($id, "$id must not be null");
        this.addOrRemoveURI(Keywords.$id, $id);
        this.location = null;
    }

    protected JsonSchemaBuilder(SchemaLocation location, URI $id) {
        checkNotNull(location, "location must not be null");
        checkNotNull($id, "$id must not be null");
        this.addOrRemoveURI(Keywords.$id, $id);
        this.location = location;
    }

    protected JsonSchemaBuilder(SchemaLocation location) {
        checkNotNull(location, "location must not be null");
        this.location = location;
    }

    public JsonSchemaBuilder withCurrentDocument(JsonObject currentDocument) {
        this.currentDocument = currentDocument;
        return this;
    }

    public JsonSchemaBuilder ref(URI ref, @Nullable SchemaFactory schemaFactory) {
        this.addOrRemoveURI(Keywords.$ref, ref);
        this.schemaFactory = schemaFactory;
        return this;
    }

    public JsonSchemaBuilder ref(String ref) {
        checkNotNull(ref, "ref must not be null");
        return this.addOrRemoveURI(Keywords.$ref, URI.create(ref));
    }

    public JsonSchemaBuilder provider(JsonProvider provider) {
        this.provider = provider;
        return this;
    }

    public JsonSchemaBuilder withSchemaFactory(SchemaFactory schemaFactory) {
        this.schemaFactory = schemaFactory;
        return this;
    }

    private void raiseIssue(SchemaLocation location, JsonSchemaKeywordType keyword, String messageCode) {
        throw new SchemaException(location.getJsonPointerFragment(), messageCode);
    }

    public Schema build() {
        final URI $id = $id();

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
        final LoadingReport report = new LoadingReport();
        final Schema built = build(location, report);
        if (report.hasErrors()) {
            throw new SchemaLoadingException(location.getJsonPointerFragment(), report, built);
        }
        return built;
    }

    public JsonObject withCurrentDocument() {
        return currentDocument;
    }

    @Nullable
    public URI $id() {
        final URIKeyword keyword = getKeyword(Keywords.$id);
        return keyword != null ? keyword.getKeywordValue() : null;
    }

    @Nullable
    public URI $ref() {
        final URIKeyword keyword = getKeyword(Keywords.$ref);
        return keyword != null ? keyword.getKeywordValue() : null;
    }

    public Schema build(SchemaLocation location, LoadingReport report) {
        checkNotNull(location, "location must not be null");
        checkNotNull(report, "repot must not be null");

        final URI $id = $id();
        final URI $ref = $ref();

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
            return RefSchemaImpl.refSchemaBuilder($ref)
                    .factory(schemaFactory)
                    .currentDocument(currentDocument)
                    .location(location)
                    .report(report)
                    .build();
        }

        ImmutableMap.Builder<KeywordMetadata<?>, SchemaKeyword> collector = ImmutableMap.builder();
        collector.putAll(this.keywords);

        for (Map.Entry<KeywordMetadata<?>, SchemaKeywordBuilder> entry : this.keywordBuilders.entrySet()) {
            final KeywordMetadata<?> keyword = entry.getKey();
            final SchemaKeywordBuilder<?> keywordBuilder = entry.getValue();

            //Pass builder context items to any child schemas.  Saves us the complication of passing things
            //down the chain.
            keywordBuilder.getAllSchemas().forEach(schema-> schema
                    .withSchemaFactory(schemaFactory)
                    .withCurrentDocument(currentDocument));

            final SchemaKeyword builtKeyword = keywordBuilder.build(location, keyword, report);
            if (builtKeyword != null) {
                collector.put(keyword, builtKeyword);
            }
        }

        return new Draft6SchemaImpl(location, collector.build());
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
        return addOrRemoveString(Keywords.title, title);
    }

    public JsonSchemaBuilder defaultValue(JsonValue defaultValue) {
        return addOrRemoveJsonValue(Keywords.$default, defaultValue);
    }

    public JsonSchemaBuilder description(String description) {
        return addOrRemoveString(Keywords.description, description);
    }

    @SuppressWarnings("unchecked")
    protected <X extends SchemaKeyword> X getKeyword(KeywordMetadata<X> keyword) {
        return (X) keywords.get(keyword);
    }

    @SuppressWarnings("unchecked")
    protected <X extends SchemaKeyword> X getKeyword(KeywordMetadata<X> keyword, Supplier<X> defaultValue) {
        return (X) keywords.computeIfAbsent(keyword, k -> defaultValue.get());
    }

    @SuppressWarnings("unchecked")
    protected <X extends SchemaKeyword, B extends SchemaKeywordBuilder<X>> B getKeywordBuilder(KeywordMetadata<X> keyword, Supplier<B> newInstanceSupplier) {
        return (B) keywordBuilders.computeIfAbsent(keyword, k -> newInstanceSupplier.get());
    }

    public JsonSchemaBuilder type(JsonSchemaType requiredType) {
        final TypeKeyword existingValue = getKeyword(Keywords.type);
        if (existingValue == null) {
            keywords.put(Keywords.type, new TypeKeyword(requiredType));
        } else {
            keywords.put(Keywords.type, existingValue.withAdditionalType(requiredType));
        }
        return this;
    }

    public <X extends SchemaKeyword> JsonSchemaBuilder keyword(KeywordMetadata<X> keyword, X value) {
        checkNotNull(keyword, "keyword must not be null");
        checkNotNull(value, "value must not be null");
        this.keywords.put(keyword, value);
        return this;
    }

    public JsonSchemaBuilder orType(JsonSchemaType requiredType) {
        return type(requiredType);
    }

    public JsonSchemaBuilder types(Set<JsonSchemaType> requiredTypes) {
        if(requiredTypes != null) {
            keywords.put(Keywords.type, new TypeKeyword(requiredTypes));
        } else {
            clearTypes();
        }
        return this;
    }

    public JsonSchemaBuilder clearTypes() {
        keywords.remove(Keywords.type);
        return this;
    }

    // #################################
    // SHARED VALIDATION KEYWORD SETTERS
    // #################################

    public JsonSchemaBuilder allOfSchema(SchemaBuilder allOfSchema) {
        return addSchemaToList(Keywords.allOf, allOfSchema);
    }

    public JsonSchemaBuilder allOfSchemas(Collection<SchemaBuilder> allOfSchemas) {
        return addOrRemoveSchemaList(Keywords.allOf, allOfSchemas);
    }

    public JsonSchemaBuilder clearAllOfSchemas() {
        return addOrRemoveSchemaList(Keywords.allOf, null);
    }

    public JsonSchemaBuilder anyOfSchema(SchemaBuilder anyOfSchema) {
        return addSchemaToList(Keywords.anyOf, anyOfSchema);
    }

    public JsonSchemaBuilder anyOfSchemas(Collection<SchemaBuilder> anyOfSchemas) {
        return addOrRemoveSchemaList(Keywords.anyOf, anyOfSchemas);
    }

    public JsonSchemaBuilder clearAnyOfSchemas() {
        return addOrRemoveSchemaList(Keywords.anyOf, null);
    }

    public JsonSchemaBuilder oneOfSchema(JsonSchemaBuilder oneOfSchema) {
        return addSchemaToList(Keywords.oneOf, oneOfSchema);
    }

    public JsonSchemaBuilder oneOfSchemas(Collection<SchemaBuilder> oneOfSchemas) {
        return addOrRemoveSchemaList(Keywords.oneOf, oneOfSchemas);
    }

    public JsonSchemaBuilder clearOneOfSchemas() {
        return addOrRemoveSchemaList(Keywords.oneOf, null);
    }

    public JsonSchemaBuilder constValue(JsonValue constValue) {
        return addOrRemoveJsonValue(Keywords.$const, constValue);
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
        return this.addOrRemoveJsonArray(Keywords.$enum, enumValues);
    }

    public JsonSchemaBuilder notSchema(JsonSchemaBuilder notSchema) {
        return this.addOrRemoveSchema(Keywords.not, notSchema);
    }

    // #######################################################
    // ARRAY KEYWORDS
    // @see ArrayKeywords
    // #######################################################

    /**
     * Method that updates a SchemaKeywordBuilder by providing an update function.  This function handles updating or
     * pruning values in {@link #keywordBuilders} map.
     *
     * @param keyword       The keyword to be updated
     * @param newInstanceFn A function that produces a new instance of the builder
     * @param updateFn      A function that takes in the current builder, updates it, and returns the new value.
     * @param <K>           Ugh - this helps a ton with making sure we don't write wrong keys into the Map.  This is the SchemaKeyword produced by the builder in question.
     * @param <B>           This is the type of the builder we are working with.
     * @return Self-reference for chaining
     */
    protected <K extends SchemaKeyword, B extends SchemaKeywordBuilder<K>> JsonSchemaBuilder updateKeywordBuilder(KeywordMetadata<K> keyword, Supplier<B> newInstanceFn, UnaryOperator<B> updateFn) {
        final B keywordBuilder = getKeywordBuilder(keyword, newInstanceFn);
        final B updatedKeyword = updateFn.apply(keywordBuilder);
        if (updatedKeyword == null) {
            keywordBuilders.remove(keyword);
        } else {
            keywordBuilders.put(keyword, updatedKeyword);
        }
        return this;
    }

    /**
     * Updates a keyword by providing an update function.  This method takes care of updating or pruning the appropriate keys in the
     * {@link #keywords} map.
     *
     * @param keyword The keyword being updated
     * @param newInstanceFn A supplier that produces a new blank instance of the keyword value
     * @param updateFn A function that takes in the current keyword value, and returns an updated value.
     * @param <K> Type parameter for the keyword in question.  Enforces that the key matches the value
     * @return Self-reference for chaining.
     */
    protected <K extends SchemaKeyword> JsonSchemaBuilder updateKeyword(KeywordMetadata<K> keyword, Supplier<K> newInstanceFn, UnaryOperator<K> updateFn) {
        final K keywordValue = getKeyword(keyword, newInstanceFn);
        final K updatedKeyword = updateFn.apply(keywordValue);
        if (updatedKeyword == null) {
            keywords.remove(keyword);
        } else {
            keywords.put(keyword, updatedKeyword);
        }
        return this;
    }

    public JsonSchemaBuilder allItemSchema(@Valid SchemaBuilder allItemSchema) {
        checkNotNull(allItemSchema, "allItemSchema must not be null");
        return updateKeywordBuilder(Keywords.items,
                ItemsKeywordBuilder::newInstance,
                itemsKeyword -> itemsKeyword.toBuilder()
                        .allItemSchema(allItemSchema)
                        .build());
    }

    public JsonSchemaBuilder itemSchema(SchemaBuilder itemSchema) {
        checkNotNull(itemSchema, "itemSchema must not be null");
        return updateKeywordBuilder(Keywords.items,
                ItemsKeywordBuilder::newInstance,
                itemsKeyword -> itemsKeyword.toBuilder()
                        .indexSchema(itemSchema)
                        .build());
    }

    public JsonSchemaBuilder itemSchemas(List<SchemaBuilder> itemSchemas) {
        checkNotNull(itemSchemas, "itemSchemas must not be null");
        return updateKeywordBuilder(Keywords.items,
                ItemsKeywordBuilder::newInstance,
                itemsKeyword -> itemsKeyword.toBuilder()
                        .indexSchemas(itemSchemas)
                        .build());
    }

    public JsonSchemaBuilder schemaOfAdditionalItems(@Valid SchemaBuilder schemaOfAdditionalItems) {
        return updateKeywordBuilder(Keywords.items,
                ItemsKeywordBuilder::newInstance,
                itemsKeyword -> itemsKeyword.toBuilder()
                        .additionalItemSchema(schemaOfAdditionalItems)
                        .build());
    }

    public JsonSchemaBuilder containsSchema(@Valid SchemaBuilder containsSchema) {
        return this.addOrRemoveSchema(Keywords.contains, containsSchema);
    }

    public JsonSchemaBuilder minItems(@Min(0) Integer minItems) {
        return this.addOrRemoveNumber(Keywords.minItems, minItems);
    }

    public JsonSchemaBuilder maxItems(@Min(0) Integer maxItems) {
        return this.addOrRemoveNumber(Keywords.maxItems, maxItems);
    }

    public JsonSchemaBuilder needsUniqueItems(boolean needsUniqueItems) {
        if (needsUniqueItems) {
            return this.addOrRemoveBoolean(Keywords.uniqueItems, true);
        } else {
            keywords.remove(Keywords.uniqueItems);
            return this;
        }
    }

    // #######################################################
    // NUMBER KEYWORDS
    // @see NumberKeywords
    // #######################################################

    public JsonSchemaBuilder maximum(Number maximum) {
        return numberLimit(Keywords.maximum, LimitKeyword::maximumKeyword, maximum);
    }

    public JsonSchemaBuilder exclusiveMaximum(Number exclusiveMaximum) {
        return numberExclusiveLimit(Keywords.maximum, LimitKeyword::maximumKeyword, exclusiveMaximum);
    }

    public JsonSchemaBuilder minimum(Number minimum) {
        return numberLimit(Keywords.minimum, LimitKeyword::minimumKeyword, minimum);
    }

    public JsonSchemaBuilder exclusiveMinimum(Number exclusiveMinimum) {
        return numberExclusiveLimit(Keywords.minimum, LimitKeyword::minimumKeyword, exclusiveMinimum);
    }

    public JsonSchemaBuilder multipleOf(@Min(1) Number multipleOf) {
        this.addOrRemoveNumber(Keywords.multipleOf, multipleOf);
        return this;
    }

    // #######################################################
    // OBJECT KEYWORDS
    // @see ObjectKeywords
    // #######################################################

    public JsonSchemaBuilder clearPropertySchemas() {
        keywordBuilders.remove(Keywords.properties);
        return this;
    }

    public JsonSchemaBuilder clearRequiredProperties() {
        keywordBuilders.remove(Keywords.required);
        return this;
    }

    public JsonSchemaBuilder maxProperties(@Min(0) Integer maxProperties) {
        this.addOrRemoveNumber(Keywords.maxProperties, maxProperties);
        return this;
    }

    public JsonSchemaBuilder minProperties(@Min(0) Integer minProperties) {
        this.addOrRemoveNumber(Keywords.minProperties, minProperties);
        return this;
    }

    public JsonSchemaBuilder patternProperty(Pattern pattern, JsonSchemaBuilder schema) {
        checkNotNull(pattern, "pattern must not be null");
        this.putKeywordSchema(Keywords.patternProperties, pattern.pattern(), schema);
        return this;
    }

    public JsonSchemaBuilder patternProperty(String pattern, JsonSchemaBuilder schema) {
        this.putKeywordSchema(Keywords.patternProperties, pattern, schema);
        return this;
    }

    public JsonSchemaBuilder propertyNameSchema(JsonSchemaBuilder propertyNameSchema) {
        this.addOrRemoveSchema(Keywords.propertyNames, propertyNameSchema);
        return this;
    }

    public JsonSchemaBuilder propertySchema(String propertySchemaKey, JsonSchemaBuilder propertySchemaValue) {
        checkNotNull(propertySchemaKey, "propertySchemaKey must not be null");
        checkNotNull(propertySchemaValue, "propertySchemaValue must not be null");
        this.putKeywordSchema(Keywords.properties, propertySchemaKey, propertySchemaValue);
        return this;
    }

    public JsonSchemaBuilder requiredProperty(String requiredProperty) {
        checkNotNull(requiredProperty, "requiredProperty must not be null");
        return updateKeyword(Keywords.required,
                StringSetKeyword::newInstance,
                stringSetKeyword -> stringSetKeyword.withAnotherValue(requiredProperty));
    }

    public JsonSchemaBuilder propertyDependency(String ifPresent, String thenRequireThisProperty) {
        return updateKeywordBuilder(Keywords.dependencies,
                DependenciesKeywordBuilder::new,
                dependenciesKeyword -> dependenciesKeyword.propertyDependency(ifPresent, thenRequireThisProperty));
    }

    public JsonSchemaBuilder schemaDependency(String property, SchemaBuilder dependency) {
        return updateKeywordBuilder(Keywords.dependencies,
                DependenciesKeywordBuilder::new,
                dependenciesKeyword -> dependenciesKeyword.addDependencySchema(property, dependency));
    }

    public JsonSchemaBuilder schemaOfAdditionalProperties(SchemaBuilder schemaOfAdditionalProperties) {
        checkNotNull(schemaOfAdditionalProperties, "schemaOfAdditionalProperties must not be null");
        this.addOrRemoveSchema(Keywords.additionalProperties, schemaOfAdditionalProperties);
        return this;
    }

    // #######################################################
    // STRING KEYWORDS
    // @see StringKeywords
    // #######################################################

    public JsonSchemaBuilder format(String format) {
        this.addOrRemoveString(Keywords.format, format);
        return this;
    }

    public JsonSchemaBuilder maxLength(@Min(0) Integer maxLength) {
        this.addOrRemoveNumber(Keywords.maxLength, maxLength);
        return this;
    }

    public JsonSchemaBuilder minLength(@Min(0) Integer minLength) {
        this.addOrRemoveNumber(Keywords.minLength, minLength);
        return this;
    }

    public JsonSchemaBuilder pattern(Pattern pattern) {
        checkNotNull(pattern, "pattern must not be null");
        this.addOrRemoveString(Keywords.pattern, pattern.pattern());
        return this;
    }

    public JsonSchemaBuilder pattern(String pattern) {
        return pattern(Pattern.compile(pattern));
    }

    // #######################################################
    // HELPER FUNCTIONS
    // #######################################################

    public JsonSchemaBuilder numberLimit(KeywordMetadata<LimitKeyword> keyword, Supplier<LimitKeyword> newInstance, Number limit) {
        checkNotNull(keyword, "keyword must not be null");
        checkNotNull(newInstance, "newInstance must not be null");
        return updateKeyword(keyword, newInstance,
                limitKeyword -> {
                    if (limit == null && !limitKeyword.isExclusive()) {
                        return null;
                    }
                    return limitKeyword.withLimit(limit);
                });
    }

    public JsonSchemaBuilder numberExclusiveLimit(KeywordMetadata<LimitKeyword> keyword, Supplier<LimitKeyword> newInstance, Number exclusiveLimit) {
        checkNotNull(keyword, "keyword must not be null");
        checkNotNull(newInstance, "newInstance must not be null");
        return updateKeyword(keyword, newInstance,
                limitKeyword -> {
                    if (exclusiveLimit == null && limitKeyword.getLimit() == null) {
                        return null;
                    }
                    return limitKeyword.withExclusiveLimit(exclusiveLimit);
                });
    }

    protected JsonSchemaBuilder addOrRemoveString(KeywordMetadata<StringKeyword> keyword, String value) {
        if (!removeIfNecessary(keyword, value)) {
            final SchemaKeywordImpl<String> keywordValue = new StringKeyword(value);
            keywords.put(keyword, keywordValue);
        }
        return this;
    }

    protected <X> boolean removeIfNecessary(KeywordMetadata<?> keyword, X value) {
        checkNotNull(keyword, "keyword must not be null");
        if (value == null) {
            keywords.remove(keyword);
        }
        return value == null;
    }

    protected JsonSchemaBuilder addOrRemoveURI(KeywordMetadata<URIKeyword> keyword, URI value) {
        if (!removeIfNecessary(keyword, value)) {
            final URIKeyword keywordValue = new URIKeyword(value);
            keywords.put(keyword, keywordValue);
        }
        return this;
    }

    protected JsonSchemaBuilder addOrRemoveJsonArray(KeywordMetadata<JsonArrayKeyword> keyword, JsonArray value) {
        if (!removeIfNecessary(keyword, value)) {
            final JsonArrayKeyword keywordValue = new JsonArrayKeyword(value);
            keywords.put(keyword, keywordValue);
        }
        return this;
    }

    protected JsonSchemaBuilder addOrRemoveNumber(KeywordMetadata<NumberKeyword> keyword, Number value) {
        if (!removeIfNecessary(keyword, value)) {
            final NumberKeyword keywordValue = new NumberKeyword(value);
            keywords.put(keyword, keywordValue);
        }
        return this;
    }

    protected JsonSchemaBuilder addOrRemoveBoolean(KeywordMetadata<BooleanKeyword> keyword, Boolean bool) {
        if (!removeIfNecessary(keyword, bool)) {
            final BooleanKeyword keywordValue = new BooleanKeyword(bool);
            keywords.put(keyword, keywordValue);
        }
        return this;
    }

    protected JsonSchemaBuilder addOrRemoveJsonValue(KeywordMetadata<JsonValueKeyword> keyword, JsonValue value) {
        if (!removeIfNecessary(keyword, value)) {
            final JsonValueKeyword keywordValue = new JsonValueKeyword(value);
            keywords.put(keyword, keywordValue);
        }
        return this;
    }

    protected JsonSchemaBuilder addSchemaToList(KeywordMetadata<SchemaListKeyword> keyword, final SchemaBuilder schema) {
        return updateKeywordBuilder(keyword, SchemaListKeywordBuilder::new,
                listKeyword -> listKeyword.withSchema(schema));
    }

    public JsonSchemaBuilder addOrRemoveSchemaList(KeywordMetadata<SchemaListKeyword> keyword, final Collection<SchemaBuilder> schemas) {
        return updateKeywordBuilder(keyword, SchemaListKeywordBuilder::new,
                listKeyword -> {
                    if (schemas == null) {
                        return null;
                    }
                    return listKeyword.withSchemas(schemas);
                });
    }

    public JsonSchemaBuilder addOrRemoveSchema(KeywordMetadata<SingleSchemaKeyword> keyword, final SchemaBuilder value) {
        if (value == null) {
            keywordBuilders.remove(keyword);
        } else {
            keywordBuilders.put(keyword, new SingleSchemaKeywordBuilder(value));
        }

        return this;
    }

    @SuppressWarnings("unchecked")
    protected JsonSchemaBuilder putKeywordSchema(KeywordMetadata<SchemaMapKeyword> keyword, String key, JsonSchemaBuilder value) {
        return updateKeywordBuilder(keyword, SchemaMapKeywordBuilder::new, builder -> builder.addSchema(key, value));
    }

    public JsonSchemaBuilder putAllKeywordSchemas(KeywordMetadata<SchemaMapKeyword> keyword, Map<String, SchemaBuilder> schemas) {
        if (schemas == null) {
            keywordBuilders.remove(keyword);
            return this;
        } else {
            return updateKeywordBuilder(keyword, SchemaMapKeywordBuilder::new, builder -> builder.addAllSchemas(schemas));
        }
    }

    // #######################################################
    // LAZY GETTERS
    // #######################################################

    private JsonProvider provider() {
        return MoreObjects.firstNonNull(provider, JsonProvider.provider());
    }
}