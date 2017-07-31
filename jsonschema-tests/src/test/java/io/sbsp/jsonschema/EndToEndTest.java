package io.sbsp.jsonschema;

import io.sbsp.jsonschema.six.enums.JsonSchemaKeyword;
import io.sbsp.jsonschema.six.Schema;
import io.sbsp.jsonschema.validator.ValidationError;
import org.junit.Test;

import javax.json.JsonObject;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Optional;

import static io.sbsp.jsonschema.ValidationMocks.createTestValidator;
import static io.sbsp.jsonschema.loader.JsonSchemaFactory.schemaFactory;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class EndToEndTest {

    @Test
    public void testParseAndValidate() throws MalformedURLException {
        final InputStream primitives = ResourceLoader.DEFAULT.getStream("primitives.json");
        final InputStream jsonSchema = ResourceLoader.DEFAULT.getStream("sbsp-account-profile.json");
        final JsonObject jsonData = ResourceLoader.DEFAULT.readObj("account-data.json");
        Schema loadedSchema = schemaFactory()
                .withPreloadedSchema(primitives)
                .load(jsonSchema);
        final Optional<ValidationError> errors = createTestValidator(loadedSchema).validate(jsonData);
        assertThat(errors).isPresent();
        final ValidationError error = errors.get();
        assertSoftly(a -> {
            assertThat(error.getCauses()).hasSize(3);

            final ValidationError e1 = error.getCauses().stream()
                    .filter(e -> e.getPointerToViolation().equals("#/secondary_color"))
                    .findFirst().orElse(null);
            a.assertThat(e1).isNotNull();
            a.assertThat(e1.getPointerToViolation()).as("secondary color").isEqualTo("#/secondary_color");
            a.assertThat(e1.getCauses()).as("secondary color").isEmpty();
            a.assertThat(e1.getKeyword()).as("secondary color").isEqualTo(JsonSchemaKeyword.PATTERN);
            a.assertThat(e1.getSchemaLocation()).as("secondary color").isEqualTo("#/properties/secondary_color");
            a.assertThat(e1.getCode()).as("secondary color").isEqualTo("validation.keyword.pattern");
            a.assertThat(e1.getModel()).as("secondary color").hasSize(2);
            a.assertThat(e1.getModel()).containsExactly("badbadleroybrown", "^#?(?:(?:[0-9a-fA-F]{2}){3}|(?:[0-9a-fA-F]){3})$");

            final ValidationError e2 = error.getCauses().stream()
                    .filter(e -> e.getPointerToViolation().equals("#/contact"))
                    .findFirst().orElse(null);
            a.assertThat(e1).isNotNull();
            a.assertThat(e2.getPointerToViolation()).as("multi-errors").isEqualTo("#/contact");
            a.assertThat(e2.getCauses()).as("multi-errors").hasSize(4);
            a.assertThat(e2.getKeyword()).as("multi-errors").isNull();
            a.assertThat(e2.getSchemaLocation()).as("multi-errors").isEqualTo("#/properties/contact");
            a.assertThat(e2.getCode()).as("multi-errors").isEqualTo("validation.multipleFailures");

            final ValidationError e3 = error.getCauses().stream()
                    .filter(e -> e.getPointerToViolation().equals("#"))
                    .findFirst().orElse(null);
            a.assertThat(e3.getPointerToViolation()).as("required").isEqualTo("#");
            a.assertThat(e3.getCauses()).as("required-errors").isEmpty();
            a.assertThat(e3.getKeyword()).as("required-errors").isEqualTo(JsonSchemaKeyword.REQUIRED);
            a.assertThat(e3.getSchemaLocation()).as("required-errors").isEqualTo("#");
            a.assertThat(e3.getCode()).as("required-errors").isEqualTo("validation.keyword.required");
            a.assertThat(e3.getModel()).as("required-errors").hasSize(1);
            a.assertThat(e3.getModel()).as("required-errors").contains("website_url");
        });
    }
}