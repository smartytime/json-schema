package io.sbsp.jsonschema.builder;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import io.sbsp.jsonschema.Draft6Schema;
import io.sbsp.jsonschema.ReferenceSchema;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaException;
import io.sbsp.jsonschema.SchemaFactory;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.enums.JsonSchemaKeywordType;
import io.sbsp.jsonschema.enums.JsonSchemaType;
import io.sbsp.jsonschema.extractor.ExtractionReport;
import io.sbsp.jsonschema.impl.Draft6SchemaImpl;
import io.sbsp.jsonschema.keyword.BooleanKeyword;
import io.sbsp.jsonschema.keyword.JsonArrayKeyword;
import io.sbsp.jsonschema.keyword.JsonValueKeyword;
import io.sbsp.jsonschema.keyword.KeywordMetadata;
import io.sbsp.jsonschema.keyword.MaximumKeyword;
import io.sbsp.jsonschema.keyword.MaximumKeyword.MaximumKeywordBuilder;
import io.sbsp.jsonschema.keyword.MinimumKeyword;
import io.sbsp.jsonschema.keyword.MinimumKeyword.MinimumKeywordBuilder;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.sbsp.jsonschema.utils.StreamUtils.supplyIfNull;
import static java.util.Collections.emptyList;

public class JsonSchemaBuilder {

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
    private ExtractionReport extractionReport;

    protected JsonSchemaBuilder() {
        this.location = null;
    }

    protected JsonSchemaBuilder(URI $id) {
        checkNotNull($id, "$id must not be null");
        this.addOrRemoveURI(SchemaKeyword.$id, $id);
        this.location = null;
    }

    protected JsonSchemaBuilder(SchemaLocation location, URI $id) {
        checkNotNull(location, "location must not be null");
        checkNotNull($id, "$id must not be null");
        this.addOrRemoveURI(SchemaKeyword.$id, $id);
        this.location = location;
    }

    protected JsonSchemaBuilder(SchemaLocation location) {
        checkNotNull(location, "location must not be null");
        this.location = location;
    }

    public JsonSchemaBuilder currentDocument(JsonObject currentDocument) {
        this.currentDocument = currentDocument;
        return this;
    }

    public JsonSchemaBuilder ref(URI ref, @Nullable SchemaFactory schemaFactory) {
        this.addOrRemoveURI(SchemaKeyword.$ref, ref);
        this.schemaFactory = schemaFactory;
        return this;
    }

