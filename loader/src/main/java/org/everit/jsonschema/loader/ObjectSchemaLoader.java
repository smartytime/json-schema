package org.everit.jsonschema.loader;

import org.everit.jsonschema.api.ObjectSchema;
import org.everit.jsonschema.api.Schema;
import org.everit.jsonschema.api.UnexpectedValueException;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.json.JsonValue.ValueType.ARRAY;
import static javax.json.JsonValue.ValueType.FALSE;
import static javax.json.JsonValue.ValueType.OBJECT;
import static javax.json.JsonValue.ValueType.TRUE;
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
        ls.schemaJson.findInt(MIN_PROPERTIES).ifPresent(builder::minProperties);
        ls.schemaJson.findInt(MAX_PROPERTIES).ifPresent(builder::maxProperties);
        ls.schemaJson.findObject(PROPERTIES).ifPresent(propertyDefs -> populatePropertySchemas(propertyDefs, builder));
        ls.schemaJson.find(ADDITIONAL_PROPERTIES).ifPresent(rawAddProps -> {
            switch (rawAddProps.getValueType()) {
                case FALSE:
                    builder.additionalProperties(false);
                    break;
                case TRUE:
                    builder.additionalProperties(true);
                    break;
                case OBJECT:
                    Schema childSchema = defaultLoader.loadChild((JsonObject) rawAddProps).build();
                    builder.schemaOfAdditionalProperties(childSchema);
                    break;
                default:
                    throw new UnexpectedValueException(rawAddProps, TRUE, FALSE, OBJECT);
            }
        });

        ls.schemaJson.findArray(REQUIRED).ifPresent(arr -> arr.getValuesAs(JsonString.class)
                .forEach(val -> builder.addRequiredProperty(val.getString())));
        ls.schemaJson.findObject(PATTERN_PROPERTIES)
                .ifPresent(patternProps -> {
                    patternProps.keySet().forEach(pattern -> {
                        Schema patternSchema = defaultLoader.loadChild(patternProps.getJsonObject(pattern)).build();
                        builder.patternProperty(pattern, patternSchema);
                    });
                });
        ls.schemaJson.findObject(DEPENDENCIES).ifPresent(deps -> addDependencies(builder, deps));
        return builder;
    }

    private void populatePropertySchemas(JsonObject propertyDefs, ObjectSchema.Builder builder) {
        propertyDefs.forEach((key, value) -> addPropertySchemaDefinition(key, (JsonObject) value, builder));
    }

    private void addPropertySchemaDefinition(String keyOfObj, JsonObject definition, ObjectSchema.Builder builder) {
        builder.addPropertySchema(keyOfObj, defaultLoader.loadChild(definition).build());
    }

    private void addDependencies(ObjectSchema.Builder builder, JsonObject deps) {
        deps.forEach((ifPresent, mustBePresent) -> addDependency(builder, ifPresent, mustBePresent));
    }

    private void addDependency(ObjectSchema.Builder builder, String ifPresent, JsonValue deps) {
        switch (deps.getValueType()) {
            case OBJECT:
                builder.schemaDependency(ifPresent, defaultLoader.loadChild((JsonObject) deps).build());
                break;
            case ARRAY:
                ((JsonArray)deps).getValuesAs(JsonString.class)
                        .forEach(entry -> builder.propertyDependency(ifPresent, entry.getString()));
                break;
            default:
                throw new UnexpectedValueException(deps, OBJECT, ARRAY);
        }
    }
}
