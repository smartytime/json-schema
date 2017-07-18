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
package io.dugnutt.jsonschema.loader.reference;

import lombok.SneakyThrows;

import java.net.URI;

/**
 * Resolves an {@code id} or {@code ref} against a parent scope.
 * <p>
 * Used by TypeBasedMultiplexer (for handling <code>id</code>s) and by SchemaLoader (for handling
 * <code>ref</code>s).
 */
public final class ReferenceScopeResolver {

    private ReferenceScopeResolver() {
    }

    /**
     * Creates an absolute JSON pointer string based on a parent scope and a newly encountered pointer
     * segment ({@code id} or {@code ref} value).
     *
     * @param parentScope        the most immediate parent scope that the resolution should be performed against
     * @param encounteredSegment the new segment (complete URI, path, fragment etc) which must be resolved
     * @return the resolved URI
     */
    @SneakyThrows
    public static URI resolveScope(final URI parentScope, final String encounteredSegment) {
        return new URI(resolveScope(parentScope == null ? null : parentScope.toString(),
                encounteredSegment));
    }

    /**
     * Creates an absolute JSON pointer string based on a parent scope and a newly encountered pointer
     * segment ({@code id} or {@code ref} value).
     *
     * @param parentScope        the most immediate parent scope that the resolution should be performed against
     * @param encounteredSegment the new segment (complete URI, path, fragment etc) which must be resolved
     * @return the resolved URI
     */
    @SneakyThrows
    public static String resolveScope(final String parentScope, final String encounteredSegment) {
        if (parentScope == null) {
            return encounteredSegment;
        }
        return new URI(parentScope).resolve(encounteredSegment).toString();
    }
}
