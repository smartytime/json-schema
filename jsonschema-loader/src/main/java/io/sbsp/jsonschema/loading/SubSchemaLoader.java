package io.sbsp.jsonschema.loading;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.builder.JsonSchemaBuilder;
import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import io.sbsp.jsonschema.keyword.KeywordInfo;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.loading.keyword.JsonValueToKeywordTransformer;
import io.sbsp.jsonschema.loading.keyword.versions.KeywordDigesterImpl;
import io.sbsp.jsonschema.utils.JsonUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import javax.json.JsonObject;
import javax.json.spi.JsonProvider;
import java.net.URI;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.sbsp.jsonschema.enums.JsonSchemaVersion.Draft3;
import static io.sbsp.jsonschema.enums.JsonSchemaVersion.Draft4;
import static io.sbsp.jsonschema.enums.JsonSchemaVersion.Draft5;
import static java.util.Collections.emptyList;

/**
 * Schema factories are responsible for extracting values from a json-object to produce an immutable {@link Schema}
 * instance.  This also includes looking up any referenced schemas.
 */
@Getter
public class SubSchemaLoader {

    private final JsonProvider provider;
    private final AllKeywordLoader allKeywordLoader;
    private final EnumSet<JsonSchemaVersion> defaultVersions;
    private final SchemaLoader schemaLoader;


    @Builder(builderClassName = "SchemaBuilderFactoryBuilder")
    public SubSchemaLoader(JsonProvider provider, @Singular List<KeywordDigester<?>> extraKeywordLoaders,
                           @Singular Set<JsonSchemaVersion> defaultVersions, SchemaLoader schemaLoader) {
        // this.schemaLoader = checkNotNull(schemaLoader, "schemaLoader must not be null");
        this.provider = MoreObjects.firstNonNull(provider, JsonProvider.provider());
        if (defaultVersions == null || defaultVersions.isEmpty()) {
            this.defaultVersions = EnumSet.of(Draft3, Draft4, Draft5);
        } else {
            this.defaultVersions = EnumSet.copyOf(defaultVersions);
        }
        this.schemaLoader = checkNotNull(schemaLoader, "schemaLoader must not be null");
        List<KeywordDigester<?>> keywordLoaders = new ImmutableList.Builder<KeywordDigester<?>>()
                .addAll(KeywordDigesters.defaultKeywordLoaders())
                .addAll(MoreObjects.firstNonNull(extraKeywordLoaders, emptyList()))
                .build();
        this.allKeywordLoader = new AllKeywordLoader(keywordLoaders, this.defaultVersions, schemaLoader);
    }

    /**
     * Extracts all keywords from a json document, reports any issues to the provided LoadingReport.
     *
     * @param schemaJson    The subschema to load.
     * @param rootDocument  The document the subschema came from
     * @param loadingReport The report to write errors into
     * @return A builder loaded up with all the keywords.
     */
    public SchemaBuilder subSchemaBuilder(JsonValueWithPath schemaJson, JsonObject rootDocument, LoadingReport loadingReport) {
        // #############################################
        // #####  $ref: Overrides everything    ########
        // #############################################

        if (schemaJson.has(Keywords.$REF)) {
            //Ignore all other keywords when encountering a ref
            String ref = schemaJson.getString(Keywords.$REF);
            return refSchemaBuilder(URI.create(ref), rootDocument, schemaJson.getLocation());
        }

        final SchemaBuilder schemaBuilder = JsonUtils.extract$IdFromObject(schemaJson)
                .map($id -> schemaBuilder(schemaJson.getLocation(), $id))
                .orElse(schemaBuilder(schemaJson.getLocation()))
                .withCurrentDocument(rootDocument)
                .withLoadingReport(loadingReport);

        allKeywordLoader.loadKeywordsForSchema(schemaJson, schemaBuilder, loadingReport);
        return schemaBuilder;
    }


    SchemaBuilder schemaBuilder(SchemaLocation location, URI $id) {
        return new JsonSchemaBuilder(location, $id).withSchemaLoader(schemaLoader);
    }

    SchemaBuilder schemaBuilder(SchemaLocation location) {
        return new JsonSchemaBuilder(location).withSchemaLoader(schemaLoader);
    }

    SchemaBuilder refSchemaBuilder(URI $ref, JsonObject currentDocument, SchemaLocation location) {
        return new JsonSchemaBuilder(location)
                .withCurrentDocument(currentDocument)
                .withSchemaLoader(schemaLoader)
                .ref($ref);
    }


    public static class SchemaBuilderFactoryBuilder {


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
        public <K extends SchemaKeyword> SchemaBuilderFactoryBuilder addCustomKeywordLoader(KeywordInfo<K> keyword, JsonValueToKeywordTransformer<K> tx) {
            extraKeywordLoaders.add(new KeywordDigesterImpl<>(keyword, tx));
            return this;
        }
    }
}
