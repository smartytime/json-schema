package io.sbsp.jsonschema;

import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.loading.SchemaLoaderImpl;
import io.sbsp.jsonschema.validation.ValidationError;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

import javax.json.JsonObject;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Optional;

import static io.sbsp.jsonschema.ResourceLoader.resourceLoader;
import static io.sbsp.jsonschema.ValidationMocks.createTestValidator;
import static io.sbsp.jsonschema.loading.SchemaLoaderImpl.schemaLoader;

public class EndToEndTest {

    @Test
    public void testParseAndValidate() throws MalformedURLException {
        final InputStream primitives = resourceLoader().getStream("primitives.json");
        final InputStream jsonSchema = resourceLoader().getStream("sbsp-account-profile.json");
        final JsonObject jsonData = resourceLoader().readJsonObject("account-data.json");
        Schema loadedSchema = SchemaLoaderImpl.schemaLoader()
                .withPreloadedSchema(primitives)
                .readSchema(jsonSchema);
        final Optional<ValidationError> errors = createTestValidator(loadedSchema).validate(jsonData);
        Assertions.assertThat(errors).isPresent();
        final ValidationError error = errors.get();
        SoftAssertions.assertSoftly(a -> {
            Assertions.assertThat(error.getCauses()).hasSize(3);

            final ValidationError e1 = error.getCauses().stream()
                    .filter(e -> e.getPointerToViolation().equals("#/secondary_color"))
                    .findFirst().orElse(null);
            a.assertThat(e1).isNotNull();
            a.assertThat(e1.getPointerToViolation()).as("secondary color").isEqualTo("#/secondary_color");
            a.assertThat(e1.getCauses()).as("secondary color").isEmpty();
            a.assertThat(e1.getKeyword()).as("secondary color").isEqualTo(Keywords.PATTERN);
            a.assertThat(e1.getSchemaLocation()).as("secondary color").hasToString("#/properties/secondary_color");
            a.assertThat(e1.getCode()).as("secondary color").isEqualTo("validation.keyword.pattern");
            a.assertThat(e1.getArguments()).as("secondary color").hasSize(2);
            a.assertThat(e1.getArguments()).containsExactly("badbadleroybrown", "^#?(?:(?:[0-9a-fA-F]{2}){3}|(?:[0-9a-fA-F]){3})$");

            final ValidationError e2 = error.getCauses().stream()
                    .filter(e -> e.getPointerToViolation().equals("#/contact"))
                    .findFirst().orElse(null);
            a.assertThat(e1).isNotNull();
            a.assertThat(e2.getPointerToViolation()).as("multi-errors").isEqualTo("#/contact");
            a.assertThat(e2.getCauses()).as("multi-errors").hasSize(4);
            a.assertThat(e2.getKeyword()).as("multi-errors").isNull();
            a.assertThat(e2.getSchemaLocation()).as("multi-errors").hasToString("#/properties/contact");
            a.assertThat(e2.getCode()).as("multi-errors").isEqualTo("validation.multipleFailures");

            final ValidationError e3 = error.getCauses().stream()
                    .filter(e -> e.getPointerToViolation().equals("#"))
                    .findFirst().orElse(null);
            a.assertThat(e3.getPointerToViolation()).as("required").isEqualTo("#");
            a.assertThat(e3.getCauses()).as("required-errors").isEmpty();
            a.assertThat(e3.getKeyword()).as("required-errors").isEqualTo(Keywords.REQUIRED);
            a.assertThat(e3.getSchemaLocation()).as("required-errors").hasToString("#");
            a.assertThat(e3.getCode()).as("required-errors").isEqualTo("validation.keyword.required");
            a.assertThat(e3.getArguments()).as("required-errors").hasSize(1);
            a.assertThat(e3.getArguments()).as("required-errors").contains("website_url");
        });
    }
}
