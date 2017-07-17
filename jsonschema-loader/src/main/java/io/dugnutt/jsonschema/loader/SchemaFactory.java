package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.loader.internal.DefaultSchemaClient;
import io.dugnutt.jsonschema.six.ArraySchema;
import io.dugnutt.jsonschema.six.BooleanSchema;
import io.dugnutt.jsonschema.six.CombinedSchema;
import io.dugnutt.jsonschema.six.EmptySchema;
import io.dugnutt.jsonschema.six.EnumSchema;
import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.JsonSchemaType;
import io.dugnutt.jsonschema.six.NotSchema;
import io.dugnutt.jsonschema.six.NullSchema;
import io.dugnutt.jsonschema.six.NumberSchema;
import io.dugnutt.jsonschema.six.ObjectSchema;
import io.dugnutt.jsonschema.six.ReferenceSchema;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.six.SchemaException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.spi.JsonProvider;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.$REF;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.ENUM;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.EXCLUSIVE_MAXIMUM;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.EXCLUSIVE_MINIMUM;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MAXIMUM;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MINIMUM;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MULTIPLE_OF;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.NOT;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.TYPE;

/**
 * Schema factories are responsible for extracting values from a json-object to produce an immutable {@link Schema}
 * instance.  This also includes looking up any referenced schemas.
 */
@Getter
@AllArgsConstructor
public class SchemaFactory {


    public static final Charset UTF8 = Charset.forName("UTF-8");

    @Wither
    private final JsonProvider provider;

    @Wither
    private final SchemaClient httpClient;

    @Wither
    private final Charset charset;

    private final Map<String, ReferenceSchema.Builder> referenceSchemas;

    public static SchemaFactory schemaFactory() {
        return schemaFactory(JsonProvider.provider());
    }

    public static SchemaFactory schemaFactory(JsonProvider jsonProvider) {
        return new SchemaFactory(jsonProvider, new DefaultSchemaClient(), UTF8, new HashMap<>());
    }

    public Schema createSchema(SchemaLoaderModel schemaModel) {
        return createSchemaBuilder(schemaModel).build();
    }

    Schema.Builder<?> createSchemaBuilder(SchemaLoaderModel schemaModel) {
        SchemaJsonObject schemaJson = schemaModel.schemaJson;

        final Schema.Builder<?> schemaBuilder = determineAndCreateSchemaBuilder(schemaModel);

        schemaJson.findString(JsonSchemaKeyword.$ID).ifPresent(schemaBuilder::id);
        schemaJson.findString(JsonSchemaKeyword.TITLE).ifPresent(schemaBuilder::title);
        schemaJson.findString(JsonSchemaKeyword.DESCRIPTION).ifPresent(schemaBuilder::description);
        schemaBuilder.schemaLocation(schemaModel.currentJsonPath.toURIFragment());

        return schemaBuilder;
    }

    private Schema.Builder<?> determineAndCreateSchemaBuilder(SchemaLoaderModel schemaModel) {
        checkNotNull(schemaModel, "model must not be null");

        final SchemaJsonObject schemaJson = schemaModel.schemaJson;

        // If this is for an explicit type, skip all the other keywords
        if (schemaModel.hasExplicitTypeValue()) {

        } else if (schemaModel.hasExplicitTypeArray()) {

        } else if(schemaModel.isRefSchema()) {
            //Ignore all other keywords
        } 


        if (schemaModel.isEnumSchema()) {
            final JsonArray possibleEnumValues = schemaModel.getSchemaJson().expectArray(ENUM);
            return EnumSchema.builder().possibleValues(possibleEnumValues);
        } else if (schemaModel.isCombinedSchema()) {
            return new CombinedSchemaFactory(schemaModel, this)
                    .combinedSchemaBuilder();
        } else if (schemaModel.isEmpty()) {
            return new EmptySchema.Builder();
        } else if (schemaModel.isRefSchema()) {
            String ref = schemaJson.getString($REF);
            return new ReferenceLookup(schemaModel).lookup(ref, schemaJson);
        } else if (schemaModel.hasExplicitTypeArray()) {
            JsonArray subtypeJsons = schemaJson.expectArray(TYPE);
            List<Schema> subSchemas = subtypeJsons.getValuesAs(JsonString.class).stream()
                    .map(jsonString -> {
                        JsonSchemaType type = JsonSchemaType.fromString(jsonString.getString());
                        return createBuilderForExplicitSchemaType(schemaModel, type).build();
                    })
                    .collect(Collectors.toList());
            return CombinedSchema.anyOf(subSchemas);
        } else if (schemaModel.hasExplicitTypeValue()) {
            JsonSchemaType explicitType = schemaModel.getExplicitType();
            return createBuilderForExplicitSchemaType(schemaModel, explicitType);
        } else if (schemaModel.isSchemaOf(JsonSchemaType.ARRAY)) {
            return buildArraySchema(schemaModel).requiresArray(false);
        } else if (schemaModel.isSchemaOf(JsonSchemaType.OBJECT)) {
            return buildObjectSchema(schemaModel).requiresObject(false);
        } else if (schemaModel.isSchemaOf(JsonSchemaType.NUMBER)) {
            return buildNumberSchema(schemaModel).requiresNumber(false);
        } else if (schemaModel.isSchemaOf(JsonSchemaType.STRING)) {
            return new StringSchemaFactory(schemaModel).load().requiresString(false);
        } else if (schemaModel.isNotSchema()) {
            Schema mustNotMatch = createChildSchemaBuilder(schemaModel, NOT).build();
            return NotSchema.builder().mustNotMatch(mustNotMatch);
        } else {
            throw schemaModel.createSchemaException("Unable to determine type");
        }
    }

