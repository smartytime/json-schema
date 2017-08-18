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
package io.sbsp.jsonschema.loading.reference;

import io.sbsp.jsonschema.JsonPath;
import io.sbsp.jsonschema.loading.SchemaLoaderImpl;

import javax.json.JsonObject;
import java.net.URI;
import java.util.Optional;

/**
 * This interface is used by {@link SchemaLoaderImpl} to fetch the contents denoted by remote JSON
 * pointer.

 * Implementations are expected to support the HTTP/1.1 protocol, the support of other protocols is
 * optional.
 */
public interface JsonDocumentClient {

    Optional<JsonObject> findLoadedDocument(URI documentLocation);
    void registerLoadedDocument(URI documentLocation, JsonObject document);
    Optional<JsonPath> resolveSchemaWithinDocument(URI documentURI, URI schemaURI, JsonObject document);

    /**
     * Returns a stream to be used for reading the remote content (response body) of the URL. In the
     * case of a HTTP URL, implementations are expected send HTTP GET requests and the response is
     * expected to be represented in UTF-8 character set.
     *
     * @param uri the URL of the remote resource
     * @return the input stream of the response
     * @throws java.io.UncheckedIOException if an IO error occurs.
     */
    JsonObject fetchDocument(URI uri);

    default JsonObject fetchDocument(String url) {
        return fetchDocument(URI.create(url));
    }

}
