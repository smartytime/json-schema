package io.sbsp.jsonschema.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import io.sbsp.jsonschema.Draft3Schema;
import io.sbsp.jsonschema.Draft4Schema;
import io.sbsp.jsonschema.Draft6Schema;
import io.sbsp.jsonschema.DraftSchema;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.enums.JsonSchemaType;
import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import io.sbsp.jsonschema.keyword.DependenciesKeyword;
import io.sbsp.jsonschema.keyword.ItemsKeyword;
import io.sbsp.jsonschema.keyword.JsonArrayKeyword;
import io.sbsp.jsonschema.keyword.KeywordMetadata;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.LimitKeyword;
import io.sbsp.jsonschema.keyword.NumberKeyword;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.keyword.SchemaKeywordImpl;
import io.sbsp.jsonschema.keyword.SchemaListKeyword;
import io.sbsp.jsonschema.keyword.SchemaMapKeyword;
import io.sbsp.jsonschema.keyword.StringKeyword;
import io.sbsp.jsonschema.keyword.StringSetKeyword;
import io.sbsp.jsonschema.keyword.TypeKeyword;
import io.sbsp.jsonschema.keyword.URIKeyword;
import io.sbsp.jsonschema.utils.JsonSchemaGenerator;
import io.sbsp.jsonschema.utils.JsonUtils;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import javax.annotation.Nullable;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;
import java.net.URI;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.sbsp.jsonschema.enums.JsonSchemaVersion.Custom;

@EqualsAndHashCode(of = "keywords")
public abstract class JsonSchemaImpl<D extends DraftSchema> implements DraftSchema<D> {

    @NonNull
    private final SchemaLocation location;

    private final Map<KeywordMetadata<?>, SchemaKeyword> keywords;
    private final JsonSchemaVersion version;

    public JsonSchemaImpl(SchemaLocation location, Map<KeywordMetadata<?>, SchemaKeyword> keywords, JsonSchemaVersion version) {
        this.location = checkNotNull(location, "location must not be null");
        checkNotNull(keywords, "keywords must not be null");
        this.version = checkNotNull(version, "version must not be null");
        this.keywords = Collections.unmodifiableMap(keywords);
    }

    // ######################################################
    // ###### Base Schema Methods Implemented  ##############
    // ######################################################

    public JsonGenerator toJson(final JsonGenerator generator, JsonSchemaVersion version) {
        generator.writeStartObject();

        final JsonSchemaGenerator schemaGenerator;
        if (generator instanceof JsonSchemaGenerator) {
            schemaGenerator = (JsonSchemaGenerator) generator;
        } else {
            schemaGenerator = new JsonSchemaGenerator(generator);
        }

        keywords.forEach((keyword, keywordValue) -> {
            final Set<JsonSchemaVersion> versions = keyword.getAppliesToVersions();
            if (Sets.intersection(versions, EnumSet.of(version, Custom)).size() > 0) {
                keywordValue.writeToGenerator(keyword, schemaGenerator, version);
            }
        });

        generator.writeEnd();
        return generator;
    }

    @Override
    public Map<KeywordMetadata<?>, SchemaKeyword> getKeywords() {
        return keywords;
    }

    @Override
    public JsonSchemaVersion getVersion() {
        return version;
    }

    public Draft6Schema asDraft6() {
        if (this instanceof Draft6Schema) {
            return (Draft6Schema) this;
        }
        return new Draft6SchemaImpl(getLocation(), keywords());
    }

    public Draft3Schema asDraft3() {
        if (this instanceof Draft3Schema) {
            return (Draft3Schema) this;
        }
        return new Draft3SchemaImpl(getLocation(), keywords());
    }

    public Draft4Schema asDraft4() {
        if (this instanceof Draft4Schema) {
            return (Draft4Schema) this;
        }
        return new Draft4SchemaImpl(getLocation(), keywords());
    }

    // ######################################################
    // ###### Getters for common keywords (draft3-6) ########
    // ######################################################

    @Override
    public SchemaLocation getLocation() {
        return location;
    }

    @Override
    public URI getId() {
        return uri(Keywords.$id);
    }

    @Override
    public URI getSchemaURI() {
        return uri(Keywords.$schema);
    }

