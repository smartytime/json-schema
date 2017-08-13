package io.sbsp.jsonschema.impl;

import io.sbsp.jsonschema.Draft6Schema;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaLocation;
import lombok.experimental.Delegate;

import java.net.URI;

class Draft6RefSchemaImpl extends RefSchemaImpl implements Draft6Schema {

    @Delegate(excludes = {Schema.class})
    private final Draft6Schema draft6;

    Draft6RefSchemaImpl(SchemaLocation location, URI refURI, Draft6Schema draft6) {
        super(location, refURI, draft6);
        this.draft6 = draft6;
    }
}
