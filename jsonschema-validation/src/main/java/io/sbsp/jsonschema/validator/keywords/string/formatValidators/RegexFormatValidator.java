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
 * Implementation of the "date-time" format value.
 */
public class RegexFormatValidator implements FormatValidator {

    @Override
    public Optional<String> validate(final String subject) {
        try {
            Pattern.compile(subject);
            return Optional.empty();
        } catch (Exception e) {
            return Optional.of(String.format("[%s] is not a valid regex", subject));
        }
    }

    @Override
    public String formatName() {
        return "regex";
    }
}
