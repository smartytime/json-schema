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
package io.sbsp.jsonschema.loader.internal;

import io.sbsp.jsonschema.six.SchemaLocation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class ReferenceScopeResolverTest {

    @Parameters(name = "{0}")
    public static List<Object[]> params() {
        return Arrays.asList(
                parList("fragment id", "http://x.y.z/root.json#foo", "http://x.y.z/root.json", "#foo"),
                parList("rel path", "http://example.org/foo", "http://example.org/bar", "foo"),
                parList("file name change", "http://x.y.z/schema/child.json",
                        "http://x.y.z/schema/parent.json",
                        "child.json"),
                parList("file name after folder path", "http://x.y.z/schema/child.json",
                        "http://x.y.z/schema/", "child.json"),
                parList("new root", "http://bserver.com", "http://aserver.com/",
                        "http://bserver.com"));
    }

    private static Object[] parList(final String... params) {
        return params;
    }

    private final URI expectedOutput;

    private final URI parentScope;

    private final URI encounteredSegment;

    public ReferenceScopeResolverTest(final String testcaseName, final String expectedOutput,
                                      final String parentScope,
                                      final String encounteredSegment) {
        this.expectedOutput = URI.create(expectedOutput);
        this.parentScope = URI.create(parentScope);
        this.encounteredSegment = URI.create(encounteredSegment);
    }

    @Test
    public void test() {
        SchemaLocation parentLocation = SchemaLocation.documentRoot(parentScope);
        SchemaLocation childLocation = parentLocation.child(encounteredSegment);
        assertEquals(expectedOutput, childLocation.getUniqueURI());
    }
}
