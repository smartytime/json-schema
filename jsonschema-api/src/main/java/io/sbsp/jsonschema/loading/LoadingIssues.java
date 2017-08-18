package io.sbsp.jsonschema.loading;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.keyword.KeywordInfo;
import io.sbsp.jsonschema.loading.LoadingIssue.LoadingIssueBuilder;

import javax.json.JsonValue;

import static com.google.common.base.Preconditions.checkNotNull;

public class LoadingIssues {

    public static LoadingIssueBuilder conflictingKeyword(KeywordInfo<?> foundKeyword, KeywordInfo<?> conflictingKeyword) {
        checkNotNull(foundKeyword, "foundKeyword must not be null");
        checkNotNull(conflictingKeyword, "conflictingKeyword must not be null");
        return LoadingIssue.builder()
                .code("keyword.conflicting")
                .value(JsonValue.NULL)
                .message("Found keyword [%s] ($3s), but also found conflicting keyword [%s]")
                .argument(foundKeyword.key())
                .argument(conflictingKeyword.key());
    }

    public static LoadingIssueBuilder invalidKeywordValue(KeywordInfo<?> keyword, String message) {
        checkNotNull(keyword, "keyword must not be null");
        checkNotNull(message, "message must not be null");
        return LoadingIssue.builder()
                .code("keyword.invalid")
                .value(JsonValue.NULL)
                .message("Invalid keyword [%s]: " + message)
                .argument(keyword);
    }

    public static LoadingIssueBuilder missingKeywordIssue(KeywordInfo<?> foundKeyword, KeywordInfo<?> expectedKeyword) {
        checkNotNull(foundKeyword, "foundKeyword must not be null");
        checkNotNull(expectedKeyword, "expectedKeyword must not be null");
        return LoadingIssue.builder()
                .code("keyword.missing")
                .value(JsonValue.NULL)
                .message("Found keyword [%s], was expecting [%s]")
                .argument(foundKeyword.key())
                .argument(expectedKeyword.key());
    }

    public static LoadingIssueBuilder typeMismatch(KeywordInfo<?> keyword, JsonValue value, SchemaLocation location) {
        checkNotNull(keyword, "foundKeyword must not be null");
        checkNotNull(value, "expectedKeyword must not be null");
        return LoadingIssue.builder()
                .code("keyword.type.mismatch")
                .value(value)
                .location(location)
                .message("Value [%s] was [%s], was expecting [%s]")
                .argument(value.toString())
                .argument(value.getValueType().toString())
                .argument(keyword.getApplicableTypes());
    }

    public static LoadingIssueBuilder typeMismatch(KeywordInfo<?> keyword, JsonValueWithPath value) {
        return typeMismatch(keyword, value, value.getLocation());
    }
}
