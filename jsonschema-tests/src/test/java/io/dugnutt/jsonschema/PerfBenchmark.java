package io.dugnutt.jsonschema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr353.JSR353Module;
import io.dugnutt.jsonschema.six.JsonPath;
import io.dugnutt.jsonschema.six.JsonValueWithLocation;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.six.SchemaLocation;
import io.dugnutt.jsonschema.validator.SchemaValidator;
import io.dugnutt.jsonschema.validator.SchemaValidatorFactory;
import io.dugnutt.jsonschema.validator.ValidationReport;

import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

import static io.dugnutt.jsonschema.loader.JsonSchemaFactory.schemaFactory;

public class PerfBenchmark {

    public static void main(String[] args) {

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JSR353Module());

        final JsonObject draft6 = ResourceLoader.DEFAULT.readObj("json-schema-draft-06.json");
        final Schema draft6Schema = schemaFactory()
                .load(draft6);
        final SchemaValidator validator = SchemaValidatorFactory.createValidatorForSchema(draft6Schema);

        final JsonObject jsonObject = ResourceLoader.DEFAULT.readObj("perftest.json");
        final List<JsonValueWithLocation> testSubjects = new ArrayList<>();
        jsonObject.getJsonObject("schemas").forEach((k, v) -> {
            testSubjects.add(JsonValueWithLocation.fromJsonValue(v, SchemaLocation.anonymousRoot()));
        });

        long startAt = System.currentTimeMillis();
        ValidationReport report = doValidations(testSubjects, validator);

        report.writeTo(System.out);
        long endAt = System.currentTimeMillis();
        long execTime = endAt - startAt;
        System.out.println("total time: " + execTime + " ms");
    }

    public static ValidationReport doValidations(List<JsonValueWithLocation> testSubjects, SchemaValidator validator) {
        ValidationReport report = new ValidationReport();

        long startAt = System.currentTimeMillis();
        for (int i = 0; i < 500; ++i) {
            for (JsonValueWithLocation testSubject : testSubjects) {
                if(!validator.validate(testSubject, report)) {
                    throw new IllegalStateException("OOPS: " + report.getErrors());
                }
            }

            if (i % 20 == 0) {
                System.out
                        .println("Iteration " + i + " (in " + (System.currentTimeMillis() - startAt) + "ms)");
            }
        }

        return report;

    }
}
