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

    public static SchemaBuilder schemaBuilder() {
        return createComponent(DEFAULT_BUILDER, SchemaBuilder.class);
    }

    public static SchemaBuilder schemaBuilder(URI id) {
        checkNotNull(id, "id must not be null");
        return createComponent(DEFAULT_BUILDER, SchemaBuilder.class, id);
    }

    public static SchemaBuilder schemaBuilder(String id) {
        return createComponent(DEFAULT_BUILDER, SchemaBuilder.class, URI.create(id));
    }

    public static SchemaValidator getValidator(Schema schema) {
        checkNotNull(schema, "value must not be null");
        return validatorFactory().createValidator(schema);
    }

    public static Schema readSchema(JsonObject jsonObject) throws SchemaLoadingException {
        return schemaReader().readSchema(jsonObject);
    }

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
