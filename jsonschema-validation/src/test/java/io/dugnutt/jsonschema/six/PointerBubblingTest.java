/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
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
package io.dugnutt.jsonschema.six;

import lombok.experimental.var;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonObject;

import static io.dugnutt.jsonschema.loader.SchemaFactory.schemaFactory;
import static io.dugnutt.jsonschema.six.ValidationTestSupport.verifyFailure;
import static io.dugnutt.jsonschema.utils.JsonUtils.readResourceAsJsonObject;
import static io.dugnutt.jsonschema.validator.SchemaValidatorFactory.createValidatorForSchema;

public class PointerBubblingTest {

    private static final ResourceLoader loader = ResourceLoader.DEFAULT;
    private final JsonObject allSchemas = readResourceAsJsonObject("testschemas.json");
    private final Schema rectangleSchema = schemaFactory().load(allSchemas.getJsonObject("pointerResolution"));
    private final JsonObject testInputs = readResourceAsJsonObject("objecttestcases.json");

    @Test
    public void rectangleMultipleFailures() {
        JsonObject input = testInputs.getJsonObject("rectangleMultipleFailures");
        var e = verifyFailure(() -> createValidatorForSchema(rectangleSchema).validate(input));
        Assert.assertEquals("#/rectangle", e.getPointerToViolation());
        Assert.assertEquals(2, e.getCauses().size());
        Assert.assertEquals(1, ValidationTestSupport.countCauseByJsonPointer(e, "#/rectangle/a"));
        Assert.assertEquals(1, ValidationTestSupport.countCauseByJsonPointer(e, "#/rectangle/b"));
    }

    @Test
    public void rectangleSingleFailure() {
        JsonObject input = testInputs.getJsonObject("rectangleSingleFailure");
        ValidationTestSupport.expectFailure(rectangleSchema, NumberSchema.class, "#/rectangle/a", input);
    }
}
