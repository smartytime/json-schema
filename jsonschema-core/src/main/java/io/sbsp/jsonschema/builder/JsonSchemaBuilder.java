package io.sbsp.jsonschema.builder;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.enums.JsonSchemaType;
import io.sbsp.jsonschema.impl.Draft6SchemaImpl;
import io.sbsp.jsonschema.impl.RefSchemaImpl;
import io.sbsp.jsonschema.keyword.BooleanKeyword;
import io.sbsp.jsonschema.keyword.DependenciesKeyword;
import io.sbsp.jsonschema.keyword.ItemsKeyword;
import io.sbsp.jsonschema.keyword.JsonArrayKeyword;
import io.sbsp.jsonschema.keyword.JsonValueKeyword;
import io.sbsp.jsonschema.keyword.KeywordInfo;
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
import io.sbsp.jsonschema.loading.SchemaLoader;
import io.sbsp.jsonschema.loading.SchemaLoadingException;
import io.sbsp.jsonschema.utils.SchemaPaths;
import io.sbsp.jsonschema.utils.Schemas;
import lombok.EqualsAndHashCode;

import javax.annotation.Nullable;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static io.sbsp.jsonschema.utils.StreamUtils.supplyIfNull;

@EqualsAndHashCode(of = {"keywords"})
public class JsonSchemaBuilder implements SchemaBuilder<JsonSchemaBuilder> {

    private final Map<KeywordInfo<?>, SchemaKeyword> keywords = new HashMap<>();
    private final Map<String, JsonValue> extraProps = new LinkedHashMap<>();

    private JsonObject currentDocument;
    private JsonProvider provider;
    private SchemaLoader schemaFactory;
    private final SchemaLocation location;
    private LoadingReport loadingReport = new LoadingReport();

    public JsonSchemaBuilder() {
        this.location = SchemaPaths.fromNonSchemaSource(UUID.randomUUID());
    }

    public JsonSchemaBuilder(URI $id) {
        checkNotNull($id, "$id must not be null");
        this.addOrRemoveURI(Keywords.$ID, $id);
        this.location = SchemaPaths.from$IdNonAbsolute($id);
    }

    public JsonSchemaBuilder(SchemaLocation location, URI $id) {
        checkNotNull(location, "location must not be null");
        checkNotNull($id, "$id must not be null");
        this.addOrRemoveURI(Keywords.$ID, $id);
        this.location = location;
    }

    public JsonSchemaBuilder(SchemaLocation location) {
        checkNotNull(location, "location must not be null");
        this.location = location;
    }

    public JsonSchemaBuilder withCurrentDocument(JsonObject currentDocument) {
        this.currentDocument = currentDocument;
        return this;
    }

    @Override
    public JsonSchemaBuilder ref(URI ref) {
        checkNotNull(ref, "ref must not be null");
        return this.addOrRemoveURI(Keywords.$REF, ref);
    }

    @Override
    public JsonSchemaBuilder provider(JsonProvider provider) {
        this.provider = provider;
        return this;
    }

    public JsonSchemaBuilder withSchemaLoader(SchemaLoader schemaFactory) {
        this.schemaFactory = schemaFactory;
        return this;
    }

    @Override
    public JsonSchemaBuilder extraProperty(String propertyName, JsonValue jsonValue) {
        this.extraProps.put(propertyName, jsonValue);
        return this;
    }

    public Schema build() {
        final URI $id = $id();

        final SchemaLocation location;
        if ($id == null) {
            location = supplyIfNull(this.location, () -> SchemaPaths.fromBuilder(this));
        } else if (this.location != null) {
            location = this.location.withId($id);
        } else {
            location = SchemaPaths.from$Id($id);
        }
        final LoadingReport report = MoreObjects.firstNonNull(loadingReport, new LoadingReport());
        final Schema built = build(location, report);
        if (report.hasErrors()) {
            throw new SchemaLoadingException(location.getJsonPointerFragment(), report, built);
        }
        return built;
    }

    public JsonObject currentDocument() {
        return currentDocument;
    }

    @Override
    @Nullable
    public URI $id() {
        final URIKeyword keyword = getKeyword(Keywords.$ID);
        return keyword != null ? keyword.getKeywordValue() : null;
    }

    @Override
    @Nullable
    public URI $ref() {
        final URIKeyword keyword = getKeyword(Keywords.$REF);
        return keyword != null ? keyword.getKeywordValue() : null;
    }

