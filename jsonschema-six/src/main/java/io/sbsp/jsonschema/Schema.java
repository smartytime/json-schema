package io.sbsp.jsonschema;

import io.sbsp.jsonschema.builder.JsonSchemaBuilder;
import lombok.ToString;

import javax.annotation.Nullable;
import javax.json.stream.JsonGenerator;
import java.net.URI;

import static com.google.common.base.Preconditions.checkNotNull;

public interface Schema {

    SchemaLocation getLocation();

    URI getId();

    String getTitle();

    String getDescription();

    JsonGenerator toJson(final JsonGenerator writer);

    default URI getAbsoluteURI() {
        return getLocation().getUniqueURI();
    }

    default URI getPointerFragmentURI() {
        return getLocation().getJsonPointerFragment();
    }

    static JsonSchemaBuilder jsonSchemaBuilder() {
        return new JsonSchemaBuilder();
    }

    static JsonSchemaBuilder jsonSchemaBuilder(SchemaLocation location) {
        return new JsonSchemaBuilder(location);
    }

    static JsonSchemaBuilder jsonSchemaBuilderWithId(SchemaLocation location, String id) {
        return new JsonSchemaBuilder(location, URI.create(id));
    }

    static JsonSchemaBuilder refSchemaBuilder(URI ref, SchemaLocation location, @Nullable SchemaFactory schemaFactory) {
        return jsonSchemaBuilder(location).ref(ref, schemaFactory);
    }

    static JsonSchemaBuilder jsonSchemaBuilderWithId(String id) {
        checkNotNull(id, "id must not be null");
        return new JsonSchemaBuilder(URI.create(id));
    }

    static JsonSchemaBuilder jsonSchemaBuilderWithId(URI id) {
        checkNotNull(id, "id must not be null");
        return new JsonSchemaBuilder(id);
    }

    @ToString

}
