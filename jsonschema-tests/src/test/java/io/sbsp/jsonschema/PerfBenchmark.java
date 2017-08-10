package io.sbsp.jsonschema;

import io.sbsp.jsonschema.validator.SchemaValidator;
import io.sbsp.jsonschema.validator.SchemaValidatorFactory;
import io.sbsp.jsonschema.validator.ValidationReport;
import org.junit.Test;

import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

import static io.sbsp.jsonschema.extractor.JsonSchemaFactory.schemaFactory;

public class PerfBenchmark {

    @Test
    public void testPerformance() {
        final JsonObject draft6 = ResourceLoader.DEFAULT.readObj("json-schema-draft-06.json");
        final Schema draft6Schema = schemaFactory()
                .load(draft6);
        final SchemaValidator validator = SchemaValidatorFactory.createValidatorForSchema(draft6Schema);

        final JsonObject jsonObject = ResourceLoader.DEFAULT.readObj("perftest.json");
        final List<JsonValueWithLocation> testSubjects = new ArrayList<>();
        jsonObject.getJsonObject("schemas").forEach((k, v) -> {
            testSubjects.add(JsonValueWithLocation.fromJsonValue(v, SchemaLocation.hashedRoot(v)));
        });

        long startAt = System.currentTimeMillis();
        ValidationReport report = doValidations(testSubjects, validator);
        System.out.println(report.toString());

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
