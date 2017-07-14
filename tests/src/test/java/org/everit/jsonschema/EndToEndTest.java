package org.everit.jsonschema;

import com.google.common.io.Resources;
import org.everit.jsonschema.api.Schema;
import org.everit.jsonschema.utils.JsonUtils;
import org.everit.jsonschema.validator.SchemaValidator;
import org.everit.jsonschema.validator.ValidationError;
import org.junit.Test;

import javax.json.JsonObject;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Optional;

import static org.everit.jsonschema.loader.SchemaFactory.schemaFactory;
import static org.everit.jsonschema.validator.SchemaValidatorFactory.findValidator;

public class EndToEndTest {

    @Test
    public void testParseAndValidate() throws MalformedURLException {
        final String jsonSchema = readResource("/org/everit/jsonschema/sbsp-account-profile.json");
        final String jsonData = readResource("/org/everit/jsonschema/account-data.json");
        Schema loadedSchema = schemaFactory().load(jsonSchema);
        final JsonObject jsonObject = JsonUtils.readObject(jsonData);
        final SchemaValidator<?> validator = findValidator(loadedSchema);
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