        // Schema.Builder builder;
        // if (loadingState.schemaJson.has(JsonSchemaProperty.ENUM)) {
        //     builder = buildEnumSchema();
        // } else {
        //     builder = new CombinedSchemaLoader(loadingState, this).load()
        //             .orElseGet(() -> {
        //                 if (!loadingState.schemaJson.has(JsonSchemaProperty.TYPE) || loadingState.schemaJson.has(JsonSchemaProperty.$REF)) {
        //                     return buildSchemaWithoutExplicitType();
        //                 } else {
        //                     return loadForType(loadingState.schemaJson.get(JsonSchemaProperty.TYPE.key()));
        //                 }
        //             });
        // }
        //
        // return builder;

    //     } else if (!schemaJson.has(TYPE) || schemaJson.has($REF)) {
    //         return buildSchemaWithoutExplicitType();
    //     } else {
    //         return loadForType(schemaModel.schemaJson.getString(TYPE.key()));
    //     }
    //     this.createEnumSchemaBuilder(schemaModel)
    //             .orElseGet()
    //
    //
    //     Schema.Builder<?> builder =
    //             .orElseGet(() -> {
    //                 return
    //                         .orElseGet(() -> {
    //
    //                         });
    //             });
    //
    //     if (schemaModel.schemaJson.has(ENUM)) {
    //         builder = enumSchemaBuilder();
    //     } else {
    //
    //     }
    //
    //     schemaModel.schemaJson.findString(ID).map(JsonString::getString).ifPresent(builder::id);
    //     schemaModel.schemaJson.findString(TITLE).map(JsonString::getString).ifPresent(builder::title);
    //     schemaModel.schemaJson.findString(DESCRIPTION).map(JsonString::getString).ifPresent(builder::description);
    //     builder.schemaLocation(schemaModel.currentJsonPath.toURIFragment());
    //     return builder;
    // }



    // Schema.Builder loadForType(JsonValue element) {
        // if (element.getValueType() == ARRAY) {
        //     return buildAnyOfSchemaForMultipleTypes();
        // } else if (element.getValueType() == STRING) {
        //     final String stringType = ((JsonString) element).getString();
        //
        // } else {
        //     throw new UnexpectedValueException(element, ARRAY, STRING);
        // }
    // }

    // public Schema.Builder<?> createChildSchemaBuilder(SchemaLoaderModel parentModel, JsonSchemaProperty childKey) {
    //     return this.loadSchema(parentModel.childModel(childKey));
    // }

    public Schema.Builder<?> createChildSchemaBuilder(SchemaLoaderModel parentModel, JsonObject childJson, JsonSchemaKeyword property, String childPath) {
        SchemaLoaderModel childModel = parentModel.childModel(childJson, property, childPath);
        return this.createSchemaBuilder(childModel);
    }

    // public EnumSchema.Builder enumSchemaBuilder(JsonArray enumArray) {
    //     checkNotNull(enumArray, "enumArray must not be null");
    //     return ;
    // }

    public NumberSchema.Builder buildNumberSchema(SchemaLoaderModel schemaModel) {
        NumberSchema.Builder builder = NumberSchema.builder();
        final SchemaJsonObject schemaJson = schemaModel.schemaJson;
        schemaJson.findNumber(MINIMUM).ifPresent(builder::minimum);
        schemaJson.findNumber(MAXIMUM).ifPresent(builder::maximum);
        schemaJson.findNumber(MULTIPLE_OF).ifPresent(builder::multipleOf);
        schemaJson.findBoolean(EXCLUSIVE_MINIMUM).ifPresent(builder::exclusiveMinimum);
        schemaJson.findBoolean(EXCLUSIVE_MAXIMUM).ifPresent(builder::exclusiveMaximum);
        return builder;
    }

    private Schema.Builder createBuilderForExplicitSchemaType(final SchemaLoaderModel model, final JsonSchemaType schemaType) {
        switch (schemaType) {
            case STRING:
                return new StringSchemaFactory(model).load().requiresString(true);
            case NUMBER:
                return buildNumberSchema(model).requiresNumber(true);
            case BOOLEAN:
                return BooleanSchema.builder();
            case NULL:
                return NullSchema.builder();
            case ARRAY:
                return buildArraySchema(model).requiresArray(true);
            case OBJECT:
                return buildObjectSchema(model).requiresObject(true);
            default:
                throw new SchemaException(String.format("unknown type: [%s]", schemaType));
        }
    }

    private ObjectSchema.Builder buildObjectSchema(SchemaLoaderModel schemaModel) {
        return ObjectSchemaFactory.createObjectSchemaBuilder(schemaModel, this);
    }

    private ArraySchema.Builder buildArraySchema(SchemaLoaderModel schemaModel) {
        return new ArraySchemaFactory(schemaModel, this).createArraySchemaBuilder();
    }




    // public Schema load(JsonObject schemaJson) {
    //     checkNotNull(schemaJson, "schemaJson must not be null");
    //     return SchemaLoader.builder()
    //             .resolutionScope(resolutionScope)
    //             .httpClient(httpClient)
    //             .pointerSchemas(referenceSchemas)
    //             .schemaJson(schemaJson)
    //             .provider(JsonProvider.provider())
    //             .build()
    //             .load()
    //             .build();
    // }
    //
    // public Schema load(InputStream inputJson) {
    //     checkNotNull(inputJson, "inputStream must not be null");
    //     return load(provider.createReader(inputJson).readObject());
    // }
    //
    // public Schema load(String inputJson) {
    //     checkNotNull(inputJson, "inputStream must not be null");
    //     return load(provider.createReader(new StringReader(inputJson)).readObject());
    // }
}
