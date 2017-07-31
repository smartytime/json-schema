package io.dugnutt.jsonschema.six;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.experimental.Delegate;

import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGenerator;
import java.io.StringWriter;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.six.enums.JsonSchemaKeyword.$ID;
import static io.dugnutt.jsonschema.six.enums.JsonSchemaKeyword.ALL_OF;
import static io.dugnutt.jsonschema.six.enums.JsonSchemaKeyword.ANY_OF;
import static io.dugnutt.jsonschema.six.enums.JsonSchemaKeyword.CONST;
import static io.dugnutt.jsonschema.six.enums.JsonSchemaKeyword.DEFAULT;
import static io.dugnutt.jsonschema.six.enums.JsonSchemaKeyword.DESCRIPTION;
import static io.dugnutt.jsonschema.six.enums.JsonSchemaKeyword.ENUM;
import static io.dugnutt.jsonschema.six.enums.JsonSchemaKeyword.NOT;
import static io.dugnutt.jsonschema.six.enums.JsonSchemaKeyword.ONE_OF;
import static io.dugnutt.jsonschema.six.enums.JsonSchemaKeyword.TITLE;
import static io.dugnutt.jsonschema.six.enums.JsonSchemaKeyword.TYPE;
import static io.dugnutt.jsonschema.utils.JsonUtils.prettyPrintGeneratorFactory;

@EqualsAndHashCode(of = "details")
public class JsonSchema implements Schema {

    @NonNull
    private final SchemaLocation location;

    @NonNull
    @Delegate
    private final JsonSchemaDetails details;

    JsonSchema(SchemaLocation location, JsonSchemaDetails details) {
        this.location = checkNotNull(location, "location must not be null");
        this.details = checkNotNull(details);
    }

    @Override
    public SchemaLocation getLocation() {
        return location;
    }

    @Override
    public JsonSchemaGenerator toJson(final JsonSchemaGenerator writer) {
        writer.object();
        writer.optionalWrite($ID, getId());

        writer.optionalWrite(TITLE, getTitle());
        writer.optionalWrite(DESCRIPTION, getDescription());
        getDefaultValue().ifPresent(defValue -> writer.write(DEFAULT, defValue));
        getEnumValues().ifPresent(writer.jsonValueWriter(ENUM));
        getConstValue().ifPresent(writer.jsonValueWriter(CONST));
        if (getTypes().size() > 1) {
            writer.writeKey(TYPE);
            writer.array();
            getTypes().forEach(type -> writer.write(type.toString()));
            writer.endArray();
        } else if (getTypes().size() == 1) {
            writer.write(TYPE, getTypes().iterator().next().toString());
        }

        getNotSchema().ifPresent(writer.schemaWriter(NOT));
        writer.writeSchemas(ALL_OF, getAllOfSchemas());
        writer.writeSchemas(ANY_OF, getAnyOfSchemas());
        writer.writeSchemas(ONE_OF, getOneOfSchemas());

        if (hasObjectKeywords()) {
            writer.write(getObjectKeywords());
        }
        if (hasNumberKeywords()) {
            writer.write(getNumberKeywords());
        }
        if (hasStringKeywords()) {
            writer.write(getStringKeywords());
        }
        if (hasArrayKeywords()) {
            writer.write(getArrayKeywords());
        }

        writer.endObject();
        return writer;
    }

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
        this.toJson(new JsonSchemaGenerator(generator));
        generator.flush();
        return stringWriter.toString();
    }

}
