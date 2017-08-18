package io.sbsp.jsonschema.keyword;

import io.sbsp.jsonschema.enums.JsonSchemaType;
import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import org.junit.Test;

import javax.json.JsonValue.ValueType;
import java.util.Optional;

import static io.sbsp.jsonschema.enums.JsonSchemaVersion.Draft5;
import static javax.json.JsonValue.ValueType.ARRAY;
import static javax.json.JsonValue.ValueType.STRING;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class KeywordInfoTest {

    @Test
    public void testBuilder_WhenDeprecatedVersions_AllVersionsWorkProperly() {
        final KeywordInfo<SchemaKeyword> keyword = KeywordInfo.keywordInfo().key("enumeration").expects(ValueType.STRING).since(JsonSchemaVersion.Draft6)
                .additionalDefinition().expects(ARRAY).from(JsonSchemaVersion.Draft3).until(Draft5)
                .build();
        assertSoftly(a -> {
            a.assertThat(keyword.key()).isEqualTo("enumeration");
            a.assertThat(keyword.getVariants()).hasSize(2);
            a.assertThat(keyword.getExpects()).isEqualTo(ValueType.STRING);
            a.assertThat(keyword.getApplicableTypes().size()).isGreaterThan(1);

            final Optional<KeywordInfo<SchemaKeyword>> draft6 = keyword.getTypeVariant(ValueType.STRING);
            a.assertThat(draft6).isNotEmpty();
            final KeywordInfo<SchemaKeyword> draft6Keyword = draft6.get();
            a.assertThat(draft6Keyword.getVariants()).hasSize(0);
            a.assertThat(draft6Keyword.getTypeVariant())
                    .contains(JsonSchemaVersion.Draft6)
                    .doesNotContain(Draft5)
            ;

            final Optional<KeywordInfo<SchemaKeyword>> draft4 = keyword.getTypeVariant(ARRAY);
            a.assertThat(draft4).isPresent();
            final KeywordInfo<SchemaKeyword> draft4Keyword = draft4.get();
            a.assertThat(draft4Keyword.key()).isEqualTo("enumeration");
            a.assertThat(draft4Keyword.getVariants()).hasSize(0);
            a.assertThat(draft4Keyword.getTypeVariant())
                    .doesNotContain(JsonSchemaVersion.Draft6)
                    .contains(Draft5)
            ;
        });
    }

    @Test
    public void testBuilder_WhenDeprecatedVersions_DefaultsAreCopiedVersionsWorkProperly() {
        final KeywordInfo<SchemaKeyword> keyword = KeywordInfo.keywordInfo().key("enumeration").expects(ValueType.STRING).validates(JsonSchemaType.INTEGER).since(JsonSchemaVersion.Draft6)
                .additionalDefinition().expects(ARRAY).from(JsonSchemaVersion.Draft3).until(Draft5)
                .build();
        assertSoftly(a -> {
            final Optional<KeywordInfo<SchemaKeyword>> draft4 = keyword.getTypeVariant(STRING);
            a.assertThat(draft4).isPresent();
            final KeywordInfo<SchemaKeyword> draft4Keyword = draft4.get();
            a.assertThat(draft4Keyword.key()).isEqualTo("enumeration");
            a.assertThat(draft4Keyword.getVariants()).hasSize(0);
            a.assertThat(draft4Keyword.getExpects())
                    .isEqualTo(ValueType.STRING);
            a.assertThat(draft4Keyword.getApplicableTypes()).hasSize(1).contains(ValueType.NUMBER);
        });
    }
}