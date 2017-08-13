package io.sbsp.jsonschema.validator.factory;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import io.sbsp.jsonschema.keyword.KeywordMetadata;

import javax.annotation.Nullable;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Extracts any necessary validation keywords from a {@link Schema} instance.
 */

public class KeywordValidatorCreators {
    private final SetMultimap<KeywordMetadata<?>, KeywordValidatorCreator<?,?>> factories;

    public KeywordValidatorCreators(SetMultimap<KeywordMetadata<?>, KeywordValidatorCreator<?, ?>> factories) {
        checkNotNull(factories, "factories must not be null");
        this.factories = ImmutableSetMultimap.copyOf(factories);
    }

    public Set<? extends KeywordValidatorCreator> get(@Nullable KeywordMetadata key) {
        return factories.get(key);
    }
}
