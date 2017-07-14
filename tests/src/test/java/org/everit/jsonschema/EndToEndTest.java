package org.everit.jsonschema;

import com.google.common.io.Resources;
import org.everit.json.JsonObject;
import org.everit.jsonschema.api.Schema;
import org.everit.jsonschema.loader.SchemaLoader;
import org.everit.jsonschema.loaders.jsoniter.JsoniterApi;
import org.everit.jsonschema.validator.SchemaValidator;
import org.everit.jsonschema.validator.ValidationError;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Optional;

import static org.everit.jsonschema.validator.SchemaValidatorFactory.findValidator;

public class EndToEndTest {

    private String readResource(String url){
        try {
            final URL resource = getClass().getResource(url);
            return Resources.toString(resource, Charset.forName("UTF-8"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    public void testParseAndValidate() throws MalformedURLException {
        final String jsonSchema = readResource("/org/everit/jsonschema/sbsp-account-profile.json");
        final String jsonData = readResource("/org/everit/jsonschema/account-data.json");
        final JsoniterApi jsonApi = new JsoniterApi();
        Schema loadedSchema = SchemaLoader.load(jsonSchema, jsonApi);
        final JsonObject jsonObject = jsonApi.readJson(jsonData).asObject();
        final SchemaValidator<?> validator = findValidator(loadedSchema);
        final Optional<ValidationError> errors = validator.validate(jsonObject);
        System.out.println();
    }
}
