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

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Implementation of the "color" format value.
 */
public class ColorFormatValidator implements FormatValidator {

    private static final Pattern COLOR_PATTERN = Pattern.compile("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");

    @Override
    public Optional<String> validate(final String subject) {
        if (!COLOR_PATTERN.matcher(subject).find()) {
            return Optional.of(String.format("[%s] is not a valid color. Expected %s", subject, "#FFFFFF"));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public String formatName() {
        return "color";
    }
}
