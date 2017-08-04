package io.sbsp.jsonschema.keyword.keywords.number;

import io.sbsp.jsonschema.keyword.SchemaKeyword;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
@EqualsAndHashCode
public class MinimumKeyword implements SchemaKeyword {

    private final Number minimum;
    private final Number exclusiveMinimum;
    private final boolean isExclusive;
}