/*
 * Copyright (C) 2017 SBSP (http://sbsp.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.sbsp.jsonschema.validator.keywords.string.formatValidators;

import com.google.common.collect.ImmutableList;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of the "date-time" format value.
 */
public class TimeFormatValidator implements FormatValidator {

    private static final List<String> FORMATS_ACCEPTED = ImmutableList.of(
            "HH:mm:ssZ", "HH:mm:ss.[0-9]{1,9}Z"
    );

    private static final String PARTIAL_DATETIME_PATTERN = "HH:mm:ss";

    private static final String ZONE_OFFSET_PATTERN = "XXX";

    private static final DateTimeFormatter FORMATTER;

    static {
        final DateTimeFormatter secondsFractionFormatter = new DateTimeFormatterBuilder()
                .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
                .toFormatter();

        final DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder()
                .appendPattern(PARTIAL_DATETIME_PATTERN)
                .appendOptional(secondsFractionFormatter)
                .appendPattern(ZONE_OFFSET_PATTERN);

        FORMATTER = builder.toFormatter();
    }

    @Override
    public Optional<String> validate(final String subject) {
        try {
            FORMATTER.parse(subject);
            return Optional.empty();
        } catch (DateTimeParseException e) {
            return Optional.of(String.format("[%s] is not a valid time. Expected %s", subject, FORMATS_ACCEPTED));
        }
    }

    @Override
    public String formatName() {
        return "time";
    }
}
