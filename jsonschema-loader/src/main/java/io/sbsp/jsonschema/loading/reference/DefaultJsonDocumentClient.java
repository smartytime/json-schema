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

import com.google.common.base.MoreObjects;
import io.sbsp.jsonschema.JsonPath;
import lombok.Builder;
import lombok.SneakyThrows;

import javax.json.JsonObject;
import javax.json.spi.JsonProvider;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link JsonDocumentClient} implementation which uses {@link URL} for reading the remote content.
 */
public class DefaultJsonDocumentClient implements JsonDocumentClient {

    private final SchemaCache schemaCache;
    private final JsonProvider jsonProvider;

    @Builder
    private DefaultJsonDocumentClient(SchemaCache schemaCache, JsonProvider jsonProvider) {
        this.schemaCache = MoreObjects.firstNonNull(schemaCache, SchemaCache.schemaCacheBuilder().build());
        this.jsonProvider = MoreObjects.firstNonNull(jsonProvider, JsonProvider.provider());
    }

    public static DefaultJsonDocumentClient getInstance() {
        return builder().build();
    }

    @Override
    public Optional<JsonObject> findLoadedDocument(URI documentLocation) {
        return schemaCache.lookupDocument(documentLocation);
    }

    @Override
    public void registerLoadedDocument(URI documentLocation, JsonObject document) {
        schemaCache.cacheDocument(documentLocation, document);
    }

    @Override
    public Optional<JsonPath> resolveSchemaWithinDocument(URI documentURI, URI schemaURI, JsonObject document) {
        return schemaCache.resolveURIToDocumentUsingLocalIdentifiers(documentURI, schemaURI, document);
    }

    @Override
    @SneakyThrows
    public JsonObject fetchDocument(URI uri) {
        try (InputStream inputStream = (InputStream) uri.toURL().getContent()) {
            return JsonProvider.provider().createReader(inputStream).readObject();
        }
    }

}
