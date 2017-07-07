package org.everit.jsonschema.loader;

import org.everit.jsonschema.api.CombinedSchema;
import org.everit.jsonschema.api.JsonSchemaProperty;
import org.everit.jsonschema.api.Schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

/**
 * @author erosb
 */
class CombinedSchemaLoader {

    /**
     * Alias for {@code Function<Collection<Schema>, CombinedSchema.Builder>}.
     */
    @FunctionalInterface
    private interface CombinedSchemaProvider
            extends Function<Collection<Schema>, CombinedSchema.Builder> {

    }

    private static final Map<JsonSchemaProperty, CombinedSchemaProvider> COMB_SCHEMA_PROVIDERS = new HashMap<>(3);

    static {
        COMB_SCHEMA_PROVIDERS.put(JsonSchemaProperty.ALL_OF, CombinedSchema::allOf);
        COMB_SCHEMA_PROVIDERS.put(JsonSchemaProperty.ANY_OF, CombinedSchema::anyOf);
        COMB_SCHEMA_PROVIDERS.put(JsonSchemaProperty.ONE_OF, CombinedSchema::oneOf);
    }

    private final LoadingState ls;

    private final SchemaLoader defaultLoader;

    public CombinedSchemaLoader(LoadingState ls, SchemaLoader defaultLoader) {
        this.ls = requireNonNull(ls, "ls cannot be null");
        this.defaultLoader = requireNonNull(defaultLoader, "defaultLoader cannot be null");
    }

    public Optional<Schema.Builder<?>> load() {
        List<JsonSchemaProperty> presentKeys = COMB_SCHEMA_PROVIDERS.keySet().stream()
                .filter(ls.schemaJson::has)
                .collect(Collectors.toList());
        if (presentKeys.size() > 1) {
            throw ls.createSchemaException(format("expected at most 1 of 'allOf', 'anyOf', 'oneOf', %d found", presentKeys.size()));
        } else if (presentKeys.size() == 1) {
            JsonSchemaProperty key = presentKeys.get(0);
            Collection<Schema> subschemas = new ArrayList<>();
            ls.schemaJson.get(key).asArray()
                    .forEach(subschema -> {
                        subschemas.add(defaultLoader.loadChild(subschema.asObject()).build());
                    });
            CombinedSchema.Builder combinedSchema = COMB_SCHEMA_PROVIDERS.get(key).apply(
                    subschemas);
            Schema.Builder<?> baseSchema;
            if (ls.schemaJson.has(JsonSchemaProperty.TYPE)) {
                baseSchema = defaultLoader.loadForType(ls.schemaJson.get(JsonSchemaProperty.TYPE));
            } else {
                baseSchema = defaultLoader.sniffSchemaByProps();
            }
            if (baseSchema == null) {
                return Optional.of(combinedSchema);
            } else {
                return Optional.of(CombinedSchema.allOf(asList(baseSchema.build(),
                        combinedSchema.build())));
            }
        } else {
            return Optional.empty();
        }
    }

}
