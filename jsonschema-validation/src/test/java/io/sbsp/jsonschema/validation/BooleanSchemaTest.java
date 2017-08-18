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
package io.sbsp.jsonschema.validation;

import org.junit.Assert;
import org.junit.Test;

import static io.sbsp.jsonschema.validation.ValidationMocks.mockBooleanSchema;
import static io.sbsp.jsonschema.validation.ValidationTestSupport.expectSuccess;
import static io.sbsp.jsonschema.validation.ValidationTestSupport.failureOf;

public class BooleanSchemaTest {

    @Test
    public void failure() {
        failureOf(mockBooleanSchema())
                .expectedKeyword("type")
                .input("false")
                .expect();
    }

    @Test
    public void success() {
        expectSuccess(mockBooleanSchema().build(), true);
    }

    @Test
    public void toStringTest() {
        Assert.assertEquals("{\"type\":\"boolean\"}", mockBooleanSchema().build().toString());
    }
}
