package io.sbsp.jsonschema.loader;

import io.sbsp.jsonschema.six.JsonValueWithLocation;
import io.sbsp.jsonschema.six.Schema;
import io.sbsp.jsonschema.six.UnexpectedValueException;

import javax.json.JsonString;
import java.util.regex.Pattern;

import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.ADDITIONAL_PROPERTIES;
import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.DEPENDENCIES;
import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.MAX_PROPERTIES;
import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.MIN_PROPERTIES;
import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.PATTERN_PROPERTIES;
import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.PROPERTIES;
import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.REQUIRED;
import static javax.json.JsonValue.ValueType.ARRAY;
import static javax.json.JsonValue.ValueType.OBJECT;

/**
 * Performs a partial load of an ObjectSchema based on a
 *
 * @author erosb
 */
class ObjectKeywordsLoader implements KeywordsLoader {

    private ObjectKeywordsLoader() {

    }

    @Override
    public void appendKeywords(JsonValueWithLocation schemaJson, Schema.JsonSchemaBuilder schemaBuilder, JsonSchemaFactory schemaFactory) {

        schemaJson.findInt(MIN_PROPERTIES).ifPresent(schemaBuilder::minProperties);
        schemaJson.findInt(MAX_PROPERTIES).ifPresent(schemaBuilder::maxProperties);

        schemaJson.findPathAwareObject(PROPERTIES).ifPresent(jsonObject -> {
            jsonObject.forEachKey((property, propSchema) -> {
                schemaBuilder.propertySchema(property, schemaFactory.createSchemaBuilder(propSchema));
            });
        });

        schemaJson.findPathAwareObject(ADDITIONAL_PROPERTIES)
                .map(schemaFactory::createSchemaBuilder)
                .ifPresent(schemaBuilder::schemaOfAdditionalProperties);

        schemaJson.streamPathAwareArrayItems(REQUIRED)
                .map(JsonValueWithLocation::asString)
                .forEach(schemaBuilder::requiredProperty);

        schemaJson.findPathAwareObject(PATTERN_PROPERTIES).ifPresent(jsonObject -> {
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

        schemaJson.findPathAwareObject(DEPENDENCIES).ifPresent(dependencyObject ->
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

    public static ObjectKeywordsLoader objectKeywordsLoader() {
        return new ObjectKeywordsLoader();
    }
}
