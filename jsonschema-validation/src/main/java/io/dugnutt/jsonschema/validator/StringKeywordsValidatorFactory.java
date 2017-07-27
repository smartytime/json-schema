package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.six.StringKeywords;

import javax.json.JsonValue.ValueType;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.FORMAT;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MAX_LENGTH;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MIN_LENGTH;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.PATTERN;
import static io.dugnutt.jsonschema.validator.ChainedValidator.ChainedValidatorBuilder;
import static io.dugnutt.jsonschema.validator.ChainedValidator.builder;
import static io.dugnutt.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class StringKeywordsValidatorFactory implements PartialValidatorFactory {

    public static StringKeywordsValidatorFactory stringKeywordsValidator() {
        return new StringKeywordsValidatorFactory();
    }

    @Override
    public boolean appliesToSchema(Schema schema) {
        checkNotNull(schema, "schema must not be null");
        return schema.getStringKeywords().isPresent();
    }

    @Override
    public SchemaValidator forSchema(Schema schema, SchemaValidatorFactory factory) {

        if (!schema.hasStringKeywords()) {
            return SchemaValidator.NOOP_VALIDATOR;
        }
        ChainedValidatorBuilder validationBuilder = builder().schema(schema).factory(factory);
        StringKeywords keywords = schema.getStringKeywords().get();

        if (keywords.getMinLength() != null) {
            int minLength = keywords.getMinLength();

            validationBuilder.addValidator(MIN_LENGTH, ((subject, report) -> {
                String string = subject.asString();
                int actualLength = string.codePointCount(0, string.length());
                if (actualLength < minLength) {
                    return report.addError(buildKeywordFailure(subject, schema, MIN_LENGTH)
                            .message("expected minLength: %d, actual: %d", minLength, actualLength)
                            .build());
                }
                return true;
            }));
        }
        if (keywords.getMaxLength() != null) {
            int maxLength = keywords.getMaxLength();
            validationBuilder.addValidator(MAX_LENGTH, ((subject, report) -> {
                String string = subject.asString();
                int actualLength = string.codePointCount(0, string.length());
                if (actualLength > maxLength) {
                    return report.addError(buildKeywordFailure(subject, schema, MAX_LENGTH)
                            .message("expected maxLength: %d, actual: %d", maxLength, actualLength)
                            .build());

                }
                return true;
            }));
        }

        //Test the pattern
        keywords.findPattern().ifPresent(pattern->{
            validationBuilder.addValidator(PATTERN, ((subject, report) -> {
                String stringSubject = subject.asString();
                if (!patternMatches(pattern, stringSubject)) {
                    return report.addError(
                            buildKeywordFailure(subject, schema, PATTERN)
                                    .message("string [%s] does not match pattern %s", stringSubject, pattern.pattern())
                                    .build());
                }
                return true;
            }));
        });

        factory.getFormatValidator(keywords.getFormat()).ifPresent(formatValidator-> {
            validationBuilder.addValidator(FORMAT, ((subject, report) -> {
                String stringSubject = subject.asString();
                Optional<String> error = formatValidator.validate(stringSubject);
                if (error.isPresent()) {
                    return report.addError(buildKeywordFailure(subject, schema, FORMAT)
                            .message(error.get()).build());
                }
                return true;
            }));
        });

        return validationBuilder.build();
    }

    @Override
    public Set<ValueType> appliesToTypes() {
        return Collections.singleton(ValueType.STRING);
    }

    private boolean patternMatches(Pattern pattern, final String string) {
        return pattern.matcher(string).find();
    }
}
