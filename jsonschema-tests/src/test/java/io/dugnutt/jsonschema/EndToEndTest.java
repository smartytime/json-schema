package io.dugnutt.jsonschema;

import com.google.common.io.Resources;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.utils.JsonUtils;
import io.dugnutt.jsonschema.validator.SchemaValidator;
import io.dugnutt.jsonschema.validator.ValidationError;
import org.junit.Test;

import javax.json.JsonObject;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Optional;

import static io.dugnutt.jsonschema.loader.SchemaFactory.schemaFactory;
import static io.dugnutt.jsonschema.validator.SchemaValidatorFactory.createValidatorForSchema;

public class EndToEndTest {

    @Test
    public void testParseAndValidate() throws MalformedURLException {
        final String jsonSchema = readResource("/org/everit/jsonschema/sbsp-account-profile.json");
        final String jsonData = readResource("/org/everit/jsonschema/account-data.json");
        Schema loadedSchema = schemaFactory().load(jsonSchema);
        final JsonObject jsonObject = JsonUtils.readJsonObject(jsonData);
        final SchemaValidator<?> validator = createValidatorForSchema(loadedSchema);
        final Optional<ValidationError> errors = validator.validate(jsonObject);
        System.out.println();
    }

    private String readResource(String url) {
        try {
            final URL resource = getClass().getResource(url);
            return Resources.toString(resource, Charset.forName("UTF-8"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
