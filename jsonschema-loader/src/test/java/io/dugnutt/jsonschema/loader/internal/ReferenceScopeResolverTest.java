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
package io.dugnutt.jsonschema.loader.internal;

import io.dugnutt.jsonschema.loader.reference.ReferenceScopeResolver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
                        "http://bserver.com"),
                parList("null parent", "http://a.b.c", null, "http://a.b.c"));
    }

    private static Object[] parList(final String... params) {
        return params;
    }

    private final String expectedOutput;

    private final String parentScope;

    private final String encounteredSegment;

    public ReferenceScopeResolverTest(final String testcaseName, final String expectedOutput,
                                      final String parentScope,
                                      final String encounteredSegment) {
        this.expectedOutput = expectedOutput;
        this.parentScope = parentScope;
        this.encounteredSegment = encounteredSegment;
    }

    @Test
    public void test() {
        String actual = ReferenceScopeResolver.resolveScope(parentScope, encounteredSegment);
        assertEquals(expectedOutput, actual);
    }

    @Test
    public void testURI() {
        URI parentScopeURI;
        try {
            parentScopeURI = new URI(parentScope);
        } catch (URISyntaxException | NullPointerException e) {
            parentScopeURI = null;
        }
        URI actual = ReferenceScopeResolver.resolveScope(parentScopeURI, encounteredSegment);
    }

    @Test
    public void resolveWrapsURISyntaxException() {
        try {
            ReferenceScopeResolver.resolveScope("\\\\somethin\010g invalid///", "segment");
            fail("did not throw exception for invalid URI");
        } catch (Exception e) {
            assertEquals(URISyntaxException.class, e.getClass());
        }
    }

    @Test public void resolveURIWrapsURISyntaxException() throws Exception {
        try {
            ReferenceScopeResolver.resolveScope(new URI("http://example.com"), "\\\\somethin\010g invalid///");
            fail("did not throw exception for invalid URI");
        } catch (IllegalArgumentException e) {
            assertEquals(URISyntaxException.class, e.getCause().getClass());
        }
    }

}
