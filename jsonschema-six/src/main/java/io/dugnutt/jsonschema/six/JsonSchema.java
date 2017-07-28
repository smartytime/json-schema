package io.dugnutt.jsonschema.six;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.experimental.Delegate;

import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGenerator;
import java.io.StringWriter;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ALL_OF;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ANY_OF;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.CONST;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.DEFAULT;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ENUM;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.NOT;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ONE_OF;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.TYPE;

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
        if (!"#".equals(location.getAbsoluteURI().toString())) {
            writer.optionalWrite(JsonSchemaKeyword.$ID, getId());
        }

        writer.optionalWrite(JsonSchemaKeyword.TITLE, getTitle());
        writer.optionalWrite(JsonSchemaKeyword.DESCRIPTION, getDescription());
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

        getObjectKeywords().ifPresent(writer::write);
        getStringKeywords().ifPresent(writer::write);
        getArrayKeywords().ifPresent(writer::write);
        getNumberKeywords().ifPresent(writer::write);
        writer.endObject();
        return writer;
    }

    @Override
    public String toString() {
        final JsonProvider provider = JsonProvider.provider();
        final StringWriter stringWriter = new StringWriter();
        final JsonGenerator generator = provider.createGenerator(stringWriter);
        toJson(new JsonSchemaGenerator(generator));
        generator.flush();
        return stringWriter.toString();
    }


}
