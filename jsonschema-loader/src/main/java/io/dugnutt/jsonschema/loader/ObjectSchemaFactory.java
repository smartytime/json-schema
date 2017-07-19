package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.six.ObjectSchema;
import io.dugnutt.jsonschema.six.Schema;

import javax.json.JsonString;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ADDITIONAL_PROPERTIES;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.DEPENDENCIES;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MAX_PROPERTIES;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MIN_PROPERTIES;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.PATTERN_PROPERTIES;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.PROPERTIES;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.REQUIRED;
import static javax.json.JsonValue.ValueType.ARRAY;
import static javax.json.JsonValue.ValueType.OBJECT;

/**
 * Performs a partial load of an ObjectSchema based on a
 *
 * @author erosb
 */
class ObjectSchemaFactory {

    private final SchemaLoaderModel schemaModel;

    private final JsonSchemaFactory schemaFactory;

    ObjectSchemaFactory(SchemaLoaderModel model, JsonSchemaFactory schemaFactory) {
        this.schemaModel = checkNotNull(model, "model cannot be null");
        this.schemaFactory = checkNotNull(schemaFactory, "schemaFactory cannot be null");
    }

    public static ObjectSchema.Builder createObjectSchemaBuilder(SchemaLoaderModel schemaModel, JsonSchemaFactory schemaFactory) {
        return new ObjectSchemaFactory(schemaModel, schemaFactory).createObjectSchemaBuilder();
    }

    private ObjectSchema.Builder createObjectSchemaBuilder() {
        ObjectSchema.Builder builder = ObjectSchema.builder(schemaModel.getLocation());
        FluentJsonObject schemaJson = schemaModel.schemaJson;
        schemaJson.findInt(MIN_PROPERTIES).ifPresent(builder::minProperties);
        schemaJson.findInt(MAX_PROPERTIES).ifPresent(builder::maxProperties);

        schemaModel.streamChildSchemaModels(PROPERTIES, OBJECT)
                .forEach(schemaModel -> {
                    String propertyName = schemaModel.getPropertyName();
                    Schema propertySchema = schemaFactory.createSchema(schemaModel);
                    builder.addPropertySchema(propertyName, propertySchema);
                });

        schemaModel.childModel(ADDITIONAL_PROPERTIES)
                .map(schemaFactory::createSchema)
                .ifPresent(builder::schemaOfAdditionalProperties);

        schemaJson.findArray(REQUIRED).ifPresent(arr -> arr.getValuesAs(JsonString.class)
                .forEach(val -> builder.addRequiredProperty(val.getString())));

        schemaModel.streamChildSchemaModels(PATTERN_PROPERTIES)
                .forEach(schemaModel -> {
                    Schema schema = schemaFactory.createSchema(schemaModel);
                    builder.patternProperty(schemaModel.getPropertyName(), schema);
                });

        /*

            "dependencies": {
              "d": {
                "type": "object",
                "properties": {
                  "rectangle": {
                    "$ref": "#/definitions/Rectangle"
                  }
                }
              }
            },

         */
        schemaJson.findObject(DEPENDENCIES).ifPresent(dependencyObject -> dependencyObject.forEach((dependencyKey, dependencyStructure) -> {
            switch (dependencyStructure.getValueType()) {
                case OBJECT:
                    SchemaLoaderModel dependencyModel = schemaModel.childModel(DEPENDENCIES, dependencyKey, dependencyStructure);
                    builder.schemaDependency(dependencyKey, schemaFactory.createSchema(dependencyModel));
                    break;
                case ARRAY:
                    dependencyStructure.asJsonArray().getValuesAs(JsonString::getString)
                            .forEach(arrayItem -> builder.propertyDependency(dependencyKey, arrayItem));
                    break;
                default:
                    throw schemaModel.unexpectedValueException(dependencyKey, dependencyStructure, OBJECT, ARRAY);
            }
        }));

        return builder;
    }
}
