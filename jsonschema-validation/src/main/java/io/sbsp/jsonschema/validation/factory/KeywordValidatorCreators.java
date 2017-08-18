package io.sbsp.jsonschema.validation.factory;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import io.sbsp.jsonschema.keyword.KeywordInfo;

import javax.annotation.Nullable;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Extracts any necessary validation keywords from a {@link io.sbsp.jsonschema.Schema} instance.
 */

public class KeywordValidatorCreators {
    private final SetMultimap<KeywordInfo<?>, KeywordValidatorCreator<?,?>> factories;

    public KeywordValidatorCreators(SetMultimap<KeywordInfo<?>, KeywordValidatorCreator<?, ?>> factories) {
        checkNotNull(factories, "factories must not be null");
        this.factories = ImmutableSetMultimap.copyOf(factories);
    }

    public Set<? extends KeywordValidatorCreator> get(@Nullable KeywordInfo key) {
        return factories.get(key);
    }
}
