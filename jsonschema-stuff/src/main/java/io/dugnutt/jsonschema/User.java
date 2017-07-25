package io.dugnutt.jsonschema;

import org.inferred.freebuilder.FreeBuilder;

@FreeBuilder
public interface User {

    Schema schema();

    String name();

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder();

    class Builder extends User_Builder {
    }

}
