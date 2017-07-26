package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.six.PathAwareJsonValue;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.six.UnexpectedValueException;

import javax.json.JsonString;
import java.util.regex.Pattern;

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
class ObjectKeywordsFactoryHelper {

    public static void appendObjectKeywords(PathAwareJsonValue schemaJson, Schema.JsonSchemaBuilder schemaBuilder,
                                            JsonSchemaFactory schemaFactory) {

        schemaJson.findInt(MIN_PROPERTIES).ifPresent(schemaBuilder::minProperties);
        schemaJson.findInt(MAX_PROPERTIES).ifPresent(schemaBuilder::maxProperties);

        schemaJson.findPathAware(PROPERTIES).ifPresent(jsonObject -> {
            jsonObject.forEachKey((property, propSchema) -> {
                schemaBuilder.propertySchema(property, schemaFactory.createSchemaBuilder(propSchema));
            });
        });

        schemaJson.findPathAware(ADDITIONAL_PROPERTIES)
                .map(schemaFactory::createSchemaBuilder)
                .ifPresent(schemaBuilder::schemaOfAdditionalProperties);

        schemaJson.streamPathAwareArrayItems(REQUIRED)
                .map(PathAwareJsonValue::asString)
                .forEach(schemaBuilder::requiredProperty);

        schemaJson.findPathAware(PATTERN_PROPERTIES).ifPresent(jsonObject -> {
            jsonObject.forEachKey((pattern, patternSchema) -> {
                schemaBuilder.patternProperty(Pattern.compile(pattern), schemaFactory.createSchemaBuilder(patternSchema));
            });
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

        schemaJson.findPathAware(DEPENDENCIES).ifPresent(dependencyObject ->
                dependencyObject.forEachKey((dependencyKey, dependencyStructure) -> {
                    switch (dependencyStructure.getValueType()) {
                        case OBJECT:
                            schemaFactory.createSchemaBuilder(dependencyStructure);
                            schemaBuilder.schemaDependency(dependencyKey, schemaFactory.createSchemaBuilder(dependencyStructure));
                            break;
                        case ARRAY:
                            dependencyStructure.asJsonArray()
                                    .getValuesAs(JsonString::getString)
                                    .forEach(arrayItem -> schemaBuilder.propertyDependency(dependencyKey, arrayItem));
                            break;
                        default:
                            throw new UnexpectedValueException(schemaJson.getPath(), dependencyStructure, OBJECT, ARRAY);
                    }
                }));
    }
}
