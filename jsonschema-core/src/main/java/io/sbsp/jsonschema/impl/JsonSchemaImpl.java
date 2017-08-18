package io.sbsp.jsonschema.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
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
import io.sbsp.jsonschema.keyword.KeywordInfo;
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
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.sbsp.jsonschema.enums.JsonSchemaVersion.Draft6;

@EqualsAndHashCode(of = "keywords")
@Slf4j
public abstract class JsonSchemaImpl<D extends DraftSchema> implements DraftSchema<D> {

    @NonNull
    private final SchemaLocation location;

    private final Map<KeywordInfo<?>, SchemaKeyword> keywords;
    private final JsonSchemaVersion version;

    public JsonSchemaImpl(SchemaLocation location, Map<KeywordInfo<?>, SchemaKeyword> keywords, JsonSchemaVersion version) {
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
            if (keyword.getTypeVariant().contains(version)) {
                keywordValue.writeToGenerator(keyword, schemaGenerator, version);
            } else {
                log.warn("Keyword {} does not apply to version: [{}], only for {}", keyword.key(), version, keyword.getTypeVariant());
            }
        });

        generator.writeEnd();
        return generator;
    }

    @Override
    public Map<KeywordInfo<?>, SchemaKeyword> getKeywords() {
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
        return uri(Keywords.$ID);
    }

    @Override
    public URI getSchemaURI() {
        return uri(Keywords.$SCHEMA);
    }

    @Override
    public String getTitle() {
        return string(Keywords.TITLE);
    }

    @Override
    public String getDescription() {
        return string(Keywords.DESCRIPTION);
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
        return toString(false, Draft6);
    }

    // ######################################################
    // ###### Helper methods for subclasses (accessing keywords) ########
    // ######################################################

    protected Map<KeywordInfo<?>, SchemaKeyword> keywords() {
        return keywords;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected URI uri(KeywordInfo<URIKeyword> keywordType) {
        return keywordValue(keywordType).orElse(null);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected String string(KeywordInfo<StringKeyword> stringKeyword) {
        checkNotNull(stringKeyword, "stringKeyword must not be null");
        return keywordValue(stringKeyword).orElse(null);
    }

    @SuppressWarnings("unchecked")
    protected <X extends SchemaKeyword> Optional<X> keyword(KeywordInfo<X> keyword) {
        checkNotNull(keyword, "keyword must not be null");
        return Optional.ofNullable((X) keywords.get(keyword));
    }

    protected List<Schema> schemaList(KeywordInfo<? extends SchemaListKeyword> keyword) {
        checkNotNull(keyword, "keyword must not be null");
        return keyword(keyword).map(SchemaListKeyword::getSchemas).orElse(Collections.emptyList());
    }

    protected Map<String, Schema> schemaMap(KeywordInfo<SchemaMapKeyword> keyword) {
        checkNotNull(keyword, "keyword must not be null");
        return keyword(keyword).map(SchemaMapKeyword::getSchemas).orElse(Collections.emptyMap());
    }

    @SuppressWarnings("unchecked")
    protected <X> Optional<X> keywordValue(KeywordInfo<? extends SchemaKeywordImpl<X>> keyword) {
        checkNotNull(keyword, "keyword must not be null");
        final SchemaKeywordImpl<X> keywordValue = (SchemaKeywordImpl<X>) keywords.get(keyword);
        return keywordValue == null ? Optional.empty() : Optional.ofNullable(keywordValue.getKeywordValue());
    }

    protected JsonArray examples() {
        return keyword(Keywords.EXAMPLES)
                .map(JsonArrayKeyword::getKeywordValue)
                .orElse(JsonUtils.emptyJsonArray());
    }

    protected Map<String, Schema> definitions() {
        return schemaMap(Keywords.DEFINITIONS);
    }

    protected Set<JsonSchemaType> types() {
        return keyword(Keywords.TYPE)
                .map(TypeKeyword::getTypes)
                .orElse(Collections.emptySet());
    }

    protected Optional<JsonArray> enumValues() {
        return keywordValue(Keywords.ENUM);
    }

    protected Optional<JsonValue> defaultValue() {
        return keywordValue(Keywords.DEFAULT);
    }

    protected Optional<Schema> notSchema() {
        return keywordValue(Keywords.NOT);
    }

    protected Optional<JsonValue> constValue() {
        return keywordValue(Keywords.CONST);
    }

    protected List<Schema> allOfSchemas() {
        return schemaList(Keywords.ALL_OF);
    }

    protected List<Schema> anyOfSchemas() {
        return schemaList(Keywords.ANY_OF);
    }

    protected List<Schema> oneOfSchemas() {
        return schemaList(Keywords.ONE_OF);
    }

    protected String format() {
        return string(Keywords.FORMAT);
    }

    protected Integer minLength() {
        return keywordValue(Keywords.MIN_LENGTH)
                .map(Number::intValue)
                .orElse(null);
    }

    protected Integer maxLength() {
        return keywordValue(Keywords.MAX_LENGTH)
                .map(Number::intValue)
                .orElse(null);
    }

    protected String pattern() {
        return string(Keywords.PATTERN);
    }

    protected Number multipleOf() {
        return keywordValue(Keywords.MULTIPLE_OF).orElse(null);
    }

    protected Number maximum() {
        return keyword(Keywords.MAXIMUM).map(LimitKeyword::getLimit).orElse(null);
    }

    protected Number minimum() {
        return keyword(Keywords.MINIMUM).map(LimitKeyword::getLimit).orElse(null);
    }

    protected Number exclusiveMinimum() {
        return keyword(Keywords.MINIMUM).map(LimitKeyword::getExclusiveLimit).orElse(null);
    }

    protected Number exclusiveMaximum() {
        return keyword(Keywords.MAXIMUM).map(LimitKeyword::getExclusiveLimit).orElse(null);
    }

    protected Integer minItems() {
        return getInteger(Keywords.MIN_ITEMS);
    }

    protected Integer getInteger(KeywordInfo<NumberKeyword> keyword) {
        checkNotNull(keyword, "keyword must not be null");
        return keywordValue(keyword).map(Number::intValue).orElse(null);
    }

    protected Integer maxItems() {
        return getInteger(Keywords.MAX_ITEMS);
    }

    protected Optional<Schema> allItemSchema() {
        return keyword(Keywords.ITEMS)
                .flatMap(ItemsKeyword::getAllItemSchema);
    }

    protected List<Schema> itemSchemas() {
        return keyword(Keywords.ITEMS)
                .map(ItemsKeyword::getIndexedSchemas)
                .orElse(Collections.emptyList());
    }

    protected Optional<Schema> additionalItemsSchema() {
        return keyword(Keywords.ITEMS).flatMap(ItemsKeyword::getAdditionalItemSchema);
    }

    protected Optional<Schema> containsSchema() {
        return keywordValue(Keywords.CONTAINS);
    }

    protected boolean uniqueItems() {
        return keywordValue(Keywords.UNIQUE_ITEMS).orElse(false);
    }

    protected Map<String, Schema> properties() {
        return schemaMap(Keywords.PROPERTIES);
    }

    protected Map<String, Schema> patternProperties() {
        return schemaMap(Keywords.PATTERN_PROPERTIES);
    }

    protected Optional<Schema> additionalPropertiesSchema() {
        return keywordValue(Keywords.ADDITIONAL_PROPERTIES);
    }

    protected Optional<Schema> propertyNameSchema() {
        return keywordValue(Keywords.PROPERTY_NAMES);
    }

    protected SetMultimap<String, String> propertyDependencies() {
        return keyword(Keywords.DEPENDENCIES)
                .map(DependenciesKeyword::getPropertyDependencies)
                .orElse(ImmutableSetMultimap.of());
    }

    protected Map<String, Schema> propertySchemaDependencies() {
        return keyword(Keywords.DEPENDENCIES)
                .map(DependenciesKeyword::getDependencySchemas)
                .map(SchemaMapKeyword::getSchemas)
                .orElse(ImmutableMap.of());
    }

    protected Integer maxProperties() {
        return getInteger(Keywords.MAX_PROPERTIES);
    }

    protected Integer minProperties() {
        return getInteger(Keywords.MIN_PROPERTIES);
    }

    protected Set<String> requiredProperties() {
        return keyword(Keywords.REQUIRED)
                .map(StringSetKeyword::getKeywordValue)
                .orElse(Collections.emptySet());
    }
}
