package io.sbsp.jsonschema.impl;

import io.sbsp.jsonschema.Draft3Schema;
import io.sbsp.jsonschema.Draft4Schema;
import io.sbsp.jsonschema.Draft6Schema;
import io.sbsp.jsonschema.RefSchema;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.loading.SchemaFactory;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.loading.LoadingReport;
import io.sbsp.jsonschema.loading.SchemaLoader;
import lombok.Builder;

import javax.json.JsonObject;
import javax.json.stream.JsonGenerator;
import java.net.URI;

public class RefSchemaImpl extends RefSchema {

    public static RefSchemaBuilder refSchemaBuilder(URI refURI) {
        return new RefSchemaBuilder().refURI(refURI);
    }

    @Builder(builderMethodName = "refSchemaBuilder", builderClassName = "RefSchemaBuilder")
    private RefSchemaImpl(SchemaLoader factory, SchemaLocation location, URI refURI, JsonObject currentDocument, LoadingReport report) {
        super(factory, location, refURI, currentDocument, report);
    }

    protected RefSchemaImpl(SchemaLocation location, URI refURI, Schema refSchema) {
        super(location, refURI, refSchema);
    }

    @Override
    public JsonSchemaVersion getVersion() {
        return JsonSchemaVersion.Draft6;
    }

    @Override
    public JsonGenerator toJson(JsonGenerator writer, JsonSchemaVersion version) {
        return writer.writeStartObject()
                .write(Keywords.$REF.key(), getRefURI().toString())
                .writeEnd();
    }

    @Override
    public Draft6Schema asDraft6() {
        return new Draft6RefSchemaImpl(getLocation(), getRefURI(), getRefSchema().asDraft6());
    }

    @Override
    public Draft4Schema asDraft4() {
        return new Draft4RefSchemaImpl(getLocation(), getRefURI(), getRefSchema().asDraft4());
    }

    @Override
    public Draft3Schema asDraft3() {
        return new Draft3RefSchemaImpl(getLocation(), getRefURI(), getRefSchema().asDraft3());
    }

    public static class RefSchemaBuilder {}
}
