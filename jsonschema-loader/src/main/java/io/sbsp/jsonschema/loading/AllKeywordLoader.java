package io.sbsp.jsonschema.loading;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import io.sbsp.jsonschema.keyword.KeywordInfo;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.loading.KeywordDigest;
import io.sbsp.jsonschema.loading.LoadingIssue;
import io.sbsp.jsonschema.loading.LoadingIssueLevel;
import io.sbsp.jsonschema.loading.LoadingIssues;
import io.sbsp.jsonschema.loading.LoadingReport;
import io.sbsp.jsonschema.loading.KeywordDigester;
import io.sbsp.jsonschema.loading.SchemaLoader;
import io.sbsp.jsonschema.utils.Pair;

import javax.json.JsonValue.ValueType;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.sbsp.jsonschema.enums.JsonSchemaVersion.*;

public class AllKeywordLoader {

    private final List<KeywordDigester<?>> allExtractors;
    private final Multimap<String, Pair<KeywordInfo<?>, KeywordDigester<?>>> filteredExtractors;
    private final SchemaLoader schemaLoader;

    public AllKeywordLoader(List<KeywordDigester<?>> digesters, Set<JsonSchemaVersion> defaultVersions, SchemaLoader schemaLoader) {
        this.schemaLoader = checkNotNull(schemaLoader, "fragmentLoader must not be null");
        checkNotNull(digesters, "keywordLoaders must not be null");
        final HashMultimap<String, Pair<KeywordInfo<?>, KeywordDigester<?>>> filtered = HashMultimap.create();

        // This instance is provided with a list of valid versions to process.
        // In this block of code, we filter out any keywords that aren't supported
        for (KeywordDigester<?> keywordLoader : digesters) {
            for (KeywordInfo<?> processedKeyword : keywordLoader.getIncludedKeywords()) {
                if(Sets.union(processedKeyword.getTypeVariant(), defaultVersions).size()>0) {
                    filtered.put(processedKeyword.key(), Pair.of(processedKeyword, keywordLoader));
                }
            }
        }
        this.allExtractors = ImmutableList.copyOf(digesters);
        this.filteredExtractors = ImmutableSetMultimap.copyOf(filtered);
    }

    public void loadKeywordsForSchema(JsonValueWithPath jsonObject, SchemaBuilder builder, LoadingReport report) {
        //Process keywords we know:
        jsonObject.forEachKey((prop, jsonValue)-> {
            EnumMap<JsonSchemaVersion, KeywordDigester<?>> matches = new EnumMap<>(JsonSchemaVersion.class);
            List<LoadingIssue> nonMatches = new ArrayList<>();
            for (Pair<KeywordInfo<?>, KeywordDigester<?>> infoAndLoader : filteredExtractors.get(prop)) {
                final KeywordInfo<?> possibleKeyword = infoAndLoader.getA();
                final KeywordDigester<?> keywordLoader = infoAndLoader.getB();
                final ValueType expectedType = possibleKeyword.getExpects();
                if (jsonValue.getValueType() != expectedType) {
                    nonMatches.add(LoadingIssues.typeMismatch(possibleKeyword, jsonValue).level(LoadingIssueLevel.ERROR).build());
                } else {
                    matches.put(possibleKeyword.getMostRecentVersion(), keywordLoader);
                }
            }
            if (matches.size() > 0) {
                // Process this loaders, newest to oldest
                Stream.of(Draft6, Draft5, Draft4, Draft3)
                        .filter(matches::containsKey)
                        .map(matches::get)
                        .filter(loader -> processKeyword(loader, jsonObject, builder, schemaLoader, report))
                        .findFirst();
            } else if (nonMatches.size() > 0) {
                nonMatches.forEach(report::log);
            } else {
                builder.extraProperty(prop, jsonValue);
            }
        });
    }

    private <K extends SchemaKeyword> boolean processKeyword(KeywordDigester<K> digester, JsonValueWithPath jsonValue,
                                                             SchemaBuilder builder, SchemaLoader factory, LoadingReport report) {
        final Optional<KeywordDigest<K>> digest = digester.extractKeyword(jsonValue, builder, factory, report);
        if (digest.isPresent()) {
            final KeywordDigest<K> keywordDigest = digest.get();
            builder.keyword(keywordDigest.getKeyword(), keywordDigest.getKeywordValue());
            return true;
        }
        return false;
    }
}
