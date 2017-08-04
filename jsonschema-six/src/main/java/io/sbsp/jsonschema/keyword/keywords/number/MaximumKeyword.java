package io.sbsp.jsonschema.keyword.keywords.number;

import io.sbsp.jsonschema.keyword.SchemaKeyword;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
@EqualsAndHashCode
public class MaximumKeyword implements SchemaKeyword {

    private final Number maximum;
    private final Number exclusiveMaximum;
    private final boolean isExclusive;


}