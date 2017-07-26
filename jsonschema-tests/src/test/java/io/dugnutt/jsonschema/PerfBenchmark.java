package io.dugnutt.jsonschema;

import com.google.common.base.Stopwatch;
import io.dugnutt.jsonschema.loader.JsonSchemaFactory;
import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.ValidationError;
import org.junit.Test;

import javax.json.JsonObject;
import javax.json.spi.JsonProvider;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.dugnutt.jsonschema.ValidationMocks.createTestValidator;
import static io.dugnutt.jsonschema.loader.JsonSchemaFactory.schemaFactory;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.$ID;
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

            final ValidationError e1 = error.getCauses().get(0);
            a.assertThat(e1.getPointerToViolation()).as("secondary color").isEqualTo("#/secondary_color");
            a.assertThat(e1.getCauses()).as("secondary color").isEmpty();
            a.assertThat(e1.getKeyword()).as("secondary color").isEqualTo(JsonSchemaKeyword.PATTERN);
            a.assertThat(e1.getSchemaLocation()).as("secondary color").isEqualTo("#/properties/secondary_color");
            a.assertThat(e1.getCode()).as("secondary color").isEqualTo("validation.keyword.pattern");
            a.assertThat(e1.getModel()).as("secondary color").hasSize(2);
            a.assertThat(e1.getModel()).containsExactly("badbadleroybrown", "^#?(?:(?:[0-9a-fA-F]{2}){3}|(?:[0-9a-fA-F]){3})$");

            final ValidationError e2 = error.getCauses().get(1);
            a.assertThat(e2.getPointerToViolation()).as("multi-errors").isEqualTo("#/contact");
            a.assertThat(e2.getCauses()).as("multi-errors").hasSize(4);
            a.assertThat(e2.getKeyword()).as("multi-errors").isNull();
            a.assertThat(e2.getSchemaLocation()).as("multi-errors").isEqualTo("#/properties/contact");
            a.assertThat(e2.getCode()).as("multi-errors").isEqualTo("validation.multipleFailures");

            final ValidationError e3 = error.getCauses().get(2);
            a.assertThat(e3.getPointerToViolation()).as("required").isEqualTo("#");
            a.assertThat(e3.getCauses()).as("multi-errors").isEmpty();
            a.assertThat(e3.getKeyword()).as("multi-errors").isEqualTo(JsonSchemaKeyword.REQUIRED);
            a.assertThat(e3.getSchemaLocation()).as("multi-errors").isEqualTo("#");
            a.assertThat(e3.getCode()).as("multi-errors").isEqualTo("validation.keyword.required");
            a.assertThat(e3.getModel()).as("multi-errors").hasSize(1);
            a.assertThat(e3.getModel()).as("multi-errors").contains("website_url");
        });
    }

    @Test
    public void poorMansLoadingTest() {
        Stopwatch watch = Stopwatch.createStarted();
        final InputStream primitives = ResourceLoader.DEFAULT.getStream("primitives.json");
        final InputStream jsonSchema = ResourceLoader.DEFAULT.getStream("sbsp-account-profile.json");
        final JsonObject jsonData = ResourceLoader.DEFAULT.readObj("account-data.json");
        final JsonProvider provider = JsonProvider.provider();
        final Duration parseJson = watch.elapsed();

        watch.reset().start();
        JsonSchemaFactory factory = schemaFactory()
                .withPreloadedSchema(primitives);
        final Duration factoryLoad = watch.elapsed();

        // #############################
        // COLD LOAD
        // #############################
        watch.reset().start();
        final Schema schema = factory.load(jsonSchema);
        final Duration loadSchema = watch.elapsed();

        // #############################
        // LOAD 100
        // #############################
        List<JsonObject> toLoad = new ArrayList<>();
        for(int i=0; i<100; i++) {
            toLoad.add(provider.createObjectBuilder(jsonData)
                    .add($ID.key(), "account-data-" + i + ".json")
                    .build());
        }

        watch.reset().start();
        for (JsonObject jsonObject : toLoad) {
            factory.load(jsonObject);
        }
        final Duration load100 = watch.elapsed();

        watch.reset().start();
        createTestValidator(schema).validate(jsonData);

        final Duration validation = watch.elapsed();
        System.out.println("Parse Initial JSON: " + parseJson.toMillis());
        System.out.println("Factory Load: " + factoryLoad.toMillis());
        System.out.println("Load First Schema: " + loadSchema.toMillis());
        System.out.println("Validate Schema: " + validation.toMillis());
        System.out.println("Avg for 100: " + ((double) load100.toMillis() / 100));
        System.out.println();
        System.out.println();


    }
}
