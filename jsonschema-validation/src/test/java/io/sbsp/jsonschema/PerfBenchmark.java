package io.sbsp.jsonschema;

import io.sbsp.jsonschema.loading.SchemaLoaderImpl;
import io.sbsp.jsonschema.utils.SchemaPaths;
import io.sbsp.jsonschema.validation.SchemaValidator;
import io.sbsp.jsonschema.validation.SchemaValidatorFactoryImpl;
import io.sbsp.jsonschema.validation.ValidationReport;
import org.junit.Test;

import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

import static io.sbsp.jsonschema.ResourceLoader.*;
import static io.sbsp.jsonschema.loading.SchemaLoaderImpl.schemaLoader;

public class PerfBenchmark {

    @Test
    public void testPerformance() {
        final JsonObject draft6 = resourceLoader().readJsonObject("json-schema-draft-06.json");
        final Schema draft6Schema = SchemaLoaderImpl.schemaLoader()
                .readSchema(draft6);
        final SchemaValidator validator = SchemaValidatorFactoryImpl.createValidatorForSchema(draft6Schema);

        final JsonObject jsonObject = resourceLoader().readJsonObject("perftest.json");
        final List<JsonValueWithPath> testSubjects = new ArrayList<>();
        jsonObject.getJsonObject("schemas").forEach((k, v) -> {
            testSubjects.add(JsonValueWithPath.fromJsonValue(v, v, SchemaPaths.fromNonSchemaSource(v)));
        });

        long startAt = System.currentTimeMillis();
        ValidationReport report = doValidations(testSubjects, validator);
        System.out.println(report.toString());

        long endAt = System.currentTimeMillis();
        long execTime = endAt - startAt;
        System.out.println("total time: " + execTime + " ms");
    }

    public static ValidationReport doValidations(List<JsonValueWithPath> testSubjects, SchemaValidator validator) {
        ValidationReport report = new ValidationReport();

        long startAt = System.currentTimeMillis();
        for (int i = 0; i < 500; ++i) {
            for (JsonValueWithPath testSubject : testSubjects) {
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