    public Schema build(SchemaLocation location, LoadingReport report) {
        checkNotNull(location, "location must not be null");
        checkNotNull(report, "report must not be null");

        final URI $id = $id();
        final URI $ref = $ref();

        // Use the location provided during building as an override
        location = MoreObjects.firstNonNull(location, this.location);
        if ($id != null) {
            location = location.withId($id);
        }

        final URI thisSchemaURI = location.getUniqueURI();

        if (schemaFactory != null) {
            final Optional<Schema> cachedSchema = schemaFactory.findLoadedSchema(thisSchemaURI);
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

        return new Draft6SchemaImpl(location, ImmutableMap.copyOf(this.keywords));
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

    @Override
    public JsonSchemaBuilder title(String title) {
        return addOrRemoveString(Keywords.TITLE, title);
    }

    @Override
    public JsonSchemaBuilder defaultValue(JsonValue defaultValue) {
        return addOrRemoveJsonValue(Keywords.DEFAULT, defaultValue);
    }

    @Override
    public JsonSchemaBuilder description(String description) {
        return addOrRemoveString(Keywords.DESCRIPTION, description);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <X extends SchemaKeyword> X getKeyword(KeywordInfo<X> keyword) {
        return (X) keywords.get(keyword);
    }

    @SuppressWarnings("unchecked")
    protected <X extends SchemaKeyword> X getKeyword(KeywordInfo<X> keyword, Supplier<X> defaultValue) {
        return (X) keywords.computeIfAbsent(keyword, k -> defaultValue.get());
    }

    @Override
    public JsonSchemaBuilder type(JsonSchemaType requiredType) {
        final TypeKeyword existingValue = getKeyword(Keywords.TYPE);
        if (existingValue == null) {
            keywords.put(Keywords.TYPE, new TypeKeyword(requiredType));
        } else {
            keywords.put(Keywords.TYPE, existingValue.withAdditionalType(requiredType));
        }
        return this;
    }

    @Override
    public JsonSchemaBuilder withLoadingReport(LoadingReport report) {
        this.loadingReport = report;
        return this;
    }

    public <X extends SchemaKeyword> JsonSchemaBuilder keyword(KeywordInfo<X> keyword, X value) {
        checkNotNull(keyword, "keyword must not be null");
        checkNotNull(value, "value must not be null");
        this.keywords.put(keyword, value);
        return this;
    }

    @Override
    public JsonSchemaBuilder orType(JsonSchemaType requiredType) {
        return type(requiredType);
    }

    @Override
    public JsonSchemaBuilder types(Set<JsonSchemaType> requiredTypes) {
        if(requiredTypes != null) {
            keywords.put(Keywords.TYPE, new TypeKeyword(requiredTypes));
        } else {
            clearTypes();
        }
        return this;
    }

    @Override
    public JsonSchemaBuilder clearTypes() {
        keywords.remove(Keywords.TYPE);
        return this;
    }

    // #################################
    // SHARED VALIDATION KEYWORD SETTERS
    // #################################

    @Override
    public JsonSchemaBuilder allOfSchema(SchemaBuilder allOfSchema) {
        return addSchemaToList(Keywords.ALL_OF, allOfSchema);
    }

    @Override
    public JsonSchemaBuilder allOfSchemas(Collection<? extends SchemaBuilder> allOfSchemas) {
        return addOrRemoveSchemaList(Keywords.ALL_OF, allOfSchemas);
    }

    public JsonSchemaBuilder clearAllOfSchemas() {
        return addOrRemoveSchemaList(Keywords.ALL_OF, null);
    }

    @Override
    public JsonSchemaBuilder anyOfSchema(SchemaBuilder anyOfSchema) {
        return addSchemaToList(Keywords.ANY_OF, anyOfSchema);
    }

    @Override
    public JsonSchemaBuilder anyOfSchemas(Collection<? extends SchemaBuilder> anyOfSchemas) {
        return addOrRemoveSchemaList(Keywords.ANY_OF, anyOfSchemas);
    }

    public JsonSchemaBuilder clearAnyOfSchemas() {
        return addOrRemoveSchemaList(Keywords.ANY_OF, null);
    }

    @Override
    public JsonSchemaBuilder oneOfSchema(SchemaBuilder oneOfSchema) {
        return addSchemaToList(Keywords.ONE_OF, oneOfSchema);
    }

    @Override
    public JsonSchemaBuilder oneOfSchemas(Collection<? extends SchemaBuilder<?>> oneOfSchemas) {
        return addOrRemoveSchemaList(Keywords.ONE_OF, oneOfSchemas);
    }

    public JsonSchemaBuilder clearOneOfSchemas() {
        return addOrRemoveSchemaList(Keywords.ONE_OF, null);
    }

    @Override
    public JsonSchemaBuilder constValue(JsonValue constValue) {
        return addOrRemoveJsonValue(Keywords.CONST, constValue);
    }

    @Override
    public JsonSchemaBuilder constValueDouble(double constValue) {
        return this.constValue(provider().createValue(constValue));
    }

    @Override
    public JsonSchemaBuilder constValueInteger(int constValue) {
        return this.constValue(provider().createValue(constValue));
    }

    @Override
    public JsonSchemaBuilder constValueString(String constValue) {
        return this.constValue(provider().createValue(constValue));
    }

    @Override
    public JsonSchemaBuilder enumValues(JsonArray enumValues) {
        return this.addOrRemoveJsonArray(Keywords.ENUM, enumValues);
    }

    @Override
    public JsonSchemaBuilder notSchema(SchemaBuilder notSchema) {
        return this.addOrRemoveSchema(Keywords.NOT, notSchema);
    }

    // #######################################################
    // ARRAY KEYWORDS
    // @see ArrayKeywords
    // #######################################################

    @Override
    public JsonSchemaBuilder allItemSchema(@Valid SchemaBuilder allItemSchema) {
        checkNotNull(allItemSchema, "allItemSchema must not be null");
        return updateKeyword(Keywords.ITEMS,
                ItemsKeyword::newInstance,
                itemsKeyword -> itemsKeyword.toBuilder()
                        .allItemSchema(buildSubSchema(allItemSchema, Keywords.ITEMS))
                        .build());
    }

    private Schema buildSubSchema(SchemaBuilder toBuild, KeywordInfo<?> keyword) {
        checkState(this.location != null, "Location cannot be null");
        final SchemaLocation childLocation = this.location.child(keyword);
        return toBuild.build(childLocation, loadingReport);
    }

    private Schema buildSubSchema(SchemaBuilder toBuild, KeywordInfo<?> keyword, String path, String... paths) {
        checkState(this.location != null, "Location cannot be null");
        final SchemaLocation childLocation = this.location.child(keyword).child(path).child(paths);
        return toBuild.build(childLocation, loadingReport);
    }

    private Schema buildSubSchema(SchemaBuilder toBuild, KeywordInfo<?> keyword, int idx) {
        checkState(this.location != null, "Location cannot be null");
        final SchemaLocation childLocation = this.location.child(keyword).child(idx);
        return toBuild.build(childLocation, loadingReport);
    }

    private Schema buildSubSchema(SchemaBuilder toBuild, KeywordInfo<?> keyword, SchemaListKeyword list) {
        checkState(this.location != null, "Location cannot be null");
        final SchemaLocation childLocation = this.location.child(keyword).child(list.getSchemas().size());
        return toBuild.build(childLocation, loadingReport);
    }

    private List<Schema> buildSubSchemas(Collection<? extends SchemaBuilder> toBuild, KeywordInfo<?> keyword) {
        checkState(this.location != null, "Location cannot be null");
        checkNotNull(toBuild, "toBuild must not be null");
        checkNotNull(keyword, "keyword must not be null");
        AtomicInteger idx = new AtomicInteger();
        final SchemaLocation childPath = this.location.child(keyword);
        return toBuild.stream()
                .map(builder -> builder.build(childPath.child(idx.getAndIncrement()), loadingReport))
                .collect(Collectors.toList());
    }

    @Override
    public JsonSchemaBuilder itemSchema(SchemaBuilder itemSchema) {
        checkNotNull(itemSchema, "itemSchema must not be null");
        return updateKeyword(Keywords.ITEMS,
                ItemsKeyword::newInstance,
                itemsKeyword -> itemsKeyword.toBuilder()
                        .indexedSchema(
                                buildSubSchema(itemSchema, Keywords.ITEMS, itemsKeyword.getIndexedSchemas().size())
                        )
                        .build());
    }

    @Override
    public JsonSchemaBuilder itemSchemas(List<? extends SchemaBuilder> itemSchemas) {
        checkNotNull(itemSchemas, "itemSchemas must not be null");
        return updateKeyword(Keywords.ITEMS,
                ItemsKeyword::newInstance,
                itemsKeyword -> itemsKeyword.toBuilder()
                        .indexedSchemas(buildSubSchemas(itemSchemas, Keywords.ITEMS))
                        .build());
    }

    @Override
    public JsonSchemaBuilder schemaOfAdditionalItems(@Valid SchemaBuilder schemaOfAdditionalItems) {
        return updateKeyword(Keywords.ITEMS,
                ItemsKeyword::newInstance,
                itemsKeyword -> itemsKeyword.toBuilder()
                        .additionalItemSchema(
                                buildSubSchema(schemaOfAdditionalItems, Keywords.ADDITIONAL_ITEMS)
                        )
                        .build());
    }

    @Override
    public JsonSchemaBuilder noAdditionalItems() {
        return updateKeyword(Keywords.ITEMS,
                ItemsKeyword::newInstance,
                itemsKeyword -> itemsKeyword.toBuilder()
                        .additionalItemSchema(
                                buildSubSchema(Schemas.falseSchemaBuilder(), Keywords.ADDITIONAL_ITEMS)
                        )
                        .build());
    }

    @Override
    public JsonSchemaBuilder containsSchema(@Valid SchemaBuilder containsSchema) {
        return this.addOrRemoveSchema(Keywords.CONTAINS, containsSchema);
    }

    @Override
    public JsonSchemaBuilder minItems(@Min(0) Integer minItems) {
        return this.addOrRemoveNumber(Keywords.MIN_ITEMS, minItems);
    }

    @Override
    public JsonSchemaBuilder maxItems(@Min(0) Integer maxItems) {
        return this.addOrRemoveNumber(Keywords.MAX_ITEMS, maxItems);
    }

    @Override
    public JsonSchemaBuilder needsUniqueItems(boolean needsUniqueItems) {
        if (needsUniqueItems) {
            return this.addOrRemoveBoolean(Keywords.UNIQUE_ITEMS, true);
        } else {
            keywords.remove(Keywords.UNIQUE_ITEMS);
            return this;
        }
    }

    // #######################################################
    // NUMBER KEYWORDS
    // @see NumberKeywords
    // #######################################################

    @Override
    public JsonSchemaBuilder maximum(Number maximum) {
        return numberLimit(Keywords.MAXIMUM, LimitKeyword::maximumKeyword, maximum);
    }

    @Override
    public JsonSchemaBuilder exclusiveMaximum(Number exclusiveMaximum) {
        return numberExclusiveLimit(Keywords.MAXIMUM, LimitKeyword::maximumKeyword, exclusiveMaximum);
    }

    @Override
    public JsonSchemaBuilder minimum(Number minimum) {
        return numberLimit(Keywords.MINIMUM, LimitKeyword::minimumKeyword, minimum);
    }

    @Override
    public JsonSchemaBuilder exclusiveMinimum(Number exclusiveMinimum) {
        return numberExclusiveLimit(Keywords.MINIMUM, LimitKeyword::minimumKeyword, exclusiveMinimum);
    }

    @Override
    public JsonSchemaBuilder multipleOf(@Min(1) Number multipleOf) {
        this.addOrRemoveNumber(Keywords.MULTIPLE_OF, multipleOf);
        return this;
    }

    // #######################################################
    // OBJECT KEYWORDS
    // @see ObjectKeywords
    // #######################################################

    @Override
    public JsonSchemaBuilder clearPropertySchemas() {
        keywords.remove(Keywords.PROPERTIES);
        return this;
    }

    @Override
    public JsonSchemaBuilder clearRequiredProperties() {
        keywords.remove(Keywords.REQUIRED);
        return this;
    }

    @Override
    public JsonSchemaBuilder maxProperties(@Min(0) Integer maxProperties) {
        this.addOrRemoveNumber(Keywords.MAX_PROPERTIES, maxProperties);
        return this;
    }

    @Override
    public JsonSchemaBuilder minProperties(@Min(0) Integer minProperties) {
        this.addOrRemoveNumber(Keywords.MIN_PROPERTIES, minProperties);
        return this;
    }

    @Override
    public JsonSchemaBuilder patternProperty(Pattern pattern, SchemaBuilder schema) {
        checkNotNull(pattern, "pattern must not be null");
        this.putKeywordSchema(Keywords.PATTERN_PROPERTIES, pattern.pattern(), schema);
        return this;
    }

    @Override
    public JsonSchemaBuilder patternProperty(String pattern, SchemaBuilder schema) {
        this.putKeywordSchema(Keywords.PATTERN_PROPERTIES, pattern, schema);
        return this;
    }

    @Override
    public JsonSchemaBuilder propertyNameSchema(SchemaBuilder propertyNameSchema) {
        this.addOrRemoveSchema(Keywords.PROPERTY_NAMES, propertyNameSchema);
        return this;
    }

    @Override
    public JsonSchemaBuilder propertySchema(String propertySchemaKey, SchemaBuilder propertySchemaValue) {
        checkNotNull(propertySchemaKey, "propertySchemaKey must not be null");
        checkNotNull(propertySchemaValue, "propertySchemaValue must not be null");
        this.putKeywordSchema(Keywords.PROPERTIES, propertySchemaKey, propertySchemaValue);
        return this;
    }

    @Override
    public JsonSchemaBuilder requiredProperty(String requiredProperty) {
        checkNotNull(requiredProperty, "requiredProperty must not be null");
        return updateKeyword(Keywords.REQUIRED,
                StringSetKeyword::newInstance,
                stringSetKeyword -> stringSetKeyword.withAnotherValue(requiredProperty));
    }

    @Override
    public JsonSchemaBuilder propertyDependency(String ifPresent, String thenRequireThisProperty) {
        return updateKeyword(Keywords.DEPENDENCIES,
                DependenciesKeyword::newInstance,
                dependenciesKeyword -> dependenciesKeyword.toBuilder()
                        .propertyDependency(ifPresent, thenRequireThisProperty)
                        .build());
    }

    @Override
    public JsonSchemaBuilder schemaDependency(String property, SchemaBuilder dependency) {
        final Schema built = buildSubSchema(dependency, Keywords.DEPENDENCIES, property);
        return updateKeyword(Keywords.DEPENDENCIES,
                DependenciesKeyword::newInstance,
                dependenciesKeyword -> dependenciesKeyword.toBuilder()
                        .addDependencySchema(property, built)
                        .build());
    }

    @Override
    public JsonSchemaBuilder schemaOfAdditionalProperties(SchemaBuilder schemaOfAdditionalProperties) {
        checkNotNull(schemaOfAdditionalProperties, "schemaOfAdditionalProperties must not be null");
        this.addOrRemoveSchema(Keywords.ADDITIONAL_PROPERTIES, schemaOfAdditionalProperties);
        return this;
    }

    // #######################################################
    // STRING KEYWORDS
    // @see StringKeywords
    // #######################################################

    @Override
    public JsonSchemaBuilder format(String format) {
        this.addOrRemoveString(Keywords.FORMAT, format);
        return this;
    }

    @Override
    public JsonSchemaBuilder maxLength(@Min(0) Integer maxLength) {
        this.addOrRemoveNumber(Keywords.MAX_LENGTH, maxLength);
        return this;
    }

    @Override
    public JsonSchemaBuilder minLength(@Min(0) Integer minLength) {
        this.addOrRemoveNumber(Keywords.MIN_LENGTH, minLength);
        return this;
    }

    @Override
    public JsonSchemaBuilder pattern(Pattern pattern) {
        checkNotNull(pattern, "pattern must not be null");
        this.addOrRemoveString(Keywords.PATTERN, pattern.pattern());
        return this;
    }

    @Override
    public JsonSchemaBuilder pattern(String pattern) {
        return pattern(Pattern.compile(pattern));
    }

    // #######################################################
    // HELPER FUNCTIONS
    // #######################################################

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
    protected <K extends SchemaKeyword> JsonSchemaBuilder updateKeyword(KeywordInfo<K> keyword, Supplier<K> newInstanceFn, UnaryOperator<K> updateFn) {
        final K keywordValue = getKeyword(keyword, newInstanceFn);
        final K updatedKeyword = updateFn.apply(keywordValue);
        if (updatedKeyword == null) {
            keywords.remove(keyword);
        } else {
            keywords.put(keyword, updatedKeyword);
        }
        return this;
    }

    public JsonSchemaBuilder numberLimit(KeywordInfo<LimitKeyword> keyword, Supplier<LimitKeyword> newInstance, Number limit) {
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

    public JsonSchemaBuilder numberExclusiveLimit(KeywordInfo<LimitKeyword> keyword, Supplier<LimitKeyword> newInstance, Number exclusiveLimit) {
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

    protected JsonSchemaBuilder addOrRemoveString(KeywordInfo<StringKeyword> keyword, String value) {
        if (!removeIfNecessary(keyword, value)) {
            final SchemaKeywordImpl<String> keywordValue = new StringKeyword(value);
            keywords.put(keyword, keywordValue);
        }
        return this;
    }

    protected <X> boolean removeIfNecessary(KeywordInfo<?> keyword, X value) {
        checkNotNull(keyword, "keyword must not be null");
        if (value == null) {
            keywords.remove(keyword);
        }
        return value == null;
    }

    protected JsonSchemaBuilder addOrRemoveURI(KeywordInfo<URIKeyword> keyword, URI value) {
        if (!removeIfNecessary(keyword, value)) {
            final URIKeyword keywordValue = new URIKeyword(value);
            keywords.put(keyword, keywordValue);
        }
        return this;
    }

    protected JsonSchemaBuilder addOrRemoveJsonArray(KeywordInfo<JsonArrayKeyword> keyword, JsonArray value) {
        if (!removeIfNecessary(keyword, value)) {
            final JsonArrayKeyword keywordValue = new JsonArrayKeyword(value);
            keywords.put(keyword, keywordValue);
        }
        return this;
    }

    protected JsonSchemaBuilder addOrRemoveNumber(KeywordInfo<NumberKeyword> keyword, Number value) {
        if (!removeIfNecessary(keyword, value)) {
            final NumberKeyword keywordValue = new NumberKeyword(value);
            keywords.put(keyword, keywordValue);
        }
        return this;
    }

    protected JsonSchemaBuilder addOrRemoveBoolean(KeywordInfo<BooleanKeyword> keyword, Boolean bool) {
        if (!removeIfNecessary(keyword, bool)) {
            final BooleanKeyword keywordValue = new BooleanKeyword(bool);
            keywords.put(keyword, keywordValue);
        }
        return this;
    }

    protected JsonSchemaBuilder addOrRemoveJsonValue(KeywordInfo<JsonValueKeyword> keyword, JsonValue value) {
        if (!removeIfNecessary(keyword, value)) {
            final JsonValueKeyword keywordValue = new JsonValueKeyword(value);
            keywords.put(keyword, keywordValue);
        }
        return this;
    }

    protected JsonSchemaBuilder addSchemaToList(KeywordInfo<SchemaListKeyword> keyword, final SchemaBuilder schema) {
        checkNotNull(schema, "schema must not be null");
        return updateKeyword(keyword, SchemaListKeyword::newInstance,
                listKeyword -> listKeyword.toBuilder()
                        .schema(buildSubSchema(schema, keyword, listKeyword))
                        .build());
    }

    public JsonSchemaBuilder addOrRemoveSchemaList(KeywordInfo<SchemaListKeyword> keyword, final Collection<? extends SchemaBuilder> schemas) {
        return updateKeyword(keyword, SchemaListKeyword::newInstance,
                listKeyword -> {
                    if (schemas == null) {
                        return null;
                    }
                    final List<Schema> built = buildSubSchemas(schemas, keyword);
                    return listKeyword.toBuilder()
                            .schemas(built)
                            .build();
                });
    }

    public JsonSchemaBuilder addOrRemoveSchema(KeywordInfo<SingleSchemaKeyword> keyword, final SchemaBuilder schema) {
        if (schema == null) {
            keywords.remove(keyword);
        } else {
            final Schema built = buildSubSchema(schema, keyword);
            keywords.put(keyword, new SingleSchemaKeyword(built));
        }

        return this;
    }

    @SuppressWarnings("unchecked")
    protected JsonSchemaBuilder putKeywordSchema(KeywordInfo<SchemaMapKeyword> keyword, String key, SchemaBuilder value) {
        final Schema schema = buildSubSchema(value, keyword, key);
        return updateKeyword(keyword, SchemaMapKeyword::empty, schemaMap -> schemaMap.toBuilder()
                .schema(key, schema)
                .build());
    }

    public JsonSchemaBuilder putAllKeywordSchemas(KeywordInfo<SchemaMapKeyword> keyword, Map<String, SchemaBuilder> schemas) {
        if (schemas == null || schemas.isEmpty()) {
            keywords.remove(keyword);
            return this;
        } else {
            final Map<String, Schema> builtSchemas = schemas.entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> e.getKey(),
                            e -> buildSubSchema(e.getValue(), keyword, e.getKey())
                    ));
            return updateKeyword(keyword, SchemaMapKeyword::empty, builder -> builder.toBuilder()
                    .schemas(builtSchemas)
                    .build());
        }
    }

    // #######################################################
    // LAZY GETTERS
    // #######################################################

    private JsonProvider provider() {
        return MoreObjects.firstNonNull(provider, JsonProvider.provider());
    }
}