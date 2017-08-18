package io.sbsp.jsonschema.keyword;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import io.sbsp.jsonschema.enums.JsonSchemaType;
import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;

import javax.annotation.Nullable;
import javax.json.JsonValue.ValueType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableSet;

/**
 * Represents a single keyword and it's applicable versions, and acceptable input types.
 * <p>
 * This class is used when loading schemas to validate that the input is of the correct type,
 * and in some cases, simplify the loading of simple values.
 *
 *
 * @param <K> The type of value this keyword produces (a schema, a string, an array, etc)
 */
@EqualsAndHashCode(of = {"key", "expects"})
public class KeywordInfo<K extends SchemaKeyword> {

    @NonNull
    private final String key;

    /**
     * Which versions this keyword applies to.  eg. additionalProperties expects a boolean or an object
     * up until Draft6, when it requires a schema.
     */
    @Singular
    private final EnumSet<JsonSchemaVersion> versions;

    /**
     * The most recent version this configuration applies to.
     */
    private final JsonSchemaVersion mostRecentVersion;

    /**
     * Which types of values this keyword applies to: string, boolean, object, array
     */
    @Singular
    private final Set<JsonSchemaType> forSchemas;

    /**
     * Which json types (correlates to {@link #forSchemas}
     */
    private final Set<ValueType> forJsonTypes;

    /**
     * The type of json value expected for this keyword.  Each instance of the keyword can only consuem
     * a single type of value, but they can be linked together using variants.
     */
    private final ValueType expects;

    @Singular
    @Getter
    private final Map<ValueType, KeywordInfo<K>> variants;

    @Builder(builderMethodName = "keywordInfo", builderClassName = "KeywordInfoBuilder")
    KeywordInfo(KeywordVersionInfoBuilder<K> mainInfo, List<KeywordVersionInfoBuilder<K>> allVersions) {
        checkNotNull(mainInfo, "mainInfo must not be null");
        checkNotNull(allVersions, "versions must not be null");

        final KeywordInfo<K> mainDefinition = mainInfo.build();
        // Copy values from most current builder
        this.key = mainDefinition.key;
        this.forSchemas = mainDefinition.forSchemas;
        this.forJsonTypes = mainDefinition.forJsonTypes;
        this.expects = mainDefinition.expects;
        this.versions = mainDefinition.versions;
        this.mostRecentVersion = mainDefinition.mostRecentVersion;

        this.variants = allVersions.stream()
                .map(mainDefinition::copyDefaults)
                .map(KeywordVersionInfoBuilder::build)
                .collect(Maps.toImmutableEnumMap(
                        KeywordInfo::getExpects,
                        Function.identity()));
    }

    @Builder(builderMethodName = "version", builderClassName = "KeywordVersionInfoBuilder")
    KeywordInfo(@Nullable String key,
                @Singular Collection<JsonSchemaType> forSchemas,
                ValueType expects,
                JsonSchemaVersion since,
                JsonSchemaVersion until) {

        since = MoreObjects.firstNonNull(since, JsonSchemaVersion.Draft3);
        this.mostRecentVersion = MoreObjects.firstNonNull(until, JsonSchemaVersion.latest());
        if (forSchemas.isEmpty()) {
            this.forSchemas = unmodifiableSet(EnumSet.allOf(JsonSchemaType.class));
        } else {
            this.forSchemas = ImmutableSet.copyOf(forSchemas);
        }
        this.forJsonTypes = this.forSchemas.stream()
                .map(JsonSchemaType::appliesTo)
                .flatMap(Collection::stream)
                .collect(ImmutableSet.toImmutableSet());
        this.key = key;
        this.expects = expects;
        this.versions = EnumSet.range(since, mostRecentVersion);
        this.variants = emptyMap();
    }

    public ValueType getExpects() {
        return expects;
    }

    public EnumSet<JsonSchemaVersion> getTypeVariant() {
        return versions;
    }

    public JsonSchemaVersion getMostRecentVersion() {
        return mostRecentVersion;
    }

    public String key() {
        return this.key;
    }

    public Optional<KeywordInfo<K>> getTypeVariant(ValueType valueType) {
        return Optional.ofNullable(variants.get(valueType));
    }

    /**
     * Returns a sublist of variants on this keyword.  Used for digesters operating on older versions of the spec.
     * @param types List of types we're looking for
     * @return A list
     */
    public List<KeywordInfo<K>> getTypeVariants(ValueType... types) {
        final ImmutableList<KeywordInfo<K>> versionsForType = Arrays.stream(types)
                .filter(variants::containsKey)
                .map(variants::get)
                .collect(ImmutableList.toImmutableList());
        checkState(versionsForType.size() > 0, format("[%s] not valid type for [%s]", key(), Arrays.toString(types)));
        return versionsForType;
    }

