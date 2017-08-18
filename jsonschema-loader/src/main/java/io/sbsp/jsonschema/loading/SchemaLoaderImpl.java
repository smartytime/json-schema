package io.sbsp.jsonschema.loading;

import com.google.common.base.MoreObjects;
import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import io.sbsp.jsonschema.keyword.KeywordInfo;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.loading.keyword.JsonValueToKeywordTransformer;
import io.sbsp.jsonschema.loading.keyword.versions.KeywordDigesterImpl;
import io.sbsp.jsonschema.loading.reference.DefaultJsonDocumentClient;
import io.sbsp.jsonschema.loading.reference.JsonDocumentClient;
import io.sbsp.jsonschema.loading.reference.SchemaCache;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import javax.json.JsonObject;
import javax.json.spi.JsonProvider;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Schema factories are responsible for extracting values from a json-object to produce an immutable {@link Schema}
 * instance.  This also includes looking up any referenced schemas.
 */
@Getter
public class SchemaLoaderImpl implements SchemaReader, SchemaLoader  {

    public static final Charset UTF8 = Charset.forName("UTF-8");

    private final JsonProvider provider;
    private final Charset charset;
    private final SchemaCache schemaCache;

    private final RefSchemaLoader refSchemaLoader;
    private final SubSchemaLoader fragmentLoader;

    @Builder(builderClassName = "SchemaLoaderBuilder")
    public SchemaLoaderImpl(JsonProvider provider, JsonDocumentClient documentClient, Charset charset, SchemaCache schemaCache,
                            @Singular List<KeywordDigester<?>> extraKeywordLoaders, @Singular Set<JsonSchemaVersion> defaultVersions) {
        this.provider = MoreObjects.firstNonNull(provider, JsonProvider.provider());
        this.charset = MoreObjects.firstNonNull(charset, UTF8);
        this.schemaCache = MoreObjects.firstNonNull(schemaCache, SchemaCache.schemaCacheBuilder().build());
        this.fragmentLoader = new SubSchemaLoader(this.provider, extraKeywordLoaders, defaultVersions, this);
        documentClient = MoreObjects.firstNonNull(documentClient, DefaultJsonDocumentClient.builder()
                .schemaCache(this.schemaCache)
                .jsonProvider(this.provider)
                .build());
        this.refSchemaLoader = new RefSchemaLoader(this.provider, documentClient, this);
    }

    public SchemaLoaderImpl() {
        this(null, null, null, null, null, null);
    }

    // #############################################################
    // ########  LOADING SCHEMAS/SUBSCHEMAS FROM JSON    ###########
    // #############################################################

    @Override
    public Schema loadRefSchema(Schema referencedFrom, URI refURI, JsonObject currentDocument, LoadingReport report) {
        return refSchemaLoader.loadRefSchema(referencedFrom, refURI, currentDocument, report);
    }

    @Override
    public Schema loadSubSchema(JsonValueWithPath schemaJson, JsonObject inDocument, LoadingReport loadingReport) {
        checkNotNull(schemaJson, "schemaJson must not be null");
        checkNotNull(inDocument, "inDocument must not be null");
        checkNotNull(loadingReport, "loadingReport must not be null");
        return schemaCache.getSchema(schemaJson.getLocation())
                .orElseGet(() -> {
                    Schema schema = subSchemaBuilder(schemaJson, inDocument, loadingReport).build();
                    schemaCache.cacheSchema(schema);
                    return schema;
                });
    }

    // #############################################################
    // #######  LOADING  & CREATING SUBSCHEMA FACTORIES  ###########
    // #############################################################

    @Override
    public SchemaBuilder subSchemaBuilder(JsonValueWithPath schemaJson, JsonObject inDocument, LoadingReport loadingReport) {
        return fragmentLoader.subSchemaBuilder(schemaJson, inDocument, loadingReport);
    }

    // #############################################################
    // ############  FINDING/STORING LOADED SCHEMAS  ###############
    // #############################################################

    @Override
    public Optional<Schema> findLoadedSchema(URI schemaURI) {
        return schemaCache.getSchema(schemaURI);
    }

    @Override
    public void registerLoadedSchema(Schema schema) {
        schemaCache.cacheSchema(schema);
    }

    public SchemaLoaderImpl withPreloadedSchema(InputStream preloadedSchema) {
        checkNotNull(preloadedSchema, "preloadedSchema must not be null");
        final JsonObject jsonObject = provider.createReader(preloadedSchema).readObject();
        readSchema(jsonObject);
        return this;
    }

    public static SchemaLoaderImpl schemaLoader() {
        return builder().build();
    }

    public static SchemaLoaderImpl schemaLoader(JsonProvider jsonProvider) {
        return SchemaLoaderImpl.builder()
                .provider(jsonProvider)
                .build();
    }

    @Override
    public SchemaLoader getLoader() {
        return this;
    }

    public static class SchemaLoaderBuilder {

        /**
         * This function is used to support a simple functional-style keyword loader. eg
         * <p>
         * <pre>
         *     factory.addCustomKeywordLoader(MyKeywords.component, jsonValue-> new StringKeyword(jsonValue));
         * </pre>
         *
         * @param keyword The keyword to load into - can't conflict with any other keywords.
         * @param tx      Converts the jsonValue into the correct keyword type.
         * @return The keyword.
         */
        public <K extends SchemaKeyword> SchemaLoaderBuilder addCustomKeywordLoader(KeywordInfo<K> keyword, JsonValueToKeywordTransformer<K> tx) {
            extraKeywordLoaders.add(new KeywordDigesterImpl<>(keyword, tx));
            return this;
        }
    }
}
