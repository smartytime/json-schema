package io.sbsp.jsonschema.keyword;

import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import org.junit.Test;

import static io.sbsp.jsonschema.JsonSchemaProvider.schemaBuilder;
import static org.assertj.core.api.Assertions.assertThat;

public class LimitKeywordTest {
    @Test
    public void testToStringDraft6() {
        final Schema limitSchema = schemaBuilder().maximum(43)
                .exclusiveMaximum(44)
                .build();
        final String toString = limitSchema.toString(false, JsonSchemaVersion.Draft6);
        final String expected = "{\"maximum\":43,\"exclusiveMaximum\":44}";
        assertThat(limitSchema.toString(false, JsonSchemaVersion.Draft6)).isEqualTo(expected);
    }

    @Test(expected = IllegalStateException.class)
    public void testToStringDraft4_WhenMaxAndExclusiveSet_ThenIllegalStateExceptionIsRaised() {
        final Schema limitSchema = schemaBuilder().maximum(43)
                .exclusiveMaximum(44)
                .build();
        limitSchema.toString(false, JsonSchemaVersion.Draft4);
    }

    @Test
    public void testToStringDraft4_WhenExclusiveSet_ThenBooleanIsReturned() {
        final Schema limitSchema = schemaBuilder()
                .exclusiveMaximum(43)
                .build();
        final String toString = limitSchema.toString(false, JsonSchemaVersion.Draft4);

        assertThat(toString).isEqualTo("{\"maximum\":43,\"exclusiveMaximum\":true}");
    }

    @Test
    public void testToStringDraft4_WhenNoExclusiveSet_ThenBooleanIsExcluded() {
        final Schema limitSchema = schemaBuilder()
                .maximum(43)
                .build();
        final String toString = limitSchema.toString(false, JsonSchemaVersion.Draft4);

        assertThat(toString).isEqualTo("{\"maximum\":43}");
    }
}