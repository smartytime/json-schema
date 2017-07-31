/*
 * Copyright (C) 2017 SBSP (http://sbsp.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.sbsp.jsonschema.validator;

import io.sbsp.jsonschema.six.ReferenceSchema;
import io.sbsp.jsonschema.six.Schema;
import lombok.experimental.var;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonObject;

import static io.sbsp.jsonschema.loader.JsonSchemaFactory.schemaFactory;
import static io.sbsp.jsonschema.validator.SchemaValidatorFactory.createValidatorForSchema;
import static io.sbsp.jsonschema.validator.ValidationTestSupport.verifyFailure;

public class PointerBubblingTest {

    private static final ResourceLoader loader = ResourceLoader.DEFAULT;
    private final JsonObject allSchemas = loader.readObj("testschemas.json");
    private final Schema rectangleSchema = schemaFactory().load(allSchemas.getJsonObject("pointerResolution"));
    private final JsonObject testInputs = loader.readObj("objecttestcases.json");

    @Test
    public void rectangleMultipleFailures() {
        JsonObject input = testInputs.getJsonObject("rectangleMultipleFailures");
        var e = verifyFailure(() -> createValidatorForSchema(rectangleSchema).validate(input));
        Assert.assertEquals("#/rectangle", e.getPointerToViolation());
        Assert.assertEquals(2, e.getCauses().size());
        Assert.assertEquals(1, ValidationTestSupport.countCauseByJsonPointer(e, "#/rectangle/a"));
        Assert.assertEquals( 1, ValidationTestSupport.countCauseByJsonPointer(e, "#/rectangle/b"));
    }

    @Test
    public void rectangleSingleFailure() {
        JsonObject input = testInputs.getJsonObject("rectangleSingleFailure");
        ValidationTestSupport.expectFailure(rectangleSchema, ReferenceSchema.class, "#/rectangle/a", input);
    }
}
