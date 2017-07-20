package io.dugnutt.jsonschema;

import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.SchemaValidator;
import io.dugnutt.jsonschema.validator.ValidationError;
import org.junit.Test;

import javax.json.JsonObject;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Optional;

import static io.dugnutt.jsonschema.loader.JsonSchemaFactory.schemaFactory;
import static io.dugnutt.jsonschema.validator.SchemaValidatorFactory.createValidatorForSchema;

public class EndToEndTest {

    @Test
    public void testParseAndValidate() throws MalformedURLException {
        final InputStream primitives = ResourceLoader.DEFAULT.getStream("primitives.json");
        final InputStream jsonSchema = ResourceLoader.DEFAULT.getStream("sbsp-account-profile.json");
        final JsonObject jsonData = ResourceLoader.DEFAULT.readObj("account-data.json");
        Schema loadedSchema = schemaFactory()
                .withPreloadedSchema(primitives)
                .load(jsonSchema);
        final SchemaValidator<?> validator = createValidatorForSchema(loadedSchema);
        final Optional<ValidationError> errors = validator.validate(jsonData);
        System.out.println();
    }
}
