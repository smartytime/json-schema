package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.six.ObjectSchema;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.six.UnexpectedValueException;
import lombok.experimental.var;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ADDITIONAL_PROPERTIES;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.DEPENDENCIES;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MAX_PROPERTIES;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MIN_PROPERTIES;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.PATTERN_PROPERTIES;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.PROPERTIES;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.REQUIRED;
import static javax.json.JsonValue.ValueType.ARRAY;
import static javax.json.JsonValue.ValueType.FALSE;
import static javax.json.JsonValue.ValueType.OBJECT;
import static javax.json.JsonValue.ValueType.TRUE;

/**
 * Performs a partial load of an ObjectSchema based on a
 * @author erosb
 */
class ObjectSchemaFactory {

    private final SchemaLoaderModel schemaModel;

    private final SchemaFactory schemaFactory;

    ObjectSchemaFactory(SchemaLoaderModel model, SchemaFactory schemaFactory) {
        this.schemaModel = checkNotNull(model, "model cannot be null");
        this.schemaFactory = checkNotNull(schemaFactory, "schemaFactory cannot be null");
    }

    public static ObjectSchema.Builder createObjectSchemaBuilder(SchemaLoaderModel schemaModel, SchemaFactory schemaFactory) {
        return new ObjectSchemaFactory(schemaModel, schemaFactory).createObjectSchemaBuilder();
    }

    private ObjectSchema.Builder createObjectSchemaBuilder() {
        ObjectSchema.Builder builder = ObjectSchema.builder();
        var schemaJson = schemaModel.schemaJson;
        schemaJson.findInt(MIN_PROPERTIES).ifPresent(builder::minProperties);
        schemaJson.findInt(MAX_PROPERTIES).ifPresent(builder::maxProperties);
        schemaJson.findObject(PROPERTIES).ifPresent(propertyDefs -> populatePropertySchemas(propertyDefs, builder));
        schemaJson.find(ADDITIONAL_PROPERTIES).ifPresent(addtlPropsJsonValue -> {
            switch (addtlPropsJsonValue.getValueType()) {
                case FALSE:
                    builder.additionalProperties(false);
                    break;
                case TRUE:
                    builder.additionalProperties(true);
                    break;
                case OBJECT:
                    Schema childSchema = schemaFactory.createChildSchemaBuilder(schemaModel, ADDITIONAL_PROPERTIES).build();
                    builder.schemaOfAdditionalProperties(childSchema);
                    break;
                default:
                    throw new UnexpectedValueException(addtlPropsJsonValue, TRUE, FALSE, OBJECT);
            }
        });

        schemaJson.findArray(REQUIRED).ifPresent(arr -> arr.getValuesAs(JsonString.class)
                .forEach(val -> builder.addRequiredProperty(val.getString())));
        schemaJson.findObject(PATTERN_PROPERTIES)
                .ifPresent(patternProps -> {
                    patternProps.keySet().forEach(pattern -> {
                        Schema patternSchema = schemaFactory.createChildSchemaBuilder(schemaModel, patternProps.getJsonObject(pattern),
                                PATTERN_PROPERTIES.key(), pattern).build();
                        builder.patternProperty(pattern, patternSchema);
                    });
                });
        schemaJson.findObject(DEPENDENCIES).ifPresent(deps -> addDependencies(builder, deps));
        return builder;
    }

    private void populatePropertySchemas(JsonObject propertyDefs, ObjectSchema.Builder builder) {
        propertyDefs.forEach((key, value) -> addPropertySchemaDefinition(key, (JsonObject) value, builder));
    }

    private void addPropertySchemaDefinition(String keyOfObj, JsonObject childDefinition, ObjectSchema.Builder builder) {
        builder.addPropertySchema(keyOfObj, schemaFactory.createSchemaBuilder(schemaModel.childModel(childDefinition, PROPERTIES, keyOfObj)).build());
    }

    private void addDependencies(ObjectSchema.Builder builder, JsonObject deps) {
        deps.forEach((ifPresent, mustBePresent) -> addDependency(builder, ifPresent, mustBePresent));
    }

    private void addDependency(ObjectSchema.Builder builder, String dependencyName, JsonValue deps) {
        switch (deps.getValueType()) {
            case OBJECT:
                builder.schemaDependency(dependencyName, schemaFactory.createSchemaBuilder(
                        schemaModel.childModel(deps.asJsonObject(), DEPENDENCIES, dependencyName)
                ).build());
                break;
            case ARRAY:
                ((JsonArray) deps).getValuesAs(JsonString.class)
                        .forEach(entry -> builder.propertyDependency(dependencyName, entry.getString()));
                break;
            default:
                throw new UnexpectedValueException(deps, OBJECT, ARRAY);
        }
    }
}
