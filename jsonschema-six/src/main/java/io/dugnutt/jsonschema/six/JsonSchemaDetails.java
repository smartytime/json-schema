package io.dugnutt.jsonschema.six;

import io.dugnutt.jsonschema.six.enums.JsonSchemaType;
import io.dugnutt.jsonschema.six.keywords.ArrayKeywords;
import io.dugnutt.jsonschema.six.keywords.NumberKeywords;
import io.dugnutt.jsonschema.six.keywords.ObjectKeywords;
import io.dugnutt.jsonschema.six.keywords.StringKeywords;
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
     * {@see io.dugnutt.jsonschema.six.schema.JsonSchemaKeyword.TYPE}
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

    public Optional<StringKeywords> getStringKeywords() {
        return Optional.ofNullable(stringKeywords);
    }

    public Optional<NumberKeywords> getNumberKeywords() {
        return Optional.ofNullable(numberKeywords);
    }

    public Optional<ObjectKeywords> getObjectKeywords() {
        return Optional.ofNullable(objectKeywords);
    }

    public Optional<ArrayKeywords> getArrayKeywords() {
        return Optional.ofNullable(arrayKeywords);
    }

}
