package io.dugnutt.jsonschema;

import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.JsonSchemaValidator;
import io.dugnutt.jsonschema.validator.SchemaValidatorFactory;

import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.ArrayList;
import java.util.List;

import static io.dugnutt.jsonschema.loader.JsonSchemaFactory.schemaFactory;

public class PerfBenchmark {

    public static void main(String[] args) {
        final JsonObject draft6 = ResourceLoader.DEFAULT.readObj("json-schema-draft-06.json");
        final Schema draft6Schema = schemaFactory()
                .load(draft6);
        final JsonSchemaValidator validator = SchemaValidatorFactory.createValidatorForSchema(draft6Schema);

        final JsonObject jsonObject = ResourceLoader.DEFAULT.readObj("perftest.json");
        final List<JsonValue> testSubjects = new ArrayList<>();
        jsonObject.getJsonObject("schemas").forEach((k, v) -> {
            testSubjects.add(v);
        });

        long startAt = System.currentTimeMillis();
        for (int i = 0; i < 500; ++i) {
            for (JsonValue testSubject : testSubjects) {
                validator.validate(testSubject);
            }

            if (i % 20 == 0) {
                System.out
                        .println("Iteration " + i + " (in " + (System.currentTimeMillis() - startAt) + "ms)");
            }
        }

        long endAt = System.currentTimeMillis();
        long execTime = endAt - startAt;
        System.out.println("total time: " + execTime + " ms");
    }
}
