package io.sbsp.jsonschema.keyword;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import io.sbsp.jsonschema.enums.JsonSchemaKeywordType;
import io.sbsp.jsonschema.enums.JsonSchemaType;
import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;

import javax.annotation.Nullable;
import javax.json.JsonValue;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.sbsp.jsonschema.enums.JsonSchemaVersion.Draft3;
import static io.sbsp.jsonschema.enums.JsonSchemaVersion.Draft6;
import static java.util.Collections.unmodifiableSet;

@Getter
@EqualsAndHashCode(of = "key")
public class KeywordMetadata<K extends SchemaKeyword> {

    @NonNull
    private final String key;

    @Singular
    private final Set<JsonSchemaVersion> appliesToVersions;

    @Singular
    private final Set<JsonSchemaType> forSchemas;

    @Singular
    private final Set<JsonValue.ValueType> expects;

    @Builder(builderMethodName = "keywordMetadata")
    KeywordMetadata(@Nullable String key, @Singular Collection<JsonSchemaType> forSchemas, JsonSchemaVersion since, JsonSchemaVersion until,
                    @Singular("expectType") Collection<JsonValue.ValueType> expectsOneOf) {
        since = MoreObjects.firstNonNull(since, Draft3);
        until = MoreObjects.firstNonNull(until, Draft6);
        if (forSchemas == null) {
            this.forSchemas = unmodifiableSet(EnumSet.allOf(JsonSchemaType.class));
        } else {
            this.forSchemas = ImmutableSet.copyOf(forSchemas);
        }
        this.key = key;
        this.appliesToVersions = unmodifiableSet(EnumSet.range(since, until));
        this.expects = ImmutableSet.copyOf(expectsOneOf);
    }


    @Override
    public String toString() {
        return key;
    }

    public Set<JsonValue.ValueType> getApplicableTypes() {
        return forSchemas.stream().map(JsonSchemaType::appliesTo).flatMap(Collection::stream).collect(ImmutableSet.toImmutableSet());
    }


    public static class KeywordMetadataBuilder<K extends SchemaKeyword> {

        private Class<K> keywordClass;

        public KeywordMetadataBuilder<K> forAllSchemas() {
            return this;
        }

        public KeywordMetadataBuilder<K> name(String name) {
            if (this.key == null) {
                this.key = name;
            }
            return this;
        }

        public KeywordMetadataBuilder<K> key(JsonSchemaKeywordType key) {
            if (this.key == null) {
                this.key = key.key();
            }
            return this;
        }

        public KeywordMetadataBuilder<K> key(String key) {
            if (this.key == null) {
                this.key = key;
            }
            return this;
        }

        public KeywordMetadataBuilder<K> keywordClass(Class<K> keywordClass) {
            this.keywordClass = keywordClass;
            return this;
        }

        public KeywordMetadata<K> build() {
            return new KeywordMetadata<K>(key, forSchemas, since, until, expectsOneOf);
        }

        public KeywordMetadataBuilder<K> expects(JsonValue.ValueType firstType, JsonValue.ValueType... types) {
            checkNotNull(firstType, "firstType must not be null");
            checkNotNull(types, "types must not be null");
            this.expectType(firstType);
            for (JsonValue.ValueType type : types) {
                this.expectType(type);
            }
            return this;
        }

        public KeywordMetadataBuilder<K> validates(JsonSchemaType type, JsonSchemaType... more) {
            checkNotNull(type, "type must not be null");
            checkNotNull(more, "more must not be null");
            this.forSchema(type);
            for (JsonSchemaType jsonSchemaType : more) {
                this.forSchema(jsonSchemaType);
            }
            return this;
        }

        public KeywordMetadataBuilder<K> from(JsonSchemaVersion fromVersion) {
            return this.since(fromVersion);
        }

        public KeywordMetadataBuilder<K> validatesAnyType() {
            return this;
        }
    }
}