    private KeywordVersionInfoBuilder<K> copyDefaults(KeywordVersionInfoBuilder<K> builder) {
        builder.setKeyIfBlank(key);
        if (builder.forSchemas == null && !this.forSchemas.isEmpty()) {
            builder.forSchemas(this.forSchemas);
        }
        return builder;
    }

    public Set<ValueType> getApplicableTypes() {
        return forJsonTypes;
    }

    @Override
    public String toString() {
        return key;
    }

    public static class KeywordInfoBuilder<K extends SchemaKeyword> {

        private final KeywordVersionInfoBuilder<K> main;
        private KeywordVersionInfoBuilder<K> current;
        private List<KeywordVersionInfoBuilder<K>> versions;

        public KeywordInfoBuilder() {
            this.current = new KeywordVersionInfoBuilder<K>();
            this.main = current;
            versions = new ArrayList<>();
        }

        public KeywordInfo<K> build() {
            this.versions.add(current);
            return new KeywordInfo<K>(main, versions);
        }

        public KeywordInfoBuilder<K> onlyForVersion(JsonSchemaVersion version) {
            current.onlyForVersion(version);
            return this;
        }

        public KeywordInfoBuilder<K> additionalDefinition() {
            this.versions.add(current);
            current = new KeywordVersionInfoBuilder<K>();
            return this;
        }

        public KeywordInfoBuilder<K> until(JsonSchemaVersion until) {
            current.until(until);
            return this;
        }

        public KeywordInfoBuilder<K> since(JsonSchemaVersion since) {
            current.since(since);
            return this;
        }

        public KeywordInfoBuilder<K> key(String key) {
            current.key(key);
            return this;
        }

        public KeywordInfoBuilder<K> expects(ValueType firstType) {
            current.expects(firstType);
            return this;
        }

        public KeywordInfoBuilder<K> validates(JsonSchemaType type, JsonSchemaType... more) {
            current.validates(type, more);
            return this;
        }

        public KeywordInfoBuilder<K> from(JsonSchemaVersion fromVersion) {
            current.from(fromVersion);
            return this;
        }
    }

    public static class KeywordVersionInfoBuilder<K extends SchemaKeyword> {

        public KeywordVersionInfoBuilder<K> onlyForVersion(JsonSchemaVersion version) {
            checkNotNull(version, "version must not be null");
            from(version).until(version);
            return this;
        }

        public KeywordVersionInfoBuilder<K> setKeyIfBlank(String key) {
            if (this.key == null) {
                this.key = key;
            }
            return this;
        }

        // private EnumMap<JsonSchemaVersion, KeywordInfoBuilder<K>> variants = new EnumMap<>(JsonSchemaVersion.class);
        //
        // public KeywordInfoBuilder<K> forAllSchemas() {
        //     return this;
        // }
        //
        // public KeywordInfoBuilder<K> name(String name) {
        //     if (this.key == null) {
        //         this.key = name;
        //     }
        //     return this;
        // }
        //
        // public KeywordInfoBuilder<K> key(String key) {
        //     if (this.key == null) {
        //         this.key = key;
        //     }
        //     return this;
        // }
        //
        // public KeywordInfoBuilder<K> keywordClass(Class<K> keywordClass) {
        //     this.keywordClass = keywordClass;
        //     return this;
        // }
        //
        // public KeywordInfoBuilder<K> since(JsonSchemaVersion version, KeywordInfoBuilder<K> variant) {
        //     this.variants.put(version, variant);
        //     return this;
        // }
        //
        // public KeywordInfoBuilder<K> modified(JsonSchemaVersion version, KeywordInfoBuilder<K> variant) {
        //     this.variants.put(version, variant);
        //     return this;
        // }
        //
        // public KeywordInfoBuilder<K> since(JsonSchemaVersion version) {
        //     this.variants.put(version, new KeywordInfoBuilder<K>());
        //     return this;
        // }
        //
        // public KeywordInfo<K> build() {
        //     return new KeywordInfo<K>(key, forSchemas, expectsOneOf, variants);
        // }
        //
        public KeywordVersionInfoBuilder<K> expects(ValueType firstType) {
            checkNotNull(firstType, "firstType must not be null");
            this.expects = firstType;
            return this;
        }

        public KeywordVersionInfoBuilder<K> validates(JsonSchemaType type, JsonSchemaType... more) {
            checkNotNull(type, "type must not be null");
            checkNotNull(more, "more must not be null");
            this.forSchema(type);
            for (JsonSchemaType jsonSchemaType : more) {
                this.forSchema(jsonSchemaType);
            }
            return this;
        }

        public KeywordVersionInfoBuilder<K> from(JsonSchemaVersion fromVersion) {
            return this.since(fromVersion);
        }
        //
        // public KeywordInfoBuilder<K> validatesAnyType() {
        //     return this;
        // }
    }
}
