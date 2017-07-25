package io.dugnutt.jsonschema;

import org.inferred.freebuilder.FreeBuilder;

@FreeBuilder
public interface Schema {
    String getLocation();

    String getName();

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder();

    class Builder extends Schema_Builder {

    }
}