    public JsonSchemaBuilder ref(String ref) {
        checkNotNull(ref, "ref must not be null");
        return this.addOrRemoveURI(SchemaKeyword.$ref, URI.create(ref));
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

    public Draft6Schema build() {
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
        return build(location).asDraft6();
    }

    public JsonObject currentDocument() {
        return currentDocument;
    }

    public ExtractionReport extractionReport() {
        return extractionReport;
    }

    public JsonSchemaBuilder extractionReport(ExtractionReport extractionReport) {
        this.extractionReport = extractionReport;
        return this;
    }

    @Nullable
    public URI $id() {
        final URIKeyword keyword = getKeyword(SchemaKeyword.$id);
        return keyword != null ? keyword.getKeywordValue() : null;
    }

    @Nullable
    public URI $ref() {
        final URIKeyword keyword = getKeyword(SchemaKeyword.$ref);
        return keyword != null ? keyword.getKeywordValue() : null;
    }

    Schema build(SchemaLocation location) {
        checkNotNull(location, "location must not be null");

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
            return ReferenceSchema.refSchemaBuilder($ref)
                    .factory(schemaFactory)
                    .currentDocument(currentDocument)
                    .location(location)
                    .build();
        }

        ImmutableMap.Builder<KeywordMetadata<?>, SchemaKeyword> collector = ImmutableMap.builder();
        collector.putAll(this.keywords);

        for (Map.Entry<KeywordMetadata<?>, SchemaKeywordBuilder> entry : this.keywordBuilders.entrySet()) {
            final KeywordMetadata<?> keyword = entry.getKey();
            final SchemaKeywordBuilder schemaBuilder = entry.getValue();
            final SchemaLocation childLocation = location.child(keyword.getKey());

            final SchemaKeyword builtKeyword = schemaBuilder.build(childLocation, schemaFactory, currentDocument);
            collector.put(keyword, builtKeyword);
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
        return addOrRemoveString(SchemaKeyword.title, title);
    }

    public JsonSchemaBuilder defaultValue(JsonValue defaultValue) {
        return addOrRemoveJsonValue(SchemaKeyword.$default, defaultValue);
    }

    public JsonSchemaBuilder description(String description) {
        return addOrRemoveString(SchemaKeyword.description, description);
    }

    @SuppressWarnings("unchecked")
    protected <X extends SchemaKeyword> X getKeyword(KeywordMetadata<X> keyword) {
        return (X) keywords.get(keyword);
    }

    public JsonSchemaBuilder type(JsonSchemaType requiredType) {
        final TypeKeyword existingValue = getKeyword(SchemaKeyword.type);
        if (existingValue == null) {
            keywords.put(SchemaKeyword.type, new TypeKeyword(requiredType));
        } else {
            keywords.put(SchemaKeyword.type, existingValue.withAdditionalType(requiredType));
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
            keywords.put(SchemaKeyword.type, new TypeKeyword(requiredTypes));
        } else {
            clearTypes();
        }
        return this;
    }

    public JsonSchemaBuilder clearTypes() {
        keywords.remove(SchemaKeyword.type);
        return this;
    }

    // #################################
    // SHARED VALIDATION KEYWORD SETTERS
    // #################################

    public JsonSchemaBuilder allOfSchema(JsonSchemaBuilder allOfSchema) {
        return addSchemaToList(SchemaKeyword.allOf, allOfSchema);
    }

    public JsonSchemaBuilder allOfSchemas(Collection<? extends JsonSchemaBuilder> allOfSchemas) {
        return addOrRemoveSchemaList(SchemaKeyword.allOf, allOfSchemas);
    }

    public JsonSchemaBuilder clearAllOfSchemas() {
        return addOrRemoveSchemaList(SchemaKeyword.allOf, null);
    }

    public JsonSchemaBuilder anyOfSchema(JsonSchemaBuilder anyOfSchema) {
        return addSchemaToList(SchemaKeyword.anyOf, anyOfSchema);
    }

    public JsonSchemaBuilder anyOfSchemas(Collection<? extends JsonSchemaBuilder> anyOfSchemas) {
        return addOrRemoveSchemaList(SchemaKeyword.anyOf, anyOfSchemas);
    }

    public JsonSchemaBuilder clearAnyOfSchemas() {
        return addOrRemoveSchemaList(SchemaKeyword.anyOf, null);
    }

    public JsonSchemaBuilder oneOfSchema(JsonSchemaBuilder oneOfSchema) {
        return addSchemaToList(SchemaKeyword.oneOf, oneOfSchema);
    }

    public JsonSchemaBuilder oneOfSchemas(Collection<? extends JsonSchemaBuilder> oneOfSchemas) {
        return addOrRemoveSchemaList(SchemaKeyword.oneOf, oneOfSchemas);
    }

    public JsonSchemaBuilder clearOneOfSchemas() {
        return addOrRemoveSchemaList(SchemaKeyword.oneOf, null);
    }

    public JsonSchemaBuilder constValue(JsonValue constValue) {
        return addOrRemoveJsonValue(SchemaKeyword.$const, constValue);
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
        return this.addOrRemoveJsonArray(SchemaKeyword.$enum, enumValues);
    }

    public JsonSchemaBuilder notSchema(JsonSchemaBuilder notSchema) {
        return this.addOrRemoveSchema(SchemaKeyword.not, notSchema);
    }

    // #######################################################
    // ARRAY KEYWORDS
    // @see ArrayKeywords
    // #######################################################

    public JsonSchemaBuilder containsSchema(@Valid JsonSchemaBuilder containsSchema) {
        return this.addOrRemoveSchema(SchemaKeyword.contains, containsSchema);
    }

    public JsonSchemaBuilder allItemSchema(@Valid JsonSchemaBuilder allItemSchema) {
        final ItemsKeywordBuilder existing = (ItemsKeywordBuilder) keywordBuilders.getOrDefault(SchemaKeyword.items, new ItemsKeywordBuilder(emptyList()));
        keywordBuilders.put(SchemaKeyword.items, existing.withAllItemSchema(allItemSchema));
        return this;
    }

    public JsonSchemaBuilder itemSchema(JsonSchemaBuilder itemSchema) {
        checkNotNull(itemSchema, "itemSchema must not be null");
        final ItemsKeywordBuilder existing = (ItemsKeywordBuilder) keywordBuilders.getOrDefault(SchemaKeyword.items, new ItemsKeywordBuilder(emptyList()));
        keywordBuilders.put(SchemaKeyword.items, existing.withAnotherSchema(itemSchema));
        return this;
    }

    public JsonSchemaBuilder itemSchemas(List<JsonSchemaBuilder> itemSchemas) {
        final ItemsKeywordBuilder existing = (ItemsKeywordBuilder) keywordBuilders.getOrDefault(SchemaKeyword.items, new ItemsKeywordBuilder(emptyList()));
        keywordBuilders.put(SchemaKeyword.items, existing.withIndexedSchemas(itemSchemas));
        return this;
    }

    public JsonSchemaBuilder minItems(@Min(0) Integer minItems) {
        return this.addOrRemoveNumber(SchemaKeyword.minItems, minItems);
    }

    public JsonSchemaBuilder maxItems(@Min(0) Integer maxItems) {
        return this.addOrRemoveNumber(SchemaKeyword.maxItems, maxItems);
    }

    public JsonSchemaBuilder needsUniqueItems(boolean needsUniqueItems) {
        if (needsUniqueItems) {
            return this.addOrRemoveBoolean(SchemaKeyword.uniqueItems, true);
        } else {
            keywords.remove(SchemaKeyword.uniqueItems);
            return this;
        }
    }

    public JsonSchemaBuilder schemaOfAdditionalItems(@Valid JsonSchemaBuilder schemaOfAdditionalItems) {
        this.addOrRemoveSchema(SchemaKeyword.additionalItems, schemaOfAdditionalItems);
        return this;
    }

    // #######################################################
    // NUMBER KEYWORDS
    // @see NumberKeywords
    // #######################################################

    public JsonSchemaBuilder exclusiveMaximum(Number exclusiveMaximum) {
        if (exclusiveMaximum == null) {
            keywords.remove(SchemaKeyword.maximum);
            return this;
        }
        final MaximumKeyword existing = getKeyword(SchemaKeyword.maximum);
        final MaximumKeywordBuilder keywordBuilder = existing != null ? existing.toBuilder() : MaximumKeyword.builder();
        final MaximumKeyword keyword = keywordBuilder.maximum(exclusiveMaximum).isExclusive(true).build();
        keywords.put(SchemaKeyword.maximum, keyword);
        return this;
    }

    public JsonSchemaBuilder exclusiveMinimum(Number exclusiveMinimum) {
        if (exclusiveMinimum == null) {
            keywords.remove(SchemaKeyword.minimum);
            return this;
        }
        final MinimumKeyword existing = getKeyword(SchemaKeyword.minimum);
        final MinimumKeywordBuilder keywordBuilder = existing != null ? existing.toBuilder() : MinimumKeyword.builder();
        final MinimumKeyword keyword = keywordBuilder.minimum(exclusiveMinimum).isExclusive(true).build();
        keywords.put(SchemaKeyword.minimum, keyword);
        return this;
    }

    public JsonSchemaBuilder minimum(Number minimum) {
        if (minimum == null) {
            keywords.remove(SchemaKeyword.minimum);
            return this;
        }
        final MinimumKeyword existing = getKeyword(SchemaKeyword.minimum);

        final MinimumKeyword update;
        if (existing != null) {
            update = existing.toBuilder().minimum(minimum).build();
        } else {
            update = MinimumKeyword.builder().minimum(minimum).build();
        }
        keywords.put(SchemaKeyword.minimum, update);
        return this;
    }

    public JsonSchemaBuilder maximum(Number maximum) {
        if (maximum == null) {
            keywords.remove(SchemaKeyword.maximum);
            return this;
        }
        final MaximumKeyword existing = getKeyword(SchemaKeyword.maximum);
        final MaximumKeyword update;
        if (existing != null) {
            update = existing.toBuilder().maximum(maximum).build();
        } else {
            update = MaximumKeyword.builder().maximum(maximum).build();
        }
        keywords.put(SchemaKeyword.maximum, update);
        return this;
    }


    public JsonSchemaBuilder multipleOf(@Min(1) Number multipleOf) {
        this.addOrRemoveNumber(SchemaKeyword.multipleOf, multipleOf);
        return this;
    }

    // #######################################################
    // OBJECT KEYWORDS
    // @see ObjectKeywords
    // #######################################################

    public JsonSchemaBuilder clearPropertySchemas() {
        keywordBuilders.remove(SchemaKeyword.properties);
        return this;
    }

    public JsonSchemaBuilder clearRequiredProperties() {
        keywordBuilders.remove(SchemaKeyword.required);
        return this;
    }

    public JsonSchemaBuilder maxProperties(@Min(0) Integer maxProperties) {
        this.addOrRemoveNumber(SchemaKeyword.maxProperties, maxProperties);
        return this;
    }

    public JsonSchemaBuilder minProperties(@Min(0) Integer minProperties) {
        this.addOrRemoveNumber(SchemaKeyword.minProperties, minProperties);
        return this;
    }

    public JsonSchemaBuilder patternProperty(Pattern pattern, JsonSchemaBuilder schema) {
        checkNotNull(pattern, "pattern must not be null");
        this.putKeywordSchema(SchemaKeyword.patternProperties, pattern.pattern(), schema);
        return this;
    }

    public JsonSchemaBuilder patternProperty(String pattern, JsonSchemaBuilder schema) {
        this.putKeywordSchema(SchemaKeyword.patternProperties, pattern, schema);
        return this;
    }

    public JsonSchemaBuilder propertyDependency(String ifPresent, String thenRequireThisProperty) {
        checkNotNull(ifPresent, "ifPresent must not be null");

        final PropertyDependencyKeywordBuilder depBuilder;
        final PropertyDependencyKeywordBuilder existing = (PropertyDependencyKeywordBuilder) keywordBuilders.get(SchemaKeyword.dependencies);
        if (existing == null) {
            depBuilder = new PropertyDependencyKeywordBuilder(ifPresent, thenRequireThisProperty);
        } else {
            depBuilder = existing.propertyDependency(ifPresent, thenRequireThisProperty);
        }

        keywordBuilders.put(SchemaKeyword.dependencies, depBuilder);
        return this;
    }

    public JsonSchemaBuilder propertyNameSchema(JsonSchemaBuilder propertyNameSchema) {
        this.addOrRemoveSchema(SchemaKeyword.propertyNames, propertyNameSchema);
        return this;
    }

    public JsonSchemaBuilder propertySchema(String propertySchemaKey, JsonSchemaBuilder propertySchemaValue) {
        checkNotNull(propertySchemaKey, "propertySchemaKey must not be null");
        checkNotNull(propertySchemaValue, "propertySchemaValue must not be null");
        this.putKeywordSchema(SchemaKeyword.properties, propertySchemaKey, propertySchemaValue);
        return this;
    }

    public JsonSchemaBuilder requiredProperty(String requiredProperty) {
        checkNotNull(requiredProperty, "requiredProperty must not be null");
        final StringSetKeyword requiredKeyword;
        StringSetKeyword existing = getKeyword(SchemaKeyword.required);
        if (existing == null) {
            requiredKeyword = new StringSetKeyword(requiredProperty);
        } else {
            requiredKeyword = existing.withAnotherValue(requiredProperty);
        }

        keywords.put(SchemaKeyword.required, requiredKeyword);
        return this;
    }

    public JsonSchemaBuilder schemaDependency(String property, JsonSchemaBuilder dependency) {
        checkNotNull(property, "property must not be null");
        checkNotNull(dependency, "dependency must not be null");
        final PropertyDependencyKeywordBuilder depBuilder;
        final PropertyDependencyKeywordBuilder existing = (PropertyDependencyKeywordBuilder) keywordBuilders.get(SchemaKeyword.dependencies);
        if (existing == null) {
            depBuilder = new PropertyDependencyKeywordBuilder(new SchemaMapKeywordBuilder(property, dependency));
        } else {
            depBuilder = existing.addSchema(property, dependency);
        }
        keywordBuilders.put(SchemaKeyword.dependencies, depBuilder);
        return this;
    }

    public JsonSchemaBuilder schemaOfAdditionalProperties(JsonSchemaBuilder schemaOfAdditionalProperties) {
        checkNotNull(schemaOfAdditionalProperties, "schemaOfAdditionalProperties must not be null");
        this.addOrRemoveSchema(SchemaKeyword.additionalProperties, schemaOfAdditionalProperties);
        return this;
    }

    // #######################################################
    // STRING KEYWORDS
    // @see StringKeywords
    // #######################################################

    public JsonSchemaBuilder format(String format) {
        this.addOrRemoveString(SchemaKeyword.format, format);
        return this;
    }

    public JsonSchemaBuilder maxLength(@Min(0) Integer maxLength) {
        this.addOrRemoveNumber(SchemaKeyword.maxLength, maxLength);
        return this;
    }

    public JsonSchemaBuilder minLength(@Min(0) Integer minLength) {
        this.addOrRemoveNumber(SchemaKeyword.minLength, minLength);
        return this;
    }

    public JsonSchemaBuilder pattern(Pattern pattern) {
        checkNotNull(pattern, "pattern must not be null");
        this.addOrRemoveString(SchemaKeyword.pattern, pattern.pattern());
        return this;
    }

    public JsonSchemaBuilder pattern(String pattern) {
        return pattern(Pattern.compile(pattern));
    }

    // #######################################################
    // HELPER FUNCTIONS
    // #######################################################

    protected <X> JsonSchemaBuilder addOrRemoveString(KeywordMetadata<StringKeyword> keyword, String value) {
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

    protected JsonSchemaBuilder addSchemaToList(KeywordMetadata<SchemaListKeyword> keyword, JsonSchemaBuilder value) {
        final SchemaListKeywordBuilder existingValue = (SchemaListKeywordBuilder) keywordBuilders.get(keyword);
        if (existingValue == null) {
            keywordBuilders.put(keyword, new SchemaListKeywordBuilder(value));
        } else {
            keywordBuilders.put(keyword, existingValue.withAnotherSchema(value));
        }
        return this;
    }

    public <T> JsonSchemaBuilder addOrRemoveSchemaList(KeywordMetadata<SchemaListKeyword> keyword, Collection<? extends JsonSchemaBuilder> schemas) {
        if (schemas == null) {
            keywordBuilders.remove(keyword);
        } else {
            keywordBuilders.put(keyword, new SchemaListKeywordBuilder(schemas));
        }
        return this;
    }

    public JsonSchemaBuilder addOrRemoveSchema(KeywordMetadata<? extends SingleSchemaKeyword> keyword, JsonSchemaBuilder value) {
        if (!removeIfNecessary(keyword, value)) {
            final SingleSchemaKeywordBuilder keywordValue = new SingleSchemaKeywordBuilder(value);
            keywordBuilders.put(keyword, keywordValue);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    protected <T extends JsonSchemaBuilder> JsonSchemaBuilder putKeywordSchema(KeywordMetadata<SchemaMapKeyword> keyword, String key, T value) {
        final SchemaMapKeywordBuilder existingValue = (SchemaMapKeywordBuilder) keywordBuilders.get(keyword);
        if (existingValue == null) {
            keywordBuilders.put(keyword, new SchemaMapKeywordBuilder(key, value));
        } else {
            keywordBuilders.put(keyword, existingValue.addSchema(key, value));
        }
        return this;
    }

    public <T extends JsonSchemaBuilder> JsonSchemaBuilder putAllKeywordSchemas(KeywordMetadata<SchemaMapKeyword> keyword, Map<String, T> schemas) {
        if (schemas == null) {
            keywordBuilders.remove(keyword);
        } else {
            keywordBuilders.put(keyword, new SchemaMapKeywordBuilder(schemas));
        }
        return this;
    }

    // #######################################################
    // LAZY GETTERS
    // #######################################################

    private JsonProvider provider() {
        return MoreObjects.firstNonNull(provider, JsonProvider.provider());
    }


}