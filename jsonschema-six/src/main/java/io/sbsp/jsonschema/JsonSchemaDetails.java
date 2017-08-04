package io.sbsp.jsonschema;

import com.google.common.base.Objects;
import io.sbsp.jsonschema.enums.JsonSchemaType;
import io.sbsp.jsonschema.keywords.ArrayKeywords;
import io.sbsp.jsonschema.keywords.NumberKeywords;
import io.sbsp.jsonschema.keywords.ObjectKeywords;
import io.sbsp.jsonschema.keywords.StringKeywords;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;

import javax.annotation.Nullable;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.MoreObjects.firstNonNull;

@Builder
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class JsonSchemaDetails {

    @Nullable
    private final URI id;

    /**
     * {@see TITLE}
     */
    @Nullable
    private final String title;

    /**
     * {@see DESCRIPTION}
     */

    @Nullable
    private final String description;

    @Nullable
    private JsonValue defaultValue;

    /**
     * {@see ALL_OF}
     */

    @NotNull
    @Singular
    private final List<Schema> allOfSchemas;

    /**
     * {@see ANY_OF}
     */

    @NotNull
    @Singular
    private final List<Schema> anyOfSchemas;

    /**
     * {@see ONE_OF}
     */

    @NotNull
    @Singular
    private final List<Schema> oneOfSchemas;

    /**
     * {@see io.sbsp.jsonschema.six.schema.JsonSchemaKeyword.TYPE}
     */
    @NotNull
    @Singular
    private final Set<JsonSchemaType> types;

    /**
     * {@see NOT}
     */
    private final Schema notSchema;

    /**
     * {@see ENUM}
     */
    private final JsonArray enumValues;

    /**
     * {@see CONST}
     */
    private final JsonValue constValue;
    private final StringKeywords stringKeywords;
    private final NumberKeywords numberKeywords;
    private final ObjectKeywords objectKeywords;
    private final ArrayKeywords arrayKeywords;

    public Optional<JsonValue> getDefaultValue() {
        return Optional.ofNullable(defaultValue);
    }

    public boolean hasObjectKeywords() {
        return objectKeywords != null;
    }

    public boolean hasArrayKeywords() {
        return arrayKeywords != null;
    }

    public boolean hasNumberKeywords() {
        return numberKeywords != null;
    }

    public boolean hasStringKeywords() {
        return stringKeywords != null;
    }

    public Optional<Schema> getNotSchema() {
        return Optional.ofNullable(notSchema);
    }

    public Optional<JsonArray> getEnumValues() {
        return Optional.ofNullable(enumValues);
    }

    public Optional<JsonValue> getConstValue() {
        return Optional.ofNullable(constValue);
    }

    public StringKeywords getStringKeywords() {
        return firstNonNull(stringKeywords, StringKeywords.blankStringKeywords());
    }

    public NumberKeywords getNumberKeywords() {
        return firstNonNull(numberKeywords, NumberKeywords.blankNumberKeywords());
    }

    public ObjectKeywords getObjectKeywords() {
        return firstNonNull(objectKeywords, ObjectKeywords.getBlankObjectKeywords());
    }

    public ArrayKeywords getArrayKeywords() {
        return firstNonNull(arrayKeywords, ArrayKeywords.getBlankArrayKeywords());
    }

    public static class JsonSchemaDetailsBuilder {

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final JsonSchemaDetailsBuilder that = (JsonSchemaDetailsBuilder) o;
            return Objects.equal(title, that.title) &&
                    Objects.equal(description, that.description) &&
                    Objects.equal(defaultValue, that.defaultValue) &&
                    Objects.equal(allOfSchemas, that.allOfSchemas) &&
                    Objects.equal(anyOfSchemas, that.anyOfSchemas) &&
                    Objects.equal(oneOfSchemas, that.oneOfSchemas) &&
                    Objects.equal(types, that.types) &&
                    Objects.equal(notSchema, that.notSchema) &&
                    Objects.equal(enumValues, that.enumValues) &&
                    Objects.equal(constValue, that.constValue) &&
                    Objects.equal(stringKeywords, that.stringKeywords) &&
                    Objects.equal(numberKeywords, that.numberKeywords) &&
                    Objects.equal(objectKeywords, that.objectKeywords) &&
                    Objects.equal(arrayKeywords, that.arrayKeywords);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(title, description, defaultValue, allOfSchemas, anyOfSchemas, oneOfSchemas, types,
                    notSchema, enumValues, constValue, stringKeywords, numberKeywords, objectKeywords, arrayKeywords);
        }
    }

}
