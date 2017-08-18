package io.sbsp.jsonschema.validation.keywords.string.formatValidators;

import io.sbsp.jsonschema.validation.FormatValidator;

import java.util.Optional;
import java.util.regex.Pattern;

public class PatternBasedValidator implements FormatValidator {
    private final Pattern pattern;
    private final String format;

    public PatternBasedValidator(Pattern pattern, String format) {
        this.pattern = pattern;
        this.format = format;
    }

    @Override
    public Optional<String> validate(String subject) {
        if (!pattern.matcher(subject).find()) {
            return Optional.of(String.format("[%s] is not a valid",  format));
        }
        return Optional.empty();
    }

    @Override
    public String formatName() {
        return format;
    }
}
