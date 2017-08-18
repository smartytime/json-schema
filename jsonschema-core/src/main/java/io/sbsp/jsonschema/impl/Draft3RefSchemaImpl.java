package io.sbsp.jsonschema.impl;

import io.sbsp.jsonschema.Draft3Schema;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import lombok.experimental.Delegate;

import java.net.URI;

public class Draft3RefSchemaImpl extends RefSchemaImpl implements Draft3Schema {

    @Delegate(excludes = {Schema.class})
    private final Draft3Schema draft3;

    public Draft3RefSchemaImpl(SchemaLocation location, URI refURI, Draft3Schema draft3) {
        super(location, refURI, draft3);
        this.draft3 = draft3;
    }

    @Override
    public JsonSchemaVersion getVersion() {
        return JsonSchemaVersion.Draft3;
    }
}
