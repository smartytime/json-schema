package io.dugnutt.jsonschema.six;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;

import javax.annotation.Nullable;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Builder
@AllArgsConstructor
@Getter
public class JsonSchemaDetails {

    public static final JsonSchemaDetails BLANK_DETAILS = builder().build();

    @Nullable
    private final String id;

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

    /**
     * {@see ALL_OF}
     */

    @NotNull
    private final List<JsonSchema> allOfSchemas;

    /**
     * {@see ANY_OF}
     */

    @NotNull
    private final List<JsonSchema> anyOfSchemas;

    /**
     * {@see ONE_OF}
     */

    @NotNull
    private final List<JsonSchema> oneOfSchemas;

    /**
     * {@see io.dugnutt.jsonschema.six.schema.JsonSchemaKeyword.TYPE}
     */
    @NotNull
    @Singular
    private final Set<JsonSchemaType> types;

    /**
     * {@see NOT}
     */
    @NonNull
    private final Optional<JsonSchema> notSchema;

    /**
     * {@see ENUM}
     */
    @NonNull
    private final Optional<JsonArray> enumValues;

    /**
     * {@see CONST}
     */
    @NonNull
    private final Optional<JsonValue> constValue;

    @NonNull
    private final Optional<StringKeywords> stringKeywords;

    @NonNull
    private final Optional<NumberKeywords> numberKeywords;

    @NonNull
    private final Optional<ObjectKeywords> objectKeywords;

    @NonNull
    private final Optional<ArrayKeywords> arrayKeywords;

    public static class JsonSchemaDetailsBuilder {
        public JsonSchemaDetailsBuilder constValue(JsonValue d) {
            this.constValue = Optional.ofNullable(d);
            return this;
        }

        public JsonSchemaDetailsBuilder enumValues(JsonArray array) {
            this.enumValues = Optional.ofNullable(array);
            return this;
        }
    }
}
