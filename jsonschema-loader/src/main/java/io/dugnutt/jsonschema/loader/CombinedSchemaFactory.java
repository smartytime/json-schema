package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.six.CombinedSchema;
import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.Schema;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * @author erosb
 */
class CombinedSchemaFactory {

    private static final Map<JsonSchemaKeyword, CombinedSchemaProvider> COMBINED_SCHEMA_PROVIDERS = new HashMap<>(3);

    static {
        COMBINED_SCHEMA_PROVIDERS.put(JsonSchemaKeyword.ALL_OF, CombinedSchema::allOf);
        COMBINED_SCHEMA_PROVIDERS.put(JsonSchemaKeyword.ANY_OF, CombinedSchema::anyOf);
        COMBINED_SCHEMA_PROVIDERS.put(JsonSchemaKeyword.ONE_OF, CombinedSchema::oneOf);
    }

    private final SchemaLoaderModel schemaModel;
    private final SchemaFactory defaultLoader;

    public CombinedSchemaFactory(SchemaLoaderModel schemaModel, SchemaFactory defaultLoader) {
        this.schemaModel = requireNonNull(schemaModel, "schemaModel cannot be null");
        this.defaultLoader = requireNonNull(defaultLoader, "defaultLoader cannot be null");
    }

    public Schema.Builder<?> combinedSchemaBuilder() {
        List<JsonSchemaKeyword> presentKeys = COMBINED_SCHEMA_PROVIDERS.keySet().stream()
                .filter(schemaModel.schemaJson::has)
                .collect(Collectors.toList());
        if (presentKeys.size() > 1) {
            throw schemaModel.createSchemaException(format("expected at most 1 of 'allOf', 'anyOf', 'oneOf', %d found", presentKeys.size()));
        } else if (presentKeys.size() == 1) {
            JsonSchemaKeyword combinedSchemaType = presentKeys.get(0);
            final List<Schema> subschemas = schemaModel.streamArrayChildSchemas(combinedSchemaType)
                    .map(defaultLoader::createSchema)
                    .collect(Collectors.toList());

            final CombinedSchemaProvider combinedSchemaProvider = COMBINED_SCHEMA_PROVIDERS.get(combinedSchemaType);
            CombinedSchema.Builder combinedSchema = combinedSchemaProvider.apply(subschemas);

            // Schema.Builder<?> baseSchema;
            //
            // if (schemaModel.has(JsonSchemaProperty.TYPE)) {
            //     baseSchema = defaultLoader.loadForType(schemaModel.schemaJson.get(JsonSchemaProperty.TYPE.key()));
            // } else {
            //     baseSchema = defaultLoader.sniffSchemaByProps();
            // }
            // if (baseSchema == null) {
            //     return combinedSchema;
            // } else {
            //     return CombinedSchema.allOf(asList(baseSchema.build(),
            //             combinedSchema.build()));
            // }
        } else {
            throw schemaModel.createSchemaException("expected at least 1 of 'allOf', 'anyOf', 'oneOf': found none");
        }
    }

    /**
     * Alias for {@code Function<Collection<Schema>, CombinedSchema.Builder>}.
     */
    @FunctionalInterface
    private interface CombinedSchemaProvider
            extends Function<Collection<Schema>, CombinedSchema.Builder> {

    }
}
