package org.everit.jsonschema.loader;

import org.everit.json.JsonElement;
import org.everit.json.JsonObject;
import org.everit.json.UnexpectedValueException;
import org.everit.jsonschema.api.ObjectSchema;
import org.everit.jsonschema.api.Schema;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static org.everit.jsonschema.api.JsonSchemaProperty.ADDITIONAL_PROPERTIES;
import static org.everit.jsonschema.api.JsonSchemaProperty.DEPENDENCIES;
import static org.everit.jsonschema.api.JsonSchemaProperty.MAX_PROPERTIES;
import static org.everit.jsonschema.api.JsonSchemaProperty.MIN_PROPERTIES;
import static org.everit.jsonschema.api.JsonSchemaProperty.PATTERN_PROPERTIES;
import static org.everit.jsonschema.api.JsonSchemaProperty.PROPERTIES;
import static org.everit.jsonschema.api.JsonSchemaProperty.REQUIRED;

/**
 * @author erosb
 */
class ObjectSchemaLoader {

    private final LoadingState ls;

    private final SchemaLoader defaultLoader;

    public ObjectSchemaLoader(LoadingState ls, SchemaLoader defaultLoader) {
        this.ls = checkNotNull(ls, "ls cannot be null");
        this.defaultLoader = checkNotNull(defaultLoader, "defaultLoader cannot be null");
    }

    ObjectSchema.Builder load() {
        ObjectSchema.Builder builder = ObjectSchema.builder();
        ls.schemaJson.find(MIN_PROPERTIES).map(JsonElement::asInteger).ifPresent(builder::minProperties);
        ls.schemaJson.find(MAX_PROPERTIES).map(JsonElement::asInteger).ifPresent(builder::maxProperties);
        ls.schemaJson.find(PROPERTIES).map(JsonElement::asObject)
                .ifPresent(propertyDefs -> populatePropertySchemas(propertyDefs, builder));
        ls.schemaJson.find(ADDITIONAL_PROPERTIES).ifPresent(rawAddProps -> {

            switch (rawAddProps.schemaType()) {
                case Boolean:
                    builder.additionalProperties(rawAddProps.asBoolean());
                    break;
                case Object:
                    Schema childSchema = defaultLoader.loadChild(rawAddProps.asObject()).build();
                    builder.schemaOfAdditionalProperties(childSchema);
                    break;
                default:
                    String errorMessage = format("Looking for boolean or object but found: %s", rawAddProps.schemaType());
                    throw new UnexpectedValueException(errorMessage);
            }
        });

        ls.schemaJson.find(REQUIRED)
                .map(JsonElement::asArray)
                .ifPresent(arr -> arr.forEach(val -> builder.addRequiredProperty(val.asString())));
        ls.schemaJson.find(PATTERN_PROPERTIES)
                .map(JsonElement::asObject)
                .ifPresent(patternProps -> {
                    patternProps.properties().forEach(pattern -> {
                        Schema patternSchema = defaultLoader.loadChild(patternProps.git(pattern).asObject()).build();
                        builder.patternProperty(pattern, patternSchema);
                    });
                });
        ls.schemaJson.find(DEPENDENCIES).map(JsonElement::asObject)
                .ifPresent(deps -> addDependencies(builder, deps));
        return builder;
    }

    private void populatePropertySchemas(JsonObject<?> propertyDefs, ObjectSchema.Builder builder) {
        propertyDefs.forEach((key, value) -> addPropertySchemaDefinition(key, value.asObject(), builder));
    }

    private void addPropertySchemaDefinition(String keyOfObj, JsonObject<?> definition, ObjectSchema.Builder builder) {
        builder.addPropertySchema(keyOfObj, defaultLoader.loadChild(definition).build());
    }

    private void addDependencies(ObjectSchema.Builder builder, JsonObject<?> deps) {
        deps.forEach((ifPresent, mustBePresent) -> addDependency(builder, ifPresent, mustBePresent));
    }

    private void addDependency(ObjectSchema.Builder builder, String ifPresent, JsonElement<?> deps) {
        switch (deps.schemaType()) {
            case Object:
                builder.schemaDependency(ifPresent, defaultLoader.loadChild(deps.asObject()).build());
                break;
            case Array:
                deps.asArray().forEach(entry -> builder.propertyDependency(ifPresent, entry.asString()));
                break;
            default:
                String errorMessage = format("Looking for Object or Array but found: %s", deps.schemaType());
                throw new UnexpectedValueException(errorMessage);
        }
    }
}
