package io.sbsp.jsonschema;

import io.sbsp.jsonschema.loading.SchemaLoader;
import io.sbsp.jsonschema.loading.SchemaLoadingException;
import io.sbsp.jsonschema.loading.SchemaReader;
import io.sbsp.jsonschema.validation.SchemaValidator;
import io.sbsp.jsonschema.validation.SchemaValidatorFactory;
import io.sbsp.jsonschema.validation.ValidationReport;

import javax.json.JsonObject;
import javax.json.JsonValue;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.Iterator;
import java.util.ServiceLoader;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.sbsp.jsonschema.JsonValueWithPath.fromJsonValue;
import static java.lang.String.format;

/**
 * This class provides the simplest way into the validation and loading APIs.
 */
public class JsonSchemaProvider {
    private static final String DEFAULT_VALIDATOR_FACTORY = "io.sbsp.jsonschema.validation.SchemaValidatorFactoryImpl";
    private static final String DEFAULT_LOADER = "io.sbsp.jsonschema.loading.SchemaLoaderImpl";
    private static final String DEFAULT_READER = "io.sbsp.jsonschema.loading.SchemaLoaderImpl";
    private static final String DEFAULT_BUILDER = "io.sbsp.jsonschema.builder.JsonSchemaBuilder";

    private static SchemaValidatorFactory validatorFactory;
    private static SchemaLoader schemaLoader;
    private static SchemaReader schemaReader;

    // ##################################################################
    // ########  CONVENIENCE ENTRY POINT METHODS ########################
    // ##################################################################

    /**
     * Returns a new instance of a schemaBuilder.  If you want to assign an $id to your schema, use one
     * of the other methods, {@link #schemaBuilder(URI)} or {@link #schemaBuilder(String)}
     */
    public static SchemaBuilder schemaBuilder() {
        return createComponent(DEFAULT_BUILDER, SchemaBuilder.class);
    }

    /**
     * Creates a new instance of a schemaBuilder, using a provided URI as the $id of the schema.
     */
    public static SchemaBuilder schemaBuilder(URI id) {
        checkNotNull(id, "id must not be null");
        return createComponent(DEFAULT_BUILDER, SchemaBuilder.class, id);
    }

    /**
     * Creates a new instance of a schemaBuilder, using a provided String as the $id of the schema.
     */

    public static SchemaBuilder schemaBuilder(String id) {
        return createComponent(DEFAULT_BUILDER, SchemaBuilder.class, URI.create(id));
    }

    public static SchemaValidator getValidator(Schema schema) {
        checkNotNull(schema, "value must not be null");
        return validatorFactory().createValidator(schema);
    }

    /**
     * Reads a schema from a {@link JsonObject} document.  This method will cache the resulting schema for future use.
     * <p>
     * If you want to control the cache or bypass the cache, then use the {@link #createSchemaReader()}, as each invocation
     * creates a brand new instance of the loader with a fresh cache.
     *
     * @param jsonObject The document to load the schema from.
     * @return The loaded schema instance
     * @throws SchemaLoadingException It's unchecked, so more for documentation
     */
    public static Schema readSchema(JsonObject jsonObject) throws SchemaLoadingException {
        return schemaReader().readSchema(jsonObject);
    }

    /**
     * Validates the provided jsonValue against a schema.  This method will cache the validator for future use.  If
     * you don't want to cache the validator, use the {@link #createValidatorFactory()} method to get a fresh
     * instance each time.
     * @param schema The schema to validate against
     * @param value The value being validated
     * @return The validation report
     */
    public static ValidationReport validateSchema(Schema schema, JsonValue value) {
        checkNotNull(value, "value must not be null");
        checkNotNull(schema, "schema must not be null");

        final SchemaValidator validator = validatorFactory().createValidator(schema);
        final JsonValueWithPath jsonValue = fromJsonValue(value, value, schema.getLocation());
        final ValidationReport report = new ValidationReport();
        validator.validate(jsonValue, report);
        return report;
    }

    // ##################################################################
    // ########  FACTORIES: CREATE NEW INSTANCES EACH TIME ##############
    // ##################################################################

    public static SchemaLoader createSchemaLoader() {
        return createComponent(DEFAULT_LOADER, SchemaLoader.class);
    }

    public static SchemaReader createSchemaReader() {
        return createComponent(DEFAULT_READER, SchemaReader.class);
    }

    public static SchemaValidatorFactory createValidatorFactory() {
        return createComponent(DEFAULT_VALIDATOR_FACTORY, SchemaValidatorFactory.class);
    }

    // ##################################################################
    // ########  LAZY GETTER METHODS  ###################################
    // ##################################################################

    private static SchemaLoader schemaLoader() {
        if (schemaLoader == null) {
            schemaLoader = createSchemaLoader();
        }
        return schemaLoader;
    }

    private static SchemaValidatorFactory validatorFactory() {
        if (validatorFactory == null) {
            validatorFactory = createValidatorFactory();
        }
        return validatorFactory;
    }

    private static SchemaReader schemaReader() {
        if (schemaReader == null) {
            schemaReader = createSchemaReader();
        }
        return schemaReader;
    }

    private static <X> X createComponent(String defaultImpl, Class<X> type) {
        ServiceLoader<X> loader = ServiceLoader.load(type);
        Iterator<X> it = loader.iterator();
        if (it.hasNext()) {
            return it.next();
        }
        try {
            Class<X> clazz = (Class<X>) Class.forName(defaultImpl);
            return clazz.newInstance();
        } catch (ClassNotFoundException x) {
            throw new IllegalStateException(format("Provider [%s] not found.  Make sure you include any appropriate modules", defaultImpl), x);
        } catch (Exception x) {
            throw new IllegalStateException(format("Provider [%s] not instantiated", defaultImpl), x);
        }
    }

    private static <X> X createComponent(String defaultImpl, Class<X> type, Object input) {
        checkNotNull(input, "input must not be null");
        ServiceLoader<X> loader = ServiceLoader.load(type);
        Iterator<X> it = loader.iterator();
        if (it.hasNext()) {
            return it.next();
        }
        try {
            Class<X> clazz = (Class<X>) Class.forName(defaultImpl);
            final Constructor<X> constructor = clazz.getDeclaredConstructor(input.getClass());
            return constructor.newInstance(input);
        } catch (ClassNotFoundException x) {
            throw new IllegalStateException(format("Provider [%s] not found.  Make sure you include any appropriate modules", defaultImpl), x);
        } catch (Exception x) {
            throw new IllegalStateException(format("Provider [%s] not instantiated", defaultImpl), x);
        }
    }
}
