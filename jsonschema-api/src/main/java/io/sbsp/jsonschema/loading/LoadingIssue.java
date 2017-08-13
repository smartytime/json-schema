package io.sbsp.jsonschema.loading;

import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.utils.JsonUtils;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;

import javax.json.JsonValue;
import java.util.List;
import java.util.StringJoiner;

@Builder
@EqualsAndHashCode
@Getter
public class LoadingIssue {
    @NonNull
    private final String code;

    @NonNull
    private final SchemaLocation location;
    private final JsonValue schemaJson;
    private final JsonValue value;

    private final String resolutionMessage;

    @NonNull
    private final LoadingIssueLevel level;

    @NonNull
    private final String message;

    @NonNull
    @Singular
    private final List<Object> arguments;

    @Override
    public String toString() {
        final StringJoiner joiner = new StringJoiner(":");
        joiner.add(level.name());
        joiner.add(location.getJsonPointerFragment().toString());
        joiner.add(String.format(message, JsonUtils.prettyPrintArgs(arguments.toArray())));
        return joiner.toString();
    }
}