    @Override
    public String getTitle() {
        return string(Keywords.title);
    }

    @Override
    public String getDescription() {
        return string(Keywords.description);
    }

    @Override
    public Set<JsonSchemaType> getTypes() {
        return types();
    }

    @Override
    public Optional<JsonArray> getEnumValues() {
        return enumValues();
    }

    @Override
    public Optional<JsonValue> getDefaultValue() {
        return defaultValue();
    }

    @Override
    public String getFormat() {
        return format();
    }

    @Override
    public Integer getMinLength() {
        return minLength();
    }

    @Override
    public Integer getMaxLength() {
        return maxLength();
    }

    @Override
    public String getPattern() {
        return pattern();
    }

    @Override
    public Number getMaximum() {
        return maximum();
    }

    @Override
    public Number getMinimum() {
        return minimum();
    }

    @Override
    public Integer getMinItems() {
        return minItems();
    }

    @Override
    public Integer getMaxItems() {
        return maxItems();
    }

    @Override
    public Optional<D> getAllItemSchema() {
        return allItemSchema().map(this::asType);
    }

    @Override
    public List<Schema> getItemSchemas() {
        return itemSchemas();
    }

    @Override
    public Optional<D> getAdditionalItemsSchema() {
        return additionalItemsSchema().map(this::asType);
    }

    @Override
    public Map<String, Schema> getProperties() {
        return properties();
    }

    @Override
    public Map<String, Schema> getPatternProperties() {
        return patternProperties();
    }

    @Override
    public Optional<D> getAdditionalPropertiesSchema() {
        return additionalPropertiesSchema().map(this::asType);
    }

    @Override
    public SetMultimap<String, String> getPropertyDependencies() {
        return propertyDependencies();
    }

    @Override
    public Map<String, Schema> getPropertySchemaDependencies() {
        return propertySchemaDependencies();
    }

    @Override
    public boolean requiresUniqueItems() {
        return uniqueItems();
    }

    @Override
    public String toString() {
        return toString(false);
    }

    // ######################################################
    // ###### Helper methods for subclasses (accessing keywords) ########
    // ######################################################

