package io.sbsp.jsonschema.impl;

import io.sbsp.jsonschema.Draft4Schema;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import lombok.experimental.Delegate;

import java.net.URI;

class Draft4RefSchemaImpl extends RefSchemaImpl implements Draft4Schema {

    @Delegate(excludes = {Schema.class})
    private final Draft4Schema draft4;

    Draft4RefSchemaImpl(SchemaLocation location, URI refURI, Draft4Schema draft4) {
        super(location, refURI, draft4);
        this.draft4 = draft4;
    }

    @Override
    public JsonSchemaVersion getVersion() {
        return JsonSchemaVersion.Draft4;
    }
}
