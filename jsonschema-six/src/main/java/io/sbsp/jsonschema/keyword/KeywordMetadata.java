package io.sbsp.jsonschema.keyword;

import com.google.common.base.MoreObjects;
import io.sbsp.jsonschema.enums.JsonSchemaType;
import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;

import javax.annotation.Nullable;
import javax.json.JsonValue;
import java.util.EnumSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.sbsp.jsonschema.enums.JsonSchemaVersion.Draft3;
import static io.sbsp.jsonschema.enums.JsonSchemaVersion.Draft6;
import static java.util.Collections.unmodifiableSet;

@Getter
@EqualsAndHashCode
@ToString
public class KeywordMetadata {
    private final String key;

    @Singular
    private final Set<JsonSchemaVersion> appliesToVersions;

    @Singular
    private final Set<JsonSchemaType> forSchemas;

    @Singular
    private final Set<JsonValue.ValueType> expects;

    @Builder(builderMethodName = "keywordMetadata")
    KeywordMetadata(@Nullable String key, @Singular Set<JsonSchemaType> forSchemas, JsonSchemaVersion since, JsonSchemaVersion until, @Singular Set<JsonValue.ValueType> expectsOneOf) {
        since = MoreObjects.firstNonNull(since, Draft3);
        until = MoreObjects.firstNonNull(until, Draft6);
        this.forSchemas = forSchemas;
        this.key = key;
        this.appliesToVersions = unmodifiableSet(EnumSet.range(since, until));
        this.expects = expectsOneOf;
    }

    public static class KeywordMetadataBuilder {

        public KeywordMetadataBuilder forAllSchemas() {
            return this;
        }

        public KeywordMetadataBuilder name(String name) {
            if (this.key == null) {
                this.key = name;
            }
            return this;
        }

        public KeywordMetadataBuilder expects(JsonValue.ValueType firstType, JsonValue.ValueType... types) {
            checkNotNull(firstType, "firstType must not be null");
            checkNotNull(types, "types must not be null");
            this.expectsOneOf(firstType);
            for (JsonValue.ValueType type : types) {
                this.expectsOneOf(type);
            }
            return this;
        }

        public KeywordMetadataBuilder validates(JsonSchemaType type, JsonSchemaType... more) {
            checkNotNull(type, "type must not be null");
            checkNotNull(more, "more must not be null");
            this.forSchema(type);
            for (JsonSchemaType jsonSchemaType : more) {
                this.forSchema(jsonSchemaType);
            }
            return this;
        }

        public KeywordMetadataBuilder from(JsonSchemaVersion fromVersion) {
            return this.since(fromVersion);
        }

        public KeywordMetadataBuilder validatesAnyType() {
            return this;
        }
    }
}
