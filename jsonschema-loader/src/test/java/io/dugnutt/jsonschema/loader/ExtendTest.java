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
package io.dugnutt.jsonschema.loader;

import org.junit.Assert;
import org.junit.Test;

public class ExtendTest extends BaseLoaderTest {

    public ExtendTest() {
        super("merge-testcases.json");
    }

    // @Test
    // public void additionalHasMoreProps() {
    //     JsonObject actual = ReferenceLookup.extend(getJsonObjectForKey("propIsTrue"), getJsonObjectForKey("empty"));
    //     assertEquals(getJsonObjectForKey("propIsTrue"), actual);
    // }
    //
    // @Test
    // public void additionalOverridesOriginal() {
    //     JsonObject actual = ReferenceLookup.extend(getJsonObjectForKey("propIsTrue"), getJsonObjectForKey("propIsFalse"));
    //     assertEquals(getJsonObjectForKey("propIsTrue"), actual);
    // }
    //
    // @Test
    // public void additionalPropsAreMerged() {
    //     JsonObject actual = ReferenceLookup.extend(getJsonObjectForKey("propIsTrue"), getJsonObjectForKey("prop2IsFalse"));
    //     assertEquals(actual, getJsonObjectForKey("propTrueProp2False"));
    // }
    //
    // private void assertEquals(JsonObject expected, JsonObject actual) {
    //     Assert.assertTrue(Objects.equals(expected, actual));
    // }
    //
    // @Test
    // public void bothEmpty() {
    //     JsonObject actual = ReferenceLookup.extend(getJsonObjectForKey("empty"), getJsonObjectForKey("empty"));
    //     assertEquals(new JsonObject(), actual);
    // }
    //
    // @Test
    // public void multiplePropsAreMerged() {
    //     JsonObject actual = ReferenceLookup.extend(getJsonObjectForKey("multipleWithPropTrue"), getJsonObjectForKey("multipleWithPropFalse"));
    //     assertEquals(getJsonObjectForKey("mergedMultiple"), actual);
    // }
    //
    // @Test
    // public void originalPropertyRemainsUnchanged() {
    //     JsonObject actual = ReferenceLookup.extend(getJsonObjectForKey("empty"), getJsonObjectForKey("propIsTrue"));
    //     assertEquals(getJsonObjectForKey("propIsTrue"), actual);
    // }

    @Test
    public void fail() {
        Assert.fail("Not implemented");

    }
    //todo:ericm Verify that this behavior is unsupported
}
