package io.sbsp.jsonschema;

import io.sbsp.jsonschema.enums.JsonSchemaKeywordType;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGenerator;
import java.io.StringWriter;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.$ID;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ALL_OF;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ANY_OF;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.CONST;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.DEFAULT;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.DESCRIPTION;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ENUM;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.NOT;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ONE_OF;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.TITLE;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.TYPE;
import static io.sbsp.jsonschema.utils.JsonUtils.prettyPrintGeneratorFactory;

@EqualsAndHashCode(of = "keywords")
public class JsonSchema implements Schema {

    @NonNull
    private final SchemaLocation location;

    private final Map<JsonSchemaKeywordType, SchemaKeyword> keywords;

    JsonSchema(SchemaLocation location, Map<JsonSchemaKeywordType, SchemaKeyword> keywords) {
        this.location = checkNotNull(location, "location must not be null");
        checkNotNull(keywords, "keywords must not be null");
        this.keywords = keywords;
    }

    @Override
    public SchemaLocation getLocation() {
        return location;
    }

    // @Override
    // public JsonGenerator toJson(final JsonGenerator generator) {
    //     final JsonSchemaGenerator writer = new JsonSchemaGenerator(generator);
    //     writer.object();
    //     writer.optionalWrite($ID, getId());
    //
    //     writer.optionalWrite(TITLE, getTitle());
    //     writer.optionalWrite(DESCRIPTION, getDescription());
    //     getDefaultValue().ifPresent(defValue -> writer.write(DEFAULT, defValue));
    //     getEnumValues().ifPresent(writer.jsonValueWriter(ENUM));
    //     getConstValue().ifPresent(writer.jsonValueWriter(CONST));
    //     if (getTypes().size() > 1) {
    //         writer.writeKey(TYPE);
    //         writer.array();
    //         getTypes().forEach(type -> writer.write(type.toString()));
    //         writer.endArray();
    //     } else if (getTypes().size() == 1) {
    //         writer.write(TYPE, getTypes().iterator().next().toString());
    //     }
    //
    //     getNotSchema().ifPresent(writer.schemaWriter(NOT));
    //     writer.writeSchemas(ALL_OF, getAllOfSchemas());
    //     writer.writeSchemas(ANY_OF, getAnyOfSchemas());
    //     writer.writeSchemas(ONE_OF, getOneOfSchemas());
    //
    //     if (hasObjectKeywords()) {
    //         getObjectKeywords().toJson(generator);
    //     }
    //     if (hasNumberKeywords()) {
    //         getNumberKeywords().toJson(generator);
    //     }
    //     if (hasStringKeywords()) {
    //         getStringKeywords().toJson(generator);
    //     }
    //     if (hasArrayKeywords()) {
    //         getArrayKeywords().toJson(generator);
    //     }
    //
    //     writer.endObject();
    //     return generator;
    // }

    @Override
    public String toString() {
        return toString(false);
    }

    public String toString(boolean pretty) {
        final StringWriter stringWriter = new StringWriter();
        final JsonGenerator generator;
        if (pretty) {
            generator = prettyPrintGeneratorFactory().createGenerator(stringWriter);
        } else {
            generator = JsonProvider.provider().createGenerator(stringWriter);
        }
        this.toJson(generator);
        generator.flush();
        return stringWriter.toString();
    }
}