    protected Map<KeywordMetadata<?>, SchemaKeyword> keywords() {
        return keywords;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected URI uri(KeywordMetadata<URIKeyword> keywordType) {
        return keywordValue(keywordType).orElse(null);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected String string(KeywordMetadata<StringKeyword> stringKeyword) {
        checkNotNull(stringKeyword, "stringKeyword must not be null");
        return keywordValue(stringKeyword).orElse(null);
    }

    @SuppressWarnings("unchecked")
    protected <X extends SchemaKeyword> Optional<X> keyword(KeywordMetadata<X> keyword) {
        checkNotNull(keyword, "keyword must not be null");
        return Optional.ofNullable((X) keywords.get(keyword));
    }

    protected List<Schema> schemaList(KeywordMetadata<? extends SchemaListKeyword> keyword) {
        checkNotNull(keyword, "keyword must not be null");
        return keyword(keyword).map(SchemaListKeyword::getSchemas).orElse(Collections.emptyList());
    }

    protected Map<String, Schema> schemaMap(KeywordMetadata<SchemaMapKeyword> keyword) {
        checkNotNull(keyword, "keyword must not be null");
        return keyword(keyword).map(SchemaMapKeyword::getSchemas).orElse(Collections.emptyMap());
    }

    @SuppressWarnings("unchecked")
    protected <X> Optional<X> keywordValue(KeywordMetadata<? extends SchemaKeywordImpl<X>> keyword) {
        checkNotNull(keyword, "keyword must not be null");
        final SchemaKeywordImpl<X> keywordValue = (SchemaKeywordImpl<X>) keywords.get(keyword);
        return keywordValue == null ? Optional.empty() : Optional.ofNullable(keywordValue.getKeywordValue());
    }

    protected JsonArray examples() {
        return keyword(Keywords.examples)
                .map(JsonArrayKeyword::getKeywordValue)
                .orElse(JsonUtils.emptyJsonArray());
    }

    protected Map<String, Schema> definitions() {
        return schemaMap(Keywords.definitions);
    }

    protected Set<JsonSchemaType> types() {
        return keyword(Keywords.type)
                .map(TypeKeyword::getTypes)
                .orElse(Collections.emptySet());
    }

    protected Optional<JsonArray> enumValues() {
        return keywordValue(Keywords.$enum);
    }

    protected Optional<JsonValue> defaultValue() {
        return keywordValue(Keywords.$default);
    }

    protected Optional<Schema> notSchema() {
        return keywordValue(Keywords.not);
    }

    protected Optional<JsonValue> constValue() {
        return keywordValue(Keywords.$const);
    }

    protected List<Schema> allOfSchemas() {
        return schemaList(Keywords.allOf);
    }

    protected List<Schema> anyOfSchemas() {
        return schemaList(Keywords.anyOf);
    }

    protected List<Schema> oneOfSchemas() {
        return schemaList(Keywords.oneOf);
    }

    protected String format() {
        return string(Keywords.format);
    }

    protected Integer minLength() {
        return keywordValue(Keywords.minLength)
                .map(Number::intValue)
                .orElse(null);
    }

    protected Integer maxLength() {
        return keywordValue(Keywords.maxLength)
                .map(Number::intValue)
                .orElse(null);
    }

    protected String pattern() {
        return string(Keywords.pattern);
    }

    protected Number multipleOf() {
        return keywordValue(Keywords.multipleOf).orElse(null);
    }

    protected Number maximum() {
        return keyword(Keywords.maximum).map(LimitKeyword::getLimit).orElse(null);
    }

    protected Number minimum() {
        return keyword(Keywords.minimum).map(LimitKeyword::getLimit).orElse(null);
    }

    protected Number exclusiveMinimum() {
        return keyword(Keywords.minimum).map(LimitKeyword::getExclusiveLimit).orElse(null);
    }

    protected Number exclusiveMaximum() {
        return keyword(Keywords.maximum).map(LimitKeyword::getExclusiveLimit).orElse(null);
    }

    protected Integer minItems() {
        return getInteger(Keywords.minItems);
    }

    protected Integer getInteger(KeywordMetadata<NumberKeyword> keyword) {
        checkNotNull(keyword, "keyword must not be null");
        return keywordValue(keyword).map(Number::intValue).orElse(null);
    }

    protected Integer maxItems() {
        return getInteger(Keywords.maxItems);
    }

    protected Optional<Schema> allItemSchema() {
        return keyword(Keywords.items)
                .flatMap(ItemsKeyword::getAllItemSchema);
    }

    protected List<Schema> itemSchemas() {
        return keyword(Keywords.items)
                .map(ItemsKeyword::getIndexedSchemas)
                .orElse(Collections.emptyList());
    }

    protected Optional<Schema> additionalItemsSchema() {
        return keyword(Keywords.items).flatMap(ItemsKeyword::getAdditionalItemSchema);
    }

    protected Optional<Schema> containsSchema() {
        return keywordValue(Keywords.contains);
    }

    protected boolean uniqueItems() {
        return keywordValue(Keywords.uniqueItems).orElse(false);
    }

    protected Map<String, Schema> properties() {
        return schemaMap(Keywords.properties);
    }

    protected Map<String, Schema> patternProperties() {
        return schemaMap(Keywords.patternProperties);
    }

    protected Optional<Schema> additionalPropertiesSchema() {
        return keywordValue(Keywords.additionalProperties);
    }

    protected Optional<Schema> propertyNameSchema() {
        return keywordValue(Keywords.propertyNames);
    }

    protected SetMultimap<String, String> propertyDependencies() {
        return keyword(Keywords.dependencies)
                .map(DependenciesKeyword::getPropertyDependencies)
                .orElse(ImmutableSetMultimap.of());
    }

    protected Map<String, Schema> propertySchemaDependencies() {
        return keyword(Keywords.dependencies)
                .map(DependenciesKeyword::getDependencySchemas)
                .map(SchemaMapKeyword::getSchemas)
                .orElse(ImmutableMap.of());
    }

    protected Integer maxProperties() {
        return getInteger(Keywords.maxProperties);
    }

    protected Integer minProperties() {
        return getInteger(Keywords.minProperties);
    }

    protected Set<String> requiredProperties() {
        return keyword(Keywords.required)
                .map(StringSetKeyword::getKeywordValue)
                .orElse(Collections.emptySet());
    }
}
